package com.coffee.backend.dto.request;

// Alta de usuario (ADMIN). 'rol' es opcional: si viene null se asume MESERO.
// Permite crear cuentas ADMIN (B-12).
public record RegistrarUsuarioRequestDTO(
        String nombreUsuario,
        String apellidoUsuario,
        String correoUsuario,
        String telefonoUsuario,
        String claveCifrada,
        String rol
) {
}
