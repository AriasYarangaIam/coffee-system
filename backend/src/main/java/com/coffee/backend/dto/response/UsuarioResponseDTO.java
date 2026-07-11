package com.coffee.backend.dto.response;

// Respuesta de gestión de usuarios (ADMIN). Incluye usuarioId y rol (B-12).
public record UsuarioResponseDTO(
     Long usuarioId,
     String nombreUsuario,
     String apellidoUsuario,
     String correoUsuario,
     String telefonoUsuario,
     String rol
) {
}
