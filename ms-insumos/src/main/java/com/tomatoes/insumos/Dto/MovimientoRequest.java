package com.tomatoes.insumos.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MovimientoRequest(
        @NotBlank(message = "Debe seleccionar un insumo") String insumoId,
        @NotNull(message = "La cantidad es obligatoria") @Positive(message = "La cantidad debe ser mayor que 0") Double cantidad,
        String cosechaId,
        @Size(max = 300, message = "La observación admite máximo 300 caracteres") String observacion) {}
