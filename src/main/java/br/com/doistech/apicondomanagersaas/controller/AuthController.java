package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.auth.LoginRequest;
import br.com.doistech.apicondomanagersaas.dto.auth.LoginResponse;
import br.com.doistech.apicondomanagersaas.dto.auth.MeResponse;
import br.com.doistech.apicondomanagersaas.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        // authentication.getName() = email (porque nosso UserDetails usa email como username)
        return authService.me(authentication.getName());
    }
}

