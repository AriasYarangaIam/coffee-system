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
        return toDTO(insumoRepository.save(insumo));
    }

    @Override
    @Transactional
    public InsumoResponseDTO actualizar(Long id, InsumoRequestDTO dto) {
        Insumos insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Insumo no encontrado con ID: " + id));
        insumo.setNombreInsumo(dto.nombreInsumo());
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

    private InsumoResponseDTO toDTO(Insumos i) {
        return new InsumoResponseDTO(i.getIdInsumo(), i.getNombreInsumo());
    }
}
