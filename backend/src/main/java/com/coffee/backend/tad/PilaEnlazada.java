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

    // Nodo de la lista enlazada: guarda un valor y el enlace al nodo de abajo en la pila.
    private static final class Nodo<T> {
        final T valor;
        Nodo<T> siguiente;

        Nodo(T valor) {
            this.valor = valor;
        }
    }

    private Nodo<T> tope;   // cabeza de la lista = elemento en el tope de la pila
    private int tamano;     // cantidad de elementos, para size() en O(1)

    /**
     * Apila (LIFO): crea un nodo nuevo, lo enlaza delante del tope actual y lo vuelve el
     * nuevo tope. O(1) — no recorre la lista.
     */
    @Override
    public void push(T item) {
        Nodo<T> nuevo = new Nodo<>(item);
        nuevo.siguiente = tope; // el nuevo apunta al que era tope
        tope = nuevo;           // y pasa a ser el tope
        tamano++;
    }

    /**
     * Desapila (LIFO): quita y devuelve el elemento del tope, moviendo el tope al nodo de
     * abajo. {@code Optional.empty()} si la pila está vacía. O(1).
     */
    @Override
    public Optional<T> pop() {
        if (tope == null) return Optional.empty();
        T valor = tope.valor;
        tope = tope.siguiente; // descarta el nodo del tope
        tamano--;
        return Optional.of(valor);
    }

    /** Mira el tope sin quitarlo; {@code empty} si está vacía. O(1). */
    @Override
    public Optional<T> peek() {
        return tope == null ? Optional.empty() : Optional.of(tope.valor);
    }

    /** True si no hay elementos. */
    @Override
    public boolean isEmpty() {
        return tope == null;
    }

    /** Cantidad de elementos apilados (contador mantenido en push/pop). */
    @Override
    public int size() {
        return tamano;
    }

    /**
     * Copia el contenido a una lista en orden tope→base, recorriendo los nodos sin
     * modificar la pila. O(n). Útil para inspeccionar/rehidratar sin drenar.
     */
    @Override
    public List<T> aLista() {
        List<T> salida = new ArrayList<>(tamano);
        for (Nodo<T> n = tope; n != null; n = n.siguiente) {
            salida.add(n.valor);
        }
        return salida;
    }
}
