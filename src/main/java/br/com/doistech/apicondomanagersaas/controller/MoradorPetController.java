package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.documento.UploadResponse;
import br.com.doistech.apicondomanagersaas.dto.morador.MoradorPetCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.pet.PetResponse;
import br.com.doistech.apicondomanagersaas.dto.pet.PetUpdateRequest;
import br.com.doistech.apicondomanagersaas.service.MoradorPetService;
import br.com.doistech.apicondomanagersaas.service.MoradorScopeService;
import br.com.doistech.apicondomanagersaas.service.storage.S3StorageService;
import br.com.doistech.apicondomanagersaas.service.storage.StoragePathService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/morador/pets")
@RequiredArgsConstructor
public class MoradorPetController {

    private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg", "webp");
    private static final long MAX_SIZE = 10L * 1024L * 1024L;

    private final MoradorPetService service;
    private final MoradorScopeService moradorScopeService;
    private final S3StorageService storageService;
    private final StoragePathService storagePathService;

    @GetMapping
    public List<PetResponse> listByUnit(Authentication auth) {
        return service.listarDaUnidade(auth.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PetResponse create(Authentication auth, @Valid @RequestBody MoradorPetCreateRequest req) {
        return service.criar(auth.getName(), req);
    }

    @PutMapping("/{id}")
    public PetResponse update(Authentication auth, @PathVariable Long id, @Valid @RequestBody PetUpdateRequest req) {
        return service.atualizar(auth.getName(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable Long id) {
        service.deletar(auth.getName(), id);
    }

    @PostMapping("/upload")
    public UploadResponse upload(Authentication auth, @RequestParam("file") MultipartFile file) {
        var scope = moradorScopeService.getScope(auth.getName());
        var stored = storageService.upload(
                file,
                storagePathService.condominioModuleFolder(scope.condominioId(), "pets"),
                "pet",
                ALLOWED_EXT,
                MAX_SIZE
        );
        return new UploadResponse(stored.url(), stored.fileName(), stored.contentType(), stored.size());
    }
}
