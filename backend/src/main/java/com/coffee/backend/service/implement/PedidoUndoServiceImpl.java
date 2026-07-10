package com.coffee.backend.service.implement;

import com.coffee.backend.service.CartSnapshot;
import com.coffee.backend.service.PedidoUndoService;
import com.coffee.backend.service.UndoState;
import com.coffee.backend.tad.Pila;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria del servicio de deshacer.
 *
 * <ul>
 *   <li>Una pila por usuario, indexada por correo (username del JWT).</li>
 *   <li>Capacidad máxima: 20 instantáneas.</li>
 *   <li>TTL: 30 minutos sin acceso; el próximo acceso evita la pila.</li>
 *   <li>Concurrencia: serialización por usuario mediante sincronización sobre
 *       el objeto {@link UserUndoStack}.</li>
 * </ul>
 */
@Slf4j
@Service
public class PedidoUndoServiceImpl implements PedidoUndoService {

    static final int CAPACIDAD_PILA = 20;
    static final Duration TTL = Duration.ofMinutes(30);

    private final ConcurrentHashMap<String, UserUndoStack> stacks = new ConcurrentHashMap<>();

    @Override
    public void push(String username, CartSnapshot snapshot) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El usuario no puede estar vacío");
        }
        UserUndoStack stack = obtenerStack(username);
        synchronized (stack) {
            evitarSiExpirado(stack);
            stack.pila.push(snapshot);
            stack.lastAccessedAt = Instant.now();
            log.info("Push undo snapshot para usuario={}, profundidad={}", username, stack.pila.size());
        }
    }

    @Override
    public Optional<CartSnapshot> undo(String username) {
        UserUndoStack stack = obtenerStack(username);
        synchronized (stack) {
            evitarSiExpirado(stack);
            stack.lastAccessedAt = Instant.now();
            Optional<CartSnapshot> snapshot = stack.pila.pop();
            if (snapshot.isPresent()) {
                log.info("Undo snapshot para usuario={}, profundidad restante={}", username, stack.pila.size());
            } else {
                log.warn("Intento de undo con pila vacía para usuario={}", username);
            }
            return snapshot;
        }
    }

    @Override
    public UndoState state(String username) {
        UserUndoStack stack = obtenerStack(username);
        synchronized (stack) {
            evitarSiExpirado(stack);
            stack.lastAccessedAt = Instant.now();
            return new UndoState(!stack.pila.isEmpty(), stack.pila.size());
        }
    }

    @Override
    public void clearForUser(String username) {
        UserUndoStack stack = stacks.get(username);
        if (stack == null) return;
        synchronized (stack) {
            stack.pila.clear();
            stack.lastAccessedAt = Instant.now();
            log.info("Undo stack limpiado para usuario={}", username);
        }
    }

    private UserUndoStack obtenerStack(String username) {
        return stacks.computeIfAbsent(username, k -> new UserUndoStack());
    }

    private void evitarSiExpirado(UserUndoStack stack) {
        if (stack.pila.isEmpty()) return;
        Instant ahora = Instant.now();
        if (Duration.between(stack.lastAccessedAt, ahora).compareTo(TTL) > 0) {
            log.info("Pila de undo expirada; se limpia antes de continuar");
            stack.pila.clear();
        }
    }

    static final class UserUndoStack {
        final Pila<CartSnapshot> pila = new Pila<>(CAPACIDAD_PILA);
        Instant lastAccessedAt = Instant.now();
    }
}
