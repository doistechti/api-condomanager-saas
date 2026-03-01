package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.domain.linkutil.LinkUtil;
import br.com.doistech.apicondomanagersaas.dto.linkutil.LinkUtilCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.linkutil.LinkUtilResponse;
import br.com.doistech.apicondomanagersaas.dto.linkutil.LinkUtilUpdateRequest;
import br.com.doistech.apicondomanagersaas.mapper.LinkUtilMapper;
import br.com.doistech.apicondomanagersaas.repository.LinkUtilRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LinkUtilService {

    private final LinkUtilRepository repository;
    private final CondominioService condominioService;
    private final LinkUtilMapper mapper;

    public LinkUtilService(LinkUtilRepository repository,
                           CondominioService condominioService,
                           LinkUtilMapper mapper) {
        this.repository = repository;
        this.condominioService = condominioService;
        this.mapper = mapper;
    }

    public LinkUtilResponse create(LinkUtilCreateRequest req) {
        var now = LocalDateTime.now();

        LinkUtil entity = LinkUtil.builder()
                .condominio(condominioService.getEntity(req.condominioId()))
                .titulo(req.titulo())
                .descricao(req.descricao())
                .url(req.url())
                .categoria(req.categoria())
                .ordem(req.ordem() == null ? 0 : req.ordem())
                .ativo(req.ativo() == null ? Boolean.TRUE : req.ativo())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return mapper.toResponse(repository.save(entity));
    }

    public LinkUtilResponse update(Long condominioId, Long id, LinkUtilUpdateRequest req) {
        LinkUtil entity = getEntity(condominioId, id);

        entity.setTitulo(req.titulo());
        entity.setDescricao(req.descricao());
        entity.setUrl(req.url());
        entity.setCategoria(req.categoria());
        entity.setOrdem(req.ordem() == null ? entity.getOrdem() : req.ordem());
        if (req.ativo() != null) {
            entity.setAtivo(req.ativo());
        }
        entity.setUpdatedAt(LocalDateTime.now());

        return mapper.toResponse(repository.save(entity));
    }

    public LinkUtilResponse getById(Long condominioId, Long id) {
        return mapper.toResponse(getEntity(condominioId, id));
    }

    public List<LinkUtilResponse> listByCondominio(Long condominioId) {
        return repository.findAllByCondominioIdOrderByOrdemAsc(condominioId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public void delete(Long condominioId, Long id) {
        repository.delete(getEntity(condominioId, id));
    }

    private LinkUtil getEntity(Long condominioId, Long id) {
        return repository.findByIdAndCondominioId(id, condominioId)
                .orElseThrow(() -> new EntityNotFoundException("Link útil não encontrado"));
    }
}