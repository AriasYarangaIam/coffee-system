package com.coffee.backend.service.implement;

import com.coffee.backend.dto.request.StockRequestDTO;
import com.coffee.backend.dto.response.StockResponseDTO;
import com.coffee.backend.entity.Almacenes;
import com.coffee.backend.entity.Insumos;
import com.coffee.backend.entity.Stocks;
import com.coffee.backend.exception.RecursoNoEncontradoException;
import com.coffee.backend.repository.AlmacenRepository;
import com.coffee.backend.repository.InsumoRepository;
import com.coffee.backend.repository.StockRepository;
import com.coffee.backend.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final InsumoRepository insumoRepository;
    private final AlmacenRepository almacenRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StockResponseDTO> listar() {
        return stockRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public StockResponseDTO registrarIngreso(StockRequestDTO dto) {
        Insumos insumo = insumoRepository.findById(dto.insumoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Insumo no encontrado con ID: " + dto.insumoId()));

        // Si ya hay stock del insumo, se incrementa; si no, se crea en el almacén indicado.
        Stocks stock = stockRepository.findByInsumos_IdInsumo(dto.insumoId())
                .orElseGet(() -> {
                    Almacenes almacen = almacenRepository.findById(dto.almacenId())
                            .orElseThrow(() -> new RecursoNoEncontradoException(
                                    "Almacén no encontrado con ID: " + dto.almacenId()));
                    Stocks nuevo = new Stocks();
                    nuevo.setInsumos(insumo);
                    nuevo.setAlmacenes(almacen);
                    nuevo.setCantidad(0L);
                    return nuevo;
                });

        stock.setCantidad(stock.getCantidad() + dto.cantidad());
        return toDTO(stockRepository.save(stock));
    }

    private StockResponseDTO toDTO(Stocks s) {
        return new StockResponseDTO(
                s.getCodigoStock(),
                s.getInsumos().getIdInsumo(),
                s.getInsumos().getNombreInsumo(),
                s.getAlmacenes().getCodigoAlmacen(),
                s.getAlmacenes().getNombreAlmacen(),
                s.getCantidad()
        );
    }
}
