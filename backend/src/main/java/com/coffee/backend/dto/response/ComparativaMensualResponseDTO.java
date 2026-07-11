package com.coffee.backend.dto.response;

import java.math.BigDecimal;

/**
 * Comparativa de ingresos de un mes contra el anterior. {@code variacionPorcentual} es
 * null cuando el mes previo no tuvo ventas (no se puede dividir entre cero).
 */
public record ComparativaMensualResponseDTO(
        int anio,
        int mes,
        BigDecimal totalMesActual,
        BigDecimal totalMesAnterior,
        BigDecimal variacionPorcentual) {
}
