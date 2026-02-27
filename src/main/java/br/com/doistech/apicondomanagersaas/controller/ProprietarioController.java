package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeResponse;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeUpdateRequest;
import br.com.doistech.apicondomanagersaas.service.PessoaUnidadeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/condominios/{condominioId}/proprietarios")
public class ProprietarioController {

    private final PessoaUnidadeService service;

    public ProprietarioController(PessoaUnidadeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PessoaUnidadeResponse>> listar(@PathVariable Long condominioId) {
        return ResponseEntity.ok(service.listProprietarios(condominioId));
    }

    @PostMapping
    public ResponseEntity<PessoaUnidadeResponse> criar(
            @PathVariable Long condominioId,
            @Valid @RequestBody PessoaUnidadeCreateRequest req
    ) {
        // ✅ proprietário "puro": ehProprietario=true, ehMorador=false
        PessoaUnidadeCreateRequest fixed = new PessoaUnidadeCreateRequest(
                condominioId,
                req.unidadeId(),
                req.pessoaId(),
                req.nome(),
                req.cpfCnpj(),
                req.email(),
                req.telefone(),
                true,   // ehProprietario
                false,  // ehMorador
                null,   // moradorTipo
                req.principal(),
                req.dataInicio(),
                req.dataFim()
        );

        return ResponseEntity.ok(service.create(fixed));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PessoaUnidadeResponse> atualizar(
            @PathVariable Long condominioId,
            @PathVariable Long id,
            @Valid @RequestBody PessoaUnidadeUpdateRequest req
    ) {
        PessoaUnidadeUpdateRequest fixed = new PessoaUnidadeUpdateRequest(
                req.nome(),
                req.cpfCnpj(),
                req.email(),
                req.telefone(),
                true,   // ehProprietario
                false,  // ehMorador
                null,   // moradorTipo
                req.principal(),
                req.dataInicio(),
                req.dataFim()
        );

        return ResponseEntity.ok(service.update(id, condominioId, fixed));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long condominioId, @PathVariable Long id) {
        service.delete(id, condominioId);
        return ResponseEntity.noContent().build();
    }
}