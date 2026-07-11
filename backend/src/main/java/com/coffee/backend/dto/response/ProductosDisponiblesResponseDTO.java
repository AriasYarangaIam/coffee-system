package com.coffee.backend.dto.response;

import java.math.BigDecimal;

public record ProductosDisponiblesResponseDTO(
      Long productoId,
      String nombreProducto,
      BigDecimal precio,
      String categoria,
      Boolean disponible
) {
}
