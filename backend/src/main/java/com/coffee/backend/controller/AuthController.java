package com.coffee.backend.controller;
import com.coffee.backend.dto.request.LoginRequestDTO;
import com.coffee.backend.dto.response.LoginResponseDTO;
import com.coffee.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/get")
    public String test() {
        System.out.println(passwordEncoder.encode("123"));
        return "Test aprobado con éxito";
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> iniciarSesion(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.iniciarSesion(dto));
    }
}
