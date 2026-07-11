package com.coffee.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Request de login: correo y contrasena.
public record LoginRequestDTO(
        @NotBlank @Email String correo,
        @NotBlank String contraseña
) {}