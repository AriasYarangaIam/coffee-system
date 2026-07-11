package com.coffee.backend.dto.response;

import java.math.BigDecimal;

public record ProductoListadoResponseDTO(Long productoId,
                                         String nombreProducto,
                                         String categoria,
                                         BigDecimal precio,
                                         Boolean disponible) {
}
