package com.coffee.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record PedidoResponseDTO(
        Long pedidoId,
        String aliasTicket,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime  fechaPedido)
{

}
