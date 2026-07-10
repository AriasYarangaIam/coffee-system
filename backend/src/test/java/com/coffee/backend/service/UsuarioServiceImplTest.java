package com.coffee.backend.service;

import com.coffee.backend.dto.request.ActualizarUsuarioRequestDTO;
import com.coffee.backend.entity.Roles;
import com.coffee.backend.entity.Usuarios;
import com.coffee.backend.exception.ReglaNegocioException;
import com.coffee.backend.mapper.UsuarioMapper;
import com.coffee.backend.repository.RolRepository;
import com.coffee.backend.repository.UsuarioRepository;
import com.coffee.backend.service.implement.UsuarioServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Guardas del borrado de usuarios: no auto-eliminarse, no dejar al sistema sin ADMIN, y
 * borrado lógico (activo=false) en vez de hard delete. Repos mockeados.
 */
class UsuarioServiceImplTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final RolRepository rolRepository = mock(RolRepository.class);
    private final UsuarioMapper usuarioMapper = mock(UsuarioMapper.class);

    private final UsuarioServiceImpl service =
            new UsuarioServiceImpl(usuarioRepository, passwordEncoder, rolRepository, usuarioMapper);

    @Test
    void eliminar_aSiMismo_lanzaReglaNegocio() {
        Usuarios admin = usuario(1L, "admin@x.com", "ADMIN");
        given(usuarioRepository.findById(1L)).willReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.eliminarPorId(1L, "admin@x.com"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("propia");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void eliminar_ultimoAdmin_lanzaReglaNegocio() {
        Usuarios otroAdmin = usuario(2L, "otro@x.com", "ADMIN");
        given(usuarioRepository.findById(2L)).willReturn(Optional.of(otroAdmin));
        given(usuarioRepository.findByRoles_NombreRol("ADMIN")).willReturn(List.of(otroAdmin));

        assertThatThrownBy(() -> service.eliminarPorId(2L, "admin@x.com"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("último");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void eliminar_meseroValido_haceBorradoLogico() {
        Usuarios mesero = usuario(3L, "mesero@x.com", "MESERO");
        given(usuarioRepository.findById(3L)).willReturn(Optional.of(mesero));

        service.eliminarPorId(3L, "admin@x.com");

        assertThat(mesero.isActivo()).isFalse(); // desactivado, no borrado
        verify(usuarioRepository).save(mesero);
    }

    @Test
    void actualizarPorId_conClave_laCifra() {
        Usuarios mesero = usuario(4L, "m@x.com", "MESERO");
        given(usuarioRepository.findById(4L)).willReturn(Optional.of(mesero));
        given(passwordEncoder.encode("nueva")).willReturn("HASH");

        service.actualizarPorId(4L, new ActualizarUsuarioRequestDTO(
                "Nuevo", null, null, null, null, "nueva"));

        assertThat(mesero.getNombreUsuario()).isEqualTo("Nuevo");
        assertThat(mesero.getClaveCifrada()).isEqualTo("HASH");
        verify(usuarioRepository).save(mesero);
    }

    @Test
    void actualizarPorId_sinClave_noTocaLaClave() {
        Usuarios mesero = usuario(5L, "m@x.com", "MESERO");
        mesero.setClaveCifrada("ORIGINAL");
        given(usuarioRepository.findById(5L)).willReturn(Optional.of(mesero));

        service.actualizarPorId(5L, new ActualizarUsuarioRequestDTO(
                "Nuevo", null, null, null, null, ""));

        assertThat(mesero.getClaveCifrada()).isEqualTo("ORIGINAL");
    }

    private Usuarios usuario(Long id, String correo, String rol) {
        Roles roles = new Roles();
        roles.setNombreRol(rol);
        return Usuarios.builder()
                .usuarioId(id)
                .correoUsuario(correo)
                .nombreUsuario("X")
                .apellidoUsuario("Y")
                .claveCifrada("c")
                .roles(roles)
                .activo(true)
                .build();
    }
}
