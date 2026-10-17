package com.tomatoes.web.Excepciones;

/** El token JWT expiró o ya no es válido: se pide iniciar sesión nuevamente. */
public class SesionExpiradaException extends RuntimeException {
    public SesionExpiradaException() { super("La sesión expiró."); }
}
