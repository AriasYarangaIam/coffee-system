package com.coffee.backend.service;

import com.coffee.backend.dto.request.PedidoRequestDTO;
import com.coffee.backend.dto.response.BoletaResponseDTO;
import com.coffee.backend.dto.response.PedidoListadoResponseDTO;
import com.coffee.backend.dto.response.PedidoResponseDTO;

import java.util.List;

public interface PedidoService {
    PedidoResponseDTO registrarPedido(PedidoRequestDTO dto, String correoUsuarioLogueado);
    BoletaResponseDTO obtenerBoleta(Long pedidoId);
    List<PedidoListadoResponseDTO> listarPedidosDeMesero(String correoUsuarioLogueado);
}
