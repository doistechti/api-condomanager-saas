package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.espaco.EspacoCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.espaco.EspacoResponse;
import br.com.doistech.apicondomanagersaas.dto.espaco.EspacoUpdateRequest;
import br.com.doistech.apicondomanagersaas.service.EspacoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/condominios/{condominioId}/espacos")
@RequiredArgsConstructor
public class EspacoController {

    private final EspacoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EspacoResponse create(@PathVariable Long condominioId, @Valid @RequestBody EspacoCreateRequest req) {
        EspacoCreateRequest fixed = new EspacoCreateRequest(
                condominioId,
                req.nome(),
                req.descricao(),
                req.regras(),
                req.capacidade(),
                req.necessitaAprovacao(),
                req.ativo(),
                req.tipoReserva(),
                req.prazoAntecedencia()
        );
        return service.create(fixed);
    }

    @PutMapping("/{id}")
    public EspacoResponse update(@PathVariable Long condominioId, @PathVariable Long id, @Valid @RequestBody EspacoUpdateRequest req) {
        return service.update(id, condominioId, req);
    }

    @GetMapping("/{id}")
    public EspacoResponse getById(@PathVariable Long condominioId, @PathVariable Long id) {
        return service.getById(id, condominioId);
    }

    @GetMapping
    public List<EspacoResponse> list(@PathVariable Long condominioId) {
        return service.listByCondominio(condominioId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long condominioId, @PathVariable Long id) {
        service.delete(id, condominioId);
    }
}
