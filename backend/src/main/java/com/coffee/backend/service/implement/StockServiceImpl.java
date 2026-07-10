package com.coffee.backend.service.implement;

import com.coffee.backend.dto.request.StockRequestDTO;
import com.coffee.backend.dto.response.MovimientoStockResponseDTO;
import com.coffee.backend.dto.response.MovimientoStockView;
import com.coffee.backend.dto.response.StockResponseDTO;
import com.coffee.backend.entity.Almacenes;
import com.coffee.backend.entity.Insumos;
import com.coffee.backend.entity.MovimientoStock;
import com.coffee.backend.entity.MovimientoStock.Estado;
import com.coffee.backend.entity.MovimientoStock.Tipo;
import com.coffee.backend.entity.Stocks;
import com.coffee.backend.entity.Usuarios;
import com.coffee.backend.exception.RecursoNoEncontradoException;
import com.coffee.backend.exception.ReglaNegocioException;
import com.coffee.backend.exception.StockInsuficienteException;
import com.coffee.backend.repository.AlmacenRepository;
import com.coffee.backend.repository.InsumoRepository;
import com.coffee.backend.repository.MovimientoStockRepository;
import com.coffee.backend.repository.StockRepository;
import com.coffee.backend.repository.UsuarioRepository;
import com.coffee.backend.service.StockService;
import com.coffee.backend.service.StockUndoService;
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
    private final MovimientoStockRepository movimientoStockRepository;
    private final UsuarioRepository usuarioRepository;
    private final StockUndoService stockUndoService;

    @Override
    @Transactional(readOnly = true)
    public List<StockResponseDTO> listar() {
        return stockRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public StockResponseDTO registrarIngreso(StockRequestDTO dto, String correo) {
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
        Stocks guardado = stockRepository.save(stock);

        // Traza del ingreso. El almacén es el EFECTIVO del stock, no el del DTO: cuando el
        // insumo ya tenía stock, registrarIngreso ignora dto.almacenId (quirk preexistente).
        Usuarios usuario = usuarioRepository.findByCorreoUsuario(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + correo));
        MovimientoStock mov = new MovimientoStock();
        mov.setInsumo(insumo);
        mov.setAlmacen(guardado.getAlmacenes());
        mov.setCantidad(dto.cantidad());
        mov.setUsuario(usuario);
        mov.setTipo(Tipo.INGRESO);
        mov.setEstado(Estado.ACTIVO);
        mov.setCodigoStock(guardado.getCodigoStock());
        MovimientoStock movGuardado = movimientoStockRepository.save(mov);

        stockUndoService.registrar(new MovimientoStockView(
                movGuardado.getId(), guardado.getCodigoStock(), dto.cantidad()));

        return toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoStockResponseDTO> listarMovimientos() {
        return movimientoStockRepository.findTop20ByEstadoOrderByFechaDesc(Estado.ACTIVO).stream()
                .map(this::toMovimientoDTO)
                .toList();
    }

    @Override
    @Transactional
    public StockResponseDTO deshacerUltimoIngreso() {
        MovimientoStockView view = stockUndoService.deshacer()
                .orElseThrow(() -> new ReglaNegocioException("No hay ingresos para deshacer"));

        MovimientoStock mov = movimientoStockRepository.findById(view.movimientoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Movimiento no encontrado"));
        if (mov.getEstado() != Estado.ACTIVO) {
            throw new ReglaNegocioException("El ingreso ya fue revertido");
        }

        Stocks stock = stockRepository.findById(view.codigoStock())
                .orElseThrow(() -> new RecursoNoEncontradoException("Stock no encontrado"));
        // Si el saldo ya se consumió en pedidos, no se puede revertir sin dejarlo negativo.
        if (stock.getCantidad() < mov.getCantidad()) {
            throw new StockInsuficienteException(
                    "El saldo ya fue consumido; no se puede deshacer este ingreso");
        }

        stock.setCantidad(stock.getCantidad() - mov.getCantidad());
        mov.setEstado(Estado.REVERSADO);
        movimientoStockRepository.save(mov);
        return toDTO(stockRepository.save(stock));
    }

    private StockResponseDTO toDTO(Stocks s) {
        return new StockResponseDTO(
                s.getCodigoStock(),
                s.getInsumos().getIdInsumo(),
                s.getInsumos().getNombreInsumo(),
                s.getInsumos().getUnidad(),
                s.getAlmacenes().getCodigoAlmacen(),
                s.getAlmacenes().getNombreAlmacen(),
                s.getCantidad()
        );
    }

    private MovimientoStockResponseDTO toMovimientoDTO(MovimientoStock m) {
        return new MovimientoStockResponseDTO(
                m.getId(),
                m.getInsumo().getIdInsumo(),
                m.getInsumo().getNombreInsumo(),
                m.getInsumo().getUnidad(),
                m.getAlmacen().getCodigoAlmacen(),
                m.getAlmacen().getNombreAlmacen(),
                m.getCantidad(),
                m.getFecha(),
                m.getUsuario().getNombreUsuario() + " " + m.getUsuario().getApellidoUsuario(),
                m.getEstado().name()
        );
    }
}
