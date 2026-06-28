package com.coffee.backend.service;

import com.coffee.backend.dto.response.ReporteMensualResponseDTO;

/** Reportes del panel admin basados en estructuras de datos del sílabo. */
public interface ReporteService {

    /** Matriz producto × día (RF-DS-02) del mes en curso. */
    ReporteMensualResponseDTO reporteMensual();
}
