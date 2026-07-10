package com.coffee.backend.service;

import com.coffee.backend.dto.response.MovimientoStockView;

import java.util.Optional;

/**
 * Pila de deshacer para ingresos de stock (RF-DS-03). Reside en memoria; la fuente de
 * verdad durable es la tabla de movimientos, que la rehidrata al arrancar.
 */
public interface StockUndoService {
    /** Apila un ingreso recién registrado (push). */
    void registrar(MovimientoStockView view);

    /** Desapila el último ingreso a deshacer (pop); {@code empty} si no hay ninguno. */
    Optional<MovimientoStockView> deshacer();
}
