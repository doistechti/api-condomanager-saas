package br.com.doistech.apicondomanagersaas.dto.documento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentoCreateRequest(
        @NotNull Long condominioId,
        @NotBlank String nome,
        String descricao,
        @NotBlank String arquivoUrl,
        @NotBlank String arquivoNome,
        String categoria,
        Boolean ativo
) {}