package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.pet.Pet;
import br.com.doistech.apicondomanagersaas.dto.pet.PetCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.pet.PetResponse;
import br.com.doistech.apicondomanagersaas.dto.pet.PetUpdateRequest;
import br.com.doistech.apicondomanagersaas.mapper.PetMapper;
import br.com.doistech.apicondomanagersaas.repository.PetRepository;
import br.com.doistech.apicondomanagersaas.service.storage.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository repository;
    private final CondominioService condominioService;
    private final UnidadeService unidadeService;
    private final PetMapper mapper;
    private final S3StorageService storageService;

    public PetResponse create(PetCreateRequest req) {
        Pet entity = Pet.builder()
                .condominio(condominioService.getEntity(req.condominioId()))
                .unidade(unidadeService.getEntity(req.unidadeId(), req.condominioId()))
                .nome(req.nome())
                .tipo(req.tipo())
                .raca(req.raca())
                .porte(req.porte())
                .cor(req.cor())
                .dataNascimento(req.dataNascimento())
                .observacoes(req.observacoes())
                .fotoUrl(req.fotoUrl())
                .fotoNome(req.fotoNome())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return mapper.toResponse(repository.save(entity));
    }

    public PetResponse update(Long id, Long condominioId, PetUpdateRequest req) {
        Pet entity = getEntity(id, condominioId);
        replaceFotoIfChanged(entity, req.fotoUrl());
        entity.setUnidade(unidadeService.getEntity(req.unidadeId(), condominioId));
        entity.setNome(req.nome());
        entity.setTipo(req.tipo());
        entity.setRaca(req.raca());
        entity.setPorte(req.porte());
        entity.setCor(req.cor());
        entity.setDataNascimento(req.dataNascimento());
        entity.setObservacoes(req.observacoes());
        entity.setFotoUrl(req.fotoUrl());
        entity.setFotoNome(req.fotoNome());
        entity.setUpdatedAt(LocalDateTime.now());
        return mapper.toResponse(repository.save(entity));
    }

    public PetResponse getById(Long id, Long condominioId) {
        return mapper.toResponse(getEntity(id, condominioId));
    }

    public List<PetResponse> listByCondominio(Long condominioId) {
        return repository.findAllByCondominioId(condominioId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<PetResponse> listByUnidade(Long condominioId, Long unidadeId) {
        return repository.findAllByCondominioIdAndUnidadeId(condominioId, unidadeId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public void delete(Long id, Long condominioId) {
        Pet entity = getEntity(id, condominioId);
        storageService.deleteByUrl(entity.getFotoUrl());
        repository.delete(entity);
    }

    public Pet getEntity(Long id, Long condominioId) {
        return repository.findByIdAndCondominioId(id, condominioId)
                .orElseThrow(() -> new NotFoundException("Pet não encontrado: " + id));
    }

    private void replaceFotoIfChanged(Pet entity, String nextFotoUrl) {
        if (entity.getFotoUrl() != null && !entity.getFotoUrl().equals(nextFotoUrl)) {
            storageService.deleteByUrl(entity.getFotoUrl());
        }
    }
}
