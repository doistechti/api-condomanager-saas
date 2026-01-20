package br.com.doistech.apicondomanagersaas.dto.setor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SetorCreateRequest(
        @NotNull Long condominioId,
        @NotBlank String nome,
        String tipo,
        String descricao
) {
}
