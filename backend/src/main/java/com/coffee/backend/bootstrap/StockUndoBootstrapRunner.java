package com.coffee.backend.bootstrap;

import com.coffee.backend.dto.response.MovimientoStockView;
import com.coffee.backend.entity.MovimientoStock.Estado;
import com.coffee.backend.repository.MovimientoStockRepository;
import com.coffee.backend.service.StockUndoService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Rehidrata la Pila de deshacer al arrancar: recorre los ingresos ACTIVO por
 * {@code fecha} ASC y los apila, de modo que el más reciente queda en el tope. Usa
 * {@link ApplicationRunner} (corre una vez con el contexto listo), como el runner de
 * despacho.
 */
@Component
@RequiredArgsConstructor
public class StockUndoBootstrapRunner implements ApplicationRunner {

    private final MovimientoStockRepository movimientoStockRepository;
    private final StockUndoService stockUndoService;

    @Override
    public void run(ApplicationArguments args) {
        movimientoStockRepository.findByEstadoOrderByFechaAsc(Estado.ACTIVO).stream()
                .map(m -> new MovimientoStockView(m.getId(), m.getCodigoStock(), m.getCantidad()))
                .forEach(stockUndoService::registrar);
    }
}
