package com.coffee.backend.dto.response;

// Almacén para poblar selects del front (ADMIN). almacenId = codigo_almacen.
public record AlmacenResponseDTO(Long almacenId, String nombreAlmacen) {
}
