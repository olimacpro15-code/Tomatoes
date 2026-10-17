package com.tomatoes.cosechas.Controlador;

import com.tomatoes.cosechas.Dto.*;
import com.tomatoes.cosechas.Entidades.Cosecha;
import com.tomatoes.cosechas.Entidades.EstadoCosecha;
import com.tomatoes.cosechas.Seguridad.UsuarioAutenticado;
import com.tomatoes.cosechas.Servicio.CosechaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cosechas")
@Tag(name = "Cosechas", description = "Ciclos de cosecha, producción, equipo asignado e insumos consumidos")
public class CosechaRestControlador {

    private final CosechaService servicio;

    public CosechaRestControlador(CosechaService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Listar cosechas", description = "Filtros opcionales: estado (PLANEADA, EN_CURSO, FINALIZADA) y loteId.")
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR','GERENTE','ADMINISTRADOR')")
    public List<Cosecha> listar(@RequestParam(required = false) EstadoCosecha estado, @RequestParam(required = false) String loteId) {
        return servicio.listar(estado, loteId);
    }

    @Operation(summary = "Mis labores", description = "Cosechas en las que el operario autenticado está asignado.")
    @GetMapping("/mis-labores")
    @PreAuthorize("hasRole('OPERARIO')")
    public List<Cosecha> misLabores(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicio.misLabores(usuario.id());
    }

    @Operation(summary = "Consultar una cosecha por id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERVISOR','GERENTE','ADMINISTRADOR')")
    public Cosecha obtener(@PathVariable String id) {
        return servicio.obtener(id);
    }

    @Operation(summary = "Crear cosecha", description = "Debe pertenecer a un lote existente y activo; inicia en estado PLANEADA.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Cosecha crear(@Valid @RequestBody CosechaRequest request) {
        return servicio.crear(request);
    }

    @Operation(summary = "Cambiar estado y registrar producción",
               description = "PLANEADA, EN_CURSO o FINALIZADA (solo hacia adelante). Para finalizar se exige cantidadKg y calidad (PRIMERA, SEGUNDA, TERCERA).")
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Cosecha cambiarEstado(@PathVariable String id, @Valid @RequestBody EstadoCosechaRequest request) {
        return servicio.cambiarEstado(id, request);
    }

    @Operation(summary = "Asignar empleados", description = "Valida con ms-empleados que cada empleado exista y esté activo.")
    @PostMapping("/{id}/empleados")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Cosecha asignarEmpleados(@PathVariable String id, @Valid @RequestBody AsignacionRequest request) {
        return servicio.asignarEmpleados(id, request);
    }

    @Operation(summary = "Quitar un empleado de la cosecha")
    @DeleteMapping("/{id}/empleados/{empleadoId}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Cosecha quitarEmpleado(@PathVariable String id, @PathVariable String empleadoId) {
        return servicio.quitarEmpleado(id, empleadoId);
    }

    @Operation(summary = "Registrar insumo usado",
               description = "Invoca a ms-insumos para descontar el stock. Si no hay existencias suficientes responde 400 con el detalle.")
    @PostMapping("/{id}/insumos")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public Cosecha registrarInsumo(@PathVariable String id, @Valid @RequestBody ConsumoRequest request) {
        return servicio.registrarInsumo(id, request);
    }
}
