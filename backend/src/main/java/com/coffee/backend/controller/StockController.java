package com.coffee.backend.controller;

import com.coffee.backend.dto.request.StockRequestDTO;
import com.coffee.backend.dto.response.MovimientoStockResponseDTO;
import com.coffee.backend.dto.response.StockResponseDTO;
import com.coffee.backend.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stocks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StockController {

    private final StockService stockService;

    @GetMapping
    public ResponseEntity<List<StockResponseDTO>> listar() {
        return ResponseEntity.ok(stockService.listar());
    }

    @PostMapping
    public ResponseEntity<StockResponseDTO> registrarIngreso(@Valid @RequestBody StockRequestDTO dto,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockService.registrarIngreso(dto, userDetails.getUsername()));
    }

    // "Últimos Ingresos" del panel de stock.
    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoStockResponseDTO>> listarMovimientos() {
        return ResponseEntity.ok(stockService.listarMovimientos());
    }

    // Deshace el último ingreso registrado (LIFO).
    @PostMapping("/deshacer")
    public ResponseEntity<StockResponseDTO> deshacer() {
        return ResponseEntity.ok(stockService.deshacerUltimoIngreso());
    }
}
