package com.tomatoes.empleados.Dto;

public record LoginResponse(String token, String tipo, String id, String nombre, String correo, String rol, long expiraEnSegundos) {}
