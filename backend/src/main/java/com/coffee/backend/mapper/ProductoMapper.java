package com.coffee.backend.mapper;

import com.coffee.backend.dto.response.ProductoListadoResponseDTO;
import com.coffee.backend.entity.Productos;
import org.springframework.stereotype.Component;

// Convierte la entidad Productos (y su receta) a los DTO de respuesta.
@Component
public class ProductoMapper {
    public ProductoListadoResponseDTO toListadoDTO(
            Productos producto,
            boolean disponible) {

        return new ProductoListadoResponseDTO(
                producto.getProductoId(),
                producto.getNombreProducto(),
                producto.getCategorias().getNombreCategoria(),
                producto.getPrecioActual(),
                disponible
        );
    }
}
