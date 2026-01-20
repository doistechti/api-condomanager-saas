package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.pessoa.PessoaCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.pessoa.PessoaResponse;
import br.com.doistech.apicondomanagersaas.dto.pessoa.PessoaUpdateRequest;
import br.com.doistech.apicondomanagersaas.service.PessoaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/condominios/{condominioId}/pessoas")
@RequiredArgsConstructor
public class PessoaController {

    private final PessoaService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PessoaResponse create(@PathVariable Long condominioId, @Valid @RequestBody PessoaCreateRequest req) {
        PessoaCreateRequest fixed = new PessoaCreateRequest(condominioId, req.nome(), req.documento(), req.email(), req.telefone());
        return service.create(fixed);
    }

    @PutMapping("/{id}")
    public PessoaResponse update(@PathVariable Long condominioId, @PathVariable Long id, @Valid @RequestBody PessoaUpdateRequest req) {
        return service.update(id, condominioId, req);
    }

    @GetMapping("/{id}")
    public PessoaResponse getById(@PathVariable Long condominioId, @PathVariable Long id) {
        return service.getById(id, condominioId);
    }

    @GetMapping
    public List<PessoaResponse> list(@PathVariable Long condominioId) {
        return service.listByCondominio(condominioId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long condominioId, @PathVariable Long id) {
        service.delete(id, condominioId);
    }
}
