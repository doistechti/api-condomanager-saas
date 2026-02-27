package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.MoradorTipo;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeResponse;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeUpdateRequest;
import br.com.doistech.apicondomanagersaas.service.PessoaUnidadeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/condominios/{condominioId}/moradores")
public class MoradorController {

    private final PessoaUnidadeService service;

    public MoradorController(PessoaUnidadeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PessoaUnidadeResponse>> listar(@PathVariable Long condominioId) {
        return ResponseEntity.ok(service.listMoradores(condominioId));
    }

    @PostMapping
    public ResponseEntity<PessoaUnidadeResponse> criar(
            @PathVariable Long condominioId,
            @Valid @RequestBody PessoaUnidadeCreateRequest req
    ) {
        // ✅ trava tenant pelo path
        // regra do "tipo":
        // - moradorTipo=PROPRIETARIO => ehProprietario=true e ehMorador=true (proprietário que mora)
        // - demais => ehProprietario=false e ehMorador=true
        boolean isTipoProprietario = req.moradorTipo() == MoradorTipo.PROPRIETARIO;

        PessoaUnidadeCreateRequest fixed = new PessoaUnidadeCreateRequest(
                condominioId,
                req.unidadeId(),
                req.pessoaId(),
                req.nome(),
                req.cpfCnpj(),
                req.email(),
                req.telefone(),
                isTipoProprietario,     // ehProprietario
                true,                   // ehMorador
                req.moradorTipo(),
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
        boolean isTipoProprietario = req.moradorTipo() == MoradorTipo.PROPRIETARIO;

        PessoaUnidadeUpdateRequest fixed = new PessoaUnidadeUpdateRequest(
                req.nome(),
                req.cpfCnpj(),
                req.email(),
                req.telefone(),
                isTipoProprietario, // ehProprietario
                true,               // ehMorador
                req.moradorTipo(),
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

    @PostMapping("/{id}/convite")
    public ResponseEntity<PessoaUnidadeResponse> enviarConvite(@PathVariable Long condominioId, @PathVariable Long id) {
        return ResponseEntity.ok(service.enviarConvite(id, condominioId));
    }
}