package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.assinatura.Assinatura;
import br.com.doistech.apicondomanagersaas.domain.assinatura.AssinaturaStatus;
import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.dto.condominio.CondominioCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.condominio.CondominioResponse;
import br.com.doistech.apicondomanagersaas.dto.condominio.CondominioUpdateRequest;
import br.com.doistech.apicondomanagersaas.repository.AssinaturaRepository;
import br.com.doistech.apicondomanagersaas.repository.CondominioRepository;
import br.com.doistech.apicondomanagersaas.repository.PlanoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CondominioService {

    private final CondominioRepository repository;
    private final PlanoRepository planoRepository;

    // ✅ Novo: repositório de assinatura
    private final AssinaturaRepository assinaturaRepository;

    @Transactional
    public CondominioResponse create(CondominioCreateRequest req) {
        Condominio entity = Condominio.builder()
                .nome(req.nome())
                .cnpj(req.cnpj())
                .responsavel(req.responsavel())
                .email(req.email())
                .telefone(req.telefone())
                .endereco(req.endereco())
                .tipoSetor(req.tipoSetor())
                .logoUrl(req.logoUrl())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 1) Aplica plano no condomínio (se vier planoId)
        applyPlano(req.planoId(), entity);

        // 2) Salva o condomínio primeiro para obter o ID (FK da assinatura)
        Condominio saved = repository.save(entity);

        // 3) Se veio plano no cadastro -> cria assinatura automaticamente (MVP)
        //    Regra: não permite criar outra ATIVA se já existir (por segurança)
        if (saved.getPlano() != null) {
            criarAssinaturaInicialSeNaoExistir(saved);
        }

        return toResponse(saved);
    }

    @Transactional
    public CondominioResponse update(Long id, CondominioUpdateRequest req) {
        Condominio entity = getEntityWithPlano(id);

        // Guardamos o plano anterior para detectar troca
        Long planoAnteriorId = entity.getPlano() != null ? entity.getPlano().getId() : null;

        entity.setNome(req.nome());
        entity.setCnpj(req.cnpj());
        entity.setResponsavel(req.responsavel());
        entity.setEmail(req.email());
        entity.setTelefone(req.telefone());
        entity.setEndereco(req.endereco());
        entity.setTipoSetor(req.tipoSetor());
        entity.setLogoUrl(req.logoUrl());
        entity.setUpdatedAt(LocalDateTime.now());

        applyPlano(req.planoId(), entity);

        Condominio saved = repository.save(entity);

        // Se o plano mudou, atualiza assinatura vigente (MVP simples)
        Long planoNovoId = saved.getPlano() != null ? saved.getPlano().getId() : null;
        boolean mudouPlano = (planoAnteriorId == null && planoNovoId != null)
                || (planoAnteriorId != null && !planoAnteriorId.equals(planoNovoId));

        if (mudouPlano) {
            atualizarPlanoDaAssinaturaVigente(saved);
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CondominioResponse getById(Long id) {
        return toResponse(getEntityWithPlano(id));
    }

    @Transactional(readOnly = true)
    public List<CondominioResponse> list() {
        return repository.findAllWithPlano()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        Condominio entity = getEntity(id);

        // (opcional) se quiser, pode cancelar assinaturas antes de deletar
        // por enquanto, vamos manter simples e deletar o condomínio direto.
        repository.delete(entity);
    }

    Condominio getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Condomínio não encontrado: " + id));
    }

    private Condominio getEntityWithPlano(Long id) {
        return repository.findByIdWithPlano(id)
                .orElseThrow(() -> new NotFoundException("Condomínio não encontrado: " + id));
    }

    private void applyPlano(Long planoId, Condominio entity) {
        if (planoId == null) {
            entity.setPlano(null);
            return;
        }

        var plano = planoRepository.findById(planoId)
                .orElseThrow(() -> new NotFoundException("Plano não encontrado: " + planoId));

        entity.setPlano(plano);
    }

    /**
     * Cria uma assinatura inicial ATIVA para o condomínio, caso ainda não exista.
     * Regra MVP: data_inicio = hoje, data_vencimento = hoje + 1 ano.
     */
    private void criarAssinaturaInicialSeNaoExistir(Condominio condominio) {
        boolean jaExisteAtiva = assinaturaRepository.existsByCondominioIdAndStatus(
                condominio.getId(),
                AssinaturaStatus.ATIVO
        );

        if (jaExisteAtiva) {
            return; // segurança: não cria duplicado
        }

        LocalDate hoje = LocalDate.now();

        Assinatura assinatura = Assinatura.builder()
                .condominio(condominio)
                .plano(condominio.getPlano())
                .status(AssinaturaStatus.ATIVO)
                .dataInicio(hoje)
                .dataVencimento(hoje.plusYears(1))
                .mercadoPagoId(null) // não integra agora
                .build();

        assinaturaRepository.save(assinatura);
    }

    /**
     * MVP: ao trocar o plano do condomínio, atualiza o plano na assinatura vigente (mais recente).
     * Alternativa futura: cancelar a assinatura anterior e criar uma nova (histórico).
     */
    private void atualizarPlanoDaAssinaturaVigente(Condominio condominio) {
        if (condominio.getPlano() == null) {
            return; // sem plano, não faz nada
        }

        var assinaturaOpt = assinaturaRepository.findTopByCondominioIdOrderByDataVencimentoDesc(condominio.getId());

        if (assinaturaOpt.isEmpty()) {
            // Se não tem assinatura ainda (caso raro), cria a inicial
            criarAssinaturaInicialSeNaoExistir(condominio);
            return;
        }

        Assinatura assinatura = assinaturaOpt.get();
        assinatura.setPlano(condominio.getPlano());

        // Mantemos status e datas como estão (simples).
        // Se você quiser, pode "renovar" vencimento aqui ao trocar de plano.
        assinaturaRepository.save(assinatura);
    }

    private CondominioResponse toResponse(Condominio entity) {
        Long planoId = entity.getPlano() != null ? entity.getPlano().getId() : null;
        String planoNome = entity.getPlano() != null ? entity.getPlano().getNome() : null;

        // ✅ Ainda não implementamos contagem real de unidades aqui (fica pra próxima)
        long unidadesCount = 0L;

        // ✅ Agora busca o status da assinatura vigente
        AssinaturaStatus assinaturaStatus = assinaturaRepository
                .findTopByCondominioIdOrderByDataVencimentoDesc(entity.getId())
                .map(Assinatura::getStatus)
                .orElse(null);

        return new CondominioResponse(
                entity.getId(),
                entity.getNome(),
                entity.getCnpj(),
                entity.getResponsavel(),
                entity.getEmail(),
                entity.getTelefone(),
                entity.getEndereco(),
                entity.getTipoSetor(),
                entity.getLogoUrl(),
                planoId,
                planoNome,
                unidadesCount,
                assinaturaStatus
        );
    }
}