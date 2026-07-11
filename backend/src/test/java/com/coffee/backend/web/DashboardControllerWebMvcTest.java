package com.coffee.backend.web;

import com.coffee.backend.controller.DashboardController;
import com.coffee.backend.dto.response.DashboardResponseDTO;
import com.coffee.backend.dto.response.StockBajoDTO;
import com.coffee.backend.exception.GlobalExceptionHandler;
import com.coffee.backend.security.JwtUtil;
import com.coffee.backend.security.UserDetailsServiceImpl;
import com.coffee.backend.service.DashboardService;
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
 * Slice hermético @WebMvcTest para GET /api/admin/dashboard (B-05, Tarea 9 Sprint 2).
 *
 * <p>Cierra el contrato de KPIs consumido por {@code frontend/js/pages/dashboard.js}:
 * {@code totalVentasDia}, {@code totalPedidosDia}, {@code productoEstrella} y la lista
 * {@code stockBajo[]}. Solo ADMIN puede leerlo. El {@link DashboardService} se mockea
 * para mantener el slice sin JPA / sin Supabase.
 *
 * <p>No toca {@code BackendApplicationTests.contextLoads} (smoke intacto).
 */
@WebMvcTest(DashboardController.class)
@Import({GlobalExceptionHandler.class, DashboardControllerWebMvcTest.TestSecurityConfig.class})
class DashboardControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminObtieneDashboard_shapeDeKpis() throws Exception {
        DashboardResponseDTO dto = new DashboardResponseDTO(
                new java.math.BigDecimal("125.50"), 8, "Capuchino",
                List.of(new StockBajoDTO("Leche", 4L, null)));

        given(dashboardService.obtenerResumen()).willReturn(dto);

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVentasDia").value(125.5))
                .andExpect(jsonPath("$.totalPedidosDia").value(8))
                .andExpect(jsonPath("$.productoEstrella").value("Capuchino"))
                .andExpect(jsonPath("$.stockBajo[0].nombreInsumo").value("Leche"))
                .andExpect(jsonPath("$.stockBajo[0].cantidad").value(4));
    }

    @Test
    @WithMockUser(roles = "MESERO")
    void meseroObtieneDashboard_denegado403() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
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
