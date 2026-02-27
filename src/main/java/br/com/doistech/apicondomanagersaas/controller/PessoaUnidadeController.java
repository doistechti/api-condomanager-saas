package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeResponse;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeUpdateRequest;
import br.com.doistech.apicondomanagersaas.service.PessoaUnidadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/condominios/{condominioId}/pessoas-unidades")
@RequiredArgsConstructor
public class PessoaUnidadeController {

    private final PessoaUnidadeService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PessoaUnidadeResponse create(@PathVariable Long condominioId,
                                        @Valid @RequestBody PessoaUnidadeCreateRequest req) {
        // força consistência do condominioId da rota
        PessoaUnidadeCreateRequest fixed = new PessoaUnidadeCreateRequest(
                condominioId,
                req.unidadeId(),
                req.pessoaId(),
                req.nome(), req.cpfCnpj(), req.email(), req.telefone(),
                req.ehProprietario(), req.ehMorador(), req.moradorTipo(), req.principal(),
                req.dataInicio(), req.dataFim()
        );
        return service.create(fixed);
    }

    @PutMapping("/{id}")
    public PessoaUnidadeResponse update(@PathVariable Long condominioId,
                                        @PathVariable Long id,
                                        @Valid @RequestBody PessoaUnidadeUpdateRequest req) {
        return service.update(id, condominioId, req);
    }

    @GetMapping("/{id}")
    public PessoaUnidadeResponse getById(@PathVariable Long condominioId,
                                         @PathVariable Long id) {
        return service.getById(id, condominioId);
    }

    @GetMapping
    public List<PessoaUnidadeResponse> list(@PathVariable Long condominioId) {
        return service.listByCondominio(condominioId);
    }

    @GetMapping("/unidades/{unidadeId}")
    public List<PessoaUnidadeResponse> listByUnidade(@PathVariable Long condominioId,
                                                     @PathVariable Long unidadeId) {
        return service.listByUnidade(condominioId, unidadeId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long condominioId,
                       @PathVariable Long id) {
        service.delete(id, condominioId);
    }
}

