package com.tomatoes.empleados.Seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * API sin estado (stateless): cada petición debe traer un JWT válido.
 * Los permisos por rol se declaran con @PreAuthorize en cada controlador.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain cadenaDeSeguridad(HttpSecurity http, JwtService jwtService, ObjectMapper mapper) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                        "/actuator/health/**", "/api/auth/login").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) ->
                        escribir(res, mapper, 401, "No autenticado: el token JWT falta, expiró o no es válido."))
                .accessDeniedHandler((req, res, ex) ->
                        escribir(res, mapper, 403, "No tiene permisos para realizar esta operación.")))
            .addFilterBefore(new JwtFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static void escribir(HttpServletResponse res, ObjectMapper mapper, int estado, String mensaje) throws IOException {
        res.setStatus(estado);
        res.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(res.getOutputStream(),
                Map.of("timestamp", LocalDateTime.now().toString(), "status", estado, "mensaje", mensaje));
    }
}
