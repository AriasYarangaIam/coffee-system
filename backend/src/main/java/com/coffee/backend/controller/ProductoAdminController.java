package com.coffee.backend.controller;

import com.coffee.backend.dto.request.ProductoRequestDTO;
import com.coffee.backend.dto.response.ProductoAdminResponseDTO;
import com.coffee.backend.service.ProductoAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// CRUD de productos de la carta (ADMIN): crear, editar, eliminar, listar (incluye la receta).
@RestController
@RequestMapping("/api/admin/productos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductoAdminController {

    private final ProductoAdminService productoAdminService;

    @GetMapping
    public ResponseEntity<List<ProductoAdminResponseDTO>> listar() {
        return ResponseEntity.ok(productoAdminService.listar());
    }

    @PostMapping
    public ResponseEntity<ProductoAdminResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoAdminService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoAdminResponseDTO> actualizar(
            @PathVariable Long id, @Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.ok(productoAdminService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoAdminService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
