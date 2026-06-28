package com.coffee.backend.mapper;

import com.coffee.backend.dto.response.UsuarioResponseDTO;
import com.coffee.backend.entity.Roles;
import com.coffee.backend.entity.Usuarios;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit test for {@link UsuarioMapper} (REQ-UMR-01 scenario "Mapper round-trips all fields").
 * No Spring context — direct instantiation of the {@code @Component}.
 */
class UsuarioMapperTest {

    private final UsuarioMapper usuarioMapper = new UsuarioMapper();

    private Usuarios usuario(Long id, String nombre, String apellido, String correo,
                             String tel, String rol) {
        Roles roles = new Roles();
        roles.setNombreRol(rol);
        return Usuarios.builder()
                .usuarioId(id)
                .nombreUsuario(nombre)
                .apellidoUsuario(apellido)
                .correoUsuario(correo)
                .telefonoUsuario(tel)
                .roles(roles)
                .build();
    }

    @Test
    void toResponseDTO_mapeaLosSeisCampos_rolADMIN() {
        // REQ-UMR-01 escenario 1 — ADMIN.
        Usuarios u = usuario(7L, "Ana", "Lopez", "ana@x.com", "555", "ADMIN");

        UsuarioResponseDTO dto = usuarioMapper.toResponseDTO(u);

        assertThat(dto.usuarioId()).isEqualTo(7L);
        assertThat(dto.nombreUsuario()).isEqualTo("Ana");
        assertThat(dto.apellidoUsuario()).isEqualTo("Lopez");
        assertThat(dto.correoUsuario()).isEqualTo("ana@x.com");
        assertThat(dto.telefonoUsuario()).isEqualTo("555");
        assertThat(dto.rol()).isEqualTo("ADMIN");
    }

    @Test
    void toResponseDTO_preservaNombreCompuesto_rolMESERO() {
        // Triangulación: nombre compuesto "Maria José" + rol MESERO.
        // REQ-UMR-04: el backend expone nombre y apellido por separado (el split por
        // espacio que corrompe nombres compuestos vive en el front, no en el mapper).
        Usuarios u = usuario(11L, "Maria José", "Pérez", "maria@x.com", "999", "MESERO");

        UsuarioResponseDTO dto = usuarioMapper.toResponseDTO(u);

        assertThat(dto.usuarioId()).isEqualTo(11L);
        assertThat(dto.nombreUsuario()).isEqualTo("Maria José");
        assertThat(dto.apellidoUsuario()).isEqualTo("Pérez");
        assertThat(dto.rol()).isEqualTo("MESERO");
    }
}