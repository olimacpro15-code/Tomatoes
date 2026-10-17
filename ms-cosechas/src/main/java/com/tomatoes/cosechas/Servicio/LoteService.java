package com.tomatoes.cosechas.Servicio;

import com.tomatoes.cosechas.Dto.LoteRequest;
import com.tomatoes.cosechas.Entidades.Estado;
import com.tomatoes.cosechas.Entidades.EstadoCosecha;
import com.tomatoes.cosechas.Entidades.Lote;
import com.tomatoes.cosechas.Excepciones.NegocioException;
import com.tomatoes.cosechas.Excepciones.NoEncontradoException;
import com.tomatoes.cosechas.Repositorio.CosechaRepository;
import com.tomatoes.cosechas.Repositorio.LoteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoteService {

    private final LoteRepository repo;
    private final CosechaRepository cosechas;

    public LoteService(LoteRepository repo, CosechaRepository cosechas) {
        this.repo = repo;
        this.cosechas = cosechas;
    }

    public List<Lote> listar(Estado estado) {
        return estado == null ? repo.findAll(Sort.by("nombre")) : repo.findByEstado(estado);
    }

    public Lote obtener(String id) {
        return repo.findById(id).orElseThrow(() -> new NoEncontradoException("No existe el lote con id " + id));
    }

    public Lote crear(LoteRequest r) {
        repo.findByNombreIgnoreCase(r.nombre().trim())
            .ifPresent(l -> { throw new NegocioException("Ya existe un lote llamado '" + r.nombre() + "'."); });
        Lote l = aplicar(new Lote(), r);
        l.setEstado(Estado.ACTIVO);
        return repo.save(l);
    }

    public Lote actualizar(String id, LoteRequest r) {
        Lote l = obtener(id);
        repo.findByNombreIgnoreCase(r.nombre().trim()).filter(o -> !o.getId().equals(id))
            .ifPresent(o -> { throw new NegocioException("Ya existe otro lote llamado '" + r.nombre() + "'."); });
        return repo.save(aplicar(l, r));
    }

    public Lote desactivar(String id) {
        Lote l = obtener(id);
        if (cosechas.existsByLoteIdAndEstadoNot(id, EstadoCosecha.FINALIZADA)) {
            throw new NegocioException("No se puede desactivar el lote: tiene cosechas planeadas o en curso.");
        }
        l.setEstado(Estado.INACTIVO);
        return repo.save(l);
    }

    public Lote activar(String id) {
        Lote l = obtener(id);
        l.setEstado(Estado.ACTIVO);
        return repo.save(l);
    }

    private Lote aplicar(Lote l, LoteRequest r) {
        l.setNombre(r.nombre().trim());
        l.setAreaHectareas(r.areaHectareas());
        l.setUbicacion(r.ubicacion().trim());
        l.setCultivo(r.cultivo().trim());
        return l;
    }
}
