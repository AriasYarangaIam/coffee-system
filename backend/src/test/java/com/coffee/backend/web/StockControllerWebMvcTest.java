package com.coffee.backend.web;

import com.coffee.backend.controller.StockController;
import com.coffee.backend.dto.response.MovimientoStockResponseDTO;
import com.coffee.backend.exception.GlobalExceptionHandler;
import com.coffee.backend.security.JwtUtil;
import com.coffee.backend.security.UserDetailsServiceImpl;
import com.coffee.backend.service.StockService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice hermético para el histórico de stock. GET /api/admin/stocks/movimientos es
 * ADMIN-only (403 al MESERO). {@link StockService} mockeado.
 */
@WebMvcTest(StockController.class)
@Import({GlobalExceptionHandler.class, StockControllerWebMvcTest.TestSecurityConfig.class})
class StockControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockService stockService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminObtieneMovimientos_shape() throws Exception {
        MovimientoStockResponseDTO dto = new MovimientoStockResponseDTO(
                1L, 5L, "Leche", "ml", 2L, "Central", 500L,
                LocalDateTime.of(2026, 7, 10, 9, 0), "Ana Pérez", "ACTIVO");
        given(stockService.listarMovimientos()).willReturn(List.of(dto));

        mockMvc.perform(get("/api/admin/stocks/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreInsumo").value("Leche"))
                .andExpect(jsonPath("$[0].registradoPor").value("Ana Pérez"))
                .andExpect(jsonPath("$[0].cantidad").value(500));
    }

    @Test
    @WithMockUser(roles = "MESERO")
    void meseroObtieneMovimientos_denegado403() throws Exception {
        mockMvc.perform(get("/api/admin/stocks/movimientos"))
                .andExpect(status().isForbidden());
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
