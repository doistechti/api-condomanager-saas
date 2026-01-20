package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.unidade.UnidadeCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.unidade.UnidadeResponse;
import br.com.doistech.apicondomanagersaas.dto.unidade.UnidadeUpdateRequest;
import br.com.doistech.apicondomanagersaas.service.UnidadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/condominios/{condominioId}/unidades")
@RequiredArgsConstructor
public class UnidadeController {

    private final UnidadeService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UnidadeResponse create(@PathVariable Long condominioId, @Valid @RequestBody UnidadeCreateRequest req) {
        UnidadeCreateRequest fixed = new UnidadeCreateRequest(condominioId, req.setorId(), req.identificacao(), req.descricao());
        return service.create(fixed);
    }

    @PutMapping("/{id}")
    public UnidadeResponse update(@PathVariable Long condominioId, @PathVariable Long id, @Valid @RequestBody UnidadeUpdateRequest req) {
        return service.update(id, condominioId, req);
    }

    @GetMapping("/{id}")
    public UnidadeResponse getById(@PathVariable Long condominioId, @PathVariable Long id) {
        return service.getById(id, condominioId);
    }

    @GetMapping
    public List<UnidadeResponse> list(@PathVariable Long condominioId) {
        return service.listByCondominio(condominioId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long condominioId, @PathVariable Long id) {
        service.delete(id, condominioId);
    }
}
