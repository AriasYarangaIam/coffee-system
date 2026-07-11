package com.coffee.backend.controller;

import com.coffee.backend.dto.request.InsumoRequestDTO;
import com.coffee.backend.dto.response.InsumoResponseDTO;
import com.coffee.backend.service.InsumoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// CRUD de insumos / materia prima (ADMIN).
@RestController
@RequestMapping("/api/admin/insumos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InsumoController {

    private final InsumoService insumoService;

    @GetMapping
    public ResponseEntity<List<InsumoResponseDTO>> listar() {
        return ResponseEntity.ok(insumoService.listar());
    }

    @PostMapping
    public ResponseEntity<InsumoResponseDTO> crear(@Valid @RequestBody InsumoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(insumoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InsumoResponseDTO> actualizar(
            @PathVariable Long id, @Valid @RequestBody InsumoRequestDTO dto) {
        return ResponseEntity.ok(insumoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        insumoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
