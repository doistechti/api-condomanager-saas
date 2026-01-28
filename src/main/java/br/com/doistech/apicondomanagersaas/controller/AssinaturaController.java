package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.assinatura.AssinaturaCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.assinatura.AssinaturaResponse;
import br.com.doistech.apicondomanagersaas.dto.assinatura.AssinaturaUpdateRequest;
import br.com.doistech.apicondomanagersaas.service.AssinaturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/assinaturas")
public class AssinaturaController {

    private final AssinaturaService assinaturaService;

    @GetMapping
    public List<AssinaturaResponse> listar() {
        return assinaturaService.listar();
    }

    @GetMapping("/{id}")
    public AssinaturaResponse buscarPorId(@PathVariable Long id) {
        return assinaturaService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssinaturaResponse criar(@RequestBody AssinaturaCreateRequest request) {
        return assinaturaService.criar(request);
    }

    @PutMapping("/{id}")
    public AssinaturaResponse atualizar(@PathVariable Long id, @RequestBody AssinaturaUpdateRequest request) {
        return assinaturaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        assinaturaService.deletar(id);
    }
}

