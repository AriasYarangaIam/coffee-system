package com.coffee.backend.service;

import com.coffee.backend.dto.request.LoginRequestDTO;
import com.coffee.backend.dto.response.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO iniciarSesion(LoginRequestDTO dto);
}