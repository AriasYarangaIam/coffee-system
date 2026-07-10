package com.coffee.backend.service;

import com.coffee.backend.dto.response.MisMetricasResponseDTO;
import com.coffee.backend.repository.PedidoRepository;
import com.coffee.backend.service.implement.PedidoServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Métricas del turno del mesero: ticket promedio y guard de división por cero. Solo se
 * mockea el PedidoRepository; el resto de dependencias no intervienen en misMetricas.
 */
class PedidoServiceMetricasTest {

    private final PedidoRepository pedidoRepository = mock(PedidoRepository.class);
    private final PedidoServiceImpl service =
            new PedidoServiceImpl(pedidoRepository, null, null, null, null, null);

    @Test
    void misMetricas_calculaTicketPromedio() {
        given(pedidoRepository.contarPedidosDeMeseroEntre(eq("m@x.com"), any(), any())).willReturn(4L);
        given(pedidoRepository.sumarVentasDeMeseroEntre(eq("m@x.com"), any(), any())).willReturn(100.0);
        given(pedidoRepository.productosMasVendidosDeMesero(eq("m@x.com"), any(), any(), any()))
                .willReturn(List.of("Capuchino"));

        MisMetricasResponseDTO r = service.misMetricas("m@x.com");

        assertThat(r.pedidosAtendidos()).isEqualTo(4);
        assertThat(r.totalVendido()).isEqualByComparingTo("100.00");
        assertThat(r.ticketPromedio()).isEqualByComparingTo("25.00"); // 100 / 4
        assertThat(r.productoEstrella()).isEqualTo("Capuchino");
    }

    @Test
    void misMetricas_sinPedidos_ticketCeroYEstrellaNull() {
        given(pedidoRepository.contarPedidosDeMeseroEntre(any(), any(), any())).willReturn(0L);
        given(pedidoRepository.sumarVentasDeMeseroEntre(any(), any(), any())).willReturn(0.0);
        given(pedidoRepository.productosMasVendidosDeMesero(any(), any(), any(), any()))
                .willReturn(List.of());

        MisMetricasResponseDTO r = service.misMetricas("m@x.com");

        assertThat(r.ticketPromedio()).isEqualByComparingTo("0.00"); // sin división por cero
        assertThat(r.productoEstrella()).isNull();
    }
}
