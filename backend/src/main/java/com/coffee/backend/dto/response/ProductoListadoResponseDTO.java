package com.coffee.backend.dto.response;

public record ProductoListadoResponseDTO(Long productoId,
                                         String nombreProducto,
                                         String categoria,
                                         Double precio,
                                         Boolean disponible) {
}
