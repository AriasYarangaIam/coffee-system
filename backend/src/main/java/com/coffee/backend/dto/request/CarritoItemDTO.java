package com.coffee.backend.dto.request;

import java.math.BigDecimal;

// Una línea del carrito del mesero, tal como viaja en los snapshots de deshacer (RF-DS-03).
public record CarritoItemDTO(
        Long productoId,
        String nombre,
        BigDecimal precio,
        Long cantidad
) {
}
