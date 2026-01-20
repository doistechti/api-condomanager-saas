package br.com.doistech.apicondomanagersaas.dto.condominio;

import jakarta.validation.constraints.NotBlank;

public record CondominioUpdateRequest(
        @NotBlank String nome,
        String cnpj,
        String responsavel,
        String email,
        String telefone,
        String endereco,
        String tipoSetor,
        String logoUrl
) {
}
