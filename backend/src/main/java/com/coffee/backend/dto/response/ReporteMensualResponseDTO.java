package com.coffee.backend.dto.response;

import java.util.List;

/**
 * Matriz producto × día del mes (RF-DS-02). {@code celdas[i][j]} = ventas del producto
 * {@code productos.get(i)} en el día {@code dias.get(j)}. Consumido por
 * {@code frontend/js/pages/dashboard.js} (gráfico de barras apiladas).
 */
public record ReporteMensualResponseDTO(
        List<String> productos,
        List<Integer> dias,
        double[][] celdas) {
}
