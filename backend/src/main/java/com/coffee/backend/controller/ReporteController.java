package com.coffee.backend.controller;

import com.coffee.backend.dto.response.BoletaResumenResponseDTO;
import com.coffee.backend.dto.response.ComparativaMensualResponseDTO;
import com.coffee.backend.dto.response.IngresoSemanalResponseDTO;
import com.coffee.backend.dto.response.ReporteMensualResponseDTO;
import com.coffee.backend.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Reportes e ingresos del panel admin. Matriz mensual producto × día (RF-DS-02) más el
 * módulo de ingresos (comparativa mensual, serie semanal, detalle de boletas). Solo ADMIN.
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

    // Ingresos del mes vs el anterior. Sin params ⇒ mes en curso.
    @GetMapping("/ingresos/comparativa")
    public ResponseEntity<ComparativaMensualResponseDTO> comparativa(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes) {
        return ResponseEntity.ok(reporteService.comparativa(anio, mes));
    }

    // Ingresos por semana del rango [desde, hasta].
    @GetMapping("/ingresos/semanal")
    public ResponseEntity<IngresoSemanalResponseDTO> ingresosSemanales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(reporteService.ingresosSemanales(desde, hasta));
    }

    // Detalle transaccional: boletas emitidas en el rango [desde, hasta].
    @GetMapping("/boletas")
    public ResponseEntity<List<BoletaResumenResponseDTO>> boletas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(reporteService.boletas(desde, hasta));
    }
}
