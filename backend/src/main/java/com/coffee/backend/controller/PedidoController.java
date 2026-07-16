package com.coffee.backend.controller;

import com.coffee.backend.dto.request.CarritoSnapshotDTO;
import com.coffee.backend.dto.request.PedidoRequestDTO;
import com.coffee.backend.dto.response.BoletaResponseDTO;
import com.coffee.backend.dto.response.CarritoUndoResponseDTO;
import com.coffee.backend.dto.response.MisMetricasResponseDTO;
import com.coffee.backend.dto.response.PedidoDespachoTokenView;
import com.coffee.backend.dto.response.PedidoListadoResponseDTO;
import com.coffee.backend.dto.response.PedidoResponseDTO;
import com.coffee.backend.service.CarritoUndoService;
import com.coffee.backend.service.PedidoDespachoService;
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
    private final CarritoUndoService carritoUndoService;
    private final PedidoDespachoService pedidoDespachoService;

    @PreAuthorize("hasRole('MESERO')")
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> registrarPedido(
            @Valid @RequestBody PedidoRequestDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        // El usuario se deriva del JWT (correo = username).
        String correo = userDetails.getUsername();
        PedidoResponseDTO pedido = pedidoService.registrarPedido(dto, correo);
        carritoUndoService.limpiar(correo); // el carrito se cerró: vaciar su pila de deshacer
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @PreAuthorize("hasRole('MESERO')")
    @GetMapping
    public ResponseEntity<List<PedidoListadoResponseDTO>> listarPedidos(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pedidoService.listarPedidosDeMesero(userDetails.getUsername()));
    }

    // --- Cola de despacho del mesero (RF-DS-04) ---

    // Cabeza actual de la cola global (peek). El front la usa para habilitar "Entregar"
    // solo en el ticket que está primero. Devuelve 200 con null si la cola está vacía.
    @PreAuthorize("hasRole('MESERO')")
    @GetMapping("/despacho/siguiente")
    public ResponseEntity<PedidoDespachoTokenView> siguienteDespacho() {
        return ResponseEntity.ok(pedidoDespachoService.siguienteDespacho().orElse(null));
    }

    // Entrega (dequeue) la cabeza de la cola si es de este mesero. Devuelve la nueva cabeza.
    @PreAuthorize("hasRole('MESERO')")
    @PostMapping("/despacho/entregar")
    public ResponseEntity<PedidoDespachoTokenView> entregarSiguiente(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pedidoService.entregarSiguienteDespacho(userDetails.getUsername()));
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

    // --- Pila de deshacer del carrito (RF-DS-03, en Java) ---

    // Apila el estado del carrito antes de una mutación.
    @PreAuthorize("hasRole('MESERO')")
    @PostMapping("/carrito/push")
    public ResponseEntity<CarritoUndoResponseDTO> pushCarrito(
            @RequestBody CarritoSnapshotDTO snapshot, @AuthenticationPrincipal UserDetails userDetails) {
        String correo = userDetails.getUsername();
        carritoUndoService.push(correo, snapshot);
        return ResponseEntity.ok(new CarritoUndoResponseDTO(
                snapshot.items(), carritoUndoService.profundidad(correo)));
    }

    // Desapila y devuelve el snapshot anterior (vacío si no hay nada que deshacer).
    @PreAuthorize("hasRole('MESERO')")
    @PostMapping("/carrito/undo")
    public ResponseEntity<CarritoUndoResponseDTO> undoCarrito(
            @AuthenticationPrincipal UserDetails userDetails) {
        String correo = userDetails.getUsername();
        List<com.coffee.backend.dto.request.CarritoItemDTO> items = carritoUndoService.undo(correo)
                .map(CarritoSnapshotDTO::items)
                .orElseGet(List::of);
        return ResponseEntity.ok(new CarritoUndoResponseDTO(items, carritoUndoService.profundidad(correo)));
    }
}
