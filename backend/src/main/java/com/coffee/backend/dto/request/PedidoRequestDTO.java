package com.coffee.backend.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PedidoRequestDTO(
        @NotNull Long usuarioId,
        @NotNull String aliasTicket,
        @NotNull @Size(min = 1) @Valid List<DetallePedidoRequestDTO> detalles)
{

}
