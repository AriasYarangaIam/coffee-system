package com.coffee.backend.service;

import com.coffee.backend.dto.response.ProductoListadoResponseDTO;
import com.coffee.backend.dto.response.ProductoRecetaResponseDTO;

import java.util.List;

public interface ProductoService {
    ProductoRecetaResponseDTO obtenerRecetaDeProducto(Long productoId);
    List<ProductoListadoResponseDTO> listarProductos();
}
