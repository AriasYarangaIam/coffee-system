package com.coffee.backend.dto.response;

// Respuesta de gestión de insumos (ADMIN).
// unidadEditable = false cuando el insumo ya tiene stock o se usa en alguna receta:
// cambiar la unidad ahí dejaría inconsistentes las cantidades existentes.
public record InsumoResponseDTO(
        Long idInsumo,
        String nombreInsumo,
        String unidad,
        boolean unidadEditable
) {
}
