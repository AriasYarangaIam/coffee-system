package com.coffee.backend.service;

import com.coffee.backend.dto.request.PedidoRequestDTO;
import com.coffee.backend.dto.response.BoletaResponseDTO;
import com.coffee.backend.dto.response.MisMetricasResponseDTO;
import com.coffee.backend.dto.response.PedidoDespachoTokenView;
import com.coffee.backend.dto.response.PedidoListadoResponseDTO;
import com.coffee.backend.dto.response.PedidoResponseDTO;

import java.util.List;

public interface PedidoService {
    PedidoResponseDTO registrarPedido(PedidoRequestDTO dto, String correoUsuarioLogueado);
    BoletaResponseDTO obtenerBoleta(Long pedidoId);
    List<PedidoListadoResponseDTO> listarPedidosDeMesero(String correoUsuarioLogueado);

    // Métricas del turno (día en curso) del mesero autenticado.
    MisMetricasResponseDTO misMetricas(String correoUsuarioLogueado);

    // Entrega (dequeue) la cabeza de la cola de despacho si es del mesero; devuelve la
    // nueva cabeza o null si la cola quedó vacía (RF-DS-04).
    PedidoDespachoTokenView entregarSiguienteDespacho(String correoUsuarioLogueado);
}
