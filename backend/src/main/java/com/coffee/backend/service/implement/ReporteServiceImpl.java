package com.coffee.backend.service.implement;

import com.coffee.backend.dto.response.BoletaResumenResponseDTO;
import com.coffee.backend.dto.response.ComparativaMensualResponseDTO;
import com.coffee.backend.dto.response.IngresoSemanalResponseDTO;
import com.coffee.backend.dto.response.IngresoSemanalResponseDTO.SemanaIngreso;
import com.coffee.backend.dto.response.ReporteMensualResponseDTO;
import com.coffee.backend.repository.PedidoRepository;
import com.coffee.backend.repository.PedidoRepository.BoletaResumen;
import com.coffee.backend.repository.PedidoRepository.IngresoSemana;
import com.coffee.backend.repository.PedidoRepository.VentaProductoDia;
import com.coffee.backend.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
                // ponytail: la matriz del reporte se queda en double (display derivado, no
                // dinero persistido); convertir arrays de BigDecimal sería ruido.
                celdas[i][j] = fila.getTotal().doubleValue();
            }
        }

        return new ReporteMensualResponseDTO(productos, dias, celdas);
    }

    @Override
    @Transactional(readOnly = true)
    public ComparativaMensualResponseDTO comparativa(Integer anio, Integer mes) {
        LocalDate hoy = LocalDate.now();
        int a = (anio == null) ? hoy.getYear() : anio;
        int m = (mes == null) ? hoy.getMonthValue() : mes;

        LocalDateTime inicioMes = LocalDate.of(a, m, 1).atStartOfDay();
        LocalDateTime finMes = inicioMes.plusMonths(1);
        LocalDateTime inicioPrev = inicioMes.minusMonths(1);

        BigDecimal actual = pedidoRepository.sumarVentasEntre(inicioMes, finMes);
        BigDecimal previo = pedidoRepository.sumarVentasEntre(inicioPrev, inicioMes);

        return new ComparativaMensualResponseDTO(
                a, m, dinero(actual), dinero(previo), variacionPct(actual, previo));
    }

    @Override
    @Transactional(readOnly = true)
    public IngresoSemanalResponseDTO ingresosSemanales(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay(); // rango inclusivo en 'hasta'

        List<IngresoSemana> filas = pedidoRepository.ingresosPorSemana(inicio, fin);

        List<SemanaIngreso> semanas = new ArrayList<>(filas.size());
        BigDecimal totalPrevio = null;
        for (IngresoSemana fila : filas) {
            BigDecimal total = fila.getTotal();
            BigDecimal variacion = (totalPrevio == null) ? null : variacionPct(total, totalPrevio);
            semanas.add(new SemanaIngreso(fila.getSemana(), dinero(total), variacion));
            totalPrevio = total;
        }
        return new IngresoSemanalResponseDTO(semanas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoletaResumenResponseDTO> boletas(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay();
        return pedidoRepository.boletasEntre(inicio, fin).stream()
                .map(b -> new BoletaResumenResponseDTO(
                        b.getPedido(), b.getAlias(), b.getFecha(), b.getMesero(), dinero(b.getTotal())))
                .toList();
    }

    // Normaliza a 2 decimales para la respuesta.
    private static BigDecimal dinero(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }

    // Variación porcentual (actual vs previo). null si el previo fue 0 (no hay base).
    private static BigDecimal variacionPct(BigDecimal actual, BigDecimal previo) {
        if (previo == null || previo.signum() == 0) return null;
        return actual.subtract(previo)
                .divide(previo, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
