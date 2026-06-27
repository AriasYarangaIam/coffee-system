package com.coffee.backend.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

// Contrato fijado (B-14): el request es SOLO la lista de detalles.
// El usuario se deriva del JWT y el aliasTicket lo genera el backend.
public record PedidoRequestDTO(
        @NotNull @Size(min = 1) @Valid List<DetallePedidoRequestDTO> detalles)
{
}
