package com.coffee.backend.service;

import com.coffee.backend.dto.response.ReporteMensualResponseDTO;
import com.coffee.backend.repository.PedidoRepository;
import com.coffee.backend.repository.PedidoRepository.VentaProductoDia;
import com.coffee.backend.service.implement.ReporteServiceImpl;
import org.junit.jupiter.api.Test;

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

    private record Celda(String producto, int dia, double total) implements VentaProductoDia {
        public String getProducto() { return producto; }
        public int getDia() { return dia; }
        public double getTotal() { return total; }
    }

    @Test
    void armaMatrizProductoPorDia_conCerosDondeNoHuboVenta() {
        PedidoRepository repo = mock(PedidoRepository.class);
        given(repo.ventasPorProductoYDia(any(), any())).willReturn(List.<VentaProductoDia>of(
                new Celda("Capuchino", 1, 30.0),
                new Celda("Capuchino", 3, 12.5),
                new Celda("Latte", 1, 20.0)));

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
}
