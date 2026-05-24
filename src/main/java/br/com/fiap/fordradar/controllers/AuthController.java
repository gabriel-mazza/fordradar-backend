package br.com.fiap.fordradar.controllers;

import br.com.fiap.fordradar.dtos.LoginRequestDTO;
import br.com.fiap.fordradar.dtos.LoginResponseDTO;
import br.com.fiap.fordradar.dtos.RegisterRequestDTO;
import br.com.fiap.fordradar.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequestDTO request) {
        authService.register(request);
        return ResponseEntity.ok().build();
    }
}

