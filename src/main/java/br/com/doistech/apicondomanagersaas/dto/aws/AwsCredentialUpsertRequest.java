package br.com.doistech.apicondomanagersaas.dto.aws;

import jakarta.validation.constraints.NotBlank;

public record AwsCredentialUpsertRequest(
        @NotBlank String accessKey,
        @NotBlank String secretKey,
        @NotBlank String region,
        @NotBlank String bucket,
        String endpoint,
        Boolean pathStyleAccess,
        Boolean ativo
) {
}
