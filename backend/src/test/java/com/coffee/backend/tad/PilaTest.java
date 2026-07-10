package com.coffee.backend.tad;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Unit test puro del TAD {@link Pila} (RF-DS-03). Sin Spring, sin BD. */
class PilaTest {

    @Test
    void pushYPop_respetanLifo() {
        Pila<String> pila = new Pila<>(5);
        pila.push("A");
        pila.push("B");
        pila.push("C");

        assertThat(pila.size()).isEqualTo(3);
        assertThat(pila.pop()).contains("C");
        assertThat(pila.pop()).contains("B");
        assertThat(pila.pop()).contains("A");
        assertThat(pila.isEmpty()).isTrue();
    }

    @Test
    void peek_leeCimaSinRemover() {
        Pila<Integer> pila = new Pila<>(5);
        pila.push(1);
        pila.push(2);

        assertThat(pila.peek()).contains(2);
        assertThat(pila.size()).isEqualTo(2);
        assertThat(pila.pop()).contains(2);
    }

    @Test
    void pilaVacia_popYpeekDevuelvenEmpty() {
        Pila<String> pila = new Pila<>(3);

        assertThat(pila.isEmpty()).isTrue();
        assertThat(pila.size()).isZero();
        assertThat(pila.pop()).isEqualTo(Optional.empty());
        assertThat(pila.peek()).isEqualTo(Optional.empty());
    }

    @Test
    void capacidadCero_lanzaExcepcion() {
        assertThatThrownBy(() -> new Pila<>(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void overflow_descartaElMasAntiguo() {
        Pila<String> pila = new Pila<>(3);
        pila.push("A");
        pila.push("B");
        pila.push("C");
        pila.push("D");

        assertThat(pila.size()).isEqualTo(3);
        assertThat(pila.pop()).contains("D");
        assertThat(pila.pop()).contains("C");
        assertThat(pila.pop()).contains("B");
        assertThat(pila.isEmpty()).isTrue();
    }

    @Test
    void clear_vaciaLaPila() {
        Pila<String> pila = new Pila<>(5);
        pila.push("A");
        pila.push("B");

        pila.clear();

        assertThat(pila.isEmpty()).isTrue();
        assertThat(pila.size()).isZero();
    }
}
