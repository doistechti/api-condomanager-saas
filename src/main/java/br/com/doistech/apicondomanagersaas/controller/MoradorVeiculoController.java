package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.morador.MoradorVeiculoCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.veiculo.VeiculoResponse;
import br.com.doistech.apicondomanagersaas.dto.veiculo.VeiculoUpdateRequest;
import br.com.doistech.apicondomanagersaas.service.MoradorVeiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/morador/veiculos")
@RequiredArgsConstructor
public class MoradorVeiculoController {

    private final MoradorVeiculoService service;

    @GetMapping
    @PreAuthorize("hasRole('MORADOR')")
    public List<VeiculoResponse> list(Authentication auth) {
        return service.listar(auth.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MORADOR')")
    public VeiculoResponse create(Authentication auth, @Valid @RequestBody MoradorVeiculoCreateRequest req) {
        return service.criar(auth.getName(), req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MORADOR')")
    public VeiculoResponse update(Authentication auth, @PathVariable Long id, @Valid @RequestBody VeiculoUpdateRequest req) {
        return service.atualizar(auth.getName(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('MORADOR')")
    public void delete(Authentication auth, @PathVariable Long id) {
        service.deletar(auth.getName(), id);
    }
}
