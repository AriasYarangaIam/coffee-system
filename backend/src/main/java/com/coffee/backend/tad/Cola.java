package com.coffee.backend.tad;

import java.util.Optional;

/**
 * TAD Cola (FIFO). Estructura de datos del sílabo (RF-DS-04, U3).
 *
 * <p>Contrato propio — la implementación NO se respalda en {@code java.util.Queue}.
 */
public interface Cola<T> {
    /** Encola al final (FIFO, prioridad por defecto). */
    void enqueue(T item);

    /** Desencola la cabeza; {@code empty} si la cola está vacía. */
    Optional<T> dequeue();

    /** Lee la cabeza sin removerla; {@code empty} si está vacía. */
    Optional<T> peek();

    boolean isEmpty();

    int size();
}
