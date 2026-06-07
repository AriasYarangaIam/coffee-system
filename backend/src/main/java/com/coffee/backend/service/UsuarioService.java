package com.coffee.backend.service;

import com.coffee.backend.dto.request.RegistrarUsuarioRequestDTO;

public interface UsuarioService {
    void registrarUsuario(RegistrarUsuarioRequestDTO requestDTO);
}
