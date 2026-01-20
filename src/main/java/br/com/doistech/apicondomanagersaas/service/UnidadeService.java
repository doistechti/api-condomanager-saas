package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.setor.Setor;
import br.com.doistech.apicondomanagersaas.domain.unidade.Unidade;
import br.com.doistech.apicondomanagersaas.dto.unidade.UnidadeCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.unidade.UnidadeUpdateRequest;
import br.com.doistech.apicondomanagersaas.dto.unidade.UnidadeResponse;
import br.com.doistech.apicondomanagersaas.mapper.UnidadeMapper;
import br.com.doistech.apicondomanagersaas.repository.SetorRepository;
import br.com.doistech.apicondomanagersaas.repository.UnidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnidadeService {

    private final UnidadeRepository repository;
    private final CondominioService condominioService;
    private final SetorRepository setorRepository;
    private final UnidadeMapper mapper;

    public UnidadeResponse create(UnidadeCreateRequest req) {
        Unidade entity = Unidade.builder()
                .condominio(condominioService.getEntity(req.condominioId()))
                .setor(resolveSetor(req.setorId(), req.condominioId()))
                .identificacao(req.identificacao())
                .descricao(req.descricao())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return mapper.toResponse(repository.save(entity));
    }

    public UnidadeResponse update(Long id, Long condominioId, UnidadeUpdateRequest req) {
        Unidade entity = getEntity(id, condominioId);
        entity.setSetor(resolveSetor(req.setorId(), condominioId));
        entity.setIdentificacao(req.identificacao());
        entity.setDescricao(req.descricao());
        entity.setUpdatedAt(LocalDateTime.now());
        return mapper.toResponse(repository.save(entity));
    }

    public UnidadeResponse getById(Long id, Long condominioId) {
        return mapper.toResponse(getEntity(id, condominioId));
    }

    public List<UnidadeResponse> listByCondominio(Long condominioId) {
        return repository.findAllByCondominioId(condominioId).stream().map(mapper::toResponse).toList();
    }

    public void delete(Long id, Long condominioId) {
        repository.delete(getEntity(id, condominioId));
    }

    public Unidade getEntity(Long id, Long condominioId) {
        return repository.findByIdAndCondominioId(id, condominioId)
                .orElseThrow(() -> new NotFoundException("Unidade não encontrada: " + id));
    }

    private Setor resolveSetor(Long setorId, Long condominioId) {
        if (setorId == null) return null;
        return setorRepository.findByIdAndCondominioId(setorId, condominioId)
                .orElseThrow(() -> new BadRequestException("Setor inválido para o condomínio: " + setorId));
    }
}
