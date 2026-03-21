package br.com.doistech.apicondomanagersaas.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CondominioAdminInviteAcceptRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 120) String senha
) {
}
