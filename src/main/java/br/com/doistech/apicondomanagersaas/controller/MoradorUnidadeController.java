package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.morador.MoradorUnidadeResponse;
import br.com.doistech.apicondomanagersaas.service.MoradorUnidadeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/morador")
public class MoradorUnidadeController {

    private final MoradorUnidadeService moradorUnidadeService;

    public MoradorUnidadeController(MoradorUnidadeService moradorUnidadeService) {
        this.moradorUnidadeService = moradorUnidadeService;
    }

    @GetMapping("/unidades")
    @PreAuthorize("hasRole('MORADOR')")
    public List<MoradorUnidadeResponse> listar(Authentication authentication) {
        return moradorUnidadeService.listarMinhasUnidades(authentication.getName());
    }
}