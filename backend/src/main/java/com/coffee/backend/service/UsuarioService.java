package com.coffee.backend.service;

import com.coffee.backend.dto.request.ActualizarUsuarioRequestDTO;
import com.coffee.backend.dto.request.RegistrarUsuarioRequestDTO;
import com.coffee.backend.dto.request.UsuarioPatchDTO;
import com.coffee.backend.dto.response.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {
    void registrarUsuario(RegistrarUsuarioRequestDTO requestDTO);
     List<UsuarioResponseDTO> obtenerUsuarios();
     void actualizarParcial(String correo, UsuarioPatchDTO dto);

     // Edición por id desde el panel admin.
     void actualizarPorId(Long id, ActualizarUsuarioRequestDTO dto);

     // Borrado lógico por id. 'actorCorreo' es el admin autenticado (no puede borrarse a
     // sí mismo ni dejar al sistema sin ADMIN).
     void eliminarPorId(Long id, String actorCorreo);
}
