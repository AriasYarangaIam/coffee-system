package com.coffee.backend.controller;

import com.coffee.backend.dto.response.ReporteMensualResponseDTO;
import com.coffee.backend.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reportes del panel admin (RF-DS-02). Matriz mensual producto × día. Solo ADMIN.
 */
@RestController
@RequestMapping("/api/admin/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/mensual")
    public ResponseEntity<ReporteMensualResponseDTO> reporteMensual() {
        return ResponseEntity.ok(reporteService.reporteMensual());
    }
}
