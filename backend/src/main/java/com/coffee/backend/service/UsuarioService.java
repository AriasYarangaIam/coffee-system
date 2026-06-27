package com.coffee.backend.service;

import com.coffee.backend.dto.request.RegistrarUsuarioRequestDTO;
import com.coffee.backend.dto.request.UsuarioPatchDTO;
import com.coffee.backend.dto.response.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {
    void registrarUsuario(RegistrarUsuarioRequestDTO requestDTO);
     List<UsuarioResponseDTO> obtenerUsuarios();
     void deleteMesero(String  correo);
     void actualizarParcial(String correo, UsuarioPatchDTO dto);
}
