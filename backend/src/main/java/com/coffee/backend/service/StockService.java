package com.coffee.backend.service;

import com.coffee.backend.dto.request.StockRequestDTO;
import com.coffee.backend.dto.response.StockResponseDTO;

import java.util.List;

// Gestión de stock para administración (ADMIN): listado e ingreso de insumos.
public interface StockService {
    List<StockResponseDTO> listar();
    StockResponseDTO registrarIngreso(StockRequestDTO dto);
}
