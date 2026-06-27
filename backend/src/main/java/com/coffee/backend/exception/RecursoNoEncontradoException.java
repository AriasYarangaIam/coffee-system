package com.coffee.backend.exception;

// Excepción genérica para recursos inexistentes en los CRUD (productos, insumos, stocks,
// usuarios, almacenes...). El GlobalExceptionHandler la traduce a HTTP 404.
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
