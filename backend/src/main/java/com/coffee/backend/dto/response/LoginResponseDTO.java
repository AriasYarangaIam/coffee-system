package com.coffee.backend.dto.response;

// Respuesta de login: token JWT + datos basicos del usuario.
public record LoginResponseDTO(
        String token,
        String correo,
        String rol,
        String nombreCompleto
) {}