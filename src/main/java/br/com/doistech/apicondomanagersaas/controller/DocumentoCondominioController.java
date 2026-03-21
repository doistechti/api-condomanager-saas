package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.documento.*;
import br.com.doistech.apicondomanagersaas.service.storage.StoragePathService;
import br.com.doistech.apicondomanagersaas.service.DocumentoCondominioService;
import br.com.doistech.apicondomanagersaas.service.storage.S3StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/condominios/{condominioId}/documentos")
@RequiredArgsConstructor
public class DocumentoCondominioController {

    private final DocumentoCondominioService service;
    private final S3StorageService storageService;
    private final StoragePathService storagePathService;

    private static final Set<String> ALLOWED_EXT = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "png", "jpg", "jpeg"
    );
    private static final long MAX_SIZE = 10L * 1024L * 1024L;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentoResponse create(@PathVariable Long condominioId,
                                    @Valid @RequestBody DocumentoCreateRequest req) {
        DocumentoCreateRequest fixed = new DocumentoCreateRequest(
                condominioId,
                req.nome(),
                req.descricao(),
                req.arquivoUrl(),
                req.arquivoNome(),
                req.categoria(),
                req.ativo()
        );
        return service.create(fixed);
    }

    @PutMapping("/{id}")
    public DocumentoResponse update(@PathVariable Long condominioId,
                                    @PathVariable Long id,
                                    @Valid @RequestBody DocumentoUpdateRequest req) {
        return service.update(id, condominioId, req);
    }

    @GetMapping("/{id}")
    public DocumentoResponse getById(@PathVariable Long condominioId, @PathVariable Long id) {
        return service.getById(id, condominioId);
    }

    @GetMapping
    public java.util.List<DocumentoResponse> list(@PathVariable Long condominioId) {
        return service.listByCondominio(condominioId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long condominioId, @PathVariable Long id) {
        service.delete(id, condominioId);
    }

    @PostMapping("/upload")
    public UploadResponse upload(@PathVariable Long condominioId,
                                 @RequestParam("file") MultipartFile file) {
        var stored = storageService.upload(
                file,
                storagePathService.condominioModuleFolder(condominioId, "documentos"),
                "arquivo",
                ALLOWED_EXT,
                MAX_SIZE
        );
        return new UploadResponse(
                stored.url(),
                stored.fileName(),
                stored.contentType(),
                stored.size()
        );
    }
}
