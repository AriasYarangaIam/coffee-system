package com.coffee.backend.dto.response;

// Respuesta de gestión de productos (ADMIN): incluye id de categoría y su nombre.
public record ProductoAdminResponseDTO(
        Long productoId,
        String nombreProducto,
        Double precioActual,
        Long categoriaId,
        String nombreCategoria
) {
}
