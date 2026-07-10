package com.coffee.backend.dto.response;

import com.coffee.backend.service.CartSnapshot;

/**
 * Instantánea recuperada de la pila de deshacer.
 */
public record UndoSnapshotResponseDTO(CartSnapshot snapshot) {
}
