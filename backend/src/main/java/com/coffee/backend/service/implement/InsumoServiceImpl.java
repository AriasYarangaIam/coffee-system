package com.coffee.backend.service.implement;

import com.coffee.backend.dto.request.InsumoRequestDTO;
import com.coffee.backend.dto.response.InsumoResponseDTO;
import com.coffee.backend.entity.Insumos;
import com.coffee.backend.exception.RecursoNoEncontradoException;
import com.coffee.backend.repository.InsumoRepository;
import com.coffee.backend.service.InsumoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InsumoServiceImpl implements InsumoService {

    private final InsumoRepository insumoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InsumoResponseDTO> listar() {
        return insumoRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public InsumoResponseDTO crear(InsumoRequestDTO dto) {
        Insumos insumo = new Insumos();
        insumo.setNombreInsumo(dto.nombreInsumo());
        insumo.setUnidad(dto.unidad());
        return toDTO(insumoRepository.save(insumo));
    }

    @Override
    @Transactional
    public InsumoResponseDTO actualizar(Long id, InsumoRequestDTO dto) {
        Insumos insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Insumo no encontrado con ID: " + id));
        // Guardrail: no se puede cambiar la unidad si el insumo ya tiene stock o está en una
        // receta (las cantidades existentes quedarían en otra medida sin convertir).
        if (estaEnUso(insumo) && !Objects.equals(insumo.getUnidad(), dto.unidad())) {
            throw new IllegalArgumentException(
                    "No se puede cambiar la unidad: el insumo ya tiene stock o se usa en una receta");
        }
        insumo.setNombreInsumo(dto.nombreInsumo());
        insumo.setUnidad(dto.unidad());
        return toDTO(insumoRepository.save(insumo));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!insumoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Insumo no encontrado con ID: " + id);
        }
        insumoRepository.deleteById(id);
    }

    // En uso = tiene existencias de stock o aparece en alguna receta.
    private boolean estaEnUso(Insumos i) {
        return (i.getStocks() != null && !i.getStocks().isEmpty())
                || (i.getRecetas() != null && !i.getRecetas().isEmpty());
    }

    private InsumoResponseDTO toDTO(Insumos i) {
        return new InsumoResponseDTO(
                i.getIdInsumo(), i.getNombreInsumo(), i.getUnidad(), !estaEnUso(i));
    }
}
