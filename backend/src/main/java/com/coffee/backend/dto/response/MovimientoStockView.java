package com.coffee.backend.dto.response;

/**
 * Proyección mínima de un ingreso residente en la Pila de deshacer (RF-DS-03).
 * Solo lo necesario para revertir: qué movimiento marcar, qué saldo tocar y cuánto.
 */
public record MovimientoStockView(
        Long movimientoId,
        Long codigoStock,
        Long cantidad) {
}
