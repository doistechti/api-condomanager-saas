package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.assinatura.Assinatura;
import br.com.doistech.apicondomanagersaas.domain.assinatura.AssinaturaStatus;
import br.com.doistech.apicondomanagersaas.dto.assinatura.AssinaturaCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.assinatura.AssinaturaResponse;
import br.com.doistech.apicondomanagersaas.dto.assinatura.AssinaturaUpdateRequest;
import br.com.doistech.apicondomanagersaas.repository.AssinaturaRepository;
import br.com.doistech.apicondomanagersaas.repository.CondominioRepository;
import br.com.doistech.apicondomanagersaas.repository.PlanoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssinaturaService {

    private final AssinaturaRepository assinaturaRepository;
    private final CondominioRepository condominioRepository;
    private final PlanoRepository planoRepository;

    @Transactional(readOnly = true)
    public List<AssinaturaResponse> listar() {
        return assinaturaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssinaturaResponse buscarPorId(Long id) {
        var a = assinaturaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assinatura não encontrada: " + id));
        return toResponse(a);
    }

    @Transactional
    public AssinaturaResponse criar(AssinaturaCreateRequest req) {
        // Validações simples e seguras
        var condominio = condominioRepository.findById(req.condominioId())
                .orElseThrow(() -> new EntityNotFoundException("Condomínio não encontrado: " + req.condominioId()));

        var plano = planoRepository.findById(req.planoId())
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado: " + req.planoId()));

        var status = (req.status() != null) ? req.status() : AssinaturaStatus.ATIVO;
        var inicio = (req.dataInicio() != null) ? req.dataInicio() : LocalDate.now();
        var venc = (req.dataVencimento() != null) ? req.dataVencimento() : inicio.plusYears(1);

        // Regra: 1 assinatura ATIVA por condomínio
        if (status == AssinaturaStatus.ATIVO &&
                assinaturaRepository.existsByCondominioIdAndStatus(condominio.getId(), AssinaturaStatus.ATIVO)) {
            throw new IllegalStateException("Condomínio já possui uma assinatura ativa.");
        }

        var assinatura = Assinatura.builder()
                .condominio(condominio)
                .plano(plano)
                .status(status)
                .dataInicio(inicio)
                .dataVencimento(venc)
                .mercadoPagoId(req.mercadoPagoId())
                .build();

        assinaturaRepository.save(assinatura);
        return toResponse(assinatura);
    }

    @Transactional
    public AssinaturaResponse atualizar(Long id, AssinaturaUpdateRequest req) {
        var assinatura = assinaturaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assinatura não encontrada: " + id));

        if (req.planoId() != null) {
            var plano = planoRepository.findById(req.planoId())
                    .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado: " + req.planoId()));
            assinatura.setPlano(plano);
        }

        if (req.status() != null) {
            // Regra: se tentando colocar ATIVO, garantir que não existe outro ATIVO no mesmo condomínio
            if (req.status() == AssinaturaStatus.ATIVO) {
                var condominioId = assinatura.getCondominio().getId();
                var jaTemAtiva = assinaturaRepository.findTopByCondominioIdAndStatusOrderByDataVencimentoDesc(condominioId, AssinaturaStatus.ATIVO)
                        .filter(a -> !a.getId().equals(assinatura.getId()))
                        .isPresent();

                if (jaTemAtiva) {
                    throw new IllegalStateException("Condomínio já possui outra assinatura ativa.");
                }
            }
            assinatura.setStatus(req.status());
        }

        if (req.dataVencimento() != null) {
            assinatura.setDataVencimento(req.dataVencimento());
        }

        if (req.mercadoPagoId() != null) {
            assinatura.setMercadoPagoId(req.mercadoPagoId());
        }

        return toResponse(assinatura);
    }

    @Transactional
    public void deletar(Long id) {
        if (!assinaturaRepository.existsById(id)) {
            throw new EntityNotFoundException("Assinatura não encontrada: " + id);
        }
        assinaturaRepository.deleteById(id);
    }

    private AssinaturaResponse toResponse(Assinatura a) {
        // cuidado: LAZY -> acessar nomes exige sessão (aqui estamos dentro de transação)
        return new AssinaturaResponse(
                a.getId(),
                a.getCondominio().getId(),
                a.getCondominio().getNome(),
                a.getPlano().getId(),
                a.getPlano().getNome(),
                a.getPlano().getPreco(),
                a.getStatus(),
                a.getDataInicio(),
                a.getDataVencimento(),
                a.getMercadoPagoId()
        );
    }
}
