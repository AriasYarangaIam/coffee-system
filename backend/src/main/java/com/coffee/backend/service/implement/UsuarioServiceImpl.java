package com.coffee.backend.service.implement;

import com.coffee.backend.dto.request.ActualizarUsuarioRequestDTO;
import com.coffee.backend.dto.request.RegistrarUsuarioRequestDTO;
import com.coffee.backend.dto.request.UsuarioPatchDTO;
import com.coffee.backend.dto.response.UsuarioResponseDTO;
import com.coffee.backend.entity.Roles;
import com.coffee.backend.entity.Usuarios;
import com.coffee.backend.exception.RecursoNoEncontradoException;
import com.coffee.backend.exception.ReglaNegocioException;
import com.coffee.backend.mapper.UsuarioMapper;
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
   final UsuarioMapper usuarioMapper;

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
              .filter(Usuarios::isActivo) // ocultar los dados de baja (borrado lógico)
              .map(usuarioMapper::toResponseDTO)
              .toList();
     }

    @Transactional
    public void actualizarPorId(Long id, ActualizarUsuarioRequestDTO dto) {
        Usuarios usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));
        if (dto.nombreUsuario() != null) usuario.setNombreUsuario(dto.nombreUsuario());
        if (dto.apellidoUsuario() != null) usuario.setApellidoUsuario(dto.apellidoUsuario());
        if (dto.correoUsuario() != null) usuario.setCorreoUsuario(dto.correoUsuario());
        if (dto.telefonoUsuario() != null) usuario.setTelefonoUsuario(dto.telefonoUsuario());
        if (dto.rol() != null && !dto.rol().isBlank()) {
            String nombreRol = dto.rol().trim().toUpperCase();
            Roles roles = rolRepository.findByNombreRol(nombreRol)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado: " + nombreRol));
            usuario.setRoles(roles);
        }
        // La clave solo se cambia si el front la envió (edición deja el campo en blanco si no).
        if (dto.claveUsuario() != null && !dto.claveUsuario().isBlank()) {
            usuario.setClaveCifrada(passwordEncoder.encode(dto.claveUsuario()));
        }
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarPorId(Long id, String actorCorreo) {
        Usuarios objetivo = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));

        // Identidad = correo del JWT (no hay usuarioId en los claims).
        if (objetivo.getCorreoUsuario().equals(actorCorreo)) {
            throw new ReglaNegocioException("No puedes eliminar tu propia cuenta");
        }
        // No dejar al sistema sin ningún ADMIN.
        if ("ADMIN".equals(objetivo.getRoles().getNombreRol())) {
            long adminsActivos = usuarioRepository.findByRoles_NombreRol("ADMIN").stream()
                    .filter(Usuarios::isActivo)
                    .count();
            if (adminsActivos <= 1) {
                throw new ReglaNegocioException("No puedes eliminar al último administrador");
            }
        }

        objetivo.setActivo(false); // borrado lógico: conserva su histórico de ventas
        usuarioRepository.save(objetivo);
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
