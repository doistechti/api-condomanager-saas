package br.com.doistech.apicondomanagersaas.dto.ocorrencia;

import jakarta.validation.constraints.NotBlank;

public record OcorrenciaMensagemCreateRequest(
        @NotBlank String mensagem
) {
}
