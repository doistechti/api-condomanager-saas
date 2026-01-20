package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.veiculo.VeiculoCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.veiculo.VeiculoResponse;
import br.com.doistech.apicondomanagersaas.dto.veiculo.VeiculoUpdateRequest;
import br.com.doistech.apicondomanagersaas.service.VeiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/condominios/{condominioId}/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    private final VeiculoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VeiculoResponse create(@PathVariable Long condominioId, @Valid @RequestBody VeiculoCreateRequest req) {
        VeiculoCreateRequest fixed = new VeiculoCreateRequest(condominioId, req.pessoaId(), req.placa(), req.modelo(), req.cor(), req.tipo());
        return service.create(fixed);
    }

    @PutMapping("/{id}")
    public VeiculoResponse update(@PathVariable Long condominioId, @PathVariable Long id, @Valid @RequestBody VeiculoUpdateRequest req) {
        return service.update(id, condominioId, req);
    }

    @GetMapping("/{id}")
    public VeiculoResponse getById(@PathVariable Long condominioId, @PathVariable Long id) {
        return service.getById(id, condominioId);
    }

    @GetMapping
    public List<VeiculoResponse> list(@PathVariable Long condominioId) {
        return service.listByCondominio(condominioId);
    }

    @GetMapping("/pessoa/{pessoaId}")
    public List<VeiculoResponse> listByPessoa(@PathVariable Long condominioId, @PathVariable Long pessoaId) {
        return service.listByPessoa(condominioId, pessoaId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long condominioId, @PathVariable Long id) {
        service.delete(id, condominioId);
    }
}
