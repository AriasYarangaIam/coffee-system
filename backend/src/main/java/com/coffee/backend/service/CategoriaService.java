package com.coffee.backend.service;

import com.coffee.backend.dto.response.CategoriaResponseDTO;

import java.util.List;

// Listado de categorías para poblar selects del front (ADMIN).
public interface CategoriaService {
    List<CategoriaResponseDTO> listar();
}
