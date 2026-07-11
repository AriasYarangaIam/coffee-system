package com.coffee.backend.service.implement;

import com.coffee.backend.dto.response.CategoriaResponseDTO;
import com.coffee.backend.entity.Categorias;
import com.coffee.backend.repository.CategoriaRepository;
import com.coffee.backend.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Lista las categorias mapeadas a DTO (para selects del admin).
@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listar() {
        return categoriaRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    private CategoriaResponseDTO toDTO(Categorias c) {
        return new CategoriaResponseDTO(c.getCategoriaId(), c.getNombreCategoria());
    }
}
