package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.comunicado.ComunicadoCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.comunicado.ComunicadoResponse;
import br.com.doistech.apicondomanagersaas.dto.comunicado.ComunicadoUpdateRequest;
import br.com.doistech.apicondomanagersaas.dto.comunicado.UploadImagemResponse;
import br.com.doistech.apicondomanagersaas.service.ComunicadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/condominios/{condominioId}/comunicados")
@RequiredArgsConstructor
public class ComunicadoController {

    private final ComunicadoService service;

    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg", "webp");
    private static final long MAX_SIZE = 5L * 1024L * 1024L;

    @GetMapping
    public List<ComunicadoResponse> list(@PathVariable Long condominioId) {
        return service.list(condominioId);
    }

    @GetMapping("/{id}")
    public ComunicadoResponse getById(@PathVariable Long condominioId, @PathVariable Long id) {
        return service.getById(id, condominioId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComunicadoResponse create(@PathVariable Long condominioId, @Valid @RequestBody ComunicadoCreateRequest req) {
        var fixed = new ComunicadoCreateRequest(
                condominioId,
                req.titulo(),
                req.conteudo(),
                req.imagemUrl(),
                req.tipo(),
                req.ativo(),
                req.destaque(),
                req.dataPublicacao(),
                req.dataExpiracao()
        );
        return service.create(fixed);
    }

    @PutMapping("/{id}")
    public ComunicadoResponse update(@PathVariable Long condominioId, @PathVariable Long id, @Valid @RequestBody ComunicadoUpdateRequest req) {
        return service.update(id, condominioId, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long condominioId, @PathVariable Long id) {
        service.delete(id, condominioId);
    }

    @PostMapping("/upload")
    public UploadImagemResponse upload(@PathVariable Long condominioId, @RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Imagem obrigatória.");

        long size = file.getSize();
        if (size > MAX_SIZE) throw new IllegalArgumentException("Imagem excede o limite de 5MB.");

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "imagem" : file.getOriginalFilename());
        String ext = getExt(original);
        if (!ALLOWED_EXT.contains(ext)) throw new IllegalArgumentException("Extensão não permitida: ." + ext);

        String safeBase = sanitizeBaseName(stripExt(original));
        String random = UUID.randomUUID().toString().replace("-", "");
        String finalName = Instant.now().toEpochMilli() + "_" + random + "_" + safeBase + "." + ext;

        Path baseDir = Path.of(uploadsDir, "condominios", String.valueOf(condominioId), "comunicados")
                .toAbsolutePath().normalize();
        Files.createDirectories(baseDir);

        Path target = baseDir.resolve(finalName).normalize();
        if (!target.startsWith(baseDir)) throw new IllegalArgumentException("Nome de arquivo inválido.");

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String publicPath = "/uploads/condominios/" + condominioId + "/comunicados/" + finalName;
        return new UploadImagemResponse(publicPath, finalName, file.getContentType(), size);
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
        base = base.replace("\\", "/");
        int slash = base.lastIndexOf('/');
        if (slash >= 0) base = base.substring(slash + 1);

        base = base.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (base.length() > 80) base = base.substring(0, 80);
        if (base.isBlank()) base = "imagem";
        return base;
    }
}