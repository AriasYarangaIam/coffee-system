package com.coffee.backend.tad;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TAD Cola con prioridad (RF-DS-04, U3). Implementación propia con lista enlazada
 * simple (nodos {@link Nodo}); NO usa {@code java.util.Queue/LinkedList/PriorityQueue}
 * como estructura interna — el sílabo exige el TAD propio.
 *
 * <p>Orden: mayor {@code prioridad} primero; dentro de la misma prioridad, FIFO
 * (orden de llegada). El despacho real usa solo prioridad 0 ⇒ FIFO puro; las bandas
 * de prioridad quedan disponibles para reglas futuras.
 */
public final class ColaPrioridad<T> implements Cola<T> {

    private static final class Nodo<T> {
        final T valor;
        final int prioridad;
        Nodo<T> siguiente;

        Nodo(T valor, int prioridad) {
            this.valor = valor;
            this.prioridad = prioridad;
        }
    }

    private Nodo<T> cabeza;
    private int tamano;

    @Override
    public void enqueue(T item) {
        enqueue(item, 0);
    }

    /** Inserta respetando prioridad (desc) y FIFO dentro de la misma prioridad. */
    public void enqueue(T item, int prioridad) {
        Nodo<T> nuevo = new Nodo<>(item, prioridad);
        // Insertar tras el último nodo cuya prioridad sea >= la del nuevo (FIFO estable).
        if (cabeza == null || cabeza.prioridad < prioridad) {
            nuevo.siguiente = cabeza;
            cabeza = nuevo;
        } else {
            Nodo<T> actual = cabeza;
            while (actual.siguiente != null && actual.siguiente.prioridad >= prioridad) {
                actual = actual.siguiente;
            }
            nuevo.siguiente = actual.siguiente;
            actual.siguiente = nuevo;
        }
        tamano++;
    }

    @Override
    public Optional<T> dequeue() {
        if (cabeza == null) return Optional.empty();
        T valor = cabeza.valor;
        cabeza = cabeza.siguiente;
        tamano--;
        return Optional.of(valor);
    }

    @Override
    public Optional<T> peek() {
        return cabeza == null ? Optional.empty() : Optional.of(cabeza.valor);
    }

    @Override
    public boolean isEmpty() {
        return cabeza == null;
    }

    @Override
    public int size() {
        return tamano;
    }

    /** Copia (snapshot) del contenido en orden de cola, sin drenar la estructura. */
    public List<T> aLista() {
        List<T> salida = new ArrayList<>(tamano);
        for (Nodo<T> n = cabeza; n != null; n = n.siguiente) {
            salida.add(n.valor);
        }
        return salida;
    }
}
