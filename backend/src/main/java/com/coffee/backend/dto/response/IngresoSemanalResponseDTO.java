package com.coffee.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Serie de ingresos por semana del rango consultado, con la tendencia semana a semana. */
public record IngresoSemanalResponseDTO(List<SemanaIngreso> semanas) {

    /** {@code variacionPct} es null en la primera semana (no hay previa con qué comparar). */
    public record SemanaIngreso(
            LocalDate inicioSemana,
            BigDecimal total,
            BigDecimal variacionPct) {
    }
}
