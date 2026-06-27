package com.coffee.backend.dto.response;

public record LoginResponseDTO(
        String token,
        String correo,
        String rol,
        String nombreCompleto
) {}