package com.coffee.backend.dto.response;

import com.coffee.backend.dto.request.CarritoItemDTO;

import java.util.List;

/**
 * Respuesta de las operaciones de la Pila de deshacer del carrito (RF-DS-03).
 * En push, {@code items} es el snapshot recién apilado (eco); en undo, el snapshot
 * anterior restaurado (vacío si no había nada que deshacer). {@code profundidad} es
 * cuántos snapshots quedan → el front habilita/deshabilita "Deshacer".
 */
public record CarritoUndoResponseDTO(
        List<CarritoItemDTO> items,
        int profundidad
) {
}
