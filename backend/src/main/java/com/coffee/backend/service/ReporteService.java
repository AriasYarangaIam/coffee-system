package com.coffee.backend.service;

import com.coffee.backend.dto.response.BoletaResumenResponseDTO;
import com.coffee.backend.dto.response.ComparativaMensualResponseDTO;
import com.coffee.backend.dto.response.IngresoSemanalResponseDTO;
import com.coffee.backend.dto.response.ReporteMensualResponseDTO;

import java.time.LocalDate;
import java.util.List;

/** Reportes del panel admin basados en estructuras de datos del sílabo, más ingresos/BI. */
public interface ReporteService {

    /** Matriz producto × día (RF-DS-02) del mes en curso. */
    ReporteMensualResponseDTO reporteMensual();

    /** Ingresos de un mes vs el anterior. {@code anio}/{@code mes} null ⇒ mes en curso. */
    ComparativaMensualResponseDTO comparativa(Integer anio, Integer mes);

    /** Ingresos por semana del rango [desde, hasta], con tendencia. */
    IngresoSemanalResponseDTO ingresosSemanales(LocalDate desde, LocalDate hasta);

    /** Boletas emitidas en el rango [desde, hasta] (detalle transaccional). */
    List<BoletaResumenResponseDTO> boletas(LocalDate desde, LocalDate hasta);
}
