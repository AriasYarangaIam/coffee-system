package com.coffee.backend.web;

import com.coffee.backend.controller.ReporteController;
import com.coffee.backend.dto.response.ReporteMensualResponseDTO;
import com.coffee.backend.exception.GlobalExceptionHandler;
import com.coffee.backend.security.JwtUtil;
import com.coffee.backend.security.UserDetailsServiceImpl;
import com.coffee.backend.service.ReporteService;
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
 * Slice hermético @WebMvcTest para GET /api/admin/reportes/mensual (RF-DS-02, Tarea 10).
 *
 * <p>Cierra el contrato de la matriz producto × día consumido por
 * {@code frontend/js/pages/dashboard.js}: {@code productos[]}, {@code dias[]},
 * {@code celdas[][]}. Solo ADMIN. {@link ReporteService} mockeado (sin JPA/Supabase).
 */
@WebMvcTest(ReporteController.class)
@Import({GlobalExceptionHandler.class, ReporteControllerWebMvcTest.TestSecurityConfig.class})
class ReporteControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReporteService reporteService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminObtieneReporte_shapeDeMatriz() throws Exception {
        ReporteMensualResponseDTO dto = new ReporteMensualResponseDTO(
                List.of("Capuchino", "Latte"),
                List.of(1, 2, 3),
                new double[][]{{30.0, 0.0, 12.5}, {20.0, 0.0, 0.0}});

        given(reporteService.reporteMensual()).willReturn(dto);

        mockMvc.perform(get("/api/admin/reportes/mensual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productos[0]").value("Capuchino"))
                .andExpect(jsonPath("$.dias[2]").value(3))
                .andExpect(jsonPath("$.celdas[0][0]").value(30.0))
                .andExpect(jsonPath("$.celdas[1][0]").value(20.0));
    }

    @Test
    @WithMockUser(roles = "MESERO")
    void meseroObtieneReporte_denegado403() throws Exception {
        mockMvc.perform(get("/api/admin/reportes/mensual"))
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
