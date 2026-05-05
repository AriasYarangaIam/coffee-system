package com.coffee.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DetallePedidoRequestDTO(
        @NotNull Long productoId,
        @NotNull @Positive Long cantidadPedida)
{

}
