package com.coffee.backend.service;

/**
 * Ítem inmutable dentro de una instantánea del carrito de pedidos.
 */
public record CartItemSnapshot(
        Long productoId,
        String nombre,
        Double precio,
        Long cantidad
) {
}
