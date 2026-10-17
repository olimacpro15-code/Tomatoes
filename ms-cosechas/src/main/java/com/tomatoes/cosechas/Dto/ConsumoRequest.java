package com.tomatoes.cosechas.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ConsumoRequest(
        @NotBlank(message = "Debe seleccionar un insumo") String insumoId,
        @NotNull(message = "La cantidad es obligatoria") @Positive(message = "La cantidad debe ser mayor que 0") Double cantidad) {}
