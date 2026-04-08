package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaCategoria;
import br.com.doistech.apicondomanagersaas.domain.ocorrencia.OcorrenciaStatus;
import br.com.doistech.apicondomanagersaas.dto.documento.UploadResponse;
import br.com.doistech.apicondomanagersaas.dto.ocorrencia.*;
import br.com.doistech.apicondomanagersaas.service.OcorrenciaService;
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
@RequiredArgsConstructor
public class OcorrenciaController {

    private static final Set<String> ALLOWED_EXT = Set.of(
            "png", "jpg", "jpeg", "webp", "mp4", "mov", "webm"
    );
    private static final long MAX_SIZE = 50L * 1024L * 1024L;

    private final OcorrenciaService ocorrenciaService;
    private final MoradorScopeService moradorScopeService;
    private final S3StorageService storageService;
    private final StoragePathService storagePathService;

    @GetMapping("/api/v1/condominios/{condominioId}/ocorrencias")
    public List<OcorrenciaSummaryResponse> listAdmin(
            @PathVariable Long condominioId,
            @RequestParam(required = false) OcorrenciaStatus status,
            @RequestParam(required = false) OcorrenciaCategoria categoria
    ) {
        return ocorrenciaService.listAdmin(condominioId, status, categoria);
    }

    @GetMapping("/api/v1/condominios/{condominioId}/ocorrencias/{id}")
    public OcorrenciaDetailResponse getAdmin(@PathVariable Long condominioId, @PathVariable Long id) {
        return ocorrenciaService.getAdmin(condominioId, id);
    }

    @PostMapping("/api/v1/condominios/{condominioId}/ocorrencias/{id}/mensagens")
    @ResponseStatus(HttpStatus.CREATED)
    public OcorrenciaMensagemResponse responderAdmin(
            @PathVariable Long condominioId,
            @PathVariable Long id,
            @Valid @RequestBody OcorrenciaMensagemCreateRequest req
    ) {
        return ocorrenciaService.addMensagemAdmin(condominioId, id, req);
    }

    @PatchMapping("/api/v1/condominios/{condominioId}/ocorrencias/{id}/status")
    public OcorrenciaDetailResponse updateStatusAdmin(
            @PathVariable Long condominioId,
            @PathVariable Long id,
            @Valid @RequestBody OcorrenciaStatusUpdateRequest req
    ) {
        return ocorrenciaService.updateStatusAdmin(condominioId, id, req);
    }

    @GetMapping("/api/v1/morador/ocorrencias")
    public List<OcorrenciaSummaryResponse> listMorador(
            Authentication auth,
            @RequestParam(required = false) OcorrenciaStatus status,
            @RequestParam(required = false) OcorrenciaCategoria categoria
    ) {
        return ocorrenciaService.listMorador(auth.getName(), status, categoria);
    }

    @GetMapping("/api/v1/morador/ocorrencias/{id}")
    public OcorrenciaDetailResponse getMorador(Authentication auth, @PathVariable Long id) {
        return ocorrenciaService.getMorador(auth.getName(), id);
    }

    @PostMapping("/api/v1/morador/ocorrencias")
    @ResponseStatus(HttpStatus.CREATED)
    public OcorrenciaDetailResponse createMorador(
            Authentication auth,
            @Valid @RequestBody OcorrenciaCreateRequest req
    ) {
        return ocorrenciaService.createMorador(auth.getName(), req);
    }

    @PostMapping("/api/v1/morador/ocorrencias/{id}/mensagens")
    @ResponseStatus(HttpStatus.CREATED)
    public OcorrenciaMensagemResponse responderMorador(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody OcorrenciaMensagemCreateRequest req
    ) {
        return ocorrenciaService.addMensagemMorador(auth.getName(), id, req);
    }

    @PostMapping("/api/v1/morador/ocorrencias/upload")
    public UploadResponse uploadMorador(Authentication auth, @RequestParam("file") MultipartFile file) {
        var scope = moradorScopeService.getScope(auth.getName());
        var stored = storageService.upload(
                file,
                storagePathService.condominioModuleFolder(scope.condominioId(), "ocorrencias"),
                "ocorrencia",
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
