package com.coffee.backend.controller;

import com.coffee.backend.dto.request.CartItemSnapshotDTO;
import com.coffee.backend.dto.request.CartSnapshotRequestDTO;
import com.coffee.backend.dto.response.UndoSnapshotResponseDTO;
import com.coffee.backend.dto.response.UndoStateResponseDTO;
import com.coffee.backend.service.CartItemSnapshot;
import com.coffee.backend.service.CartSnapshot;
import com.coffee.backend.service.PedidoUndoService;
import com.coffee.backend.service.UndoState;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos/undo")
@RequiredArgsConstructor
public class PedidoUndoController {

    private final PedidoUndoService pedidoUndoService;

    @PreAuthorize("hasRole('MESERO')")
    @PostMapping("/push")
    public ResponseEntity<Void> push(
            @Valid @RequestBody CartSnapshotRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        pedidoUndoService.push(userDetails.getUsername(), toDomain(dto));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("hasRole('MESERO')")
    @PostMapping
    public ResponseEntity<UndoSnapshotResponseDTO> undo(
            @AuthenticationPrincipal UserDetails userDetails) {
        CartSnapshot snapshot = pedidoUndoService.undo(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("No hay acciones para deshacer"));
        return ResponseEntity.ok(new UndoSnapshotResponseDTO(snapshot));
    }

    @PreAuthorize("hasRole('MESERO')")
    @GetMapping
    public ResponseEntity<UndoStateResponseDTO> state(
            @AuthenticationPrincipal UserDetails userDetails) {
        UndoState state = pedidoUndoService.state(userDetails.getUsername());
        return ResponseEntity.ok(new UndoStateResponseDTO(state.canUndo(), state.depth()));
    }

    @PreAuthorize("hasRole('MESERO')")
    @DeleteMapping
    public ResponseEntity<Void> clear(
            @AuthenticationPrincipal UserDetails userDetails) {
        pedidoUndoService.clearForUser(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    private CartSnapshot toDomain(CartSnapshotRequestDTO dto) {
        List<CartItemSnapshot> items = dto.items().stream()
                .map(this::toDomain)
                .toList();
        return new CartSnapshot(items, dto.total());
    }

    private CartItemSnapshot toDomain(CartItemSnapshotDTO dto) {
        return new CartItemSnapshot(
                dto.productoId(),
                dto.nombre(),
                dto.precio(),
                dto.cantidad()
        );
    }
}
