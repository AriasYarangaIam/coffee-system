package com.coffee.backend.controller;

import com.coffee.backend.dto.response.PedidoDespachoTokenView;
import com.coffee.backend.service.PedidoDespachoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Cola de despacho (RF-DS-04). Transport por polling: el cliente hace GET para ver el
 * orden FIFO actual. Solo ADMIN.
 */
@RestController
@RequestMapping("/api/admin/despacho")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DespachoController {

    private final PedidoDespachoService pedidoDespachoService;

    @GetMapping
    public ResponseEntity<List<PedidoDespachoTokenView>> listarDespacho() {
        return ResponseEntity.ok(pedidoDespachoService.listarDespacho());
    }
}
