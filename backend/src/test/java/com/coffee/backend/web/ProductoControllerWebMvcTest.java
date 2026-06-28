package com.coffee.backend.web;

import com.coffee.backend.controller.ProductoController;
import com.coffee.backend.dto.response.ProductoListadoResponseDTO;
import com.coffee.backend.exception.GlobalExceptionHandler;
import com.coffee.backend.security.JwtUtil;
import com.coffee.backend.security.UserDetailsServiceImpl;
import com.coffee.backend.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice hermético @WebMvcTest para GET /api/productos (B-10, Tarea 12 Sprint 2).
 *
 * <p>Cierra el cambio de acceso por rol: el catálogo de meseros ahora también es
 * legible por ADMIN. {@code @PreAuthorize("hasAnyRole('MESERO','ADMIN')")} se aplica
 * de verdad gracias a {@link TestSecurityConfig} con method security. El
 * {@link ProductoService} se mockea para mantener el slice sin JPA / sin Supabase.
 *
 * <p>No toca {@code BackendApplicationTests.contextLoads} (smoke intacto).
 */
@WebMvcTest(ProductoController.class)
@Import({GlobalExceptionHandler.class, ProductoControllerWebMvcTest.TestSecurityConfig.class})
class ProductoControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    // Dependencias del JwtFilter (@Component levantado por el slice). Sin Authorization
    // header (caso @WithMockUser) es pass-through; se mockean para no tocar BD.
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Test
    @WithMockUser(roles = "MESERO")
    void meseroListaProductos_ok200() throws Exception {
        given(productoService.listarProductos()).willReturn(List.of(
                new ProductoListadoResponseDTO(1L, "Capuchino", "Bebidas", 8.5, true)));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreProducto").value("Capuchino"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminListaProductos_ok200() throws Exception {
        // B-10: ADMIN ahora también puede leer el catálogo (antes recibía 403).
        given(productoService.listarProductos()).willReturn(List.of(
                new ProductoListadoResponseDTO(1L, "Capuchino", "Bebidas", 8.5, true)));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreProducto").value("Capuchino"));
    }

    @Test
    @WithMockUser(roles = "COCINERO")
    void rolNoAutorizado_denegado403() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isForbidden());
    }

    @EnableWebSecurity
    @EnableMethodSecurity
    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            // permitAll() en la cadena; la denegación la decide @PreAuthorize.
            return http.csrf(c -> c.disable())
                    .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                    .build();
        }
    }
}
