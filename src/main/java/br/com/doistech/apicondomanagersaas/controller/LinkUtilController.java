package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.linkutil.LinkUtilCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.linkutil.LinkUtilResponse;
import br.com.doistech.apicondomanagersaas.dto.linkutil.LinkUtilUpdateRequest;
import br.com.doistech.apicondomanagersaas.service.LinkUtilService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/condominios/{condominioId}/links")
public class LinkUtilController {

    private final LinkUtilService service;

    public LinkUtilController(LinkUtilService service) {
        this.service = service;
    }

    @GetMapping
    public List<LinkUtilResponse> list(@PathVariable Long condominioId) {
        return service.listByCondominio(condominioId);
    }

    @GetMapping("/{id}")
    public LinkUtilResponse getById(@PathVariable Long condominioId, @PathVariable Long id) {
        return service.getById(condominioId, id);
    }

    @PostMapping
    public LinkUtilResponse create(@PathVariable Long condominioId,
                                   @Valid @RequestBody LinkUtilCreateRequest body) {
        // Mantém padrão do projeto: condominioId vem tanto no path quanto no body.
        LinkUtilCreateRequest req = new LinkUtilCreateRequest(
                condominioId,
                body.titulo(),
                body.descricao(),
                body.url(),
                body.categoria(),
                body.ordem(),
                body.ativo()
        );
        return service.create(req);
    }

    @PutMapping("/{id}")
    public LinkUtilResponse update(@PathVariable Long condominioId,
                                   @PathVariable Long id,
                                   @Valid @RequestBody LinkUtilUpdateRequest body) {
        return service.update(condominioId, id, body);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long condominioId, @PathVariable Long id) {
        service.delete(condominioId, id);
    }
}