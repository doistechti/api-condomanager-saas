package br.com.doistech.apicondomanagersaas.dto.pessoa;

import jakarta.validation.constraints.NotBlank;

public record PessoaUpdateRequest(
        @NotBlank String nome,
        String documento,
        String email,
        String telefone
) {
}
