package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.morador.MoradorScopeResponse;
import br.com.doistech.apicondomanagersaas.service.MoradorScopeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/morador")
public class MoradorScopeController {

    private final MoradorScopeService moradorScopeService;

    public MoradorScopeController(MoradorScopeService moradorScopeService) {
        this.moradorScopeService = moradorScopeService;
    }

    @GetMapping("/scope")
    @PreAuthorize("hasRole('MORADOR')")
    public MoradorScopeResponse getScope(Authentication authentication) {
        return moradorScopeService.getScope(authentication.getName());
    }
}