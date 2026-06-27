package com.coffee.backend.service;

import com.coffee.backend.dto.request.InsumoRequestDTO;
import com.coffee.backend.dto.response.InsumoResponseDTO;

import java.util.List;

// CRUD de insumos para el panel de administración (ADMIN).
public interface InsumoService {
    List<InsumoResponseDTO> listar();
    InsumoResponseDTO crear(InsumoRequestDTO dto);
    InsumoResponseDTO actualizar(Long id, InsumoRequestDTO dto);
    void eliminar(Long id);
}
