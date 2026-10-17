package com.tomatoes.web.Seguridad;

import java.io.Serializable;

/** Usuario autenticado: se guarda en la sesión HTTP junto con su token JWT. */
public record UsuarioSesion(String id, String correo, String nombre, String rol, String token) implements Serializable {}
