package com.tomatoes.insumos.Servicio;

import com.tomatoes.insumos.Dto.MovimientoRequest;
import com.tomatoes.insumos.Entidades.*;
import com.tomatoes.insumos.Excepciones.NegocioException;
import com.tomatoes.insumos.Excepciones.NoEncontradoException;
import com.tomatoes.insumos.Repositorio.InsumoRepository;
import com.tomatoes.insumos.Repositorio.MovimientoRepository;
import com.tomatoes.insumos.Seguridad.UsuarioAutenticado;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Entradas y salidas de inventario. El stock se modifica con operaciones atómicas de MongoDB
 * ($inc con condición), de modo que nunca puede quedar negativo aunque haya peticiones simultáneas.
 */
@Service
public class MovimientoService {

    private final MovimientoRepository movimientos;
    private final InsumoRepository insumos;
    private final MongoTemplate mongo;

    public MovimientoService(MovimientoRepository movimientos, InsumoRepository insumos, MongoTemplate mongo) {
        this.movimientos = movimientos;
        this.insumos = insumos;
        this.mongo = mongo;
    }

    /** RF-08: suma la cantidad al stock. */
    public Movimiento entrada(MovimientoRequest r, UsuarioAutenticado usuario) {
        Insumo i = insumoActivo(r.insumoId());
        mongo.findAndModify(Query.query(Criteria.where("id").is(i.getId())),
                new Update().inc("stockActual", r.cantidad()),
                FindAndModifyOptions.options().returnNew(true), Insumo.class);
        return registrar(TipoMovimiento.ENTRADA, r, usuario);
    }

    /** RF-09: valida existencias suficientes y descuenta del stock. */
    public Movimiento salida(MovimientoRequest r, UsuarioAutenticado usuario) {
        Insumo i = insumoActivo(r.insumoId());
        Insumo actualizado = mongo.findAndModify(
                Query.query(Criteria.where("id").is(i.getId()).and("stockActual").gte(r.cantidad())),
                new Update().inc("stockActual", -r.cantidad()),
                FindAndModifyOptions.options().returnNew(true), Insumo.class);
        if (actualizado == null) {
            throw new NegocioException("Stock insuficiente de " + i.getNombre() + ". Disponible: "
                    + i.getStockActual() + " " + i.getUnidadMedida() + ".");
        }
        return registrar(TipoMovimiento.SALIDA, r, usuario);
    }

    /** Salida asociada a una cosecha (la invoca ms-cosechas, RF-15). */
    public Movimiento consumo(MovimientoRequest r, UsuarioAutenticado usuario) {
        if (r.cosechaId() == null || r.cosechaId().isBlank()) {
            throw new NegocioException("El consumo debe indicar la cosecha (cosechaId).");
        }
        return salida(r, usuario);
    }

    /** RF-14: historial con filtros opcionales por insumo, tipo y rango de fechas. */
    public List<Movimiento> historial(String insumoId, TipoMovimiento tipo, LocalDate desde, LocalDate hasta) {
        Query q = new Query().with(Sort.by(Sort.Direction.DESC, "fecha"));
        if (insumoId != null && !insumoId.isBlank()) q.addCriteria(Criteria.where("insumoId").is(insumoId));
        if (tipo != null) q.addCriteria(Criteria.where("tipo").is(tipo));
        if (desde != null || hasta != null) {
            Criteria fecha = Criteria.where("fecha");
            if (desde != null) fecha = fecha.gte(desde.atStartOfDay());
            if (hasta != null) fecha = fecha.lte(hasta.atTime(LocalTime.MAX));
            q.addCriteria(fecha);
        }
        return mongo.find(q, Movimiento.class);
    }

    private Insumo insumoActivo(String id) {
        Insumo i = insumos.findById(id).orElseThrow(() -> new NoEncontradoException("No existe el insumo con id " + id));
        if (i.getEstado() != Estado.ACTIVO) {
            throw new NegocioException("El insumo " + i.getNombre() + " está desactivado.");
        }
        return i;
    }

    private Movimiento registrar(TipoMovimiento tipo, MovimientoRequest r, UsuarioAutenticado usuario) {
        Movimiento m = new Movimiento();
        m.setInsumoId(r.insumoId());
        m.setTipo(tipo);
        m.setCantidad(r.cantidad());
        m.setFecha(LocalDateTime.now());
        m.setCosechaId(r.cosechaId() == null || r.cosechaId().isBlank() ? null : r.cosechaId());
        m.setUsuarioId(usuario.id());
        m.setUsuarioNombre(usuario.nombre());
        m.setObservacion(r.observacion());
        return movimientos.save(m);
    }
}
