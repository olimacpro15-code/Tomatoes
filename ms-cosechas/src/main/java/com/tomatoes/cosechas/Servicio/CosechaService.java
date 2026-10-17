package com.tomatoes.cosechas.Servicio;

import com.tomatoes.cosechas.Dto.*;
import com.tomatoes.cosechas.Entidades.*;
import com.tomatoes.cosechas.Excepciones.NegocioException;
import com.tomatoes.cosechas.Excepciones.NoEncontradoException;
import com.tomatoes.cosechas.Repositorio.CosechaRepository;
import com.tomatoes.cosechas.Repositorio.LoteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CosechaService {

    private final CosechaRepository repo;
    private final LoteRepository lotes;
    private final ClienteMicroservicios clientes;

    public CosechaService(CosechaRepository repo, LoteRepository lotes, ClienteMicroservicios clientes) {
        this.repo = repo;
        this.lotes = lotes;
        this.clientes = clientes;
    }

    public List<Cosecha> listar(EstadoCosecha estado, String loteId) {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "fechaInicio")).stream()
                .filter(c -> estado == null || c.getEstado() == estado)
                .filter(c -> loteId == null || loteId.isBlank() || loteId.equals(c.getLoteId()))
                .collect(Collectors.toList());
    }

    public Cosecha obtener(String id) {
        return repo.findById(id).orElseThrow(() -> new NoEncontradoException("No existe la cosecha con id " + id));
    }

    /** HU-04: labores (cosechas) en las que el operario está asignado. */
    public List<Cosecha> misLabores(String empleadoId) {
        return repo.buscarPorEmpleado(empleadoId);
    }

    public Cosecha crear(CosechaRequest r) {
        Lote lote = lotes.findById(r.loteId()).orElseThrow(() -> new NegocioException("El lote seleccionado no existe."));
        if (lote.getEstado() != Estado.ACTIVO) {
            throw new NegocioException("El lote " + lote.getNombre() + " está desactivado.");
        }
        validarFechas(r.fechaInicio(), r.fechaFin());
        Cosecha c = new Cosecha();
        c.setLoteId(lote.getId());
        c.setFechaInicio(r.fechaInicio());
        c.setFechaFin(r.fechaFin());
        c.setEstado(EstadoCosecha.PLANEADA);
        return repo.save(c);
    }

    /** RF-10 y RF-11: cambia el estado (solo hacia adelante) y registra producción y calidad. */
    public Cosecha cambiarEstado(String id, EstadoCosechaRequest r) {
        Cosecha c = obtener(id);
        if (c.getEstado() == EstadoCosecha.FINALIZADA) {
            throw new NegocioException("La cosecha ya está finalizada y no admite cambios.");
        }
        if (r.estado().ordinal() < c.getEstado().ordinal()) {
            throw new NegocioException("No se puede retroceder el estado de la cosecha.");
        }
        if (r.cantidadKg() != null) c.setCantidadKg(r.cantidadKg());
        if (r.calidad() != null) c.setCalidad(r.calidad());
        if (r.estado() == EstadoCosecha.FINALIZADA) {
            if (c.getCantidadKg() == null || c.getCantidadKg() <= 0) {
                throw new NegocioException("Para finalizar la cosecha registre la cantidad cosechada (kg).");
            }
            if (c.getCalidad() == null) {
                throw new NegocioException("Para finalizar la cosecha seleccione la calidad obtenida.");
            }
            c.setFechaFin(r.fechaFin() != null ? r.fechaFin() : (c.getFechaFin() != null ? c.getFechaFin() : LocalDate.now()));
        } else if (r.fechaFin() != null) {
            c.setFechaFin(r.fechaFin());
        }
        validarFechas(c.getFechaInicio(), c.getFechaFin());
        c.setEstado(r.estado());
        return repo.save(c);
    }

    /** RF-12: solo se asignan empleados activos (se valida contra ms-empleados). */
    public Cosecha asignarEmpleados(String id, AsignacionRequest r) {
        Cosecha c = modificable(id);
        for (String empleadoId : r.empleadosIds()) {
            Map<String, Object> e = clientes.empleado(empleadoId);
            if (!"ACTIVO".equals(e.get("estado"))) {
                throw new NegocioException("El empleado " + e.get("nombres") + " " + e.get("apellidos") + " no está activo.");
            }
            if (!c.getEmpleadosIds().contains(empleadoId)) c.getEmpleadosIds().add(empleadoId);
        }
        return repo.save(c);
    }

    public Cosecha quitarEmpleado(String id, String empleadoId) {
        Cosecha c = modificable(id);
        c.getEmpleadosIds().remove(empleadoId);
        return repo.save(c);
    }

    /** RF-15: descuenta el insumo del inventario (llamada REST a ms-insumos) y lo registra en la cosecha. */
    public Cosecha registrarInsumo(String id, ConsumoRequest r) {
        Cosecha c = modificable(id);
        Map<String, Object> insumo = clientes.insumo(r.insumoId());
        clientes.consumirInsumo(r.insumoId(), r.cantidad(), id); // lanza error si no hay stock suficiente
        double costo = insumo.get("costoUnitario") instanceof Number n ? n.doubleValue() : 0;
        Optional<InsumoUsado> existente = c.getInsumosUsados().stream().filter(u -> u.getInsumoId().equals(r.insumoId())).findFirst();
        if (existente.isPresent()) {
            existente.get().setCantidad(existente.get().getCantidad() + r.cantidad());
        } else {
            InsumoUsado u = new InsumoUsado();
            u.setInsumoId(r.insumoId());
            u.setInsumoNombre(String.valueOf(insumo.get("nombre")));
            u.setCantidad(r.cantidad());
            u.setCostoUnitario(costo);
            c.getInsumosUsados().add(u);
        }
        return repo.save(c);
    }

    /** RF-17: producción total por lote; usa la fecha de fin (o la de inicio si aún no termina) para el rango. */
    public List<ReporteProduccion> reporteProduccion(LocalDate desde, LocalDate hasta, String loteId) {
        Map<String, Lote> catalogo = lotes.findAll().stream().collect(Collectors.toMap(Lote::getId, l -> l));
        Map<String, List<Cosecha>> porLote = repo.findAll().stream()
                .filter(c -> c.getCantidadKg() != null && c.getCantidadKg() > 0)
                .filter(c -> loteId == null || loteId.isBlank() || loteId.equals(c.getLoteId()))
                .filter(c -> {
                    LocalDate f = c.getFechaFin() != null ? c.getFechaFin() : c.getFechaInicio();
                    return (desde == null || !f.isBefore(desde)) && (hasta == null || !f.isAfter(hasta));
                })
                .collect(Collectors.groupingBy(Cosecha::getLoteId));
        return porLote.entrySet().stream().map(en -> {
            Lote l = catalogo.get(en.getKey());
            double kg = en.getValue().stream().mapToDouble(Cosecha::getCantidadKg).sum();
            double costo = en.getValue().stream().flatMap(c -> c.getInsumosUsados().stream())
                    .mapToDouble(u -> u.getCantidad() * u.getCostoUnitario()).sum();
            return new ReporteProduccion(en.getKey(), l != null ? l.getNombre() : "Lote eliminado",
                    l != null ? l.getCultivo() : "-", en.getValue().size(), kg, costo);
        }).sorted(Comparator.comparingDouble(ReporteProduccion::totalKg).reversed()).collect(Collectors.toList());
    }

    private Cosecha modificable(String id) {
        Cosecha c = obtener(id);
        if (c.getEstado() == EstadoCosecha.FINALIZADA) {
            throw new NegocioException("La cosecha está finalizada y no admite cambios.");
        }
        return c;
    }

    private void validarFechas(LocalDate inicio, LocalDate fin) {
        if (inicio != null && fin != null && fin.isBefore(inicio)) {
            throw new NegocioException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
    }
}
