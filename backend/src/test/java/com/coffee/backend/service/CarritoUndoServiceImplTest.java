package com.coffee.backend.service;

import com.coffee.backend.dto.request.CarritoItemDTO;
import com.coffee.backend.dto.request.CarritoSnapshotDTO;
import com.coffee.backend.service.implement.CarritoUndoServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pila de deshacer del carrito (RF-DS-03 en Java): LIFO por usuario, aislamiento entre
 * meseros y limpieza al confirmar. Sin Spring ni BD.
 */
class CarritoUndoServiceImplTest {

    private final CarritoUndoServiceImpl service = new CarritoUndoServiceImpl();

    @Test
    void undo_devuelveSnapshotsEnOrdenLifo() {
        service.push("m@x.com", snapshot("Café", 1));
        service.push("m@x.com", snapshot("Café", 2));

        assertThat(service.profundidad("m@x.com")).isEqualTo(2);
        // El último apilado sale primero.
        assertThat(service.undo("m@x.com")).get()
                .extracting(s -> s.items().get(0).cantidad()).isEqualTo(2L);
        assertThat(service.undo("m@x.com")).get()
                .extracting(s -> s.items().get(0).cantidad()).isEqualTo(1L);
        assertThat(service.undo("m@x.com")).isEmpty(); // pila vacía
    }

    @Test
    void pilas_sonIndependientesPorUsuario() {
        service.push("ana@x.com", snapshot("Latte", 1));
        service.push("beto@x.com", snapshot("Té", 5));

        assertThat(service.profundidad("ana@x.com")).isEqualTo(1);
        assertThat(service.profundidad("beto@x.com")).isEqualTo(1);
        // Deshacer el de Ana no toca el de Beto.
        service.undo("ana@x.com");
        assertThat(service.profundidad("ana@x.com")).isZero();
        assertThat(service.profundidad("beto@x.com")).isEqualTo(1);
    }

    @Test
    void limpiar_vaciaSoloAlUsuarioDado() {
        service.push("ana@x.com", snapshot("Latte", 1));
        service.push("beto@x.com", snapshot("Té", 5));

        service.limpiar("ana@x.com");

        assertThat(service.profundidad("ana@x.com")).isZero();
        assertThat(service.profundidad("beto@x.com")).isEqualTo(1);
    }

    private CarritoSnapshotDTO snapshot(String nombre, long cantidad) {
        return new CarritoSnapshotDTO(List.of(
                new CarritoItemDTO(1L, nombre, new BigDecimal("10.00"), cantidad)));
    }
}
