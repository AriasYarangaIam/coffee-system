package com.coffee.backend.service;

import com.coffee.backend.service.implement.PedidoUndoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests del servicio de deshacer en memoria.
 * Sin Spring; se instancia el service directamente para poder manipular el TTL.
 */
class PedidoUndoServiceImplTest {

    private PedidoUndoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PedidoUndoServiceImpl();
    }

    @Test
    void pushYundo_respetanLifo() {
        service.push("luis", snapshot("A", 1));
        service.push("luis", snapshot("B", 2));

        Optional<CartSnapshot> undo = service.undo("luis");

        assertThat(undo).isPresent();
        assertThat(undo.get().items().get(0).nombre()).isEqualTo("B");
        assertThat(service.state("luis").canUndo()).isTrue();
    }

    @Test
    void usuarios_aislados() {
        service.push("luis", snapshot("L", 1));
        service.push("ana", snapshot("A", 2));

        assertThat(service.state("luis").depth()).isEqualTo(1);
        assertThat(service.state("ana").depth()).isEqualTo(1);

        service.undo("luis");

        assertThat(service.state("luis").canUndo()).isFalse();
        assertThat(service.state("ana").canUndo()).isTrue();
    }

    @Test
    void cap20_descartaElMasAntiguo() {
        for (int i = 1; i <= 22; i++) {
            service.push("luis", snapshot("item-" + i, i));
        }

        UndoState state = service.state("luis");
        assertThat(state.depth()).isEqualTo(20);

        // Los dos más antiguos debieron descartarse.
        Optional<CartSnapshot> undo = service.undo("luis");
        assertThat(undo).isPresent();
        assertThat(undo.get().items().get(0).nombre()).isEqualTo("item-22");
    }

    @Test
    void ttl30min_evitaSnapshotsViejos() throws Exception {
        service.push("luis", snapshot("A", 1));

        // Simular que la última acceso fue hace 31 minutos.
        Object stack = obtenerStack("luis");
        Field lastAccessedField = stack.getClass().getDeclaredField("lastAccessedAt");
        lastAccessedField.setAccessible(true);
        lastAccessedField.set(stack, Instant.now().minus(Duration.ofMinutes(31)));

        UndoState state = service.state("luis");
        assertThat(state.canUndo()).isFalse();
        assertThat(state.depth()).isZero();
    }

    @Test
    void clearForUser_vaciaLaPila() {
        service.push("luis", snapshot("A", 1));
        service.clearForUser("luis");

        assertThat(service.state("luis").canUndo()).isFalse();
        assertThat(service.undo("luis")).isEmpty();
    }

    @Test
    void push_conUsuarioVacio_lanzaExcepcion() {
        assertThatThrownBy(() -> service.push("", snapshot("A", 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private CartSnapshot snapshot(String nombre, double total) {
        return new CartSnapshot(
                List.of(new CartItemSnapshot(1L, nombre, total, 1L)),
                total
        );
    }

    private Object obtenerStack(String username) throws Exception {
        Field stacksField = PedidoUndoServiceImpl.class.getDeclaredField("stacks");
        stacksField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.concurrent.ConcurrentHashMap<String, Object> stacks =
                (java.util.concurrent.ConcurrentHashMap<String, Object>) stacksField.get(service);
        return stacks.get(username);
    }
}
