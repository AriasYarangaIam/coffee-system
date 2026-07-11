package com.coffee.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

// Alta/edición de insumo (ADMIN). `unidad` = medida en que se cuenta (ml, g, kg, L, unidad).
public record InsumoRequestDTO(
        @NotBlank String nombreInsumo,
        @NotBlank String unidad
) {
}
