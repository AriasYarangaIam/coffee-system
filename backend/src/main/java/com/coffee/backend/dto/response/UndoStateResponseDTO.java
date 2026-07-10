package com.coffee.backend.dto.response;

/**
 * Estado de la pila de deshacer expuesto al cliente.
 */
public record UndoStateResponseDTO(boolean canUndo, int depth) {
}
