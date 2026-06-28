package com.coffee.backend.dto.response;

import java.time.LocalDateTime;

/**
 * Proyección mínima de un pedido en la cola de despacho (RF-DS-04). Desacoplada del
 * listado de pedidos; solo lo necesario para mostrar el orden de atención.
 */
public record PedidoDespachoTokenView(
        Long pedidoId,
        String aliasTicket,
        LocalDateTime fechaPedido) {
}
