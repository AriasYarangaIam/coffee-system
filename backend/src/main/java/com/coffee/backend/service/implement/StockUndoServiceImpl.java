package com.coffee.backend.service.implement;

import com.coffee.backend.dto.response.MovimientoStockView;
import com.coffee.backend.service.StockUndoService;
import com.coffee.backend.tad.Pila;
import com.coffee.backend.tad.PilaEnlazada;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementación de la Pila de deshacer. El {@code @Service} es singleton ⇒ la
 * {@link PilaEnlazada} residente es única en el contexto. LIFO: se deshace el último
 * ingreso registrado.
 *
 * <p>Nota: estado en memoria de UNA instancia; multi-instancia fuera de alcance
 * (mismo criterio que la cola de despacho).
 */
@Service
public class StockUndoServiceImpl implements StockUndoService {

    private final Pila<MovimientoStockView> pila = new PilaEnlazada<>();

    @Override
    public void registrar(MovimientoStockView view) {
        pila.push(view);
    }

    @Override
    public Optional<MovimientoStockView> deshacer() {
        return pila.pop();
    }
}
