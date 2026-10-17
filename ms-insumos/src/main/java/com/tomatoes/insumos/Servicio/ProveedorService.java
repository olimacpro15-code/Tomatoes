package com.tomatoes.insumos.Servicio;

import com.tomatoes.insumos.Dto.ProveedorRequest;
import com.tomatoes.insumos.Entidades.Proveedor;
import com.tomatoes.insumos.Excepciones.NegocioException;
import com.tomatoes.insumos.Excepciones.NoEncontradoException;
import com.tomatoes.insumos.Repositorio.InsumoRepository;
import com.tomatoes.insumos.Repositorio.ProveedorRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProveedorService {

    private final ProveedorRepository repo;
    private final InsumoRepository insumos;

    public ProveedorService(ProveedorRepository repo, InsumoRepository insumos) {
        this.repo = repo;
        this.insumos = insumos;
    }

    public List<Proveedor> listar() {
        return repo.findAll(Sort.by("nombre"));
    }

    public Proveedor obtener(String id) {
        return repo.findById(id).orElseThrow(() -> new NoEncontradoException("No existe el proveedor con id " + id));
    }

    public Proveedor crear(ProveedorRequest r) {
        repo.findByNit(r.nit().trim()).ifPresent(p -> { throw new NegocioException("Ya existe un proveedor con el NIT " + r.nit() + "."); });
        return repo.save(aplicar(new Proveedor(), r));
    }

    public Proveedor actualizar(String id, ProveedorRequest r) {
        Proveedor p = obtener(id);
        repo.findByNit(r.nit().trim()).filter(o -> !o.getId().equals(id))
            .ifPresent(o -> { throw new NegocioException("Ya existe otro proveedor con el NIT " + r.nit() + "."); });
        return repo.save(aplicar(p, r));
    }

    public void eliminar(String id) {
        obtener(id);
        if (insumos.existsByProveedorId(id)) {
            throw new NegocioException("No se puede eliminar: hay insumos asociados a este proveedor. Cambie el proveedor de esos insumos primero.");
        }
        repo.deleteById(id);
    }

    private Proveedor aplicar(Proveedor p, ProveedorRequest r) {
        p.setNombre(r.nombre().trim());
        p.setNit(r.nit().trim());
        p.setTelefono(r.telefono());
        p.setCorreo(r.correo() == null || r.correo().isBlank() ? null : r.correo().trim().toLowerCase());
        return p;
    }
}
