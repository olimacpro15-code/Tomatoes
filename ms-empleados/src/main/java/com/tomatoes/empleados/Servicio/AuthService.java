package com.tomatoes.empleados.Servicio;

import com.tomatoes.empleados.Dto.LoginRequest;
import com.tomatoes.empleados.Dto.LoginResponse;
import com.tomatoes.empleados.Entidades.Empleado;
import com.tomatoes.empleados.Entidades.Estado;
import com.tomatoes.empleados.Excepciones.NoAutorizadoException;
import com.tomatoes.empleados.Repositorio.EmpleadoRepository;
import com.tomatoes.empleados.Seguridad.JwtService;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/** Valida credenciales (BCrypt) y emite el token JWT que usan el frontend y los demás microservicios. */
@Service
public class AuthService {

    private final EmpleadoRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final long horas;

    public AuthService(EmpleadoRepository repo, PasswordEncoder encoder, JwtService jwtService,
                       @Value("${tomatoes.jwt.expiracion-horas}") long horas) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.horas = horas;
    }

    public LoginResponse login(LoginRequest r) {
        Empleado e = repo.findByCorreo(r.correo().trim().toLowerCase())
                .orElseThrow(() -> new NoAutorizadoException("Correo o contraseña incorrectos."));
        if (!encoder.matches(r.password(), e.getPasswordHash())) {
            throw new NoAutorizadoException("Correo o contraseña incorrectos.");
        }
        if (e.getEstado() != Estado.ACTIVO) {
            throw new NoAutorizadoException("Su usuario está desactivado. Comuníquese con el administrador.");
        }
        String nombre = e.getNombres() + " " + e.getApellidos();
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(horas, ChronoUnit.HOURS);
        String token = Jwts.builder()
                .subject(e.getCorreo())
                .claim("id", e.getId())
                .claim("rol", e.getRol().name())
                .claim("nombre", nombre)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expira))
                .signWith(jwtService.getClave())
                .compact();
        return new LoginResponse(token, "Bearer", e.getId(), nombre, e.getCorreo(), e.getRol().name(), horas * 3600);
    }
}
