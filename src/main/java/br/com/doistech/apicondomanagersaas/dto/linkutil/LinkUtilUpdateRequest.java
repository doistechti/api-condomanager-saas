package br.com.doistech.apicondomanagersaas.dto.linkutil;

import jakarta.validation.constraints.NotBlank;

public record LinkUtilUpdateRequest(
        @NotBlank String titulo,
        String descricao,
        @NotBlank String url,
        String categoria,
        Integer ordem,
        Boolean ativo
) {
}