package br.com.doistech.apicondomanagersaas.dto.ocorrencia;

import jakarta.validation.constraints.NotBlank;

public record OcorrenciaAnexoCreateRequest(
        @NotBlank String arquivoUrl,
        @NotBlank String arquivoNome,
        String contentType,
        Long tamanhoBytes
) {
}
