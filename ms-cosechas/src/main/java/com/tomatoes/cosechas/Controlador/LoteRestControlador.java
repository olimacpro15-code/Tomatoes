package com.tomatoes.cosechas.Controlador;

import com.tomatoes.cosechas.Dto.LoteRequest;
import com.tomatoes.cosechas.Entidades.Estado;
import com.tomatoes.cosechas.Entidades.Lote;
import com.tomatoes.cosechas.Servicio.LoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lotes")
@Tag(name = "Lotes", description = "Lotes de cultivo (escritura solo para el Supervisor de Cosechas)")
public class LoteRestControlador {

    private final LoteService servicio;

    public LoteRestControlador(LoteService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Listar lotes", description = "Filtro opcional por estado (ACTIVO o INACTIVO).")
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR','GERENTE','ADMINISTRADOR','OPERARIO')")
    public List<Lote> listar(@RequestParam(required = false) Estado estado) {
        return servicio.listar(estado);
    }

    @Operation(summary = "Consultar un lote por id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERVISOR','GERENTE','ADMINISTRADOR','OPERARIO')")
    public Lote obtener(@PathVariable String id) {
        return servicio.obtener(id);
    }

    @Operation(summary = "Crear lote", description = "El nombre del lote debe ser único.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Lote crear(@Valid @RequestBody LoteRequest request) {
        return servicio.crear(request);
    }

    @Operation(summary = "Editar lote")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Lote actualizar(@PathVariable String id, @Valid @RequestBody LoteRequest request) {
        return servicio.actualizar(id, request);
    }

    @Operation(summary = "Desactivar lote", description = "Borrado lógico. No se permite si tiene cosechas planeadas o en curso.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Lote desactivar(@PathVariable String id) {
        return servicio.desactivar(id);
    }

    @Operation(summary = "Reactivar lote")
    @PutMapping("/{id}/activar")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Lote activar(@PathVariable String id) {
        return servicio.activar(id);
    }
}
