package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.reserva.Reserva;
import br.com.doistech.apicondomanagersaas.domain.reserva.ReservaStatus;
import br.com.doistech.apicondomanagersaas.domain.veiculo.Veiculo;
import br.com.doistech.apicondomanagersaas.domain.pessoaUnidade.PessoaUnidade;
import br.com.doistech.apicondomanagersaas.dto.dashboardcondominio.CadastroRecenteResponse;
import br.com.doistech.apicondomanagersaas.dto.dashboardcondominio.DashboardStatsResponse;
import br.com.doistech.apicondomanagersaas.dto.dashboardcondominio.ReservaRecenteResponse;
import br.com.doistech.apicondomanagersaas.repository.PessoaUnidadeRepository;
import br.com.doistech.apicondomanagersaas.repository.ReservaRepository;
import br.com.doistech.apicondomanagersaas.repository.UnidadeRepository;
import br.com.doistech.apicondomanagersaas.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardCondominioService {

    private final UnidadeRepository unidadeRepository;
    private final PessoaUnidadeRepository pessoaUnidadeRepository;
    private final VeiculoRepository veiculoRepository;
    private final ReservaRepository reservaRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse stats(Long condominioId) {
        long unidades = unidadeRepository.countByCondominioIdAndAtivoTrue(condominioId);
        long moradores = pessoaUnidadeRepository.countByCondominioIdAndEhMoradorTrueAndAtivoTrue(condominioId);
        long veiculos = veiculoRepository.countByCondominioId(condominioId);
        long reservasPendentes = reservaRepository.countByCondominioIdAndStatus(condominioId, ReservaStatus.PENDENTE);

        return new DashboardStatsResponse(unidades, moradores, veiculos, reservasPendentes);
    }

    @Transactional(readOnly = true)
    public List<ReservaRecenteResponse> reservasRecentes(Long condominioId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));

        // Dashboard atual mostra pendentes ordenadas por data (asc)
        List<Reserva> pendentes = reservaRepository.findAllByCondominioIdAndStatus(condominioId, ReservaStatus.PENDENTE);

        return pendentes.stream()
                .sorted(Comparator.comparing(Reserva::getDataReserva))
                .limit(safeLimit)
                .map(r -> new ReservaRecenteResponse(
                        r.getId(),
                        r.getEspaco() != null ? r.getEspaco().getNome() : null,
                        (r.getVinculo() != null && r.getVinculo().getPessoa() != null) ? r.getVinculo().getPessoa().getNome() : null,
                        r.getDataReserva()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CadastroRecenteResponse> cadastrosRecentes(Long condominioId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));

        List<CadastroRecenteResponse> items = new ArrayList<>();

        // Moradores recentes (PessoaUnidade)
        List<PessoaUnidade> moradores = pessoaUnidadeRepository.findAllByCondominioIdAndEhMoradorTrueAndAtivoTrue(condominioId);
        for (PessoaUnidade pu : moradores) {
            String unidade = "";
            if (pu.getUnidade() != null) {
                String setorNome = (pu.getUnidade().getSetor() != null) ? pu.getUnidade().getSetor().getNome() : null;
                String identificacao = pu.getUnidade().getIdentificacao();
                unidade = setorNome != null && !setorNome.isBlank()
                        ? setorNome + " - " + identificacao
                        : (identificacao != null ? identificacao : "");
            }

            items.add(new CadastroRecenteResponse(
                    String.valueOf(pu.getId()),
                    "Morador",
                    (pu.getPessoa() != null ? pu.getPessoa().getNome() : null),
                    unidade.isBlank() ? null : unidade,
                    pu.getCreatedAt()
            ));
        }

        // Veículos recentes
        List<Veiculo> veiculos = veiculoRepository.findAllByCondominioId(condominioId);
        for (Veiculo v : veiculos) {
            String nome = (v.getModelo() != null ? v.getModelo() : "") + " - " + (v.getPlaca() != null ? v.getPlaca() : "");
            items.add(new CadastroRecenteResponse(
                    String.valueOf(v.getId()),
                    "Veículo",
                    nome.trim(),
                    null,
                    v.getCreatedAt()
            ));
        }

        return items.stream()
                .filter(i -> i.createdAt() != null)
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                .limit(safeLimit)
                .toList();
    }
}