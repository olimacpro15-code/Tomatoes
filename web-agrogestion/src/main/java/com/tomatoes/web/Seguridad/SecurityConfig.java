package com.tomatoes.web.Seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Inicio de sesión con formulario. Las rutas públicas son la portada, el login y los recursos estáticos;
 * los permisos de cada módulo por rol se declaran con @PreAuthorize en los controladores.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain cadenaDeSeguridad(HttpSecurity http, ProveedorAutenticacion proveedor) throws Exception {
        http.authenticationProvider(proveedor)
            .authorizeHttpRequests(a -> a
                .requestMatchers("/", "/login", "/css/**", "/js/**", "/img/**", "/favicon.ico",
                        "/error", "/error/**", "/actuator/health/**").permitAll()
                .anyRequest().authenticated())
            .formLogin(f -> f
                .loginPage("/login").loginProcessingUrl("/login")
                .usernameParameter("correo").passwordParameter("password")
                .defaultSuccessUrl("/inicio", true)
                .failureHandler((req, res, ex) -> {
                    req.getSession().setAttribute("LOGIN_ERROR", ex.getMessage());
                    res.sendRedirect(req.getContextPath() + "/login?error");
                })
                .permitAll())
            .logout(l -> l
                .logoutUrl("/logout").logoutSuccessUrl("/login?salio")
                .invalidateHttpSession(true).deleteCookies("JSESSIONID"))
            .exceptionHandling(e -> e.accessDeniedPage("/acceso-denegado"));
        return http.build();
    }
}
