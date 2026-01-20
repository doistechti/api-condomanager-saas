package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.condominio.Condominio;
import br.com.doistech.apicondomanagersaas.dto.condominio.CondominioCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.condominio.CondominioUpdateRequest;
import br.com.doistech.apicondomanagersaas.mapper.CondominioMapper;
import br.com.doistech.apicondomanagersaas.dto.condominio.CondominioResponse;
import br.com.doistech.apicondomanagersaas.repository.CondominioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CondominioService {

    private final CondominioRepository repository;
    private final CondominioMapper mapper;

    public CondominioResponse create(CondominioCreateRequest req) {
        Condominio entity = Condominio.builder()
                .nome(req.nome())
                .cnpj(req.cnpj())
                .responsavel(req.responsavel())
                .email(req.email())
                .telefone(req.telefone())
                .endereco(req.endereco())
                .tipoSetor(req.tipoSetor())
                .logoUrl(req.logoUrl())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return mapper.toResponse(repository.save(entity));
    }

    public CondominioResponse update(Long id, CondominioUpdateRequest req) {
        Condominio entity = getEntity(id);
        entity.setNome(req.nome());
        entity.setCnpj(req.cnpj());
        entity.setResponsavel(req.responsavel());
        entity.setEmail(req.email());
        entity.setTelefone(req.telefone());
        entity.setEndereco(req.endereco());
        entity.setTipoSetor(req.tipoSetor());
        entity.setLogoUrl(req.logoUrl());
        entity.setUpdatedAt(LocalDateTime.now());

        return mapper.toResponse(repository.save(entity));
    }

    public CondominioResponse getById(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    public List<CondominioResponse> list() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    public void delete(Long id) {
        Condominio entity = getEntity(id);
        repository.delete(entity);
    }

    public Condominio getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Condomínio não encontrado: " + id));
    }
}
