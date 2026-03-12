package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.auth.LoginResponse;
import br.com.doistech.apicondomanagersaas.dto.auth.MoradorInviteAcceptRequest;
import br.com.doistech.apicondomanagersaas.dto.auth.MoradorInviteResponse;
import br.com.doistech.apicondomanagersaas.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/convites/morador")
@RequiredArgsConstructor
public class PublicMoradorInviteController {

    private final AuthService authService;

    @GetMapping("/{token}")
    public MoradorInviteResponse getInvite(@PathVariable String token) {
        return authService.getMoradorInvite(token);
    }

    @PostMapping("/aceitar")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse acceptInvite(@Valid @RequestBody MoradorInviteAcceptRequest request) {
        return authService.acceptMoradorInvite(request);
    }
}
