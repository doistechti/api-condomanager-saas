package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.setor.SetorCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.setor.SetorResponse;
import br.com.doistech.apicondomanagersaas.dto.setor.SetorUpdateRequest;
import br.com.doistech.apicondomanagersaas.service.SetorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/condominios/{condominioId}/setores")
@RequiredArgsConstructor
public class SetorController {

    private final SetorService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SetorResponse create(@PathVariable Long condominioId, @Valid @RequestBody SetorCreateRequest req) {
        // garante que o condominioId do path prevaleça
        SetorCreateRequest fixed = new SetorCreateRequest(condominioId, req.nome(), req.tipo(), req.descricao());
        return service.create(fixed);
    }

    @PutMapping("/{id}")
    public SetorResponse update(@PathVariable Long condominioId, @PathVariable Long id, @Valid @RequestBody SetorUpdateRequest req) {
        return service.update(id, condominioId, req);
    }

    @GetMapping("/{id}")
    public SetorResponse getById(@PathVariable Long condominioId, @PathVariable Long id) {
        return service.getById(id, condominioId);
    }

    @GetMapping
    public List<SetorResponse> list(@PathVariable Long condominioId) {
        return service.listByCondominio(condominioId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long condominioId, @PathVariable Long id) {
        service.delete(id, condominioId);
    }
}
