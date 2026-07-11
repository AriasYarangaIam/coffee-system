package com.coffee.backend.dto.response;

import java.math.BigDecimal;

// Producto disponible para el POS (con precio y disponibilidad).
public record ProductosDisponiblesResponseDTO(
      Long productoId,
      String nombreProducto,
      BigDecimal precio,
      String categoria,
      Boolean disponible
) {
}
