package com.coffee.backend.service;

import com.coffee.backend.dto.request.ProductoRequestDTO;
import java.math.BigDecimal;
import com.coffee.backend.dto.request.RecetaItemRequestDTO;
import com.coffee.backend.dto.response.ProductoAdminResponseDTO;
import com.coffee.backend.entity.Categorias;
import com.coffee.backend.entity.Insumos;
import com.coffee.backend.entity.Productos;
import com.coffee.backend.exception.RecursoNoEncontradoException;
import com.coffee.backend.repository.CategoriaRepository;
import com.coffee.backend.repository.InsumoRepository;
import com.coffee.backend.repository.ProductoRepository;
import com.coffee.backend.service.implement.ProductoAdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Unit test de la sincronización de recetas en el alta/edición de producto (la "carta").
 * Repos mockeados, sin Spring ni BD.
 */
class ProductoAdminServiceImplTest {

    private ProductoRepository productoRepository;
    private CategoriaRepository categoriaRepository;
    private InsumoRepository insumoRepository;
    private ProductoAdminServiceImpl service;

    @BeforeEach
    void setUp() {
        productoRepository = mock(ProductoRepository.class);
        categoriaRepository = mock(CategoriaRepository.class);
        insumoRepository = mock(InsumoRepository.class);
        service = new ProductoAdminServiceImpl(productoRepository, categoriaRepository, insumoRepository);

        given(categoriaRepository.findById(1L)).willReturn(Optional.of(categoria(1L, "Bebidas")));
        given(insumoRepository.findById(10L)).willReturn(Optional.of(insumo(10L, "Leche")));
        given(insumoRepository.findById(20L)).willReturn(Optional.of(insumo(20L, "Café")));
        // save devuelve el mismo producto (con sus recetas ya seteadas en memoria).
        given(productoRepository.save(any(Productos.class))).willAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void crear_armaLaRecetaConSusInsumos() {
        ProductoRequestDTO dto = new ProductoRequestDTO("Latte", new BigDecimal("12.0"), 1L, List.of(
                new RecetaItemRequestDTO(10L, 200L),
                new RecetaItemRequestDTO(20L, 18L)));

        ProductoAdminResponseDTO res = service.crear(dto);

        assertThat(res.receta())
                .extracting(r -> r.nombreInsumo() + ":" + r.cantidadUsada())
                .containsExactly("Leche:200", "Café:18");
    }

    @Test
    void actualizar_reemplazaLaReceta() {
        Productos existente = new Productos();
        existente.setProductoId(5L);
        existente.setCategorias(categoria(1L, "Bebidas"));
        existente.setRecetas(new java.util.ArrayList<>()); // tenía receta previa (vacía aquí)
        given(productoRepository.findById(5L)).willReturn(Optional.of(existente));

        ProductoRequestDTO dto = new ProductoRequestDTO("Latte", new BigDecimal("12.0"), 1L, List.of(
                new RecetaItemRequestDTO(10L, 150L)));

        ProductoAdminResponseDTO res = service.actualizar(5L, dto);

        assertThat(res.receta()).hasSize(1);
        assertThat(res.receta().get(0).nombreInsumo()).isEqualTo("Leche");
        assertThat(res.receta().get(0).cantidadUsada()).isEqualTo(150L);
    }

    @Test
    void crear_conInsumoInexistente_lanza404() {
        ProductoRequestDTO dto = new ProductoRequestDTO("X", new BigDecimal("1.0"), 1L, List.of(
                new RecetaItemRequestDTO(999L, 1L)));

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("999");
    }

    @Test
    void crear_conInsumoDuplicado_lanzaIllegalArgument() {
        ProductoRequestDTO dto = new ProductoRequestDTO("X", new BigDecimal("1.0"), 1L, List.of(
                new RecetaItemRequestDTO(10L, 100L),
                new RecetaItemRequestDTO(10L, 50L)));

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("repetido");
    }

    @Test
    void crear_sinReceta_productoValidoSinInsumos() {
        ProductoRequestDTO dto = new ProductoRequestDTO("Agua embotellada", new BigDecimal("3.0"), 1L, null);

        ProductoAdminResponseDTO res = service.crear(dto);

        assertThat(res.receta()).isEmpty();
    }

    private Categorias categoria(Long id, String nombre) {
        Categorias c = new Categorias();
        c.setCategoriaId(id);
        c.setNombreCategoria(nombre);
        return c;
    }

    private Insumos insumo(Long id, String nombre) {
        Insumos i = new Insumos();
        i.setIdInsumo(id);
        i.setNombreInsumo(nombre);
        return i;
    }
}
