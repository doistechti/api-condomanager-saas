package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.plano.*;
import br.com.doistech.apicondomanagersaas.service.PlanoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/planos")
@RequiredArgsConstructor
public class PlanoController {

    private final PlanoService service;

    @GetMapping
    public ResponseEntity<List<PlanoResponse>> list(@RequestParam(required = false) Boolean onlyActive) {
        return ResponseEntity.ok(service.list(onlyActive));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanoResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<PlanoResponse> create(@RequestBody @Valid PlanoCreateRequest req) {
        var created = service.create(req);
        return ResponseEntity.created(URI.create("/api/v1/planos/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanoResponse> update(@PathVariable Long id, @RequestBody @Valid PlanoUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

