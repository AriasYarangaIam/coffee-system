package com.coffee.backend.service;

import com.coffee.backend.dto.response.AlmacenResponseDTO;

import java.util.List;

// Listado de almacenes para poblar selects del front (ADMIN).
public interface AlmacenService {
    List<AlmacenResponseDTO> listar();
}
