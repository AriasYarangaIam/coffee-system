package com.coffee.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 409 para conflictos de estado: stock insuficiente e invariantes de negocio
    // (auto-borrado, último admin, deshacer sin ingresos).
    @ExceptionHandler({StockInsuficienteException.class, ReglaNegocioException.class})
    public ResponseEntity<Map<String, String>> handleConflicto(RuntimeException ex) {
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
}
