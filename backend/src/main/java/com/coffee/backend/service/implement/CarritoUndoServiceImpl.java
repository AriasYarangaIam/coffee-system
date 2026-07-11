package com.coffee.backend.service.implement;

import com.coffee.backend.dto.request.CarritoSnapshotDTO;
import com.coffee.backend.service.CarritoUndoService;
import com.coffee.backend.tad.Pila;
import com.coffee.backend.tad.PilaEnlazada;
import org.springframework.stereotype.Service;

import java.util.List;
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

    // ponytail: tope de profundidad por usuario. Un mesero no necesita deshacer 100 pasos
    // del carrito; sin este límite, pushes ilimitados harían crecer la memoria sin freno.
    private static final int MAX_SNAPSHOTS = 100;

    private final Map<String, Pila<CarritoSnapshotDTO>> pilasPorUsuario = new ConcurrentHashMap<>();

    @Override
    public void push(String correo, CarritoSnapshotDTO snapshot) {
        Pila<CarritoSnapshotDTO> pila = pilasPorUsuario.computeIfAbsent(correo, k -> new PilaEnlazada<>());
        if (pila.size() >= MAX_SNAPSHOTS) {
            pila = recortarConservandoRecientes(pila);
            pilasPorUsuario.put(correo, pila);
        }
        pila.push(snapshot);
    }

    // Reconstruye la pila dejando solo los snapshots más recientes (descarta los más
    // antiguos, del fondo). aLista() da el orden tope→base; conservamos los primeros
    // MAX-1 y los re-apilamos base→tope para preservar el orden.
    private static Pila<CarritoSnapshotDTO> recortarConservandoRecientes(Pila<CarritoSnapshotDTO> pila) {
        List<CarritoSnapshotDTO> recientes = pila.aLista().subList(0, MAX_SNAPSHOTS - 1);
        Pila<CarritoSnapshotDTO> nueva = new PilaEnlazada<>();
        for (int i = recientes.size() - 1; i >= 0; i--) {
            nueva.push(recientes.get(i));
        }
        return nueva;
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
