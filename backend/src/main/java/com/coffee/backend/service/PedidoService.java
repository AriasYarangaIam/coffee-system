package com.coffee.backend.service;

import com.coffee.backend.dto.request.PedidoRequestDTO;
import com.coffee.backend.dto.response.BoletaResponseDTO;
import com.coffee.backend.dto.response.PedidoResponseDTO;

public interface PedidoService {
    PedidoResponseDTO registrarPedido(PedidoRequestDTO dto, String correoUsuarioLogueado);
    BoletaResponseDTO obtenerBoleta(Long pedidoId);
}