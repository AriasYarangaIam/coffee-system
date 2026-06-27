package com.coffee.backend.service.implement;

import com.coffee.backend.dto.request.ProductoRequestDTO;
import com.coffee.backend.dto.response.ProductoAdminResponseDTO;
import com.coffee.backend.entity.Categorias;
import com.coffee.backend.entity.Productos;
import com.coffee.backend.exception.RecursoNoEncontradoException;
import com.coffee.backend.repository.CategoriaRepository;
import com.coffee.backend.repository.ProductoRepository;
import com.coffee.backend.service.ProductoAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoAdminServiceImpl implements ProductoAdminService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

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
        Categorias categoria = buscarCategoria(dto.categoriaId());
        Productos producto = new Productos();
        producto.setNombreProducto(dto.nombreProducto());
        producto.setPrecioActual(dto.precioActual());
        producto.setCategorias(categoria);
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
        return toDTO(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Producto no encontrado con ID: " + id);
        }
        productoRepository.deleteById(id);
    }

    private Categorias buscarCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con ID: " + categoriaId));
    }

    private ProductoAdminResponseDTO toDTO(Productos p) {
        return new ProductoAdminResponseDTO(
                p.getProductoId(),
                p.getNombreProducto(),
                p.getPrecioActual(),
                p.getCategorias().getCategoriaId(),
                p.getCategorias().getNombreCategoria()
        );
    }
}
