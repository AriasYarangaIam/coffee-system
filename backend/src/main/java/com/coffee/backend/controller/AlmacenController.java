package com.coffee.backend.controller;

import com.coffee.backend.dto.response.AlmacenResponseDTO;
import com.coffee.backend.service.AlmacenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/almacenes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AlmacenController {

    private final AlmacenService almacenService;

    @GetMapping
    public ResponseEntity<List<AlmacenResponseDTO>> listar() {
        return ResponseEntity.ok(almacenService.listar());
    }
}
