package com.coffee.backend.web;

import com.coffee.backend.controller.UsuarioController;
import org.junit.jupiter.api.Test;
import com.coffee.backend.dto.response.UsuarioResponseDTO;
import com.coffee.backend.exception.GlobalExceptionHandler;
import com.coffee.backend.security.JwtUtil;
import com.coffee.backend.security.UserDetailsServiceImpl;
import com.coffee.backend.service.implement.UsuarioServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import com.coffee.backend.exception.ReglaNegocioException;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Hermetic @WebMvcTest regression guard for GET /api/admin/usuarios.
 *
 * <p>Locks the existing wire contract (REQ-UMR-02 + REQ-UMR-03) — this is an
 * approval/regression test for the {@code usuario-mapper-refactor} extraction,
 * NOT a new contract. The {@link UsuarioServiceImpl} is mocked so the slice
 * stays hermetic (no JPA / no Supabase / no JwtFilter / no UserDetailsServiceImpl).
 * Method security is enabled via {@link TestSecurityConfig} so the existing
 * {@code @PreAuthorize("hasRole('ADMIN')")} annotation is actually enforced.
 *
 * <p>Does NOT touch {@code BackendApplicationTests.contextLoads} (untouched smoke).
 */
@WebMvcTest(UsuarioController.class)
@Import({GlobalExceptionHandler.class, UsuarioControllerWebMvcTest.TestSecurityConfig.class})
class UsuarioControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioServiceImpl usuarioServiceImple;

    // JwtFilter es un @Component Filter levantado por el slice @WebMvcTest.
    // Sin Authorization header (caso @WithMockUser) es pass-through. Se satisfacen
    // sus dependencias de constructor con mocks para mantener el slice hermético.
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminListaUsuarios_retornaShapeConUsuarioIdYRol_seisCampos() throws Exception {
        // REQ-UMR-02: wire shape locked — usuarioId (JSON number), rol (String), +4 campos.
        // Stub retorna los 6 campos; GREEN: locks la contract shape del GET.
        UsuarioResponseDTO dto = new UsuarioResponseDTO(
                7L, "Ana", "Lopez", "ana@x.com", "555", "ADMIN");

        given(usuarioServiceImple.obtenerUsuarios()).willReturn(List.of(dto));

        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioId").value(7))
                .andExpect(jsonPath("$[0].nombreUsuario").value("Ana"))
                .andExpect(jsonPath("$[0].apellidoUsuario").value("Lopez"))
                .andExpect(jsonPath("$[0].correoUsuario").value("ana@x.com"))
                .andExpect(jsonPath("$[0].telefonoUsuario").value("555"))
                .andExpect(jsonPath("$[0].rol").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "MESERO")
    void meseroListaUsuarios_denegado403() throws Exception {
        // REQ-UMR-03: rol no-ADMIN recibe 403 (re-afirma @PreAuthorize existente).
        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isForbidden());
    }

    // --- PUT/DELETE por id (editar rol / borrado lógico con guardas) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminActualizaUsuarioPorId_ok200() throws Exception {
        doNothing().when(usuarioServiceImple).actualizarPorId(eq(5L), any());
        mockMvc.perform(put("/api/admin/usuarios/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"Ana\",\"rol\":\"ADMIN\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MESERO")
    void meseroActualizaUsuarioPorId_denegado403() throws Exception {
        mockMvc.perform(put("/api/admin/usuarios/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rol\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEliminaUsuarioPorId_ok200() throws Exception {
        doNothing().when(usuarioServiceImple).eliminarPorId(eq(5L), anyString());
        mockMvc.perform(delete("/api/admin/usuarios/5"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MESERO")
    void meseroEliminaUsuarioPorId_denegado403() throws Exception {
        mockMvc.perform(delete("/api/admin/usuarios/5"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void eliminarViolaGuarda_devuelve409() throws Exception {
        // Ej.: intentar borrar al último ADMIN → ReglaNegocioException → 409.
        willThrow(new ReglaNegocioException("No puedes eliminar al último administrador"))
                .given(usuarioServiceImple).eliminarPorId(anyLong(), anyString());
        mockMvc.perform(delete("/api/admin/usuarios/5"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("No puedes eliminar al último administrador"));
    }

    @EnableWebSecurity
    @EnableMethodSecurity
    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            // permitAll() en la cadena; la denegación la decide @PreAuthorize vía method security.
            return http.csrf(c -> c.disable())
                    .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                    .build();
        }
    }
}