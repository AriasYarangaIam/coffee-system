package com.coffee.backend.service;

import com.coffee.backend.dto.response.ComparativaMensualResponseDTO;
import com.coffee.backend.dto.response.IngresoSemanalResponseDTO;
import com.coffee.backend.dto.response.ReporteMensualResponseDTO;
import com.coffee.backend.repository.PedidoRepository;
import com.coffee.backend.repository.PedidoRepository.IngresoSemana;
import com.coffee.backend.repository.PedidoRepository.VentaProductoDia;
import com.coffee.backend.service.implement.ReporteServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Unit test de la construcción de la matriz 2D producto × día (RF-DS-02). Repo mockeado,
 * sin Spring ni BD.
 */
class ReporteServiceImplTest {

    private record Celda(String producto, int dia, BigDecimal total) implements VentaProductoDia {
        public String getProducto() { return producto; }
        public int getDia() { return dia; }
        public BigDecimal getTotal() { return total; }
    }

    private static BigDecimal bd(String v) { return new BigDecimal(v); }

    @Test
    void armaMatrizProductoPorDia_conCerosDondeNoHuboVenta() {
        PedidoRepository repo = mock(PedidoRepository.class);
        given(repo.ventasPorProductoYDia(any(), any())).willReturn(List.<VentaProductoDia>of(
                new Celda("Capuchino", 1, bd("30.00")),
                new Celda("Capuchino", 3, bd("12.50")),
                new Celda("Latte", 1, bd("20.00"))));

        ReporteMensualResponseDTO r = new ReporteServiceImpl(repo).reporteMensual();

        int diasDelMes = LocalDate.now().lengthOfMonth();
        assertThat(r.productos()).containsExactly("Capuchino", "Latte");
        assertThat(r.dias()).hasSize(diasDelMes).startsWith(1).endsWith(diasDelMes);
        assertThat(r.celdas()).hasNumberOfRows(2);

        // Capuchino (fila 0): día 1 = 30.0, día 3 = 12.5, día 2 = 0.0
        assertThat(r.celdas()[0][0]).isEqualTo(30.0);
        assertThat(r.celdas()[0][1]).isZero();
        assertThat(r.celdas()[0][2]).isEqualTo(12.5);
        // Latte (fila 1): día 1 = 20.0
        assertThat(r.celdas()[1][0]).isEqualTo(20.0);
    }

    @Test
    void mesSinVentas_matrizVaciaDeProductos_peroConColumnasDeDias() {
        PedidoRepository repo = mock(PedidoRepository.class);
        given(repo.ventasPorProductoYDia(any(), any())).willReturn(List.of());

        ReporteMensualResponseDTO r = new ReporteServiceImpl(repo).reporteMensual();

        assertThat(r.productos()).isEmpty();
        assertThat(r.celdas()).isEmpty();
        assertThat(r.dias()).hasSize(LocalDate.now().lengthOfMonth());
    }

    // --- Módulo de ingresos ---

    @Test
    void comparativa_calculaVariacionPorcentual() {
        PedidoRepository repo = mock(PedidoRepository.class);
        // Primera llamada = mes actual (120), segunda = mes previo (100).
        given(repo.sumarVentasEntre(any(), any())).willReturn(bd("120.00"), bd("100.00"));

        ComparativaMensualResponseDTO r = new ReporteServiceImpl(repo).comparativa(2026, 7);

        assertThat(r.totalMesActual()).isEqualByComparingTo("120.00");
        assertThat(r.totalMesAnterior()).isEqualByComparingTo("100.00");
        assertThat(r.variacionPorcentual()).isEqualByComparingTo("20.00"); // +20%
    }

    @Test
    void comparativa_mesPrevioSinVentas_variacionNull() {
        PedidoRepository repo = mock(PedidoRepository.class);
        given(repo.sumarVentasEntre(any(), any())).willReturn(bd("80.00"), bd("0.00"));

        ComparativaMensualResponseDTO r = new ReporteServiceImpl(repo).comparativa(2026, 7);

        assertThat(r.variacionPorcentual()).isNull(); // sin división por cero
    }

    @Test
    void ingresosSemanales_calculaTendenciaEntreSemanas() {
        PedidoRepository repo = mock(PedidoRepository.class);
        given(repo.ingresosPorSemana(any(), any())).willReturn(List.<IngresoSemana>of(
                semana(LocalDate.of(2026, 7, 6), bd("100.00")),
                semana(LocalDate.of(2026, 7, 13), bd("150.00"))));

        IngresoSemanalResponseDTO r = new ReporteServiceImpl(repo)
                .ingresosSemanales(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        assertThat(r.semanas()).hasSize(2);
        assertThat(r.semanas().get(0).variacionPct()).isNull();          // sin previa
        assertThat(r.semanas().get(1).variacionPct()).isEqualByComparingTo("50.00"); // +50%
    }

    private static IngresoSemana semana(LocalDate inicio, BigDecimal total) {
        return new IngresoSemana() {
            public LocalDate getSemana() { return inicio; }
            public BigDecimal getTotal() { return total; }
        };
    }
}
