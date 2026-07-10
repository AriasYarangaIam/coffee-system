package com.coffee.backend.dto.request;

// Edición de un usuario por id (ADMIN). 'claveUsuario' es el nombre que envía el front
// (usuarios.js); si viene null o vacío, la contraseña no se cambia.
public record ActualizarUsuarioRequestDTO(
        String nombreUsuario,
        String apellidoUsuario,
        String correoUsuario,
        String telefonoUsuario,
        String rol,
        String claveUsuario
) {
}
