package com.coffee.backend.service;

import com.coffee.backend.dto.response.MovimientoStockView;
import com.coffee.backend.entity.Almacenes;
import com.coffee.backend.entity.Insumos;
import com.coffee.backend.entity.MovimientoStock;
import com.coffee.backend.entity.MovimientoStock.Estado;
import com.coffee.backend.entity.Stocks;
import com.coffee.backend.exception.ReglaNegocioException;
import com.coffee.backend.exception.StockInsuficienteException;
import com.coffee.backend.repository.AlmacenRepository;
import com.coffee.backend.repository.InsumoRepository;
import com.coffee.backend.repository.MovimientoStockRepository;
import com.coffee.backend.repository.StockRepository;
import com.coffee.backend.repository.UsuarioRepository;
import com.coffee.backend.service.implement.StockServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Deshacer el último ingreso de stock (RF-DS-03): revierte el saldo y marca el
 * movimiento REVERSADO, salvo que el saldo ya se haya consumido. Repos y Pila mockeados.
 */
class StockServiceImplTest {

    private final StockRepository stockRepository = mock(StockRepository.class);
    private final InsumoRepository insumoRepository = mock(InsumoRepository.class);
    private final AlmacenRepository almacenRepository = mock(AlmacenRepository.class);
    private final MovimientoStockRepository movimientoStockRepository = mock(MovimientoStockRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final StockUndoService stockUndoService = mock(StockUndoService.class);

    private final StockServiceImpl service = new StockServiceImpl(
            stockRepository, insumoRepository, almacenRepository,
            movimientoStockRepository, usuarioRepository, stockUndoService);

    @Test
    void deshacer_revierteSaldoYMarcaReversado() {
        given(stockUndoService.deshacer())
                .willReturn(Optional.of(new MovimientoStockView(7L, 3L, 10L)));
        MovimientoStock mov = movimiento(7L, 10L, Estado.ACTIVO);
        given(movimientoStockRepository.findById(7L)).willReturn(Optional.of(mov));
        Stocks stock = stock(3L, 25L);
        given(stockRepository.findById(3L)).willReturn(Optional.of(stock));
        given(stockRepository.save(any(Stocks.class))).willAnswer(i -> i.getArgument(0));

        service.deshacerUltimoIngreso();

        assertThat(stock.getCantidad()).isEqualTo(15L); // 25 - 10
        assertThat(mov.getEstado()).isEqualTo(Estado.REVERSADO);
        verify(movimientoStockRepository).save(mov);
    }

    @Test
    void deshacer_saldoYaConsumido_lanzaStockInsuficiente() {
        given(stockUndoService.deshacer())
                .willReturn(Optional.of(new MovimientoStockView(7L, 3L, 10L)));
        given(movimientoStockRepository.findById(7L))
                .willReturn(Optional.of(movimiento(7L, 10L, Estado.ACTIVO)));
        given(stockRepository.findById(3L)).willReturn(Optional.of(stock(3L, 4L))); // saldo < 10

        assertThatThrownBy(service::deshacerUltimoIngreso)
                .isInstanceOf(StockInsuficienteException.class);

        verify(stockRepository, never()).save(any());
    }

    @Test
    void deshacer_pilaVacia_lanzaReglaNegocio() {
        given(stockUndoService.deshacer()).willReturn(Optional.empty());

        assertThatThrownBy(service::deshacerUltimoIngreso)
                .isInstanceOf(ReglaNegocioException.class);
    }

    private MovimientoStock movimiento(Long id, Long cantidad, Estado estado) {
        MovimientoStock m = new MovimientoStock();
        m.setId(id);
        m.setCantidad(cantidad);
        m.setEstado(estado);
        return m;
    }

    private Stocks stock(Long codigo, Long cantidad) {
        Insumos insumo = new Insumos();
        insumo.setIdInsumo(5L);
        insumo.setNombreInsumo("Leche");
        insumo.setUnidad("ml");
        Almacenes almacen = new Almacenes();
        almacen.setCodigoAlmacen(2L);
        almacen.setNombreAlmacen("Central");
        Stocks s = new Stocks();
        s.setCodigoStock(codigo);
        s.setCantidad(cantidad);
        s.setInsumos(insumo);
        s.setAlmacenes(almacen);
        return s;
    }
}
