package com.coffee.backend.tad;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit test puro del TAD {@link ColaPrioridad} (RF-DS-04). Sin Spring, sin BD. */
class ColaPrioridadTest {

    @Test
    void enqueueSinPrioridad_esFifo() {
        ColaPrioridad<String> cola = new ColaPrioridad<>();
        cola.enqueue("A");
        cola.enqueue("B");
        cola.enqueue("C");

        assertThat(cola.size()).isEqualTo(3);
        assertThat(cola.aLista()).containsExactly("A", "B", "C");
    }

    @Test
    void peek_leeCabezaSinRemover() {
        ColaPrioridad<String> cola = new ColaPrioridad<>();
        cola.enqueue("A");
        cola.enqueue("B");

        assertThat(cola.peek()).contains("A");
        assertThat(cola.size()).isEqualTo(2); // peek no muta
        assertThat(cola.aLista()).containsExactly("A", "B");
    }

    @Test
    void dequeue_remueveEnOrdenFifo() {
        ColaPrioridad<String> cola = new ColaPrioridad<>();
        cola.enqueue("A");
        cola.enqueue("B");

        assertThat(cola.dequeue()).contains("A");
        assertThat(cola.dequeue()).contains("B");
        assertThat(cola.isEmpty()).isTrue();
    }

    @Test
    void colaVacia_peekYDequeueDevuelvenEmpty() {
        ColaPrioridad<String> cola = new ColaPrioridad<>();

        assertThat(cola.isEmpty()).isTrue();
        assertThat(cola.size()).isZero();
        assertThat(cola.peek()).isEqualTo(Optional.empty());
        assertThat(cola.dequeue()).isEqualTo(Optional.empty());
    }

    @Test
    void prioridadMayor_seAtiendePrimero_yFifoDentroDeLaMismaBanda() {
        ColaPrioridad<String> cola = new ColaPrioridad<>();
        cola.enqueue("normal-1", 0);
        cola.enqueue("normal-2", 0);
        cola.enqueue("urgente-1", 5);
        cola.enqueue("urgente-2", 5);

        // Banda 5 antes que banda 0; FIFO dentro de cada banda.
        assertThat(cola.aLista())
                .containsExactly("urgente-1", "urgente-2", "normal-1", "normal-2");
    }

    @Test
    void aLista_esCopia_noDrenaLaCola() {
        ColaPrioridad<Integer> cola = new ColaPrioridad<>();
        cola.enqueue(1);
        cola.enqueue(2);

        List<Integer> copia = cola.aLista();
        copia.clear(); // mutar la copia no afecta la cola

        assertThat(cola.size()).isEqualTo(2);
    }
}
