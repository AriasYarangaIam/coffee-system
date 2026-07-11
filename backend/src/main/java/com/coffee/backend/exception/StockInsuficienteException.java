package com.coffee.backend.exception;

// Excepcion: no hay stock suficiente para la venta. Se traduce a HTTP 409.
public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}