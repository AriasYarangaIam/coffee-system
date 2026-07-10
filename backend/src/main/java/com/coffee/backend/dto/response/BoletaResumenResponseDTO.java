package com.coffee.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Una boleta emitida para el detalle transaccional de ingresos (ADMIN).
public record BoletaResumenResponseDTO(
        Long pedidoId,
        String aliasTicket,
        LocalDateTime fecha,
        String mesero,
        BigDecimal total) {
}
