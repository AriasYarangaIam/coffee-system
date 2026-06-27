package com.coffee.backend.dto.response;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;

public record UsuarioResponseDTO(
     String nombreUsuario,
     String apellidoUsuario,
     String correoUsuario,
     String telefonoUsuario
) {
}
