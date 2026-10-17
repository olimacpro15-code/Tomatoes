package com.tomatoes.web.Excepciones;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ManejadorExcepcionesWeb {

    @ExceptionHandler(ServicioNoDisponibleException.class)
    public String servicioNoDisponible(ServicioNoDisponibleException ex, Model model, HttpServletResponse res) {
        res.setStatus(503);
        model.addAttribute("mensaje", ex.getMessage());
        return "error/servicio";
    }

    @ExceptionHandler(ApiException.class)
    public String errorDeApi(ApiException ex, Model model, HttpServletResponse res) {
        res.setStatus(ex.getEstado() >= 400 ? ex.getEstado() : 400);
        model.addAttribute("mensaje", ex.getMessage());
        return "error/servicio";
    }

    @ExceptionHandler(SesionExpiradaException.class)
    public String sesionExpirada(HttpServletRequest req) {
        SecurityContextHolder.clearContext();
        if (req.getSession(false) != null) req.getSession(false).invalidate();
        return "redirect:/login?expirada";
    }
}
