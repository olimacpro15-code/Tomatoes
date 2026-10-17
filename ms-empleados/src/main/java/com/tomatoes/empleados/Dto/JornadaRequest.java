package com.tomatoes.empleados.Dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record JornadaRequest(
        @NotNull(message = "La fecha es obligatoria") LocalDate fecha,
        @NotNull(message = "Las horas son obligatorias")
        @DecimalMin(value = "0.1", message = "Las horas deben ser mayores que 0")
        @DecimalMax(value = "12", message = "La jornada no puede superar las 12 horas") Double horas,
        @NotBlank(message = "La labor es obligatoria") String labor,
        String cosechaId,
        @Size(max = 300, message = "Las observaciones admiten máximo 300 caracteres") String observaciones) {}
