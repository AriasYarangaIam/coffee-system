package com.coffee.backend.dto.response;

import java.time.LocalDateTime;

// Un ingreso de stock para el panel "Últimos Ingresos" (ADMIN).
public record MovimientoStockResponseDTO(
        Long id,
        Long insumoId,
        String nombreInsumo,
        String unidad,
        Long almacenId,
        String nombreAlmacen,
        Long cantidad,
        LocalDateTime fecha,
        String registradoPor,
        String estado
) {
}
