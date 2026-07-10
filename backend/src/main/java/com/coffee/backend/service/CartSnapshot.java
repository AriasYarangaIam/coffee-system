package com.coffee.backend.service;

import java.util.Collections;
import java.util.List;

/**
 * Instantánea inmutable del carrito de pedidos en progreso.
 */
public record CartSnapshot(
        List<CartItemSnapshot> items,
        Double total
) {
    public CartSnapshot {
        items = items == null ? List.of() : List.copyOf(items);
        total = total == null ? 0.0 : total;
    }

    @Override
    public List<CartItemSnapshot> items() {
        return Collections.unmodifiableList(items);
    }
}
