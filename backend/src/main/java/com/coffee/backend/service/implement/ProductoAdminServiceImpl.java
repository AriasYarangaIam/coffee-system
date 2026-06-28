package com.coffee.backend.service.implement;

import com.coffee.backend.dto.request.ProductoRequestDTO;
import com.coffee.backend.dto.request.RecetaItemRequestDTO;
import com.coffee.backend.dto.response.InsumoRecetaDTO;
import com.coffee.backend.dto.response.ProductoAdminResponseDTO;
import com.coffee.backend.entity.Categorias;
import com.coffee.backend.entity.Insumos;
import com.coffee.backend.entity.Productos;
import com.coffee.backend.entity.Recetas;
import com.coffee.backend.exception.RecursoNoEncontradoException;
import com.coffee.backend.repository.CategoriaRepository;
import com.coffee.backend.repository.InsumoRepository;
import com.coffee.backend.repository.ProductoRepository;
import com.coffee.backend.service.ProductoAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductoAdminServiceImpl implements ProductoAdminService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final InsumoRepository insumoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoAdminResponseDTO> listar() {
        return productoRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public ProductoAdminResponseDTO crear(ProductoRequestDTO dto) {
        Productos producto = new Productos();
        producto.setNombreProducto(dto.nombreProducto());
        producto.setPrecioActual(dto.precioActual());
        producto.setCategorias(buscarCategoria(dto.categoriaId()));
        sincronizarRecetas(producto, dto.receta());
        return toDTO(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public ProductoAdminResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        Productos producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con ID: " + id));
        producto.setNombreProducto(dto.nombreProducto());
        producto.setPrecioActual(dto.precioActual());
        producto.setCategorias(buscarCategoria(dto.categoriaId()));
        sincronizarRecetas(producto, dto.receta());
        return toDTO(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Producto no encontrado con ID: " + id);
        }
        productoRepository.deleteById(id); // cascade REMOVE borra las recetas del producto
    }

    // Reemplaza la receta completa del producto. orphanRemoval borra las filas viejas;
    // la cascada PERSIST guarda las nuevas al hacer save(producto).
    private void sincronizarRecetas(Productos producto, List<RecetaItemRequestDTO> items) {
        if (producto.getRecetas() == null) {
            producto.setRecetas(new ArrayList<>());
        } else {
            producto.getRecetas().clear();
        }
        if (items == null || items.isEmpty()) {
            return; // producto sin receta: válido (no consume stock al venderse)
        }

        Set<Long> vistos = new HashSet<>();
        for (RecetaItemRequestDTO item : items) {
            if (!vistos.add(item.insumoId())) {
                throw new IllegalArgumentException(
                        "Insumo repetido en la receta (id: " + item.insumoId() + ")");
            }
            Insumos insumo = insumoRepository.findById(item.insumoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Insumo no encontrado con ID: " + item.insumoId()));

            Recetas receta = new Recetas();
            receta.setProductos(producto);
            receta.setInsumos(insumo);
            receta.setCantidadUsada(item.cantidadUsada());
            receta.setNombreReceta(producto.getNombreProducto() + " - " + insumo.getNombreInsumo());
            producto.getRecetas().add(receta);
        }
    }

    private Categorias buscarCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con ID: " + categoriaId));
    }

    private ProductoAdminResponseDTO toDTO(Productos p) {
        List<InsumoRecetaDTO> receta = (p.getRecetas() == null ? List.<Recetas>of() : p.getRecetas())
                .stream()
                .map(r -> new InsumoRecetaDTO(
                        r.getInsumos().getIdInsumo(),
                        r.getInsumos().getNombreInsumo(),
                        r.getCantidadUsada()))
                .toList();
        return new ProductoAdminResponseDTO(
                p.getProductoId(),
                p.getNombreProducto(),
                p.getPrecioActual(),
                p.getCategorias().getCategoriaId(),
                p.getCategorias().getNombreCategoria(),
                receta
        );
    }
}
