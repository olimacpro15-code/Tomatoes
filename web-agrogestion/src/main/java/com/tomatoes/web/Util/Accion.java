package com.tomatoes.web.Util;

import com.tomatoes.web.Excepciones.ApiException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Ejecuta una operación contra un microservicio y deja el resultado como mensaje en la siguiente pantalla. */
public final class Accion {

    private Accion() {}

    public static String hacer(RedirectAttributes ra, String destino, Runnable operacion, String mensajeExito) {
        try {
            operacion.run();
            ra.addFlashAttribute("exito", mensajeExito);
        } catch (ApiException e) {
            ra.addFlashAttribute("error", e.getMessage());
            if (e.getErrores() != null && !e.getErrores().isEmpty()) ra.addFlashAttribute("errores", e.getErrores());
        }
        return "redirect:" + destino;
    }
}
