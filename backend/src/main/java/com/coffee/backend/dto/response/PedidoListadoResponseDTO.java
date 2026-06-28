package com.coffee.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

// Listado de pedidos del mesero logueado (GET /api/pedidos). Incluye el total (B-08)
// y el detalle de líneas (producto x cantidad) para mostrarlo y poder filtrarlo.
public record PedidoListadoResponseDTO(
        Long pedidoId,
        String aliasTicket,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime fechaPedido,
        double total,
        List<DetalleBoletaResponseDTO> detalle)
{
}
