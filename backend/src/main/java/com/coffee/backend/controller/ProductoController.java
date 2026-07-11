package com.coffee.backend.controller;

import com.coffee.backend.dto.response.ProductoListadoResponseDTO;
import com.coffee.backend.dto.response.ProductoRecetaResponseDTO;
import com.coffee.backend.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * Consulta de productos para operar (no el CRUD admin, que vive en
 * {@link ProductoAdminController}). Lo usa el mesero para armar el pedido.
 */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    /** GET /api/productos/{id}/receta → insumos y cantidades de un producto (solo ADMIN). */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/receta")
    public ResponseEntity<ProductoRecetaResponseDTO> obtenerReceta(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerRecetaDeProducto(id));
    }

    /** GET /api/productos → catálogo con disponibilidad, para el POS (MESERO y ADMIN). */
    @PreAuthorize("hasAnyRole('MESERO','ADMIN')")
    @GetMapping
    public ResponseEntity<List<ProductoListadoResponseDTO>> listarProductos() {
        return ResponseEntity.ok(productoService.listarProductos());
    }
}
