package com.coffee.backend.service.implement;

import com.coffee.backend.dto.response.ReporteMensualResponseDTO;
import com.coffee.backend.repository.PedidoRepository;
import com.coffee.backend.repository.PedidoRepository.VentaProductoDia;
import com.coffee.backend.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reporte mensual como matriz 2D producto × día (RF-DS-02, U1 — arreglos 2D). El
 * artefacto que califica es el {@code double[][]} construido explícitamente aquí.
 */
@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final PedidoRepository pedidoRepository;

    @Override
    @Transactional(readOnly = true)
    public ReporteMensualResponseDTO reporteMensual() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioMes = hoy.withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes = inicioMes.plusMonths(1);

        List<VentaProductoDia> filas = pedidoRepository.ventasPorProductoYDia(inicioMes, finMes);

        // Columnas = días 1..N del mes; índice de columna = día - 1.
        int diasDelMes = hoy.lengthOfMonth();
        List<Integer> dias = new ArrayList<>(diasDelMes);
        for (int d = 1; d <= diasDelMes; d++) dias.add(d);

        // Filas = productos distintos con venta este mes, en orden de aparición.
        Map<String, Integer> indicePorProducto = new LinkedHashMap<>();
        for (VentaProductoDia fila : filas) {
            indicePorProducto.computeIfAbsent(fila.getProducto(), k -> indicePorProducto.size());
        }
        List<String> productos = new ArrayList<>(indicePorProducto.keySet());

        // Matriz 2D producto × día, rellenada desde las filas (resto queda en 0.0).
        double[][] celdas = new double[productos.size()][diasDelMes];
        for (VentaProductoDia fila : filas) {
            int i = indicePorProducto.get(fila.getProducto());
            int j = fila.getDia() - 1; // día 1 → columna 0
            if (j >= 0 && j < diasDelMes) {
                celdas[i][j] = fila.getTotal();
            }
        }

        return new ReporteMensualResponseDTO(productos, dias, celdas);
    }
}
