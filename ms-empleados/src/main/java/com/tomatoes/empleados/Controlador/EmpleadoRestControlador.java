package com.tomatoes.empleados.Controlador;

import com.tomatoes.empleados.Dto.CambioRolRequest;
import com.tomatoes.empleados.Dto.EmpleadoRequest;
import com.tomatoes.empleados.Entidades.Empleado;
import com.tomatoes.empleados.Entidades.Estado;
import com.tomatoes.empleados.Seguridad.UsuarioAutenticado;
import com.tomatoes.empleados.Servicio.EmpleadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
@Tag(name = "Empleados", description = "Gestión de empleados y roles (escritura solo para el Administrador)")
public class EmpleadoRestControlador {

    private final EmpleadoService servicio;

    public EmpleadoRestControlador(EmpleadoService servicio) {
        this.servicio = servicio;
    }

    @Operation(summary = "Listar empleados", description = "Se puede filtrar por estado (ACTIVO o INACTIVO).")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','GERENTE')")
    public List<Empleado> listar(@RequestParam(required = false) Estado estado) {
        return servicio.listar(estado);
    }

    @Operation(summary = "Consultar un empleado por id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','GERENTE')")
    public Empleado obtener(@PathVariable String id) {
        return servicio.obtener(id);
    }

    @Operation(summary = "Crear empleado", description = "Valida que el documento y el correo no se repitan y guarda la contraseña cifrada con BCrypt.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Empleado crear(@Valid @RequestBody EmpleadoRequest request) {
        return servicio.crear(request);
    }

    @Operation(summary = "Editar empleado", description = "La contraseña es opcional: si no se envía, se conserva la actual.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Empleado actualizar(@PathVariable String id, @Valid @RequestBody EmpleadoRequest request) {
        return servicio.actualizar(id, request);
    }

    @Operation(summary = "Desactivar empleado", description = "Borrado lógico: el empleado pasa a INACTIVO y ya no puede iniciar sesión.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Empleado desactivar(@PathVariable String id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicio.desactivar(id, usuario.id());
    }

    @Operation(summary = "Reactivar empleado")
    @PutMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Empleado activar(@PathVariable String id) {
        return servicio.activar(id);
    }

    @Operation(summary = "Asignar rol", description = "Cada usuario tiene un único rol: ADMINISTRADOR, GERENTE, SUPERVISOR, BODEGA u OPERARIO.")
    @PutMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Empleado cambiarRol(@PathVariable String id, @Valid @RequestBody CambioRolRequest request) {
        return servicio.cambiarRol(id, request.rol());
    }
}
