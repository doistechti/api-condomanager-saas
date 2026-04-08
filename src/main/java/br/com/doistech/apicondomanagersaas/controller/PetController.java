package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.documento.UploadResponse;
import br.com.doistech.apicondomanagersaas.dto.pet.PetCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.pet.PetResponse;
import br.com.doistech.apicondomanagersaas.dto.pet.PetUpdateRequest;
import br.com.doistech.apicondomanagersaas.service.PetService;
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
@RequestMapping("/api/v1/condominios/{condominioId}/pets")
@RequiredArgsConstructor
public class PetController {

    private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg", "webp");
    private static final long MAX_SIZE = 10L * 1024L * 1024L;

    private final PetService service;
    private final S3StorageService storageService;
    private final StoragePathService storagePathService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PetResponse create(@PathVariable Long condominioId, @Valid @RequestBody PetCreateRequest req) {
        PetCreateRequest fixed = new PetCreateRequest(
                condominioId,
                req.unidadeId(),
                req.nome(),
                req.tipo(),
                req.raca(),
                req.porte(),
                req.cor(),
                req.dataNascimento(),
                req.observacoes(),
                req.fotoUrl(),
                req.fotoNome()
        );
        return service.create(fixed);
    }

    @PutMapping("/{id}")
    public PetResponse update(@PathVariable Long condominioId, @PathVariable Long id, @Valid @RequestBody PetUpdateRequest req) {
        return service.update(id, condominioId, req);
    }

    @GetMapping("/{id}")
    public PetResponse getById(@PathVariable Long condominioId, @PathVariable Long id) {
        return service.getById(id, condominioId);
    }

    @GetMapping
    public List<PetResponse> list(@PathVariable Long condominioId) {
        return service.listByCondominio(condominioId);
    }

    @GetMapping("/unidade/{unidadeId}")
    public List<PetResponse> listByUnidade(@PathVariable Long condominioId, @PathVariable Long unidadeId) {
        return service.listByUnidade(condominioId, unidadeId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long condominioId, @PathVariable Long id) {
        service.delete(id, condominioId);
    }

    @PostMapping("/upload")
    public UploadResponse upload(@PathVariable Long condominioId, @RequestParam("file") MultipartFile file) {
        var stored = storageService.upload(
                file,
                storagePathService.condominioModuleFolder(condominioId, "pets"),
                "pet",
                ALLOWED_EXT,
                MAX_SIZE
        );
        return new UploadResponse(stored.url(), stored.fileName(), stored.contentType(), stored.size());
    }
}
