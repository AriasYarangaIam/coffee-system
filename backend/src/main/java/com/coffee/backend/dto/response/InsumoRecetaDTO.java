package com.coffee.backend.dto.response;

// Un insumo dentro de la receta de un producto (id, nombre, cantidad).
public record InsumoRecetaDTO(
        Long idInsumo,
        String nombreInsumo,
        Long cantidadUsada
) {
}
