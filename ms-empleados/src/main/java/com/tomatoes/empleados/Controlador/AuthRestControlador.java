package com.tomatoes.empleados.Controlador;

import com.tomatoes.empleados.Dto.LoginRequest;
import com.tomatoes.empleados.Dto.LoginResponse;
import com.tomatoes.empleados.Servicio.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Inicio de sesión y emisión del token JWT")
public class AuthRestControlador {

    private final AuthService auth;

    public AuthRestControlador(AuthService auth) {
        this.auth = auth;
    }

    @Operation(summary = "Iniciar sesión", description = "Valida correo y contraseña y devuelve el token JWT junto con el rol del usuario.")
    @SecurityRequirements
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return auth.login(request);
    }
}
