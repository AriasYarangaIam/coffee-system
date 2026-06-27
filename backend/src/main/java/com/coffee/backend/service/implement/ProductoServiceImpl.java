package com.coffee.backend.service.implement;

import com.coffee.backend.dto.response.InsumoRecetaDTO;
import com.coffee.backend.dto.response.ProductoListadoResponseDTO;
import com.coffee.backend.dto.response.ProductoRecetaResponseDTO;
import com.coffee.backend.entity.Productos;
import com.coffee.backend.entity.Recetas;
import com.coffee.backend.entity.Stocks;
import com.coffee.backend.exception.ProductoNoEncontradoException;
import com.coffee.backend.mapper.ProductoMapper;
import com.coffee.backend.repository.ProductoRepository;
import com.coffee.backend.repository.StockRepository;
import com.coffee.backend.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final StockRepository stocksRepository;
    private final ProductoMapper productoMapper;

    @Override
    @Transactional(readOnly = true)
    public ProductoRecetaResponseDTO obtenerRecetaDeProducto(Long productoId) {
        Productos producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNoEncontradoException("Producto no encontrado con ID: " + productoId));

        List<InsumoRecetaDTO> insumos = producto.getRecetas().stream()
                .map(receta -> new InsumoRecetaDTO(
                        receta.getInsumos().getIdInsumo(),
                        receta.getInsumos().getNombreInsumo(),
                        receta.getCantidadUsada()
                ))
                .toList();

        return new ProductoRecetaResponseDTO(
                producto.getProductoId(),
                producto.getNombreProducto(),
                insumos
        );
    }

    @Override
    @Transactional
    public List<ProductoListadoResponseDTO> listarProductos() {
        return productoRepository.findAll()
                .stream()
                .map(producto -> { boolean disponible = calcularDisponibilidad(producto);
                    return productoMapper.toListadoDTO(producto, disponible);
                })
                .toList();
    }

    private boolean calcularDisponibilidad(Productos producto) {
        List<Recetas> recetas = producto.getRecetas();
        if (recetas == null || recetas.isEmpty()) {
            return true;
        }
        for (Recetas receta : recetas) {
            Stocks stock = stocksRepository
                    .findByInsumos_IdInsumo(receta.getInsumos().getIdInsumo())
                    .orElse(null);
            if (stock == null || stock.getCantidad() < receta.getCantidadUsada()) {
                return false;
            }
        }
        return true;
    }
}
