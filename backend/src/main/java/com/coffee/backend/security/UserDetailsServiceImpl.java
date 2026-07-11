package com.coffee.backend.security;
import com.coffee.backend.entity.Usuarios;
import com.coffee.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
@Service                  // ← faltaba
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuarios usuario = usuarioRepository.findByCorreoUsuario(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        String rol = usuario.getRoles().getNombreRol(); // "ADMIN" o "MESERO"

        // enabled = activo: un usuario dado de baja (borrado lógico) no puede autenticarse.
        return User.withUsername(usuario.getCorreoUsuario())
                .password(usuario.getClaveCifrada())
                .disabled(!usuario.isActivo())
                .authorities(new SimpleGrantedAuthority("ROLE_" + rol))
                .build();
    }

}
