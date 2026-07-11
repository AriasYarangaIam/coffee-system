package com.coffee.backend.dto.response;

import java.math.BigDecimal;

// Métricas del turno (día en curso) del mesero autenticado.
public record MisMetricasResponseDTO(
        long pedidosAtendidos,
        BigDecimal totalVendido,
        BigDecimal ticketPromedio,
        String productoEstrella) {
}
