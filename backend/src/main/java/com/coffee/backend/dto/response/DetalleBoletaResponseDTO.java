package com.coffee.backend.dto.response;

import java.math.BigDecimal;

// Una linea de la boleta (producto, cantidad, precio unitario).
public record DetalleBoletaResponseDTO(
        String nombreProducto,
        Long cantidadPedida,
        BigDecimal precioUnitario
) {
}
