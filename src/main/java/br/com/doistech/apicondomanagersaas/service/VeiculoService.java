package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.veiculo.Veiculo;
import br.com.doistech.apicondomanagersaas.dto.veiculo.VeiculoCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.veiculo.VeiculoUpdateRequest;
import br.com.doistech.apicondomanagersaas.dto.veiculo.VeiculoResponse;
import br.com.doistech.apicondomanagersaas.mapper.VeiculoMapper;
import br.com.doistech.apicondomanagersaas.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository repository;
    private final CondominioService condominioService;
    private final PessoaService pessoaService;
    private final VeiculoMapper mapper;

    public VeiculoResponse create(VeiculoCreateRequest req) {
        Veiculo entity = Veiculo.builder()
                .condominio(condominioService.getEntity(req.condominioId()))
                .pessoa(pessoaService.getEntity(req.pessoaId(), req.condominioId()))
                .placa(req.placa())
                .modelo(req.modelo())
                .cor(req.cor())
                .tipo(req.tipo())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return mapper.toResponse(repository.save(entity));
    }

    public VeiculoResponse update(Long id, Long condominioId, VeiculoUpdateRequest req) {
        Veiculo entity = getEntity(id, condominioId);
        entity.setPlaca(req.placa());
        entity.setModelo(req.modelo());
        entity.setCor(req.cor());
        entity.setTipo(req.tipo());
        entity.setUpdatedAt(LocalDateTime.now());
        return mapper.toResponse(repository.save(entity));
    }

    public VeiculoResponse getById(Long id, Long condominioId) {
        return mapper.toResponse(getEntity(id, condominioId));
    }

    public List<VeiculoResponse> listByCondominio(Long condominioId) {
        return repository.findAllByCondominioId(condominioId).stream().map(mapper::toResponse).toList();
    }

    public List<VeiculoResponse> listByPessoa(Long condominioId, Long pessoaId) {
        return repository.findAllByPessoaIdAndCondominioId(pessoaId, condominioId).stream().map(mapper::toResponse).toList();
    }

    public void delete(Long id, Long condominioId) {
        repository.delete(getEntity(id, condominioId));
    }

    private Veiculo getEntity(Long id, Long condominioId) {
        return repository.findByIdAndCondominioId(id, condominioId)
                .orElseThrow(() -> new NotFoundException("Veículo não encontrado: " + id));
    }
}
