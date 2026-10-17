package com.tomatoes.empleados.Dto;

import com.tomatoes.empleados.Entidades.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Datos para crear o editar un empleado. La contraseña es obligatoria al crear y opcional al editar. */
public record EmpleadoRequest(
        @NotBlank(message = "Los nombres son obligatorios") @Size(max = 80) String nombres,
        @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 80) String apellidos,
        @NotBlank(message = "El documento es obligatorio") @Size(min = 5, max = 20, message = "El documento debe tener entre 5 y 20 caracteres") String documento,
        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no tiene un formato válido") String correo,
        @Size(min = 8, max = 60, message = "La contraseña debe tener entre 8 y 60 caracteres") String password,
        @NotBlank(message = "El cargo es obligatorio") String cargo,
        @NotNull(message = "Debe asignar un rol") Rol rol,
        LocalDate fechaIngreso) {}
