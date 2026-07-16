package com.coffee.backend.service;

import com.coffee.backend.bootstrap.DespachoBootstrapRunner;
import com.coffee.backend.dto.response.PedidoDespachoTokenView;
import com.coffee.backend.entity.Pedidos;
import com.coffee.backend.repository.PedidoRepository;
import com.coffee.backend.service.implement.PedidoDespachoServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Unit test puro de la cola de despacho (RF-DS-04): servicio residente + rehidratación
 * por el {@link DespachoBootstrapRunner}. Sin Spring, sin BD (repo mockeado).
 */
class PedidoDespachoServiceImplTest {

    private PedidoDespachoTokenView token(long id) {
        return new PedidoDespachoTokenView(id, "T" + id, LocalDateTime.now().plusMinutes(id));
    }

    @Test
    void enqueue_yListar_enOrdenFifo() {
        PedidoDespachoServiceImpl service = new PedidoDespachoServiceImpl();
        service.enqueueDespacho(token(1));
        service.enqueueDespacho(token(2));
        service.enqueueDespacho(token(3));

        assertThat(service.listarDespacho())
                .extracting(PedidoDespachoTokenView::pedidoId)
                .containsExactly(1L, 2L, 3L);
    }

    @Test
    void siguienteDespacho_peekDeLaCabeza_sinRemover() {
        PedidoDespachoServiceImpl service = new PedidoDespachoServiceImpl();
        service.enqueueDespacho(token(1));
        service.enqueueDespacho(token(2));

        assertThat(service.siguienteDespacho()).map(PedidoDespachoTokenView::pedidoId).contains(1L);
        assertThat(service.listarDespacho()).hasSize(2); // peek no drena
    }

    @Test
    void entregarCabeza_desencolaFifo() {
        PedidoDespachoServiceImpl service = new PedidoDespachoServiceImpl();
        service.enqueueDespacho(token(1));
        service.enqueueDespacho(token(2));

        assertThat(service.entregarCabeza()).map(PedidoDespachoTokenView::pedidoId).contains(1L);
        assertThat(service.listarDespacho())
                .extracting(PedidoDespachoTokenView::pedidoId)
                .containsExactly(2L); // el primero salió (dequeue)
    }

    @Test
    void colaVacia_siguienteDespachoEmpty() {
        PedidoDespachoServiceImpl service = new PedidoDespachoServiceImpl();
        assertThat(service.siguienteDespacho()).isEmpty();
        assertThat(service.listarDespacho()).isEmpty();
    }

    @Test
    void bootstrapRunner_rehidrataDesdeBdEnOrden() throws Exception {
        PedidoRepository repo = mock(PedidoRepository.class);
        given(repo.findByEntregadoFalseOrderByFechaPedidoAsc()).willReturn(List.of(
                pedido(10L, "AAA"), pedido(20L, "BBB"), pedido(30L, "CCC")));

        PedidoDespachoServiceImpl service = new PedidoDespachoServiceImpl();
        new DespachoBootstrapRunner(repo, service).run(null);

        assertThat(service.listarDespacho())
                .extracting(PedidoDespachoTokenView::aliasTicket)
                .containsExactly("AAA", "BBB", "CCC");
    }

    private Pedidos pedido(Long id, String alias) {
        Pedidos p = new Pedidos();
        p.setPedidoId(id);
        p.setAliasTicket(alias);
        p.setFechaPedido(LocalDateTime.now());
        return p;
    }
}
