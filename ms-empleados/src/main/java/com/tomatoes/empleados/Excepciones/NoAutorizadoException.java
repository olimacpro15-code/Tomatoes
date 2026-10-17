package com.tomatoes.empleados.Excepciones;

/** Credenciales o token inválidos (HTTP 401). */
public class NoAutorizadoException extends RuntimeException {
    public NoAutorizadoException(String mensaje) { super(mensaje); }
}
