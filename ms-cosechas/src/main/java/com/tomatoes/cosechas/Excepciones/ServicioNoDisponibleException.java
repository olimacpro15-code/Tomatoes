package com.tomatoes.cosechas.Excepciones;

/** Otro microservicio no responde (HTTP 503). */
public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String mensaje) { super(mensaje); }
}
