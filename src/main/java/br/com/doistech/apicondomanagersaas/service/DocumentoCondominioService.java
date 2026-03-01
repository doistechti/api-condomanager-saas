package br.com.doistech.apicondomanagersaas.service;

import br.com.doistech.apicondomanagersaas.common.exception.NotFoundException;
import br.com.doistech.apicondomanagersaas.domain.documentosCondominio.DocumentoCondominio;
import br.com.doistech.apicondomanagersaas.dto.documento.DocumentoCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.documento.DocumentoResponse;
import br.com.doistech.apicondomanagersaas.dto.documento.DocumentoUpdateRequest;
import br.com.doistech.apicondomanagersaas.mapper.DocumentoMapper;
import br.com.doistech.apicondomanagersaas.repository.DocumentoCondominioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentoCondominioService {

    private final DocumentoCondominioRepository repository;
    private final CondominioService condominioService;
    private final DocumentoMapper mapper;

    public DocumentoResponse create(DocumentoCreateRequest req) {
        DocumentoCondominio entity = DocumentoCondominio.builder()
                .condominio(condominioService.getEntity(req.condominioId()))
                .nome(req.nome())
                .descricao(req.descricao())
                .arquivoUrl(req.arquivoUrl())
                .arquivoNome(req.arquivoNome())
                .categoria(req.categoria())
                .ativo(req.ativo() == null ? true : req.ativo())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return mapper.toResponse(repository.save(entity));
    }

    public DocumentoResponse update(Long id, Long condominioId, DocumentoUpdateRequest req) {
        DocumentoCondominio entity = getEntity(id, condominioId);
        entity.setNome(req.nome());
        entity.setDescricao(req.descricao());
        entity.setCategoria(req.categoria());
        entity.setAtivo(req.ativo() == null ? entity.getAtivo() : req.ativo());
        entity.setUpdatedAt(LocalDateTime.now());
        return mapper.toResponse(repository.save(entity));
    }

    public DocumentoResponse getById(Long id, Long condominioId) {
        return mapper.toResponse(getEntity(id, condominioId));
    }

    public List<DocumentoResponse> listByCondominio(Long condominioId) {
        return repository.findAllByCondominioIdOrderByCreatedAtDesc(condominioId)
                .stream().map(mapper::toResponse).toList();
    }

    public void delete(Long id, Long condominioId, String uploadsDir) {
        DocumentoCondominio entity = getEntity(id, condominioId);

        // MVP: tenta apagar o arquivo físico se for URL local /uploads/...
        tryDeleteLocalFile(entity.getArquivoUrl(), uploadsDir);

        repository.delete(entity);
    }

    private DocumentoCondominio getEntity(Long id, Long condominioId) {
        return repository.findByIdAndCondominioId(id, condominioId)
                .orElseThrow(() -> new NotFoundException("Documento não encontrado: " + id));
    }

    private void tryDeleteLocalFile(String arquivoUrl, String uploadsDir) {
        if (arquivoUrl == null) return;

        String pathPart = null;
        try {
            // Se vier URL absoluta, extrai o path
            URI uri = URI.create(arquivoUrl);
            pathPart = uri.getPath();
        } catch (Exception ignored) {
            // pode ser só um path "/uploads/.."
            pathPart = arquivoUrl;
        }

        if (pathPart == null || !pathPart.startsWith("/uploads/")) return;

        // remove prefixo "/uploads/"
        String relative = pathPart.substring("/uploads/".length());
        Path base = Path.of(uploadsDir).toAbsolutePath().normalize();
        Path target = base.resolve(relative).normalize();

        // Anti path traversal: só apaga se ainda estiver dentro de uploadsDir
        if (!target.startsWith(base)) return;

        try {
            Files.deleteIfExists(target);
        } catch (Exception ignored) {
            // MVP: não falha o delete do registro se não conseguir apagar o arquivo
        }
    }
}
