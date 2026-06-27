package com.coffee.backend.controller;

import com.coffee.backend.dto.request.DeleteUserDTO;
import com.coffee.backend.dto.request.RegistrarUsuarioRequestDTO;
import com.coffee.backend.dto.request.UsuarioPatchDTO;
import com.coffee.backend.dto.response.UsuarioResponseDTO;
import com.coffee.backend.service.implement.UsuarioServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    final UsuarioServiceImpl usuarioServiceImple;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> agregarUsuario(@RequestBody RegistrarUsuarioRequestDTO requestDTO) {
        usuarioServiceImple.registrarUsuario(requestDTO);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerUsuarios() {
        return ResponseEntity.ok(usuarioServiceImple.obtenerUsuarios());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    public ResponseEntity<?> deleteUsuarios(@RequestBody DeleteUserDTO deleteUserDTO) {
        usuarioServiceImple.deleteMesero(deleteUserDTO.correo());
        return ResponseEntity.ok().build();
    }

    // Edita al usuario logueado (deriva del JWT). Disponible para ADMIN y MESERO.
    @PreAuthorize("hasAnyRole('ADMIN','MESERO')")
    @PatchMapping("/actualizar")
    public ResponseEntity<?> actualizarUsuario(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UsuarioPatchDTO usuarioPatchDTO) {
        usuarioServiceImple.actualizarParcial(userDetails.getUsername(), usuarioPatchDTO);
        return ResponseEntity.ok().build();
    }

}
