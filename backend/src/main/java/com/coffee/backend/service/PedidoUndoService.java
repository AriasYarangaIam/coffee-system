package com.coffee.backend.service;

import java.util.Optional;

/**
 * Servicio de pila de deshacer para carritos de pedidos en progreso.
 * Una pila por usuario autenticado (correo); los datos son transientes en memoria.
 */
public interface PedidoUndoService {

    /** Guarda una instantánea en la cima de la pila del usuario. */
    void push(String username, CartSnapshot snapshot);

    /** Saca y devuelve la instantánea de la cima; vacía si no hay nada que deshacer. */
    Optional<CartSnapshot> undo(String username);

    /** Devuelve el estado actual de la pila del usuario. */
    UndoState state(String username);

    /** Vacía la pila del usuario (por ejemplo, tras confirmar un pedido). */
    void clearForUser(String username);
}
