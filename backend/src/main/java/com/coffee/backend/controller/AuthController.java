package com.coffee.backend.controller;
import com.coffee.backend.dto.request.LoginRequestDTO;
import com.coffee.backend.dto.response.LoginResponseDTO;
import com.coffee.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Autenticación. Único endpoint público del sistema: valida correo+contraseña y devuelve
 * un JWT que el resto de la API exige. Todo lo demás pasa por {@code /api/auth}.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /** POST /api/auth/login → valida credenciales y responde token + datos del usuario. */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> iniciarSesion(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.iniciarSesion(dto));
    }
}
