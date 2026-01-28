package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.assinatura.AssinaturaStatus;
import br.com.doistech.apicondomanagersaas.dto.adminsaas.DashboardStatsResponse;
import br.com.doistech.apicondomanagersaas.dto.adminsaas.RecentCondominioResponse;
import br.com.doistech.apicondomanagersaas.dto.adminsaas.RecentPaymentResponse;
import br.com.doistech.apicondomanagersaas.repository.AssinaturaRepository;
import br.com.doistech.apicondomanagersaas.repository.CondominioRepository;
import br.com.doistech.apicondomanagersaas.repository.PessoaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AdminSaasDashboardService {

    private final CondominioRepository condominioRepository;
    private final AssinaturaRepository assinaturaRepository;
    private final PessoaRepository pessoaRepository;

    public AdminSaasDashboardService(
            CondominioRepository condominioRepository,
            AssinaturaRepository assinaturaRepository,
            PessoaRepository pessoaRepository
    ) {
        this.condominioRepository = condominioRepository;
        this.assinaturaRepository = assinaturaRepository;
        this.pessoaRepository = pessoaRepository;
    }

    public DashboardStatsResponse getStats() {
        long totalCondominios = condominioRepository.count();
        long totalUsuarios = pessoaRepository.count();

        long assinaturasAtivas = assinaturaRepository.countByStatus(AssinaturaStatus.ATIVO);
        long assinaturasInadimplentes = assinaturaRepository.countByStatus(AssinaturaStatus.INADIMPLENTE);

        BigDecimal receitaMensal = assinaturaRepository.sumPrecoByStatus(AssinaturaStatus.ATIVO);
        if (receitaMensal == null) receitaMensal = BigDecimal.ZERO;

        return new DashboardStatsResponse(
                totalCondominios,
                receitaMensal,
                assinaturasAtivas,
                assinaturasInadimplentes,
                totalUsuarios
        );
    }

    public List<RecentCondominioResponse> getRecentCondominios(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        var pageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return condominioRepository.findRecentCondominios(pageable);
    }

    public List<RecentPaymentResponse> getRecentPayments(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        var pageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "updatedAt"));

        return assinaturaRepository.findRecentPayments(pageable).stream()
                .map(row -> new RecentPaymentResponse(
                        row.id(),
                        row.condominioNome(),
                        row.valor(),
                        mapPaymentLabel(row.status())
                ))
                .toList();
    }

    private String mapPaymentLabel(AssinaturaStatus status) {
        if (status == null) return "Pendente";

        return switch (status) {
            case ATIVO -> "Pago";
            case CANCELADO -> "Cancelado";
            case PENDENTE, INADIMPLENTE -> "Pendente";
        };
    }
}

