package com.coffee.backend.service;

import com.coffee.backend.dto.request.CarritoSnapshotDTO;

import java.util.Optional;

/**
 * Pila de deshacer del carrito del mesero (RF-DS-03, en Java). Una pila por usuario,
 * en memoria. La identidad es el correo del JWT.
 */
public interface CarritoUndoService {
    /** Apila el estado del carrito antes de una mutación (push). */
    void push(String correo, CarritoSnapshotDTO snapshot);

    /** Desapila el último snapshot para restaurarlo (pop); {@code empty} si no hay. */
    Optional<CarritoSnapshotDTO> undo(String correo);

    /** Vacía la pila del usuario (al confirmar el pedido). */
    void limpiar(String correo);

    /** Cuántos snapshots quedan apilados para ese usuario. */
    int profundidad(String correo);
}
