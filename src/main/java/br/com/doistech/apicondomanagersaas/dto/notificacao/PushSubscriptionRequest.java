package br.com.doistech.apicondomanagersaas.dto.notificacao;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PushSubscriptionRequest(
        @NotBlank String endpoint,
        Long expirationTime,
        @Valid @NotNull Keys keys,
        String userAgent
) {
    public record Keys(
            @NotBlank String p256dh,
            @NotBlank String auth
    ) {
    }
}
