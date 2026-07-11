package com.coffee.backend.bootstrap;

import com.coffee.backend.dto.response.PedidoDespachoTokenView;
import com.coffee.backend.repository.PedidoRepository;
import com.coffee.backend.service.PedidoDespachoService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Rehidrata la cola de despacho al arrancar: recorre los pedidos persistidos por
 * {@code fechaPedido} ASC y los encola en orden FIFO. Usa {@link ApplicationRunner}
 * (corre una vez con el contexto ya listo), no {@code @PostConstruct}.
 */
@Component
@RequiredArgsConstructor
public class DespachoBootstrapRunner implements ApplicationRunner {

    private final PedidoRepository pedidoRepository;
    private final PedidoDespachoService pedidoDespachoService;

    @Override
    public void run(ApplicationArguments args) {
        pedidoRepository.findAllByOrderByFechaPedidoAsc().stream()
                .map(p -> new PedidoDespachoTokenView(
                        p.getPedidoId(), p.getAliasTicket(), p.getFechaPedido()))
                .forEach(pedidoDespachoService::enqueueDespacho);
    }
}
