package com.tomatoes.empleados.Dto;

import com.tomatoes.empleados.Entidades.Rol;
import jakarta.validation.constraints.NotNull;

public record CambioRolRequest(@NotNull(message = "Debe indicar el rol") Rol rol) {}
