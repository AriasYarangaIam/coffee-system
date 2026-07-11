package com.coffee.backend.controller;

import com.coffee.backend.dto.request.ActualizarUsuarioRequestDTO;
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

    // Edición de un usuario por id desde el panel admin.
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPorId(@PathVariable Long id,
                                             @RequestBody ActualizarUsuarioRequestDTO dto) {
        usuarioServiceImple.actualizarPorId(id, dto);
        return ResponseEntity.ok().build();
    }

    // Borrado lógico por id. El admin autenticado (del JWT) no puede borrarse a sí mismo
    // ni dejar al sistema sin ADMIN (ambos ⇒ 409).
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPorId(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetails actor) {
        usuarioServiceImple.eliminarPorId(id, actor.getUsername());
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
