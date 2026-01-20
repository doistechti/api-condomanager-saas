package br.com.doistech.apicondomanagersaas.dto.setor;

import jakarta.validation.constraints.NotBlank;

public record SetorUpdateRequest(
        @NotBlank String nome,
        String tipo,
        String descricao
) {
}
