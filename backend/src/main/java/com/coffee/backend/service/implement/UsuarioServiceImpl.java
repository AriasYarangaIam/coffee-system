package com.coffee.backend.service.implement;

import com.coffee.backend.dto.request.RegistrarUsuarioRequestDTO;
import com.coffee.backend.entity.Roles;
import com.coffee.backend.entity.Usuarios;
import com.coffee.backend.repository.RolRepository;
import com.coffee.backend.repository.UsuarioRepository;
import com.coffee.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {
   final UsuarioRepository usuarioRepository;
   final PasswordEncoder passwordEncoder;
   final RolRepository rolRepository;

    @Override
    public void registrarUsuario(RegistrarUsuarioRequestDTO requestDTO) {
        //Obtenemos el rol
        Roles roles = rolRepository.findById(2l).
                orElseThrow(()->new RuntimeException("Error rol no encontrado"));
        //Creamos el objeto con el patron Builder
        Usuarios usuarios = Usuarios.builder().nombreUsuario(requestDTO.nombreUsuario())
                .correoUsuario(requestDTO.correoUsuario()).apellidoUsuario(requestDTO.apellidoUsuario())
                .telefonoUsuario(requestDTO.telefonoUsuario()).
                claveCifrada(passwordEncoder.encode(requestDTO.claveCifrada())).roles(roles).build();
        //Almacenamos el objeto en la bd
        usuarioRepository.save(usuarios);

    }
}
