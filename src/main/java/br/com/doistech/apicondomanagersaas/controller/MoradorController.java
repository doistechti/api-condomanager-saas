package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.MoradorTipo;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeResponse;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeUpdateRequest;
import br.com.doistech.apicondomanagersaas.service.PessoaUnidadeService;
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
            @RequestBody PessoaUnidadeCreateRequest req
    ) {
        validateCreateRequest(req);

        MoradorTipo moradorTipo = req.moradorTipo() != null ? req.moradorTipo() : MoradorTipo.INQUILINO;
        boolean isTipoProprietario = moradorTipo == MoradorTipo.PROPRIETARIO;
        boolean principal = Boolean.TRUE.equals(req.principal());

        PessoaUnidadeCreateRequest fixed = new PessoaUnidadeCreateRequest(
                condominioId,
                req.unidadeId(),
                req.pessoaId(),
                req.nome(),
                req.cpfCnpj(),
                req.email(),
                req.telefone(),
                isTipoProprietario,
                true,
                moradorTipo,
                principal,
                req.dataInicio(),
                req.dataFim()
        );

        return ResponseEntity.ok(service.create(fixed));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PessoaUnidadeResponse> atualizar(
            @PathVariable Long condominioId,
            @PathVariable Long id,
            @RequestBody PessoaUnidadeUpdateRequest req
    ) {
        validateUpdateRequest(req);

        MoradorTipo moradorTipo = req.moradorTipo() != null ? req.moradorTipo() : MoradorTipo.INQUILINO;
        boolean isTipoProprietario = moradorTipo == MoradorTipo.PROPRIETARIO;
        boolean principal = Boolean.TRUE.equals(req.principal());

        PessoaUnidadeUpdateRequest fixed = new PessoaUnidadeUpdateRequest(
                req.nome(),
                req.cpfCnpj(),
                req.email(),
                req.telefone(),
                isTipoProprietario,
                true,
                moradorTipo,
                principal,
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

    private void validateCreateRequest(PessoaUnidadeCreateRequest req) {
        if (req == null) {
            throw new BadRequestException("Body da requisicao e obrigatorio.");
        }
        if (req.unidadeId() == null) {
            throw new BadRequestException("unidadeId e obrigatorio.");
        }
        if (req.pessoaId() == null && (req.nome() == null || req.nome().isBlank())) {
            throw new BadRequestException("Nome e obrigatorio para criar um morador novo.");
        }
    }

    private void validateUpdateRequest(PessoaUnidadeUpdateRequest req) {
        if (req == null) {
            throw new BadRequestException("Body da requisicao e obrigatorio.");
        }
    }
}
