package br.com.doistech.apicondomanagersaas.dto.unidade;

import jakarta.validation.constraints.NotBlank;

public record UnidadeUpdateRequest(
        Long setorId,
        @NotBlank String identificacao,
        String descricao
) {
}
