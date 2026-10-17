package com.tomatoes.insumos.Dto;

import com.tomatoes.insumos.Entidades.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** stockActual solo se usa al crear (stock inicial); luego el stock cambia únicamente con movimientos. */
public record InsumoRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotNull(message = "La categoría es obligatoria") Categoria categoria,
        @NotBlank(message = "La unidad de medida es obligatoria") String unidadMedida,
        @PositiveOrZero(message = "El stock actual no puede ser negativo") Double stockActual,
        @NotNull(message = "El stock mínimo es obligatorio") @PositiveOrZero(message = "El stock mínimo no puede ser negativo") Double stockMinimo,
        @NotNull(message = "El costo unitario es obligatorio") @PositiveOrZero(message = "El costo unitario no puede ser negativo") Double costoUnitario,
        String proveedorId) {}
