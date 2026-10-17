package com.tomatoes.insumos.Controlador;

import com.tomatoes.insumos.Dto.ProveedorRequest;
import com.tomatoes.insumos.Entidades.Proveedor;
import com.tomatoes.insumos.Servicio.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@Tag(name = "Proveedores", description = "Proveedores de insumos")
public class ProveedorRestControlador {

    private final ProveedorService servicio;

    public ProveedorRestControlador(ProveedorService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Listar proveedores")
    @GetMapping
    @PreAuthorize("hasAnyRole('BODEGA','GERENTE','ADMINISTRADOR')")
    public List<Proveedor> listar() {
        return servicio.listar();
    }

    @Operation(summary = "Consultar un proveedor por id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BODEGA','GERENTE','ADMINISTRADOR')")
    public Proveedor obtener(@PathVariable String id) {
        return servicio.obtener(id);
    }

    @Operation(summary = "Crear proveedor")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('BODEGA')")
    public Proveedor crear(@Valid @RequestBody ProveedorRequest request) {
        return servicio.crear(request);
    }

    @Operation(summary = "Editar proveedor")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BODEGA')")
    public Proveedor actualizar(@PathVariable String id, @Valid @RequestBody ProveedorRequest request) {
        return servicio.actualizar(id, request);
    }

    @Operation(summary = "Eliminar proveedor", description = "No se puede eliminar si tiene insumos asociados.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BODEGA')")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
