package br.com.doistech.apicondomanagersaas.dto.lead;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record LeadCreateRequest(
        @NotBlank String nomeCondominio,
        String cnpj,
        @NotBlank String responsavel,
        @NotBlank @Email String email,
        @NotBlank String senha,
        String telefone,
        @Positive Integer unidadesEstimadas,
        String mensagem
) {}

