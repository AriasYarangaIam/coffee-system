package com.coffee.backend.service;

import com.coffee.backend.dto.request.InsumoRequestDTO;
import com.coffee.backend.dto.response.InsumoResponseDTO;
import com.coffee.backend.entity.Insumos;
import com.coffee.backend.entity.Recetas;
import com.coffee.backend.entity.Stocks;
import com.coffee.backend.repository.InsumoRepository;
import com.coffee.backend.service.implement.InsumoServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Guardrail de la unidad de medida: no se puede cambiar si el insumo ya tiene stock o se
 * usa en una receta (las cantidades existentes quedarían en otra medida). Repo mockeado.
 */
class InsumoServiceImplTest {

    private final InsumoRepository repo = mock(InsumoRepository.class);
    private final InsumoServiceImpl service = new InsumoServiceImpl(repo);

    @Test
    void actualizar_sinUso_permiteCambiarUnidad() {
        Insumos libre = insumo(1L, "Leche", "ml");
        given(repo.findById(1L)).willReturn(Optional.of(libre));
        given(repo.save(any(Insumos.class))).willAnswer(i -> i.getArgument(0));

        InsumoResponseDTO res = service.actualizar(1L, new InsumoRequestDTO("Leche", "L"));

        assertThat(res.unidad()).isEqualTo("L");
        assertThat(res.unidadEditable()).isTrue(); // sigue sin stock/recetas
    }

    @Test
    void actualizar_enUso_cambiandoUnidad_lanzaError() {
        Insumos enUso = insumo(2L, "Café", "g");
        enUso.setStocks(List.of(new Stocks())); // tiene stock
        given(repo.findById(2L)).willReturn(Optional.of(enUso));

        assertThatThrownBy(() -> service.actualizar(2L, new InsumoRequestDTO("Café", "kg")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unidad");
    }

    @Test
    void actualizar_enUso_mismaUnidad_permiteEditarNombre() {
        Insumos enUso = insumo(3L, "Leche", "ml");
        enUso.setRecetas(List.of(new Recetas())); // usado en receta
        given(repo.findById(3L)).willReturn(Optional.of(enUso));
        given(repo.save(any(Insumos.class))).willAnswer(i -> i.getArgument(0));

        InsumoResponseDTO res = service.actualizar(3L, new InsumoRequestDTO("Leche entera", "ml"));

        assertThat(res.nombreInsumo()).isEqualTo("Leche entera");
        assertThat(res.unidadEditable()).isFalse(); // bloqueada por estar en uso
    }

    private Insumos insumo(Long id, String nombre, String unidad) {
        Insumos i = new Insumos();
        i.setIdInsumo(id);
        i.setNombreInsumo(nombre);
        i.setUnidad(unidad);
        return i;
    }
}
