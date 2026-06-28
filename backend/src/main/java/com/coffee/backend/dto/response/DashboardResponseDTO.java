package com.coffee.backend.dto.response;

import java.util.List;

/**
 * KPIs del panel admin (B-05). Contrato consumido por {@code frontend/js/pages/dashboard.js}:
 * ventas y pedidos del día, producto más vendido del mes y los insumos con stock bajo.
 */
public record DashboardResponseDTO(
        double totalVentasDia,
        long totalPedidosDia,
        String productoEstrella,
        List<StockBajoDTO> stockBajo) {
}
