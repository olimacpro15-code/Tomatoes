package com.tomatoes.cosechas.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LoteRequest(
        @NotBlank(message = "El nombre del lote es obligatorio") String nombre,
        @NotNull(message = "El área es obligatoria") @Positive(message = "El área debe ser mayor que 0") Double areaHectareas,
        @NotBlank(message = "La ubicación es obligatoria") String ubicacion,
        @NotBlank(message = "El cultivo es obligatorio") String cultivo) {}
