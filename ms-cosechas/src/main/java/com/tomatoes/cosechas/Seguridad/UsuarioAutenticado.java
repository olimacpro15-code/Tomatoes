package com.tomatoes.cosechas.Seguridad;

/** Datos del usuario extraídos del token JWT (el token se conserva para reenviarlo a otros servicios). */
public record UsuarioAutenticado(String id, String correo, String nombre, String rol, String token) {}
