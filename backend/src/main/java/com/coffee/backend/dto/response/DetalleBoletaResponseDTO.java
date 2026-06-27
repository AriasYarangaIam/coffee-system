package com.coffee.backend.dto.response;

public record DetalleBoletaResponseDTO(
        String nombreProducto,
        Long cantidadPedida,
        Double precioUnitario
) {
}
