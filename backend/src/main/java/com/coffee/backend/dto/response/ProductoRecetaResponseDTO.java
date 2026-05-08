package com.coffee.backend.dto.response;

import java.util.List;

public record ProductoRecetaResponseDTO(
        Long productoId,
        String nombreProducto,
        List<InsumoRecetaDTO> insumos
) {
}
