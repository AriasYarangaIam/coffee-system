package com.coffee.backend.controller;

import com.coffee.backend.dto.response.DashboardResponseDTO;
import com.coffee.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Expone los KPIs del panel admin: ventas y pedidos del dia, producto estrella, stock bajo.
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> obtenerResumen() {
        return ResponseEntity.ok(dashboardService.obtenerResumen());
    }
}
