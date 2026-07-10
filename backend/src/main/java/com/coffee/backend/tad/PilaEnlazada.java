package com.coffee.backend.tad;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TAD Pila (RF-DS-03, U3). Implementación propia con lista enlazada simple (nodos
 * {@link Nodo}); NO usa {@code java.util.Stack/Deque/LinkedList} como estructura
 * interna — el sílabo exige el TAD propio.
 *
 * <p>El tope es siempre la cabeza de la lista: {@code push}/{@code pop} son O(1).
 * Se usa para deshacer el último ingreso de stock (LIFO).
 */
public final class PilaEnlazada<T> implements Pila<T> {

    private static final class Nodo<T> {
        final T valor;
        Nodo<T> siguiente;

        Nodo(T valor) {
            this.valor = valor;
        }
    }

    private Nodo<T> tope;
    private int tamano;

    @Override
    public void push(T item) {
        Nodo<T> nuevo = new Nodo<>(item);
        nuevo.siguiente = tope;
        tope = nuevo;
        tamano++;
    }

    @Override
    public Optional<T> pop() {
        if (tope == null) return Optional.empty();
        T valor = tope.valor;
        tope = tope.siguiente;
        tamano--;
        return Optional.of(valor);
    }

    @Override
    public Optional<T> peek() {
        return tope == null ? Optional.empty() : Optional.of(tope.valor);
    }

    @Override
    public boolean isEmpty() {
        return tope == null;
    }

    @Override
    public int size() {
        return tamano;
    }

    @Override
    public List<T> aLista() {
        List<T> salida = new ArrayList<>(tamano);
        for (Nodo<T> n = tope; n != null; n = n.siguiente) {
            salida.add(n.valor);
        }
        return salida;
    }
}
