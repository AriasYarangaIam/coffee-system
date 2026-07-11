package com.coffee.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Request: una linea del pedido (producto + cantidad) que envia el mesero.
public record DetallePedidoRequestDTO(
        @NotNull Long productoId,
        @NotNull @Positive Long cantidadPedida)
{

}
