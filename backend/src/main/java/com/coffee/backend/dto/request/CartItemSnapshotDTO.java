package com.coffee.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Ítem de una instantánea del carrito de pedidos.
 */
public record CartItemSnapshotDTO(
        @NotNull Long productoId,
        @NotNull String nombre,
        @NotNull @Positive Double precio,
        @NotNull @Positive Long cantidad
) {
}
