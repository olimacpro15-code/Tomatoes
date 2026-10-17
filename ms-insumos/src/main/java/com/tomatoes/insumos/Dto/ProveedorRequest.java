package com.tomatoes.insumos.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProveedorRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "El NIT es obligatorio") String nit,
        String telefono,
        @Email(message = "El correo no tiene un formato válido") String correo) {}
