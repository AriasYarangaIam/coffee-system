package com.coffee.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

// Alta/edición de producto (ADMIN). La categoría se referencia por id.
// `receta` puede venir vacía/null → el producto no consume stock al venderse.
public record ProductoRequestDTO(
        @NotBlank String nombreProducto,
        @NotNull @PositiveOrZero Double precioActual,
        @NotNull Long categoriaId,
        @Valid List<RecetaItemRequestDTO> receta
) {
}
