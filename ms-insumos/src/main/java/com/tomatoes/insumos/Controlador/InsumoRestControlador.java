package com.tomatoes.insumos.Controlador;

import com.tomatoes.insumos.Dto.InsumoRequest;
import com.tomatoes.insumos.Entidades.Estado;
import com.tomatoes.insumos.Entidades.Insumo;
import com.tomatoes.insumos.Seguridad.UsuarioAutenticado;
import com.tomatoes.insumos.Servicio.InsumoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insumos")
@Tag(name = "Insumos", description = "Catálogo de insumos agrícolas (escritura solo para el Encargado de Bodega)")
public class InsumoRestControlador {

    private final InsumoService servicio;

    public InsumoRestControlador(InsumoService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Listar insumos", description = "Filtro opcional por estado (ACTIVO o INACTIVO).")
    @GetMapping
    @PreAuthorize("hasAnyRole('BODEGA','SUPERVISOR','GERENTE','ADMINISTRADOR')")
    public List<Insumo> listar(@RequestParam(required = false) Estado estado) {
        return servicio.listar(estado);
    }

    @Operation(summary = "Insumos con stock bajo", description = "Activos cuyo stock actual es menor o igual al stock mínimo.")
    @GetMapping("/stock-bajo")
    @PreAuthorize("hasAnyRole('BODEGA','SUPERVISOR','GERENTE','ADMINISTRADOR')")
    public List<Insumo> stockBajo() {
        return servicio.stockBajo();
    }

    @Operation(summary = "Consultar un insumo por id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BODEGA','SUPERVISOR','GERENTE','ADMINISTRADOR')")
    public Insumo obtener(@PathVariable String id) {
        return servicio.obtener(id);
    }

    @Operation(summary = "Crear insumo", description = "Si se envía stockActual > 0 se registra automáticamente una entrada de 'stock inicial'.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('BODEGA')")
    public Insumo crear(@Valid @RequestBody InsumoRequest request, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicio.crear(request, usuario.id(), usuario.nombre());
    }

    @Operation(summary = "Editar insumo", description = "No modifica el stock: para eso se usan las entradas y salidas.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BODEGA')")
    public Insumo actualizar(@PathVariable String id, @Valid @RequestBody InsumoRequest request) {
        return servicio.actualizar(id, request);
    }

    @Operation(summary = "Desactivar insumo", description = "Borrado lógico: se conserva el historial de movimientos.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BODEGA')")
    public Insumo desactivar(@PathVariable String id) {
        return servicio.cambiarEstado(id, Estado.INACTIVO);
    }

    @Operation(summary = "Reactivar insumo")
    @PutMapping("/{id}/activar")
    @PreAuthorize("hasRole('BODEGA')")
    public Insumo activar(@PathVariable String id) {
        return servicio.cambiarEstado(id, Estado.ACTIVO);
    }
}
