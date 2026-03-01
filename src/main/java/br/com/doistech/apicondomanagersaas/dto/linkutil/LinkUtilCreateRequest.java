package br.com.doistech.apicondomanagersaas.dto.linkutil;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Create payload.
 *
 * Padrão do projeto: o backend costuma exigir condominioId no body para create.
 */
public record LinkUtilCreateRequest(
        @NotNull Long condominioId,
        @NotBlank String titulo,
        String descricao,
        @NotBlank String url,
        String categoria,
        Integer ordem,
        Boolean ativo
) {
}