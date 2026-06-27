package com.coffee.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

// Alta/edición de producto (ADMIN). La categoría se referencia por id.
public record ProductoRequestDTO(
        @NotBlank String nombreProducto,
        @NotNull @PositiveOrZero Double precioActual,
        @NotNull Long categoriaId
) {
}
