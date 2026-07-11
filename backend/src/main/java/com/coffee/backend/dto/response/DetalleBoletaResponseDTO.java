package com.coffee.backend.dto.response;

import java.math.BigDecimal;

public record DetalleBoletaResponseDTO(
        String nombreProducto,
        Long cantidadPedida,
        BigDecimal precioUnitario
) {
}
