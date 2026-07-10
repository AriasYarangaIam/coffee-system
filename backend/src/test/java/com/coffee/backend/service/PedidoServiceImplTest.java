package com.coffee.backend.service;

import com.coffee.backend.dto.request.DetallePedidoRequestDTO;
import com.coffee.backend.dto.request.PedidoRequestDTO;
import com.coffee.backend.entity.Insumos;
import com.coffee.backend.entity.Productos;
import com.coffee.backend.entity.Recetas;
import com.coffee.backend.entity.Roles;
import com.coffee.backend.entity.Stocks;
import com.coffee.backend.entity.Usuarios;
import com.coffee.backend.repository.PedidoRepository;
import com.coffee.backend.repository.ProductoRepository;
import com.coffee.backend.repository.RecetaRepository;
import com.coffee.backend.repository.StockRepository;
import com.coffee.backend.repository.UsuarioRepository;
import com.coffee.backend.service.implement.PedidoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests de {@link PedidoServiceImpl} con dependencias mockeadas.
 * No requiere base de datos real.
 */
class PedidoServiceImplTest {

    private PedidoRepository pedidoRepository;
    private RecetaRepository recetaRepository;
    private StockRepository stockRepository;
    private ProductoRepository productoRepository;
    private UsuarioRepository usuarioRepository;
    private PedidoDespachoService pedidoDespachoService;
    private PedidoUndoService pedidoUndoService;
    private PedidoServiceImpl service;

    @BeforeEach
    void setUp() {
        pedidoRepository = mock(PedidoRepository.class);
        recetaRepository = mock(RecetaRepository.class);
        stockRepository = mock(StockRepository.class);
        productoRepository = mock(ProductoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        pedidoDespachoService = mock(PedidoDespachoService.class);
        pedidoUndoService = mock(PedidoUndoService.class);

        service = new PedidoServiceImpl(
                pedidoRepository,
                recetaRepository,
                stockRepository,
                productoRepository,
                usuarioRepository,
                pedidoDespachoService,
                pedidoUndoService
        );

        given(pedidoRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void registrarPedido_despuesDeGuardarLimpiaUndoStackDelUsuario() {
        String correo = "mesero@coffee.com";
        Long productoId = 1L;
        Long insumoId = 10L;

        DetallePedidoRequestDTO detalle = new DetallePedidoRequestDTO(productoId, 2L);
        PedidoRequestDTO dto = new PedidoRequestDTO(List.of(detalle));

        Productos producto = new Productos();
        producto.setProductoId(productoId);
        producto.setNombreProducto("Café");
        producto.setPrecioActual(2500.0);
        producto.setCategorias(new com.coffee.backend.entity.Categorias());

        Insumos insumo = new Insumos();
        insumo.setIdInsumo(insumoId);
        insumo.setNombreInsumo("Café en grano");

        Recetas receta = new Recetas();
        receta.setRecetaID(100L);
        receta.setCantidadUsada(10L);
        receta.setProductos(producto);
        receta.setInsumos(insumo);

        Stocks stock = new Stocks();
        stock.setCodigoStock(1L);
        stock.setCantidad(100L);
        stock.setInsumos(insumo);

        Usuarios usuario = Usuarios.builder()
                .usuarioId(1L)
                .nombreUsuario("Mesero")
                .apellidoUsuario("Test")
                .correoUsuario(correo)
                .claveCifrada("secret")
                .roles(new Roles())
                .build();

        given(recetaRepository.findByProductos_ProductoId(productoId)).willReturn(List.of(receta));
        given(stockRepository.findByInsumos_IdInsumo(insumoId)).willReturn(Optional.of(stock));
        given(usuarioRepository.findByCorreoUsuario(correo)).willReturn(Optional.of(usuario));
        given(productoRepository.findById(productoId)).willReturn(Optional.of(producto));

        service.registrarPedido(dto, correo);

        verify(pedidoUndoService).clearForUser(correo);
        verify(pedidoDespachoService).enqueueDespacho(argThat(token ->
                token.aliasTicket() != null && token.pedidoId() == null));
    }
}
