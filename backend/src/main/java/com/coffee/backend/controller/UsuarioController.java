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
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/obtenerMeseros")
    public ResponseEntity<List <UsuarioResponseDTO>>obtenerUsuarios(){
     return ResponseEntity.ok(usuarioServiceImple.obtenerUsuarios());
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/delete")
    public ResponseEntity<?>deleteUsuarios(@RequestBody DeleteUserDTO deleteUserDTO){
        usuarioServiceImple.deleteMesero(deleteUserDTO.correo());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MESERO')")
    @PatchMapping("/actualizar")
    public ResponseEntity<?>actualizarUsuario(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UsuarioPatchDTO usuarioPatchDTO){
        usuarioServiceImple.actualizarParcial(userDetails.getUsername(),usuarioPatchDTO);
        return ResponseEntity.ok().build();
    }

}
