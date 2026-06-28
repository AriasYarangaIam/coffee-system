package com.coffee.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Una línea de receta: qué insumo y cuánto usa el producto (ADMIN).
public record RecetaItemRequestDTO(
        @NotNull Long insumoId,
        @NotNull @Positive Long cantidadUsada
) {
}
