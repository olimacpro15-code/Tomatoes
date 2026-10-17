package com.tomatoes.cosechas.Dto;

import com.tomatoes.cosechas.Entidades.Calidad;
import com.tomatoes.cosechas.Entidades.EstadoCosecha;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/** Cambia el estado y, opcionalmente, registra la producción (kg) y la calidad obtenida. */
public record EstadoCosechaRequest(
        @NotNull(message = "Debe indicar el estado") EstadoCosecha estado,
        @Positive(message = "La cantidad cosechada debe ser mayor que 0") Double cantidadKg,
        Calidad calidad,
        LocalDate fechaFin) {}
