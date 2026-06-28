package com.coffee.backend.service;

import com.coffee.backend.dto.response.PedidoDespachoTokenView;

import java.util.List;
import java.util.Optional;

/**
 * Cola lógica de despacho de pedidos (RF-DS-04). Mantiene una cola residente en memoria
 * (orden de atención FIFO); no gestiona estados de cocina.
 */
public interface PedidoDespachoService {

    /** Encola un pedido al final de la cola de despacho (FIFO). */
    void enqueueDespacho(PedidoDespachoTokenView token);

    /** Snapshot de la cola en orden FIFO, sin drenarla. */
    List<PedidoDespachoTokenView> listarDespacho();

    /** Cabeza de la cola (próximo a atender) sin removerla; {@code empty} si vacía. */
    Optional<PedidoDespachoTokenView> siguienteDespacho();
}
