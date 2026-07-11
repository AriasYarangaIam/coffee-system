package com.coffee.backend.service.implement;

import com.coffee.backend.dto.request.CarritoSnapshotDTO;
import com.coffee.backend.service.CarritoUndoService;
import com.coffee.backend.tad.Pila;
import com.coffee.backend.tad.PilaEnlazada;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pila de deshacer del carrito, en memoria (RF-DS-03 en Java). El {@code @Service} es
 * singleton ⇒ una {@link PilaEnlazada} por mesero, guardada en un mapa por correo.
 *
 * <p>Estado en memoria de UNA instancia (multi-instancia fuera de alcance, como las otras
 * estructuras). El mapa es concurrente; se asume un mesero = una sesión (sin acceso
 * concurrente a la misma pila).
 */
@Service
public class CarritoUndoServiceImpl implements CarritoUndoService {

    private final Map<String, Pila<CarritoSnapshotDTO>> pilasPorUsuario = new ConcurrentHashMap<>();

    @Override
    public void push(String correo, CarritoSnapshotDTO snapshot) {
        pilasPorUsuario.computeIfAbsent(correo, k -> new PilaEnlazada<>()).push(snapshot);
    }

    @Override
    public Optional<CarritoSnapshotDTO> undo(String correo) {
        Pila<CarritoSnapshotDTO> pila = pilasPorUsuario.get(correo);
        return pila == null ? Optional.empty() : pila.pop();
    }

    @Override
    public void limpiar(String correo) {
        pilasPorUsuario.remove(correo);
    }

    @Override
    public int profundidad(String correo) {
        Pila<CarritoSnapshotDTO> pila = pilasPorUsuario.get(correo);
        return pila == null ? 0 : pila.size();
    }
}
