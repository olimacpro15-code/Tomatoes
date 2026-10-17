package com.tomatoes.web.Excepciones;

/** Un microservicio no responde (RNF-10): se muestra un mensaje controlado en lugar de una pantalla de error. */
public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String mensaje) { super(mensaje); }
}
