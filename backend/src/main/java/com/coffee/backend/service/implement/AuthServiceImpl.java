package com.coffee.backend.service.implement;

import com.coffee.backend.dto.request.LoginRequestDTO;
import com.coffee.backend.dto.response.LoginResponseDTO;
import com.coffee.backend.entity.Usuarios;
import com.coffee.backend.repository.UsuarioRepository;
import com.coffee.backend.security.JwtUtil;
import com.coffee.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager gestorAutenticacion;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponseDTO iniciarSesion(LoginRequestDTO dto) {
        // Valida credenciales — lanza excepción automática si son incorrectas
        gestorAutenticacion.authenticate(
                new UsernamePasswordAuthenticationToken(dto.correo(), dto.password())
        );

        // Carga el usuario para armar la respuesta
        Usuarios usuario = usuarioRepository.findByCorreoUsuario(dto.correo())
                .orElseThrow();

        String rol = usuario.getRoles().getNombreRol();
        String token = jwtUtil.generarToken(usuario.getCorreoUsuario(), rol);

        return new LoginResponseDTO(
                token,
                usuario.getCorreoUsuario(),
                rol,
                usuario.getNombreUsuario() + " " + usuario.getApellidoUsuario()
        );
    }
}