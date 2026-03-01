package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.comunicado.Comunicado;
import br.com.doistech.apicondomanagersaas.dto.comunicado.ComunicadoCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.comunicado.ComunicadoResponse;
import br.com.doistech.apicondomanagersaas.dto.comunicado.ComunicadoUpdateRequest;
import br.com.doistech.apicondomanagersaas.mapper.ComunicadoMapper;
import br.com.doistech.apicondomanagersaas.repository.ComunicadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComunicadoService {

    private final ComunicadoRepository repository;
    private final CondominioService condominioService;
    private final ComunicadoMapper mapper;

    public ComunicadoResponse create(ComunicadoCreateRequest req) {
        var now = LocalDateTime.now();

        Comunicado entity = Comunicado.builder()
                .condominio(condominioService.getEntity(req.condominioId()))
                .titulo(req.titulo())
                .conteudo(req.conteudo())
                .imagemUrl(req.imagemUrl())
                .tipo(req.tipo())
                .ativo(req.ativo())
                // ✅ default false se vier null
                .destaque(req.destaque() == null ? false : req.destaque())
                .dataPublicacao(req.dataPublicacao())
                .dataExpiracao(req.dataExpiracao())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return mapper.toResponse(repository.save(entity));
    }

    public ComunicadoResponse update(Long id, Long condominioId, ComunicadoUpdateRequest req) {
        Comunicado entity = getEntity(id, condominioId);

        entity.setTitulo(req.titulo());
        entity.setConteudo(req.conteudo());
        entity.setImagemUrl(req.imagemUrl());
        entity.setTipo(req.tipo());
        entity.setAtivo(req.ativo());
        // ✅ mantém valor se vier null
        entity.setDestaque(req.destaque() == null ? entity.getDestaque() : req.destaque());
        entity.setDataPublicacao(req.dataPublicacao());
        entity.setDataExpiracao(req.dataExpiracao());
        entity.setUpdatedAt(LocalDateTime.now());

        return mapper.toResponse(repository.save(entity));
    }

    public ComunicadoResponse getById(Long id, Long condominioId) {
        return mapper.toResponse(getEntity(id, condominioId));
    }

    public List<ComunicadoResponse> list(Long condominioId) {
        return repository.findAllByCondominioIdOrderByDataPublicacaoDesc(condominioId)
                .stream().map(mapper::toResponse).toList();
    }

    public void delete(Long id, Long condominioId) {
        repository.delete(getEntity(id, condominioId));
    }

    private Comunicado getEntity(Long id, Long condominioId) {
        return repository.findByIdAndCondominioId(id, condominioId)
                .orElseThrow(() -> new NotFoundException("Comunicado não encontrado: " + id));
    }
}
