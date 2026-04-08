package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.ForbiddenException;
import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.pet.Pet;
import br.com.doistech.apicondomanagersaas.dto.morador.MoradorPetCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.pet.PetResponse;
import br.com.doistech.apicondomanagersaas.dto.pet.PetUpdateRequest;
import br.com.doistech.apicondomanagersaas.mapper.PetMapper;
import br.com.doistech.apicondomanagersaas.repository.CondominioRepository;
import br.com.doistech.apicondomanagersaas.repository.PetRepository;
import br.com.doistech.apicondomanagersaas.repository.UnidadeRepository;
import br.com.doistech.apicondomanagersaas.service.storage.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MoradorPetService {

    private final MoradorScopeService moradorScopeService;
    private final PetRepository petRepository;
    private final CondominioRepository condominioRepository;
    private final UnidadeRepository unidadeRepository;
    private final PetMapper petMapper;
    private final S3StorageService storageService;

    @Transactional(readOnly = true)
    public List<PetResponse> listarDaUnidade(String email) {
        var scope = moradorScopeService.getScope(email);
        if (scope.unidadeIds() == null || scope.unidadeIds().isEmpty()) {
            return List.of();
        }

        return petRepository.findAllByCondominioIdAndUnidadeIdIn(scope.condominioId(), scope.unidadeIds())
                .stream()
                .map(petMapper::toResponse)
                .toList();
    }

    @Transactional
    public PetResponse criar(String email, MoradorPetCreateRequest req) {
        var scope = moradorScopeService.getScope(email);
        validateUnitAccess(scope.unidadeIds(), req.unidadeId());

        var condominio = condominioRepository.findById(scope.condominioId())
                .orElseThrow(() -> new NotFoundException("Condomínio não encontrado"));
        var unidade = unidadeRepository.findByIdAndCondominioId(req.unidadeId(), scope.condominioId())
                .orElseThrow(() -> new NotFoundException("Unidade não encontrada"));

        Pet entity = Pet.builder()
                .condominio(condominio)
                .unidade(unidade)
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

        return petMapper.toResponse(petRepository.save(entity));
    }

    @Transactional
    public PetResponse atualizar(String email, Long id, PetUpdateRequest req) {
        var scope = moradorScopeService.getScope(email);
        validateUnitAccess(scope.unidadeIds(), req.unidadeId());

        Pet entity = petRepository.findByIdAndCondominioId(id, scope.condominioId())
                .orElseThrow(() -> new NotFoundException("Pet não encontrado"));
        validateUnitAccess(scope.unidadeIds(), entity.getUnidade().getId());

        if (entity.getFotoUrl() != null && !entity.getFotoUrl().equals(req.fotoUrl())) {
            storageService.deleteByUrl(entity.getFotoUrl());
        }

        entity.setUnidade(unidadeRepository.findByIdAndCondominioId(req.unidadeId(), scope.condominioId())
                .orElseThrow(() -> new NotFoundException("Unidade não encontrada")));
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

        return petMapper.toResponse(petRepository.save(entity));
    }

    @Transactional
    public void deletar(String email, Long id) {
        var scope = moradorScopeService.getScope(email);

        Pet entity = petRepository.findByIdAndCondominioId(id, scope.condominioId())
                .orElseThrow(() -> new NotFoundException("Pet não encontrado"));
        validateUnitAccess(scope.unidadeIds(), entity.getUnidade().getId());

        storageService.deleteByUrl(entity.getFotoUrl());
        petRepository.delete(entity);
    }

    private void validateUnitAccess(List<Long> unidadeIds, Long unidadeId) {
        if (unidadeIds == null || unidadeIds.stream().noneMatch(id -> id.equals(unidadeId))) {
            throw new ForbiddenException("Acesso negado: pet não pertence às unidades do morador");
        }
    }
}
