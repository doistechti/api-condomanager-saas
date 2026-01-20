package br.com.doistech.apicondomanagersaas.dto.pessoa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PessoaCreateRequest(
        @NotNull Long condominioId,
        @NotBlank String nome,
        String documento,
        String email,
        String telefone
) {
}
