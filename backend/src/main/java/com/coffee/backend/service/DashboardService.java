package com.coffee.backend.service;

import com.coffee.backend.dto.response.DashboardResponseDTO;

// Contrato del resumen de KPIs del panel admin.
public interface DashboardService {
    DashboardResponseDTO obtenerResumen();
}
