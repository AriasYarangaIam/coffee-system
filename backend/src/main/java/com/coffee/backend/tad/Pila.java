package com.coffee.backend.tad;

import java.util.Optional;

/**
 * TAD Pila (LIFO) con capacidad acotada (RF-DS-03, U3).
 * Implementación propia con nodos enlazados; NO usa {@code java.util.Stack}
 * ni {@code ArrayDeque} como estructura interna.
 *
 * <p>Si se alcanza la capacidad máxima, el push descarta el elemento más
 * antiguo (fondo) antes de insertar el nuevo en la cima.</p>
 */
public final class Pila<T> {

    private static final class Nodo<T> {
        T valor;
        Nodo<T> siguiente;

        Nodo(T valor) {
            this.valor = valor;
        }
    }

    private Nodo<T> cima;
    private Nodo<T> fondo;
    private int tamano;
    private final int capacidad;

    public Pila(int capacidad) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor a cero");
        }
        this.capacidad = capacidad;
    }

    /** Inserta en la cima; descarta el fondo si se excede la capacidad. */
    public void push(T item) {
        Nodo<T> nuevo = new Nodo<>(item);
        if (cima == null) {
            cima = nuevo;
            fondo = nuevo;
        } else {
            nuevo.siguiente = cima;
            cima = nuevo;
        }
        tamano++;

        if (tamano > capacidad) {
            removerFondo();
        }
    }

    /** Remueve y devuelve la cima; {@code empty} si la pila está vacía. */
    public Optional<T> pop() {
        if (cima == null) return Optional.empty();
        T valor = cima.valor;
        cima = cima.siguiente;
        if (cima == null) {
            fondo = null;
        }
        tamano--;
        return Optional.of(valor);
    }

    /** Lee la cima sin removerla; {@code empty} si está vacía. */
    public Optional<T> peek() {
        return cima == null ? Optional.empty() : Optional.of(cima.valor);
    }

    public boolean isEmpty() {
        return cima == null;
    }

    public int size() {
        return tamano;
    }

    public int capacidad() {
        return capacidad;
    }

    /** Limpia todos los elementos. */
    public void clear() {
        cima = null;
        fondo = null;
        tamano = 0;
    }

    private void removerFondo() {
        if (cima == null || cima == fondo) {
            clear();
            return;
        }
        Nodo<T> actual = cima;
        while (actual.siguiente != fondo) {
            actual = actual.siguiente;
        }
        actual.siguiente = null;
        fondo = actual;
        tamano--;
    }
}
