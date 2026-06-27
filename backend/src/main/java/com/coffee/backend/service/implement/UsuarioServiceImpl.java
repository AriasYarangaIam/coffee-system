package com.coffee.backend.service.implement;

import com.coffee.backend.dto.request.RegistrarUsuarioRequestDTO;
import com.coffee.backend.dto.request.UsuarioPatchDTO;
import com.coffee.backend.dto.response.UsuarioResponseDTO;
import com.coffee.backend.entity.Roles;
import com.coffee.backend.entity.Usuarios;
import com.coffee.backend.exception.RecursoNoEncontradoException;
import com.coffee.backend.repository.RolRepository;
import com.coffee.backend.repository.UsuarioRepository;
import com.coffee.backend.service.UsuarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {
   final UsuarioRepository usuarioRepository;
   final PasswordEncoder passwordEncoder;
   final RolRepository rolRepository;

    @Override
    public void registrarUsuario(RegistrarUsuarioRequestDTO requestDTO) {
        //Rol solicitado (por defecto MESERO si no se envía). Permite crear ADMIN.
        String nombreRol = (requestDTO.rol() == null || requestDTO.rol().isBlank())
                ? "MESERO"
                : requestDTO.rol().trim().toUpperCase();
        Roles roles = rolRepository.findByNombreRol(nombreRol)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado: " + nombreRol));
        //Creamos el objeto con el patron Builder
        Usuarios usuarios = Usuarios.builder().nombreUsuario(requestDTO.nombreUsuario())
                .correoUsuario(requestDTO.correoUsuario()).apellidoUsuario(requestDTO.apellidoUsuario())
                .telefonoUsuario(requestDTO.telefonoUsuario()).
                claveCifrada(passwordEncoder.encode(requestDTO.claveCifrada())).roles(roles).build();
        //Almacenamos el objeto en la bd
        usuarioRepository.save(usuarios);
    }

     public List<UsuarioResponseDTO> obtenerUsuarios(){
      return usuarioRepository.findAll().stream()
              .map(u -> new UsuarioResponseDTO(
                      u.getUsuarioId(),
                      u.getNombreUsuario(),
                      u.getApellidoUsuario(),
                      u.getCorreoUsuario(),
                      u.getTelefonoUsuario(),
                      u.getRoles().getNombreRol()
              )).toList();
     }

    @Transactional
     public void deleteMesero(String correo){
        Usuarios  usuarios = usuarioRepository.findByCorreoUsuario(correo).
                orElseThrow(() -> new RuntimeException("Usuario No encontrado"));
        usuarioRepository.deleteByCorreoUsuario(correo);
     }

    @Transactional
    public void actualizarParcial(String correo, UsuarioPatchDTO dto){
        Usuarios  usuarios = usuarioRepository.findByCorreoUsuario(correo).
                orElseThrow(() -> new RuntimeException("Usuario No encontrado"));
        if(dto.nombreUsuario() != null){usuarios.setNombreUsuario(dto.nombreUsuario());}
        if (dto.apellidoUsuario() != null) usuarios.setApellidoUsuario(dto.apellidoUsuario());
        if (dto.correoUsuario() != null) usuarios.setCorreoUsuario(dto.correoUsuario());
        if (dto.telefonoUsuario() != null) usuarios.setTelefonoUsuario(dto.telefonoUsuario());
        usuarioRepository.save(usuarios);
    }
}
