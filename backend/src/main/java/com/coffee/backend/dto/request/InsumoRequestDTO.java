package com.coffee.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

// Alta/edición de insumo (ADMIN).
public record InsumoRequestDTO(
        @NotBlank String nombreInsumo
) {
}
