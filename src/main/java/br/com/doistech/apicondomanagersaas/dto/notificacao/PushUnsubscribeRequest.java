package br.com.doistech.apicondomanagersaas.dto.notificacao;

import jakarta.validation.constraints.NotBlank;

public record PushUnsubscribeRequest(
        @NotBlank String endpoint
) {
}
