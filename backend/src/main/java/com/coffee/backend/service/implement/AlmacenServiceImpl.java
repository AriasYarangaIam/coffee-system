package com.coffee.backend.service.implement;

import com.coffee.backend.dto.response.AlmacenResponseDTO;
import com.coffee.backend.entity.Almacenes;
import com.coffee.backend.repository.AlmacenRepository;
import com.coffee.backend.service.AlmacenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Lista los almacenes mapeados a DTO.
@Service
@RequiredArgsConstructor
public class AlmacenServiceImpl implements AlmacenService {

    private final AlmacenRepository almacenRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AlmacenResponseDTO> listar() {
        return almacenRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    private AlmacenResponseDTO toDTO(Almacenes a) {
        return new AlmacenResponseDTO(a.getCodigoAlmacen(), a.getNombreAlmacen());
    }
}
