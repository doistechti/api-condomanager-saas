package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.setor.Setor;
import br.com.doistech.apicondomanagersaas.dto.setor.SetorCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.setor.SetorUpdateRequest;
import br.com.doistech.apicondomanagersaas.dto.setor.SetorResponse;
import br.com.doistech.apicondomanagersaas.mapper.SetorMapper;
import br.com.doistech.apicondomanagersaas.repository.SetorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SetorService {

    private final SetorRepository repository;
    private final CondominioService condominioService;
    private final SetorMapper mapper;

    public SetorResponse create(SetorCreateRequest req) {
        Setor entity = Setor.builder()
                .condominio(condominioService.getEntity(req.condominioId()))
                .nome(req.nome())
                .tipo(req.tipo())
                .descricao(req.descricao())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return mapper.toResponse(repository.save(entity));
    }

    public SetorResponse update(Long id, Long condominioId, SetorUpdateRequest req) {
        Setor entity = getEntity(id, condominioId);
        entity.setNome(req.nome());
        entity.setTipo(req.tipo());
        entity.setDescricao(req.descricao());
        entity.setUpdatedAt(LocalDateTime.now());
        return mapper.toResponse(repository.save(entity));
    }

    public SetorResponse getById(Long id, Long condominioId) {
        return mapper.toResponse(getEntity(id, condominioId));
    }

    public List<SetorResponse> listByCondominio(Long condominioId) {
        return repository.findAllByCondominioId(condominioId).stream().map(mapper::toResponse).toList();
    }

    public void delete(Long id, Long condominioId) {
        repository.delete(getEntity(id, condominioId));
    }

    private Setor getEntity(Long id, Long condominioId) {
        return repository.findByIdAndCondominioId(id, condominioId)
                .orElseThrow(() -> new NotFoundException("Setor não encontrado: " + id));
    }
}
