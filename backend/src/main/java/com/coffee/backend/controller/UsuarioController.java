package com.coffee.backend.controller;

import com.coffee.backend.dto.request.RegistrarUsuarioRequestDTO;
import com.coffee.backend.service.UsuarioService;
import com.coffee.backend.service.implement.UsuarioServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuario/")
@RequiredArgsConstructor
public class UsuarioController {
    final UsuarioServiceImpl usuarioServiceImple;
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MESERO')")
    @PostMapping("/registrar")
    public ResponseEntity<?> agregarUsuario(@RequestBody RegistrarUsuarioRequestDTO requestDTO) {
        usuarioServiceImple.registrarUsuario(requestDTO);
        return ResponseEntity.ok().build();
    }
}
