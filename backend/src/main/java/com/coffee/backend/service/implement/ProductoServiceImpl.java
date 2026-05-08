package com.coffee.backend.service.implement;

import com.coffee.backend.dto.response.InsumoRecetaDTO;
import com.coffee.backend.dto.response.ProductoRecetaResponseDTO;
import com.coffee.backend.entity.Productos;
import com.coffee.backend.exception.ProductoNoEncontradoException;
import com.coffee.backend.repository.ProductoRepository;
import com.coffee.backend.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

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
}
