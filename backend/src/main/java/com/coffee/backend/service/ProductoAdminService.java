package com.coffee.backend.service;

import com.coffee.backend.dto.request.ProductoRequestDTO;
import com.coffee.backend.dto.response.ProductoAdminResponseDTO;

import java.util.List;

// CRUD de productos para el panel de administración (ADMIN).
public interface ProductoAdminService {
    List<ProductoAdminResponseDTO> listar();
    ProductoAdminResponseDTO crear(ProductoRequestDTO dto);
    ProductoAdminResponseDTO actualizar(Long id, ProductoRequestDTO dto);
    void eliminar(Long id);
}
