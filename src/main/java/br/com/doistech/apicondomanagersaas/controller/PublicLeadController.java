package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.lead.LeadCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.lead.LeadResponse;
import br.com.doistech.apicondomanagersaas.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/leads")
@RequiredArgsConstructor
public class PublicLeadController {

    private final LeadService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeadResponse create(@Valid @RequestBody LeadCreateRequest req) {
        return service.createPublic(req);
    }
}

