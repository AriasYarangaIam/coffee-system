package com.coffee.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Ingreso de stock (ADMIN): suma 'cantidad' al insumo en el almacén indicado.
public record StockRequestDTO(
        @NotNull Long insumoId,
        @NotNull Long almacenId,
        @NotNull @Positive Long cantidad
) {
}
