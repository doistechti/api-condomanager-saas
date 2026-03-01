package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.ForbiddenException;
import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.veiculo.Veiculo;
import br.com.doistech.apicondomanagersaas.dto.morador.MoradorVeiculoCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.veiculo.VeiculoResponse;
import br.com.doistech.apicondomanagersaas.dto.veiculo.VeiculoUpdateRequest;
import br.com.doistech.apicondomanagersaas.mapper.VeiculoMapper;
import br.com.doistech.apicondomanagersaas.repository.CondominioRepository;
import br.com.doistech.apicondomanagersaas.repository.PessoaRepository;
import br.com.doistech.apicondomanagersaas.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MoradorVeiculoService {

    private final MoradorScopeService moradorScopeService;
    private final VeiculoRepository veiculoRepository;
    private final CondominioRepository condominioRepository;
    private final PessoaRepository pessoaRepository;
    private final VeiculoMapper veiculoMapper;

    private String normalizePlaca(String placa) {
        if (placa == null) return null;
        return placa.trim().toUpperCase().replaceAll("\\s+", "");
    }

    @Transactional(readOnly = true)
    public List<VeiculoResponse> listar(String email) {
        var scope = moradorScopeService.getScope(email);
        return veiculoRepository.findAllByPessoaIdAndCondominioId(scope.pessoaId(), scope.condominioId())
                .stream()
                .map(veiculoMapper::toResponse)
                .toList();
    }

    @Transactional
    public VeiculoResponse criar(String email, MoradorVeiculoCreateRequest req) {
        var scope = moradorScopeService.getScope(email);

        String placa = normalizePlaca(req.placa());
        if (veiculoRepository.existsByCondominioIdAndPlacaIgnoreCase(scope.condominioId(), placa)) {
            throw new ForbiddenException("Já existe um veículo com esta placa no condomínio");
        }

        var condominio = condominioRepository.findById(scope.condominioId())
                .orElseThrow(() -> new NotFoundException("Condomínio não encontrado"));

        var pessoa = pessoaRepository.findById(scope.pessoaId())
                .orElseThrow(() -> new NotFoundException("Pessoa do morador não encontrada"));

        Veiculo v = Veiculo.builder()
                .condominio(condominio)
                .pessoa(pessoa)
                .placa(placa)
                .modelo(req.modelo())
                .cor(req.cor())
                .tipo(req.tipo() == null ? "carro" : req.tipo())
                .build();

        return veiculoMapper.toResponse(veiculoRepository.save(v));
    }

    @Transactional
    public VeiculoResponse atualizar(String email, Long id, VeiculoUpdateRequest req) {
        var scope = moradorScopeService.getScope(email);

        Veiculo v = veiculoRepository.findByIdAndCondominioId(id, scope.condominioId())
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado"));

        // ✅ ownership: veículo precisa ser do morador (pessoaId do escopo)
        if (!v.getPessoa().getId().equals(scope.pessoaId())) {
            throw new ForbiddenException("Acesso negado: veículo não pertence ao morador");
        }

        String placa = normalizePlaca(req.placa());
        if (veiculoRepository.existsByCondominioIdAndPlacaIgnoreCaseAndIdNot(scope.condominioId(), placa, id)) {
            throw new ForbiddenException("Já existe um veículo com esta placa no condomínio");
        }

        v.setPlaca(placa);
        v.setModelo(req.modelo());
        v.setCor(req.cor());
        v.setTipo(req.tipo());

        return veiculoMapper.toResponse(veiculoRepository.save(v));
    }

    @Transactional
    public void deletar(String email, Long id) {
        var scope = moradorScopeService.getScope(email);

        Veiculo v = veiculoRepository.findByIdAndCondominioId(id, scope.condominioId())
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado"));

        if (!v.getPessoa().getId().equals(scope.pessoaId())) {
            throw new ForbiddenException("Acesso negado: veículo não pertence ao morador");
        }

        veiculoRepository.delete(v);
    }
}
