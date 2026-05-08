package com.coffee.backend.controller;

import com.coffee.backend.dto.response.ProductoRecetaResponseDTO;
import com.coffee.backend.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping("/{id}/receta")
    public ResponseEntity<ProductoRecetaResponseDTO> obtenerReceta(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerRecetaDeProducto(id));
    }

}
