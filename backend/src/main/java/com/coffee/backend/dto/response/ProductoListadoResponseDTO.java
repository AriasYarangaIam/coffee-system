package com.coffee.backend.dto.response;

import java.math.BigDecimal;

// Producto del catalogo para el POS (id, nombre, categoria, precio, disponible).
public record ProductoListadoResponseDTO(Long productoId,
                                         String nombreProducto,
                                         String categoria,
                                         BigDecimal precio,
                                         Boolean disponible) {
}
