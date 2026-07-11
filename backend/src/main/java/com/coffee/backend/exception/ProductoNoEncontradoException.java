package com.coffee.backend.exception;

// Excepcion: producto inexistente. GlobalExceptionHandler la traduce a HTTP 404.
public class ProductoNoEncontradoException extends RuntimeException {
    public ProductoNoEncontradoException(String message) {
        super(message);
    }
}
