package com.coffee.backend.mapper;

import com.coffee.backend.dto.response.UsuarioResponseDTO;
import com.coffee.backend.entity.Usuarios;
import org.springframework.stereotype.Component;

/**
 * Mapper entity→DTO para Usuarios (mirrors {@link ProductoMapper}).
 * Extrae la conversión que antes vivía inline en {@code UsuarioServiceImpl.obtenerUsuarios}.
 */
@Component
public class UsuarioMapper {

    public UsuarioResponseDTO toResponseDTO(Usuarios usuario) {
        return new UsuarioResponseDTO(
                usuario.getUsuarioId(),
                usuario.getNombreUsuario(),
                usuario.getApellidoUsuario(),
                usuario.getCorreoUsuario(),
                usuario.getTelefonoUsuario(),
                usuario.getRoles().getNombreRol()
        );
    }
}