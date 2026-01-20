package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.condominio.CondominioCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.condominio.CondominioResponse;
import br.com.doistech.apicondomanagersaas.dto.condominio.CondominioUpdateRequest;
import br.com.doistech.apicondomanagersaas.service.CondominioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/condominios")
@RequiredArgsConstructor
public class CondominioController {

    private final CondominioService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CondominioResponse create(@Valid @RequestBody CondominioCreateRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public CondominioResponse update(@PathVariable Long id, @Valid @RequestBody CondominioUpdateRequest req) {
        return service.update(id, req);
    }

    @GetMapping("/{id}")
    public CondominioResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    public List<CondominioResponse> list() {
        return service.list();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
