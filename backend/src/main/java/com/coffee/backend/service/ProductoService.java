package com.coffee.backend.service;

import com.coffee.backend.dto.response.ProductoRecetaResponseDTO;

public interface ProductoService {
    ProductoRecetaResponseDTO obtenerRecetaDeProducto(Long productoId);
}
