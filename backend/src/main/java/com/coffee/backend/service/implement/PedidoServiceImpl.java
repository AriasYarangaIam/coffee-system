package com.coffee.backend.service.implement;

import com.coffee.backend.dto.request.DetallePedidoRequestDTO;
import com.coffee.backend.dto.request.PedidoRequestDTO;
import com.coffee.backend.dto.response.BoletaResponseDTO;
import com.coffee.backend.dto.response.DetalleBoletaResponseDTO;
import com.coffee.backend.dto.response.PedidoDespachoTokenView;
import com.coffee.backend.dto.response.PedidoListadoResponseDTO;
import com.coffee.backend.dto.response.PedidoResponseDTO;
import com.coffee.backend.entity.*;
import com.coffee.backend.exception.StockInsuficienteException;
import com.coffee.backend.repository.*;
import com.coffee.backend.service.PedidoDespachoService;
import com.coffee.backend.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidosRepository;
    private final RecetaRepository recetaRepository;
    private final StockRepository stocksRepository;
    private final ProductoRepository productosRepository;
    private final UsuarioRepository usuariosRepository;
    private final PedidoDespachoService pedidoDespachoService;

    // DetallePedidoRepository eliminado — usa Cascada

    @Transactional
    @Override
    public PedidoResponseDTO registrarPedido(PedidoRequestDTO dto, String correoUsuarioLogueado) {

        // 1. VALIDAR STOCK Y GUARDAR RECETAS EN MEMORIA
        Map<Long, List<Recetas>> recetasPorProducto = new HashMap<>();

        for (DetallePedidoRequestDTO detalle : dto.detalles()) {
            List<Recetas> recetas = recetaRepository
                    .findByProductos_ProductoId(detalle.productoId());
            recetasPorProducto.put(detalle.productoId(), recetas);

            for (Recetas receta : recetas) {
                Long necesario = receta.getCantidadUsada() * detalle.cantidadPedida();

                Stocks stock = stocksRepository
                        .findByInsumos_IdInsumo(receta.getInsumos().getIdInsumo())
                        .orElseThrow(() -> new StockInsuficienteException(
                                "No hay stock para: " + receta.getInsumos().getNombreInsumo()
                        ));

                if (stock.getCantidad() < necesario) {
                    throw new StockInsuficienteException(
                            "Stock insuficiente para: " + receta.getInsumos().getNombreInsumo()
                    );
                }
            }
        }

        // 2. BUSCAR USUARIO POR CORREO
        Usuarios usuario = usuariosRepository.findByCorreoUsuario(correoUsuarioLogueado)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado: " + correoUsuarioLogueado
                ));

        // 3. ARMAR EL PEDIDO EN MEMORIA
        Pedidos pedido = new Pedidos();
        pedido.setAliasTicket(generarAlias()); // el backend genera el alias (B-14)
        pedido.setUsuario(usuario);
        pedido.setFechaPedido(LocalDateTime.now());

        // 4. CREAR DETALLES Y DESCONTAR STOCK
        for (DetallePedidoRequestDTO detalle : dto.detalles()) {

            Productos producto = productosRepository.findById(detalle.productoId())
                    .orElseThrow(() -> new RuntimeException(
                            "Producto no encontrado: " + detalle.productoId()
                    ));

            DetallePedido detallePedido = new DetallePedido();
            detallePedido.setCantidadPedida(detalle.cantidadPedida());
            detallePedido.setPedidos(pedido);
            detallePedido.setProductos(producto);
            detallePedido.setPrecioUnitario(producto.getPrecioActual());

            // Agrega el detalle a la lista del pedido EN MEMORIA
            pedido.getDetalles().add(detallePedido);

            // Descontar stock
            List<Recetas> recetas = recetasPorProducto.get(detalle.productoId());
            for (Recetas receta : recetas) {
                Long necesario = receta.getCantidadUsada() * detalle.cantidadPedida();
                Stocks stock = stocksRepository
                        .findByInsumos_IdInsumo(receta.getInsumos().getIdInsumo())
                        .orElseThrow(() -> new StockInsuficienteException(
                                "Stock no encontrado para: " + receta.getInsumos().getNombreInsumo()
                        ));
                stock.setCantidad(stock.getCantidad() - necesario);
                stocksRepository.save(stock);
            }
        }

        // 5. UN SOLO GUARDADO — Spring guarda pedido y detalles por Cascada
        pedidosRepository.save(pedido);

        // 6. Encolar para despacho (RF-DS-04). Última sentencia tras el save: si el save
        //    falla, el rollback ocurre antes y la cola en memoria no recibe un token fantasma.
        pedidoDespachoService.enqueueDespacho(new PedidoDespachoTokenView(
                pedido.getPedidoId(), pedido.getAliasTicket(), pedido.getFechaPedido()));

        return new PedidoResponseDTO(
                pedido.getPedidoId(),
                pedido.getAliasTicket(),
                pedido.getFechaPedido()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BoletaResponseDTO obtenerBoleta(Long pedidoId) {

        Pedidos pedido = pedidosRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        List<DetalleBoletaResponseDTO> detalleDTO = pedido.getDetalles()
                .stream()
                .map(d -> new DetalleBoletaResponseDTO(
                        d.getProductos().getNombreProducto(),
                        d.getCantidadPedida(),
                        d.getPrecioUnitario()
                ))
                .toList();

        double total = pedido.getDetalles()
                .stream()
                .mapToDouble(d -> d.getCantidadPedida() * d.getPrecioUnitario())
                .sum();

        return new BoletaResponseDTO(
                pedido.getPedidoId(),
                pedido.getAliasTicket(),
                pedido.getFechaPedido(),
                detalleDTO,
                total
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoListadoResponseDTO> listarPedidosDeMesero(String correoUsuarioLogueado) {
        return pedidosRepository.findByUsuario_CorreoUsuario(correoUsuarioLogueado)
                .stream()
                .map(pedido -> {
                    double total = pedido.getDetalles().stream()
                            .mapToDouble(d -> d.getCantidadPedida() * d.getPrecioUnitario())
                            .sum();
                    List<DetalleBoletaResponseDTO> detalle = pedido.getDetalles().stream()
                            .map(d -> new DetalleBoletaResponseDTO(
                                    d.getProductos().getNombreProducto(),
                                    d.getCantidadPedida(),
                                    d.getPrecioUnitario()))
                            .toList();
                    return new PedidoListadoResponseDTO(
                            pedido.getPedidoId(),
                            pedido.getAliasTicket(),
                            pedido.getFechaPedido(),
                            total,
                            detalle
                    );
                })
                .toList();
    }

    // Alias de boleta único y corto (UUID de 8 chars en mayúsculas).
    private String generarAlias() {
        return UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }
}