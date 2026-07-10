package com.coffee.backend.web;

import com.coffee.backend.controller.PedidoUndoController;
import com.coffee.backend.dto.request.CartItemSnapshotDTO;
import com.coffee.backend.dto.request.CartSnapshotRequestDTO;
import com.coffee.backend.exception.GlobalExceptionHandler;
import com.coffee.backend.security.JwtUtil;
import com.coffee.backend.security.UserDetailsServiceImpl;
import com.coffee.backend.service.CartItemSnapshot;
import com.coffee.backend.service.CartSnapshot;
import com.coffee.backend.service.PedidoUndoService;
import com.coffee.backend.service.UndoState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice @WebMvcTest para /api/pedidos/undo/*.
 */
@WebMvcTest(PedidoUndoController.class)
@Import({GlobalExceptionHandler.class, PedidoUndoControllerWebMvcTest.TestSecurityConfig.class})
class PedidoUndoControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoUndoService pedidoUndoService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Test
    @WithMockUser(roles = "MESERO", username = "luis@coffee.com")
    void push_ok204() throws Exception {
        doNothing().when(pedidoUndoService).push(eq("luis@coffee.com"), any(CartSnapshot.class));

        mockMvc.perform(post("/api/pedidos/undo/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {"productoId": 1, "nombre": "Café", "precio": 2500, "cantidad": 2}
                                  ],
                                  "total": 5000
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "MESERO", username = "luis@coffee.com")
    void push_payloadInvalido_400() throws Exception {
        mockMvc.perform(post("/api/pedidos/undo/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {"productoId": null, "nombre": "Café", "precio": 2500, "cantidad": 2}
                                  ],
                                  "total": 5000
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MESERO", username = "luis@coffee.com")
    void undo_ok200() throws Exception {
        given(pedidoUndoService.undo("luis@coffee.com"))
                .willReturn(Optional.of(snapshot("Café", 5000.0)));

        mockMvc.perform(post("/api/pedidos/undo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.snapshot.items[0].nombre").value("Café"))
                .andExpect(jsonPath("$.snapshot.total").value(5000.0));
    }

    @Test
    @WithMockUser(roles = "MESERO", username = "luis@coffee.com")
    void undo_vacia_409() throws Exception {
        given(pedidoUndoService.undo("luis@coffee.com")).willReturn(Optional.empty());

        mockMvc.perform(post("/api/pedidos/undo"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("No hay acciones para deshacer"));
    }

    @Test
    @WithMockUser(roles = "MESERO", username = "luis@coffee.com")
    void meseroPuedeConsultarEstado_ok200() throws Exception {
        given(pedidoUndoService.state("luis@coffee.com"))
                .willReturn(new UndoState(true, 2));

        mockMvc.perform(get("/api/pedidos/undo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canUndo").value(true))
                .andExpect(jsonPath("$.depth").value(2));
    }

    @Test
    @WithMockUser(roles = "COCINERO")
    void rolNoAutorizado_denegado403() throws Exception {
        mockMvc.perform(get("/api/pedidos/undo"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MESERO", username = "luis@coffee.com")
    void clear_ok204() throws Exception {
        doNothing().when(pedidoUndoService).clearForUser("luis@coffee.com");

        mockMvc.perform(delete("/api/pedidos/undo"))
                .andExpect(status().isNoContent());
    }

    private CartSnapshot snapshot(String nombre, double total) {
        return new CartSnapshot(
                List.of(new CartItemSnapshot(1L, nombre, total, 2L)),
                total
        );
    }

    @EnableWebSecurity
    @EnableMethodSecurity
    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(c -> c.disable())
                    .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                    .build();
        }
    }
}
