package com.coffee.backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

// Respuesta de gestión de productos (ADMIN): id/nombre de categoría + la receta
// (insumos y cantidades) para que el modal de edición la precargue.
public record ProductoAdminResponseDTO(
        Long productoId,
        String nombreProducto,
        BigDecimal precioActual,
        Long categoriaId,
        String nombreCategoria,
        List<InsumoRecetaDTO> receta
) {
}
