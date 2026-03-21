package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.comunicado.ComunicadoCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.comunicado.ComunicadoResponse;
import br.com.doistech.apicondomanagersaas.dto.comunicado.ComunicadoUpdateRequest;
import br.com.doistech.apicondomanagersaas.dto.comunicado.UploadImagemResponse;
import br.com.doistech.apicondomanagersaas.service.ComunicadoService;
import br.com.doistech.apicondomanagersaas.service.storage.S3StorageService;
import br.com.doistech.apicondomanagersaas.service.storage.StoragePathService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/condominios/{condominioId}/comunicados")
@RequiredArgsConstructor
public class ComunicadoController {

    private final ComunicadoService service;
    private final S3StorageService storageService;
    private final StoragePathService storagePathService;

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
    public UploadImagemResponse upload(@PathVariable Long condominioId, @RequestParam("file") MultipartFile file) {
        var stored = storageService.upload(
                file,
                storagePathService.condominioModuleFolder(condominioId, "comunicados"),
                "imagem",
                ALLOWED_EXT,
                MAX_SIZE
        );
        return new UploadImagemResponse(stored.url(), stored.fileName(), stored.contentType(), stored.size());
    }
}
