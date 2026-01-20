package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.espaco.Espaco;
import br.com.doistech.apicondomanagersaas.dto.espaco.EspacoCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.espaco.EspacoUpdateRequest;
import br.com.doistech.apicondomanagersaas.dto.espaco.EspacoResponse;
import br.com.doistech.apicondomanagersaas.mapper.EspacoMapper;
import br.com.doistech.apicondomanagersaas.repository.EspacoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EspacoService {

    private final EspacoRepository repository;
    private final CondominioService condominioService;
    private final EspacoMapper mapper;

    public EspacoResponse create(EspacoCreateRequest req) {
        Espaco entity = Espaco.builder()
                .condominio(condominioService.getEntity(req.condominioId()))
                .nome(req.nome())
                .descricao(req.descricao())
                .regras(req.regras())
                .capacidade(req.capacidade())
                .necessitaAprovacao(req.necessitaAprovacao())
                .ativo(req.ativo())
                .tipoReserva(req.tipoReserva())
                .prazoAntecedencia(req.prazoAntecedencia())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return mapper.toResponse(repository.save(entity));
    }

    public EspacoResponse update(Long id, Long condominioId, EspacoUpdateRequest req) {
        Espaco entity = getEntity(id, condominioId);
        entity.setNome(req.nome());
        entity.setDescricao(req.descricao());
        entity.setRegras(req.regras());
        entity.setCapacidade(req.capacidade());
        entity.setNecessitaAprovacao(req.necessitaAprovacao());
        entity.setAtivo(req.ativo());
        entity.setTipoReserva(req.tipoReserva());
        entity.setPrazoAntecedencia(req.prazoAntecedencia());
        entity.setUpdatedAt(LocalDateTime.now());
        return mapper.toResponse(repository.save(entity));
    }

    public EspacoResponse getById(Long id, Long condominioId) {
        return mapper.toResponse(getEntity(id, condominioId));
    }

    public List<EspacoResponse> listByCondominio(Long condominioId) {
        return repository.findAllByCondominioId(condominioId).stream().map(mapper::toResponse).toList();
    }

    public void delete(Long id, Long condominioId) {
        repository.delete(getEntity(id, condominioId));
    }

    public Espaco getEntity(Long id, Long condominioId) {
        return repository.findByIdAndCondominioId(id, condominioId)
                .orElseThrow(() -> new NotFoundException("Espaço não encontrado: " + id));
    }
}
