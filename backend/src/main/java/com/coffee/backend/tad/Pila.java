package com.coffee.backend.tad;

import java.util.List;
import java.util.Optional;

/**
 * TAD Pila (LIFO). Estructura de datos del sílabo (RF-DS-03, U3).
 *
 * <p>Contrato propio — la implementación NO se respalda en {@code java.util.Stack}
 * ni {@code Deque}.
 */
public interface Pila<T> {
    /** Apila en el tope (LIFO). */
    void push(T item);

    /** Desapila el tope; {@code empty} si la pila está vacía. */
    Optional<T> pop();

    /** Lee el tope sin removerlo; {@code empty} si está vacía. */
    Optional<T> peek();

    boolean isEmpty();

    int size();

    /** Copia (snapshot) del contenido tope→base, sin drenar la estructura. */
    List<T> aLista();
}
