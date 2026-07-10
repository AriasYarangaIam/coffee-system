package com.coffee.backend.service;

/**
 * Estado de la pila de deshacer de un usuario.
 */
public record UndoState(boolean canUndo, int depth) {
}
