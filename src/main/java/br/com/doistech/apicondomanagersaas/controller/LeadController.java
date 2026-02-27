package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.domain.lead.LeadStatus;
import br.com.doistech.apicondomanagersaas.dto.lead.LeadResponse;
import br.com.doistech.apicondomanagersaas.dto.lead.LeadUpdateStatusRequest;
import br.com.doistech.apicondomanagersaas.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService service;

    @GetMapping
    public Page<LeadResponse> list(@RequestParam(required = false) LeadStatus status, Pageable pageable) {
        return service.list(status, pageable);
    }

    @GetMapping("/{id}")
    public LeadResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PatchMapping("/{id}/status")
    public LeadResponse updateStatus(@PathVariable Long id, @Valid @RequestBody LeadUpdateStatusRequest req) {
        return service.updateStatus(id, req);
    }

    /**
     * Admin SaaS libera o trial do lead.
     * - cria condomínio + assinatura trial
     * - ativa o usuário ADMIN_CONDOMINIO
     */
    @PostMapping("/{id}/liberar-trial")
    @PreAuthorize("hasRole('ADMIN_SAAS')")
    public LeadResponse liberarTrial(@PathVariable Long id) {
        return service.liberarTrial(id);
    }
}

