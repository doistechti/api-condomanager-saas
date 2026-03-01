package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.documento.*;
import br.com.doistech.apicondomanagersaas.service.DocumentoCondominioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/condominios/{condominioId}/documentos")
@RequiredArgsConstructor
public class DocumentoCondominioController {

    private final DocumentoCondominioService service;

    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    private static final Set<String> ALLOWED_EXT = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "png", "jpg", "jpeg"
    );

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
        service.delete(id, condominioId, uploadsDir);
    }

    @PostMapping("/upload")
    public UploadResponse upload(@PathVariable Long condominioId,
                                 @RequestParam("file") MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo obrigatório.");
        }

        // tamanho (já tem limite no properties, mas validamos também)
        long size = file.getSize();
        long max = 10L * 1024L * 1024L;
        if (size > max) {
            throw new IllegalArgumentException("Arquivo excede o limite de 10MB.");
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "arquivo" : file.getOriginalFilename());
        String ext = getExt(original);

        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("Extensão não permitida: ." + ext);
        }

        String safeBaseName = sanitizeBaseName(stripExt(original));
        String random = UUID.randomUUID().toString().replace("-", "");
        String finalName = Instant.now().toEpochMilli() + "_" + random + "_" + safeBaseName + "." + ext;

        Path baseDir = Path.of(uploadsDir, "condominios", String.valueOf(condominioId), "documentos")
                .toAbsolutePath().normalize();

        Files.createDirectories(baseDir);

        Path target = baseDir.resolve(finalName).normalize();
        if (!target.startsWith(baseDir)) {
            throw new IllegalArgumentException("Nome de arquivo inválido.");
        }

        // grava
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String publicPath = "/uploads/condominios/" + condominioId + "/documentos/" + finalName;

        return new UploadResponse(
                publicPath,
                finalName,
                file.getContentType(),
                size
        );
    }

    private String getExt(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0) return "";
        return name.substring(i + 1).toLowerCase();
    }

    private String stripExt(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0) return name;
        return name.substring(0, i);
    }

    private String sanitizeBaseName(String base) {
        // remove path, mantém só nome
        base = base.replace("\\", "/");
        int slash = base.lastIndexOf('/');
        if (slash >= 0) base = base.substring(slash + 1);

        // troca caracteres perigosos
        base = base.replaceAll("[^a-zA-Z0-9._-]", "_");

        // limita tamanho
        if (base.length() > 80) base = base.substring(0, 80);
        if (base.isBlank()) base = "arquivo";
        return base;
    }
}
