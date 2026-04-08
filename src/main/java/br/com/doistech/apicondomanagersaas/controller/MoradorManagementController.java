package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.documento.UploadResponse;
import br.com.doistech.apicondomanagersaas.dto.morador.MoradorManagedCreateRequest;
import br.com.doistech.apicondomanagersaas.dto.pessoaunidade.PessoaUnidadeResponse;
import br.com.doistech.apicondomanagersaas.service.MoradorManagementService;
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
@RequestMapping("/api/v1/morador/moradores")
@RequiredArgsConstructor
public class MoradorManagementController {

    private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg", "webp");
    private static final long MAX_SIZE = 10L * 1024L * 1024L;

    private final MoradorManagementService service;
    private final MoradorScopeService moradorScopeService;
    private final S3StorageService storageService;
    private final StoragePathService storagePathService;

    @GetMapping
    public List<PessoaUnidadeResponse> list(Authentication auth) {
        return service.listManagedMoradores(auth.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PessoaUnidadeResponse create(Authentication auth, @Valid @RequestBody MoradorManagedCreateRequest req) {
        return service.createMorador(auth.getName(), req);
    }

    @PutMapping("/{id}")
    public PessoaUnidadeResponse update(Authentication auth, @PathVariable Long id, @Valid @RequestBody MoradorManagedCreateRequest req) {
        return service.updateMorador(auth.getName(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable Long id) {
        service.deleteMorador(auth.getName(), id);
    }

    @PostMapping("/{id}/convite")
    public PessoaUnidadeResponse sendInvite(Authentication auth, @PathVariable Long id) {
        return service.sendInvite(auth.getName(), id);
    }

    @PostMapping("/upload")
    public UploadResponse upload(Authentication auth, @RequestParam("file") MultipartFile file) {
        var scope = moradorScopeService.getScope(auth.getName());
        var stored = storageService.upload(
                file,
                storagePathService.condominioModuleFolder(scope.condominioId(), "moradores"),
                "morador",
                ALLOWED_EXT,
                MAX_SIZE
        );
        return new UploadResponse(stored.url(), stored.fileName(), stored.contentType(), stored.size());
    }
}
