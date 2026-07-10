package com.coffee.backend.tad;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit test puro del TAD {@link PilaEnlazada} (RF-DS-03). Sin Spring, sin BD. */
class PilaEnlazadaTest {

    @Test
    void push_esLifo() {
        PilaEnlazada<String> pila = new PilaEnlazada<>();
        pila.push("A");
        pila.push("B");
        pila.push("C");

        assertThat(pila.size()).isEqualTo(3);
        // Snapshot tope→base: el último apilado va primero.
        assertThat(pila.aLista()).containsExactly("C", "B", "A");
    }

    @Test
    void peek_leeTopeSinRemover() {
        PilaEnlazada<String> pila = new PilaEnlazada<>();
        pila.push("A");
        pila.push("B");

        assertThat(pila.peek()).contains("B");
        assertThat(pila.size()).isEqualTo(2); // peek no muta
        assertThat(pila.aLista()).containsExactly("B", "A");
    }

    @Test
    void pop_remueveEnOrdenLifo() {
        PilaEnlazada<String> pila = new PilaEnlazada<>();
        pila.push("A");
        pila.push("B");

        assertThat(pila.pop()).contains("B");
        assertThat(pila.pop()).contains("A");
        assertThat(pila.isEmpty()).isTrue();
    }

    @Test
    void pilaVacia_peekYPopDevuelvenEmpty() {
        PilaEnlazada<String> pila = new PilaEnlazada<>();

        assertThat(pila.isEmpty()).isTrue();
        assertThat(pila.size()).isZero();
        assertThat(pila.peek()).isEqualTo(Optional.empty());
        assertThat(pila.pop()).isEqualTo(Optional.empty());
    }

    @Test
    void aLista_esCopia_noDrenaLaPila() {
        PilaEnlazada<Integer> pila = new PilaEnlazada<>();
        pila.push(1);
        pila.push(2);

        List<Integer> copia = pila.aLista();
        copia.clear(); // mutar la copia no afecta la pila

        assertThat(pila.size()).isEqualTo(2);
    }
}
