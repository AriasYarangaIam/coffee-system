package com.coffee.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<Map<String, String>> handleStockInsuficiente(
            StockInsuficienteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    // 404 para recursos inexistentes (CRUD admin y producto/receta).
    @ExceptionHandler({RecursoNoEncontradoException.class, ProductoNoEncontradoException.class})
    public ResponseEntity<Map<String, String>> handleNoEncontrado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    // 400 para datos inválidos de negocio (ej.: insumo repetido en una receta).
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleArgumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    // 409 para estados inválidos de negocio (ej.: pila de deshacer vacía).
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleEstadoInvalido(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}
