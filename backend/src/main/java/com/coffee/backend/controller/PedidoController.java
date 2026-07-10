package com.coffee.backend.controller;

import com.coffee.backend.dto.request.PedidoRequestDTO;
import com.coffee.backend.dto.response.BoletaResponseDTO;
import com.coffee.backend.dto.response.MisMetricasResponseDTO;
import com.coffee.backend.dto.response.PedidoListadoResponseDTO;
import com.coffee.backend.dto.response.PedidoResponseDTO;
import com.coffee.backend.service.PedidoService;
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
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PreAuthorize("hasRole('MESERO')")
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> registrarPedido(
            @Valid @RequestBody PedidoRequestDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        // El usuario se deriva del JWT (correo = username).
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoService.registrarPedido(dto, userDetails.getUsername()));
    }

    @PreAuthorize("hasRole('MESERO')")
    @GetMapping
    public ResponseEntity<List<PedidoListadoResponseDTO>> listarPedidos(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pedidoService.listarPedidosDeMesero(userDetails.getUsername()));
    }

    @PreAuthorize("hasRole('MESERO')")
    @GetMapping("/{id}/boleta")
    public ResponseEntity<BoletaResponseDTO> obtenerBoleta(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerBoleta(id));
    }

    // Métricas del turno del mesero autenticado (día en curso).
    @PreAuthorize("hasRole('MESERO')")
    @GetMapping("/mis-metricas")
    public ResponseEntity<MisMetricasResponseDTO> misMetricas(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pedidoService.misMetricas(userDetails.getUsername()));
    }
}
