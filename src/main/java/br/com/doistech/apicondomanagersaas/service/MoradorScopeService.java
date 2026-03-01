package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.ForbiddenException;
import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.dto.morador.MoradorScopeResponse;
import br.com.doistech.apicondomanagersaas.repository.PessoaUnidadeRepository;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import br.com.doistech.apicondomanagersaas.repository.VinculoUnidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MoradorScopeService {

    private final UsuarioRepository usuarioRepository;
    private final PessoaUnidadeRepository pessoaUnidadeRepository;
    private final VinculoUnidadeRepository vinculoUnidadeRepository;
    private final CondominioService condominioService;

    public MoradorScopeResponse getScope(String email) {
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        boolean isMorador = usuario.getRoles().stream()
                .anyMatch(r -> "MORADOR".equals(r.getNome()));

        if (!isMorador) {
            throw new ForbiddenException("Acesso negado: perfil do usuário não é MORADOR");
        }

        if (usuario.getCondominioId() == null) {
            throw new ForbiddenException("Acesso negado: usuário sem condomínio associado");
        }

        List<PessoaUnidade> vinculos = pessoaUnidadeRepository
                .findAllByUsuarioIdAndEhMoradorTrueAndAtivoTrue(usuario.getId());

        if (vinculos.isEmpty()) {
            throw new ForbiddenException("Acesso negado: usuário não possui vínculo de morador ativo");
        }

        // ✅ Tenant isolation: vínculos precisam pertencer ao mesmo condomínio do usuário
        boolean anyMismatch = vinculos.stream().anyMatch(v ->
                v.getCondominio() == null
                        || v.getCondominio().getId() == null
                        || !usuario.getCondominioId().equals(v.getCondominio().getId())
        );
        if (anyMismatch) {
            throw new ForbiddenException("Acesso negado: vínculo não pertence ao condomínio do usuário");
        }

        var principalOpt = vinculos.stream()
                .filter(v -> Boolean.TRUE.equals(v.getPrincipal()))
                .min(Comparator.comparing(PessoaUnidade::getId));

        PessoaUnidade principal = principalOpt.orElseGet(() ->
                vinculos.stream().min(Comparator.comparing(PessoaUnidade::getId)).orElseThrow()
        );

        Long pessoaId = principal.getPessoa().getId();

        List<Long> unidadeIds = vinculos.stream()
                .map(v -> v.getUnidade().getId())
                .distinct()
                .sorted()
                .toList();

        var condominio = condominioService.getEntity(usuario.getCondominioId());

        // ✅ IMPORTANTÍSSIMO: Reservas usam VinculoUnidade (vinculos_unidade), não PessoaUnidade.
        var vinculoOperacional = vinculoUnidadeRepository
                .findByCondominioIdAndPessoaIdAndUnidadeId(
                        usuario.getCondominioId(),
                        principal.getPessoa().getId(),
                        principal.getUnidade().getId()
                )
                .orElseThrow(() -> new ForbiddenException(
                        "Acesso negado: vínculo operacional (vinculos_unidade) não encontrado para este morador. " +
                                "Rode o bootstrap/semente do MORADOR para criar o vínculo."
                ));

        return new MoradorScopeResponse(
                usuario.getId(),
                usuario.getCondominioId(),
                condominio.getNome(),
                pessoaId,
                vinculoOperacional.getId(), // ✅ agora é o ID correto para reservas
                unidadeIds
        );
    }
}