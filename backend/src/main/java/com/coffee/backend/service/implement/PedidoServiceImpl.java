package com.coffee.backend.service.implement;

import com.coffee.backend.dto.request.DetallePedidoRequestDTO;
import com.coffee.backend.dto.request.PedidoRequestDTO;
import com.coffee.backend.dto.response.PedidoResponseDTO;
import com.coffee.backend.entity.*;
import com.coffee.backend.exception.StockInsuficienteException;
import com.coffee.backend.repository.*;
import com.coffee.backend.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidosRepository;
    private final RecetaRepository recetaRepository;
    private final StockRepository stocksRepository;
    private final ProductoRepository productosRepository;
    private final UsuarioRepository usuariosRepository;

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
        pedido.setAliasTicket(dto.aliasTicket());
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

        return new PedidoResponseDTO(
                pedido.getPedidoId(),
                pedido.getAliasTicket(),
                pedido.getFechaPedido()
        );
    }
}