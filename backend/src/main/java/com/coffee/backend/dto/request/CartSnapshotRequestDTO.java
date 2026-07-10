package com.coffee.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Payload para guardar una instantánea del carrito de pedidos en la pila de deshacer.
 */
public record CartSnapshotRequestDTO(
        @NotNull @Valid List<CartItemSnapshotDTO> items,
        @NotNull Double total
) {
}
