package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.dto.plano.*;
import br.com.doistech.apicondomanagersaas.mapper.PlanoMapper;
import br.com.doistech.apicondomanagersaas.repository.PlanoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanoService {

    private final PlanoRepository repository;
    private final PlanoMapper mapper;

    @Transactional(readOnly = true)
    public List<PlanoResponse> list(Boolean onlyActive) {
        var all = repository.findAll();

        return all.stream()
                .filter(p -> onlyActive == null || !onlyActive || Boolean.TRUE.equals(p.getAtivo()))
                .sorted((a, b) -> a.getPreco().compareTo(b.getPreco()))
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanoResponse getById(Long id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado: " + id));
        return mapper.toResponse(entity);
    }

    @Transactional
    public PlanoResponse create(PlanoCreateRequest req) {
        if (repository.existsByNome(req.nome())) {
            throw new IllegalArgumentException("Já existe um plano com esse nome.");
        }
        var entity = mapper.toEntity(req);
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional
    public PlanoResponse update(Long id, PlanoUpdateRequest req) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Plano não encontrado: " + id));

        // regra simples: nome único
        if (!entity.getNome().equalsIgnoreCase(req.nome()) && repository.existsByNome(req.nome())) {
            throw new IllegalArgumentException("Já existe um plano com esse nome.");
        }

        mapper.updateEntity(req, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Plano não encontrado: " + id);
        }

        // Depois a gente liga isso no Condominio:
        // if (condominioRepository.existsByPlanoId(id)) { throw ... }

        repository.deleteById(id);
    }
}
