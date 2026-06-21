package com.coffee.backend.dto.response;

public record ProductosDisponiblesResponseDTO(
      Long productoId,
      String nombreProducto,
      Double precio,
      String categoria,
      Boolean disponible
) {
}
