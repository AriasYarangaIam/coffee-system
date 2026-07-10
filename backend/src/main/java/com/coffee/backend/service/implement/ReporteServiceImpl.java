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
                celdas[i][j] = fila.getTotal();
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

        double actual = pedidoRepository.sumarVentasEntre(inicioMes, finMes);
        double previo = pedidoRepository.sumarVentasEntre(inicioPrev, inicioMes);

        // Sin ventas el mes previo no hay base para el porcentaje.
        BigDecimal variacion = (previo == 0)
                ? null
                : dinero((actual - previo) / previo * 100.0);

        return new ComparativaMensualResponseDTO(a, m, dinero(actual), dinero(previo), variacion);
    }

    @Override
    @Transactional(readOnly = true)
    public IngresoSemanalResponseDTO ingresosSemanales(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay(); // rango inclusivo en 'hasta'

        List<IngresoSemana> filas = pedidoRepository.ingresosPorSemana(inicio, fin);

        List<SemanaIngreso> semanas = new ArrayList<>(filas.size());
        Double totalPrevio = null;
        for (IngresoSemana fila : filas) {
            double total = fila.getTotal();
            BigDecimal variacion = (totalPrevio == null || totalPrevio == 0)
                    ? null
                    : dinero((total - totalPrevio) / totalPrevio * 100.0);
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

    // Envuelve el double acumulado en la consulta a 2 decimales. Corrige la presentación,
    // no la acumulación (los @Query siguen sumando en double); suficiente para esta app.
    private static BigDecimal dinero(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }
}
