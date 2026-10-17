package com.tomatoes.cosechas.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CosechaRequest(
        @NotBlank(message = "Debe seleccionar un lote") String loteId,
        @NotNull(message = "La fecha de inicio es obligatoria") LocalDate fechaInicio,
        LocalDate fechaFin) {}
