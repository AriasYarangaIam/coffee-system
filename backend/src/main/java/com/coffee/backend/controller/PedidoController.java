package com.coffee.backend.controller;

import com.coffee.backend.dto.request.PedidoRequestDTO;
import com.coffee.backend.dto.response.PedidoResponseDTO;
import com.coffee.backend.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> registrarPedido(
            @Valid @RequestBody PedidoRequestDTO dto) {
        // Por ahora puse  puse un ocrreo como para testear "mesero@test.com"
        // hasta que llegue el JWT ahi si utilizare userDetails.getUsername()
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoService.registrarPedido(dto, "mesero@test.com"));
    }
}
