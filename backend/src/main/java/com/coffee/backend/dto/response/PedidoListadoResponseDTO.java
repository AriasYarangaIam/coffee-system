package com.coffee.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

// Listado de pedidos del mesero logueado (GET /api/pedidos). Incluye el total (B-08).
public record PedidoListadoResponseDTO(
        Long pedidoId,
        String aliasTicket,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime fechaPedido,
        double total)
{
}
