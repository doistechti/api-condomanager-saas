package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.vinculo.VinculoCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.vinculo.VinculoResponse;
import br.com.doistech.apicondomanagersaas.dto.vinculo.VinculoUpdateRequest;
import br.com.doistech.apicondomanagersaas.service.VinculoUnidadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/condominios/{condominioId}/vinculos")
@RequiredArgsConstructor
public class VinculoUnidadeController {

    private final VinculoUnidadeService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VinculoResponse create(@PathVariable Long condominioId, @Valid @RequestBody VinculoCreateRequest req) {
        VinculoCreateRequest fixed = new VinculoCreateRequest(
                condominioId,
                req.unidadeId(),
                req.pessoaId(),
                req.isProprietario(),
                req.isMorador(),
                req.tipoMoradia(),
                req.dataInicio(),
                req.dataFim()
        );
        return service.create(fixed);
    }

    @PutMapping("/{id}")
    public VinculoResponse update(@PathVariable Long condominioId, @PathVariable Long id, @Valid @RequestBody VinculoUpdateRequest req) {
        return service.update(id, condominioId, req);
    }

    @GetMapping("/{id}")
    public VinculoResponse getById(@PathVariable Long condominioId, @PathVariable Long id) {
        return service.getById(id, condominioId);
    }

    @GetMapping
    public List<VinculoResponse> list(@PathVariable Long condominioId) {
        return service.listByCondominio(condominioId);
    }

    @GetMapping("/unidade/{unidadeId}")
    public List<VinculoResponse> listByUnidade(@PathVariable Long condominioId, @PathVariable Long unidadeId) {
        return service.listByUnidade(condominioId, unidadeId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long condominioId, @PathVariable Long id) {
        service.delete(id, condominioId);
    }
}
