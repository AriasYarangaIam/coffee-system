package com.coffee.backend.dto.request;

// Request: edicion parcial del propio usuario (autoedicion desde el JWT).
public record UsuarioPatchDTO(
        String nombreUsuario,
        String apellidoUsuario,
        String correoUsuario,
        String telefonoUsuario
) {
}
