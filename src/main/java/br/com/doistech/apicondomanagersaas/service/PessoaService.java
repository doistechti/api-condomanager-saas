package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.pessoa.Pessoa;
import br.com.doistech.apicondomanagersaas.dto.pessoa.PessoaCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.pessoa.PessoaUpdateRequest;
import br.com.doistech.apicondomanagersaas.dto.pessoa.PessoaResponse;
import br.com.doistech.apicondomanagersaas.mapper.PessoaMapper;
import br.com.doistech.apicondomanagersaas.repository.PessoaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PessoaService {

    private final PessoaRepository repository;
    private final CondominioService condominioService;
    private final PessoaMapper mapper;

    public PessoaResponse create(PessoaCreateRequest req) {
        if (req.email() != null && !req.email().isBlank()) {
            repository.findByCondominioIdAndEmail(req.condominioId(), req.email())
                    .ifPresent(p -> {
                        throw new BadRequestException("Já existe pessoa com este e-mail no condomínio");
                    });
        }

        Pessoa entity = Pessoa.builder()
                .condominio(condominioService.getEntity(req.condominioId()))
                .nome(req.nome())
                .email(req.email())
                .telefone(req.telefone())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return mapper.toResponse(repository.save(entity));
    }

    public PessoaResponse update(Long id, Long condominioId, PessoaUpdateRequest req) {
        Pessoa entity = getEntity(id, condominioId);
        entity.setNome(req.nome());
        entity.setEmail(req.email());
        entity.setTelefone(req.telefone());
        entity.setUpdatedAt(LocalDateTime.now());
        return mapper.toResponse(repository.save(entity));
    }

    public PessoaResponse getById(Long id, Long condominioId) {
        return mapper.toResponse(getEntity(id, condominioId));
    }

    public List<PessoaResponse> listByCondominio(Long condominioId) {
        return repository.findAllByCondominioId(condominioId).stream().map(mapper::toResponse).toList();
    }

    public void delete(Long id, Long condominioId) {
        repository.delete(getEntity(id, condominioId));
    }

    public Pessoa getEntity(Long id, Long condominioId) {
        return repository.findByIdAndCondominioId(id, condominioId)
                .orElseThrow(() -> new NotFoundException("Pessoa não encontrada: " + id));
    }
}
