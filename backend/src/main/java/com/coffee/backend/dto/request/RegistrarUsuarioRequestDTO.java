package com.coffee.backend.dto.request;

import com.coffee.backend.entity.Roles;

public record RegistrarUsuarioRequestDTO(
        String nombreUsuario,
        String apellidoUsuario,
        String correoUsuario,
        String telefonoUsuario,
        String claveCifrada
) {
}
