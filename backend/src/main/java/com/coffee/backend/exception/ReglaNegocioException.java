package com.coffee.backend.exception;

// Invariante de negocio violada con la petición autorizada (ej.: borrarte a ti mismo,
// deshacer sin ingresos, dejar al sistema sin ADMIN). El GlobalExceptionHandler la
// traduce a HTTP 409 Conflict.
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
