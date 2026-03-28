package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.ForbiddenException;
import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.MoradorTipo;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.domain.vinculo.TipoMoradia;
import br.com.doistech.apicondomanagersaas.domain.vinculo.VinculoUnidade;
import br.com.doistech.apicondomanagersaas.dto.morador.MoradorScopeResponse;
import br.com.doistech.apicondomanagersaas.repository.PessoaUnidadeRepository;
import br.com.doistech.apicondomanagersaas.repository.UsuarioRepository;
import br.com.doistech.apicondomanagersaas.repository.VinculoUnidadeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MoradorScopeService {

    private final UsuarioRepository usuarioRepository;
    private final PessoaUnidadeRepository pessoaUnidadeRepository;
    private final VinculoUnidadeRepository vinculoUnidadeRepository;
    private final CondominioService condominioService;

    @Transactional
    public MoradorScopeResponse getScope(String email) {
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado"));

        if (usuario.getCondominioId() == null) {
            throw new ForbiddenException("Acesso negado: usuario sem condominio associado");
        }

        List<PessoaUnidade> vinculos = pessoaUnidadeRepository
                .findAllByUsuarioIdAndEhMoradorTrueAndAtivoTrue(usuario.getId());

        if (vinculos.isEmpty()) {
            throw new ForbiddenException("Acesso negado: usuario nao possui vinculo de morador ativo");
        }

        boolean anyMismatch = vinculos.stream().anyMatch(v ->
                v.getCondominio() == null
                        || v.getCondominio().getId() == null
                        || !usuario.getCondominioId().equals(v.getCondominio().getId())
        );
        if (anyMismatch) {
            throw new ForbiddenException("Acesso negado: vinculo nao pertence ao condominio do usuario");
        }

        var principalOpt = vinculos.stream()
                .filter(v -> Boolean.TRUE.equals(v.getPrincipal()))
                .min(Comparator.comparing(PessoaUnidade::getId));

        PessoaUnidade principal = principalOpt.orElseGet(() ->
                vinculos.stream().min(Comparator.comparing(PessoaUnidade::getId)).orElseThrow()
        );

        List<Long> unidadeIds = vinculos.stream()
                .map(v -> v.getUnidade().getId())
                .distinct()
                .sorted()
                .toList();

        var condominio = condominioService.getEntity(usuario.getCondominioId());
        Long vinculoPrincipalId = principal.getId();
        try {
            vinculoPrincipalId = resolveVinculoOperacional(principal).getId();
        } catch (RuntimeException ex) {
            log.warn(
                    "Falha ao resolver vinculo operacional do morador. usuarioId={}, pessoaUnidadeId={}, condominioId={}",
                    usuario.getId(),
                    principal.getId(),
                    usuario.getCondominioId(),
                    ex
            );
        }

        return new MoradorScopeResponse(
                usuario.getId(),
                usuario.getCondominioId(),
                condominio.getNome(),
                principal.getPessoa().getId(),
                vinculoPrincipalId,
                unidadeIds
        );
    }

    private VinculoUnidade resolveVinculoOperacional(PessoaUnidade principal) {
        Long condominioId = principal.getCondominio().getId();
        Long pessoaId = principal.getPessoa().getId();
        Long unidadeId = principal.getUnidade().getId();

        var vinculo = findVinculoOperacional(condominioId, pessoaId, unidadeId)
                .orElseGet(() -> vinculoUnidadeRepository.save(
                        VinculoUnidade.builder()
                                .condominio(principal.getCondominio())
                                .unidade(principal.getUnidade())
                                .pessoa(principal.getPessoa())
                                .isMorador(true)
                                .isProprietario(Boolean.TRUE.equals(principal.getEhProprietario()))
                                .tipoMoradia(resolveTipoMoradia(principal))
                                .dataInicio(principal.getDataInicio() != null ? principal.getDataInicio() : LocalDate.now())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()
                ));

        boolean changed = false;

        if (!vinculo.isMorador()) {
            vinculo.setMorador(true);
            changed = true;
        }

        boolean shouldBeProprietario = Boolean.TRUE.equals(principal.getEhProprietario());
        if (vinculo.isProprietario() != shouldBeProprietario) {
            vinculo.setProprietario(shouldBeProprietario);
            changed = true;
        }

        TipoMoradia tipoMoradia = resolveTipoMoradia(principal);
        if (vinculo.getTipoMoradia() != tipoMoradia) {
            vinculo.setTipoMoradia(tipoMoradia);
            changed = true;
        }

        if (vinculo.getDataInicio() == null) {
            vinculo.setDataInicio(principal.getDataInicio() != null ? principal.getDataInicio() : LocalDate.now());
            changed = true;
        }

        if (vinculo.getCreatedAt() == null) {
            vinculo.setCreatedAt(LocalDateTime.now());
            changed = true;
        }

        if (changed) {
            vinculo.setUpdatedAt(LocalDateTime.now());
            return vinculoUnidadeRepository.save(vinculo);
        }

        return vinculo;
    }

    private java.util.Optional<VinculoUnidade> findVinculoOperacional(Long condominioId, Long pessoaId, Long unidadeId) {
        List<VinculoUnidade> vinculos = vinculoUnidadeRepository
                .findAllByCondominioIdAndPessoaIdAndUnidadeIdOrderByDataFimAscUpdatedAtDescCreatedAtDescIdDesc(
                        condominioId,
                        pessoaId,
                        unidadeId
                );

        if (vinculos.size() > 1) {
            log.warn(
                    "Duplicidade em vinculos_unidade para condominioId={}, pessoaId={}, unidadeId={}. Usando vinculoId={}.",
                    condominioId,
                    pessoaId,
                    unidadeId,
                    vinculos.get(0).getId()
            );
        }

        return vinculos.stream().findFirst();
    }

    private TipoMoradia resolveTipoMoradia(PessoaUnidade principal) {
        if (Boolean.TRUE.equals(principal.getEhProprietario())
                || principal.getMoradorTipo() == MoradorTipo.PROPRIETARIO) {
            return TipoMoradia.PROPRIETARIO;
        }

        return TipoMoradia.INQUILINO;
    }
}
