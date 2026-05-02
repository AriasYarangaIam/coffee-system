package com.coffee.backend.service;

import com.coffee.backend.dto.request.DetallePedidoRequestDTO;
import com.coffee.backend.dto.request.PedidoRequestDTO;
import com.coffee.backend.dto.response.PedidoResponseDTO;
import com.coffee.backend.entity.*;
import com.coffee.backend.exception.StockInsuficienteException;
import com.coffee.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidosRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final RecetaRepository recetaRepository;
    private final StockRepository stocksRepository;
    private final ProductoRepository productosRepository;
    private final UsuarioRepository usuariosRepository;

    @Transactional
    @Override
    public PedidoResponseDTO registrarPedido(PedidoRequestDTO dto) {

        // VALIDAR STOCK Y GUARDAR RECETAS EN MEMORIA
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

        // CREAR PEDIDO, DETALLES Y DESCONTAR STOCK
        Usuarios usuario = usuariosRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado: " + dto.usuarioId()
                ));

        Pedidos pedido = new Pedidos();
        pedido.setAliasTicket(dto.aliasTicket());
        pedido.setUsuario(usuario);
        pedido.setFechaPedido(new java.sql.Date(System.currentTimeMillis()));
        pedidosRepository.save(pedido);

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
            detallePedidoRepository.save(detallePedido);

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

        return new PedidoResponseDTO(
                pedido.getPedidoId(),
                pedido.getAliasTicket(),
                pedido.getFechaPedido()
        );
    }

    private String generarAlias() {
        return UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }
}