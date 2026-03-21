package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.aws.AwsCredentialResponse;
import br.com.doistech.apicondomanagersaas.dto.aws.AwsCredentialUpsertRequest;
import br.com.doistech.apicondomanagersaas.service.AwsCredentialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin-saas/configuracoes")
@PreAuthorize("hasRole('ADMIN_SAAS')")
@RequiredArgsConstructor
public class AdminSaasSettingsController {

    private final AwsCredentialService awsCredentialService;

    @GetMapping("/aws")
    public ResponseEntity<AwsCredentialResponse> getAwsConfig() {
        return ResponseEntity.ok(awsCredentialService.getConfig());
    }

    @PutMapping("/aws")
    public ResponseEntity<AwsCredentialResponse> upsertAwsConfig(
            @RequestBody @Valid AwsCredentialUpsertRequest req
    ) {
        return ResponseEntity.ok(awsCredentialService.upsert(req));
    }

    @DeleteMapping("/aws")
    public ResponseEntity<Void> deleteAwsConfig() {
        awsCredentialService.deleteConfig();
        return ResponseEntity.noContent().build();
    }
}
