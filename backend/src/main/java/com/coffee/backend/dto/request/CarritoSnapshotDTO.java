package com.coffee.backend.dto.request;

import java.util.List;

// Estado completo del carrito en un instante. La Pila de deshacer apila estos snapshots.
public record CarritoSnapshotDTO(
        List<CarritoItemDTO> items
) {
}
