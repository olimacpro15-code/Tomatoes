package com.tomatoes.web.Excepciones;

import java.util.Map;

/** Respuesta de error (4xx) de un microservicio: lleva el mensaje y los errores por campo. */
public class ApiException extends RuntimeException {

    private final int estado;
    private final Map<String, String> errores;

    public ApiException(int estado, String mensaje, Map<String, String> errores) {
        super(mensaje);
        this.estado = estado;
        this.errores = errores;
    }

    public int getEstado() { return estado; }
    public Map<String, String> getErrores() { return errores; }
}
