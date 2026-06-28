package com.coffee.backend.dto.response;

/**
 * Insumo con stock por debajo del umbral de alerta del dashboard.
 *
 * <p>{@code unidad} es {@code null} hoy: la entidad {@code Insumos} no modela una unidad
 * de medida. El front lo trata como opcional ({@code i.unidad ?? ''}).
 */
public record StockBajoDTO(String nombreInsumo, Long cantidad, String unidad) {
}
