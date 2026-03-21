package br.com.doistech.apicondomanagersaas.dto.aws;

import java.time.LocalDateTime;

public record AwsCredentialResponse(
        Long id,
        String accessKey,
        String secretKey,
        String region,
        String bucket,
        String endpoint,
        Boolean pathStyleAccess,
        Boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
