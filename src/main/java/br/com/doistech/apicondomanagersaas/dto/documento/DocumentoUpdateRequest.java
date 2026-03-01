package br.com.doistech.apicondomanagersaas.dto.documento;

import jakarta.validation.constraints.NotBlank;

public record DocumentoUpdateRequest(
        @NotBlank String nome,
        String descricao,
        String categoria,
        Boolean ativo
) {}