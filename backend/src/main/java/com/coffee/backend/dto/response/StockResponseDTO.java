package com.coffee.backend.dto.response;

// Estado de stock por insumo/almacén (ADMIN).
public record StockResponseDTO(
        Long codigoStock,
        Long insumoId,
        String nombreInsumo,
        Long almacenId,
        String nombreAlmacen,
        Long cantidad
) {
}
