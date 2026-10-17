package com.tomatoes.web.Config;

import com.tomatoes.web.Seguridad.UsuarioSesion;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Variables disponibles en todas las vistas: usuario conectado y ruta actual (para marcar el menú activo). */
@ControllerAdvice
public class AtributosGlobales {

    @ModelAttribute("rutaActual")
    public String rutaActual(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("usuario")
    public UsuarioSesion usuario(Authentication autenticacion) {
        return autenticacion != null && autenticacion.getPrincipal() instanceof UsuarioSesion u ? u : null;
    }
}
