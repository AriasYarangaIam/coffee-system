package com.coffee.backend.service.implement;

import com.coffee.backend.dto.response.PedidoDespachoTokenView;
import com.coffee.backend.service.PedidoDespachoService;
import com.coffee.backend.tad.ColaPrioridad;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementación de la cola de despacho. El {@code @Service} es singleton ⇒ la
 * {@link ColaPrioridad} residente es única en el contexto (REQ-PDI-01). Despacho FIFO
 * puro (prioridad 0). {@code siguienteDespacho} es peek (sin efecto), preservando la
 * traza de pendientes; el dequeue/marcado se difiere a un cambio futuro con estados.
 *
 * <p>Nota: estado en memoria de UNA instancia; multi-instancia fuera de alcance.
 */
@Service
public class PedidoDespachoServiceImpl implements PedidoDespachoService {

    private final ColaPrioridad<PedidoDespachoTokenView> cola = new ColaPrioridad<>();

    @Override
    public void enqueueDespacho(PedidoDespachoTokenView token) {
        cola.enqueue(token); // prioridad 0 (FIFO)
    }

    @Override
    public List<PedidoDespachoTokenView> listarDespacho() {
        return cola.aLista();
    }

    @Override
    public Optional<PedidoDespachoTokenView> siguienteDespacho() {
        return cola.peek();
    }
}
