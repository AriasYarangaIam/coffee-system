package com.coffee.backend.service;

import com.coffee.backend.dto.response.ProductoListadoResponseDTO;
import com.coffee.backend.dto.response.ProductoRecetaResponseDTO;

import java.util.List;

// Contrato de consulta de productos y su receta.
public interface ProductoService {
    ProductoRecetaResponseDTO obtenerRecetaDeProducto(Long productoId);
    List<ProductoListadoResponseDTO> listarProductos();
}
