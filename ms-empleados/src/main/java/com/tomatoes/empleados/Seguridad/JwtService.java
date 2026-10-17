package com.tomatoes.empleados.Seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/** Valida tokens JWT firmados con la clave compartida entre los microservicios (variable JWT_SECRET). */
@Service
public class JwtService {

    private final SecretKey clave;

    public JwtService(@Value("${tomatoes.jwt.secret}") String secreto) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }

    public SecretKey getClave() {
        return clave;
    }

    public Claims validar(String token) {
        return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
    }
}
