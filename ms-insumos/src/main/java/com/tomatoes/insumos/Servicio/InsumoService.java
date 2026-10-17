package com.tomatoes.insumos.Servicio;

import com.tomatoes.insumos.Dto.InsumoRequest;
import com.tomatoes.insumos.Entidades.*;
import com.tomatoes.insumos.Excepciones.NegocioException;
import com.tomatoes.insumos.Excepciones.NoEncontradoException;
import com.tomatoes.insumos.Repositorio.InsumoRepository;
import com.tomatoes.insumos.Repositorio.MovimientoRepository;
import com.tomatoes.insumos.Repositorio.ProveedorRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class InsumoService {

    private final InsumoRepository repo;
    private final ProveedorRepository proveedores;
    private final MovimientoRepository movimientos;

    public InsumoService(InsumoRepository repo, ProveedorRepository proveedores, MovimientoRepository movimientos) {
        this.repo = repo;
        this.proveedores = proveedores;
        this.movimientos = movimientos;
    }

    public List<Insumo> listar(Estado estado) {
        return estado == null ? repo.findAll(Sort.by("nombre")) : repo.findByEstado(estado);
    }

    public Insumo obtener(String id) {
        return repo.findById(id).orElseThrow(() -> new NoEncontradoException("No existe el insumo con id " + id));
    }

    /** RF-13: insumos activos cuyo stock es menor o igual al stock mínimo. */
    public List<Insumo> stockBajo() {
        return repo.findByEstado(Estado.ACTIVO).stream()
                .filter(i -> i.getStockActual() <= i.getStockMinimo())
                .sorted(Comparator.comparingDouble(Insumo::getStockActual))
                .toList();
    }

    public Insumo crear(InsumoRequest r, String usuarioId, String usuarioNombre) {
        validarProveedor(r.proveedorId());
        Insumo i = new Insumo();
        aplicar(i, r);
        i.setStockActual(r.stockActual() == null ? 0 : r.stockActual());
        i.setEstado(Estado.ACTIVO);
        i = repo.save(i);
        if (i.getStockActual() > 0) {
            Movimiento m = new Movimiento();
            m.setInsumoId(i.getId()); m.setTipo(TipoMovimiento.ENTRADA); m.setCantidad(i.getStockActual());
            m.setFecha(LocalDateTime.now()); m.setUsuarioId(usuarioId); m.setUsuarioNombre(usuarioNombre);
            m.setObservacion("Stock inicial al crear el insumo");
            movimientos.save(m);
        }
        return i;
    }

    public Insumo actualizar(String id, InsumoRequest r) {
        validarProveedor(r.proveedorId());
        Insumo i = obtener(id);
        aplicar(i, r);
        return repo.save(i);
    }

    public Insumo cambiarEstado(String id, Estado estado) {
        Insumo i = obtener(id);
        i.setEstado(estado);
        return repo.save(i);
    }

    private void aplicar(Insumo i, InsumoRequest r) {
        i.setNombre(r.nombre().trim());
        i.setCategoria(r.categoria());
        i.setUnidadMedida(r.unidadMedida().trim());
        i.setStockMinimo(r.stockMinimo());
        i.setCostoUnitario(r.costoUnitario());
        i.setProveedorId(r.proveedorId() == null || r.proveedorId().isBlank() ? null : r.proveedorId());
    }

    private void validarProveedor(String proveedorId) {
        if (proveedorId != null && !proveedorId.isBlank() && !proveedores.existsById(proveedorId)) {
            throw new NegocioException("El proveedor seleccionado no existe.");
        }
    }
}
