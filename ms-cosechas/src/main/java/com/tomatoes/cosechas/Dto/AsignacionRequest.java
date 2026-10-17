package com.tomatoes.cosechas.Dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AsignacionRequest(@NotEmpty(message = "Seleccione al menos un empleado") List<String> empleadosIds) {}
