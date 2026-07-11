package com.coffee.backend.dto.response;

import java.util.List;

// Receta completa de un producto (sus insumos y cantidades).
public record ProductoRecetaResponseDTO(
        Long productoId,
        String nombreProducto,
        List<InsumoRecetaDTO> insumos
) {
}
