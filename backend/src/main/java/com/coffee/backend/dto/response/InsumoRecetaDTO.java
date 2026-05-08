package com.coffee.backend.dto.response;

public record InsumoRecetaDTO(
        Long idInsumo,
        String nombreInsumo,
        Long cantidadUsada
) {
}
