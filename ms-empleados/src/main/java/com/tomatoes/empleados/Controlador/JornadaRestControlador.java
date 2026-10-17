package com.tomatoes.empleados.Controlador;

import com.tomatoes.empleados.Dto.JornadaRequest;
import com.tomatoes.empleados.Dto.ResumenHoras;
import com.tomatoes.empleados.Entidades.Jornada;
import com.tomatoes.empleados.Seguridad.UsuarioAutenticado;
import com.tomatoes.empleados.Servicio.JornadaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/jornadas")
@Tag(name = "Jornadas", description = "Registro de jornadas de trabajo y reporte de horas")
public class JornadaRestControlador {

    private final JornadaService servicio;

    public JornadaRestControlador(JornadaService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Registrar mi jornada", description = "El empleado se toma del token JWT: el operario solo registra jornadas propias.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OPERARIO')")
    public Jornada registrar(@Valid @RequestBody JornadaRequest request, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicio.registrar(usuario.id(), request);
    }

    @Operation(summary = "Consultar mis jornadas")
    @GetMapping("/mias")
    @PreAuthorize("hasRole('OPERARIO')")
    public List<Jornada> mias(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicio.deEmpleado(usuario.id());
    }

    @Operation(summary = "Consultar jornadas", description = "Filtros opcionales: empleadoId, desde y hasta (formato AAAA-MM-DD).")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE')")
    public List<Jornada> listar(@RequestParam(required = false) String empleadoId,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.listar(empleadoId, desde, hasta);
    }

    @Operation(summary = "Reporte de horas por empleado", description = "Suma las horas laboradas por cada empleado en el periodo indicado.")
    @GetMapping("/reporte")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE')")
    public List<ResumenHoras> reporte(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return servicio.reporteHoras(desde, hasta);
    }
}
