package com.coffee.backend.dto.request;

public record UsuarioPatchDTO(
        String nombreUsuario,
        String apellidoUsuario,
        String correoUsuario,
        String telefonoUsuario
) {
}
