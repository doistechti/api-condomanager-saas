package br.com.doistech.apicondomanagersaas.dto.unidade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UnidadeCreateRequest(
        @NotNull Long condominioId,
        Long setorId,
        @NotBlank String identificacao,
        String descricao
) {
}
