package br.com.doistech.apicondomanagersaas.dto.auth;

import java.time.LocalDateTime;

public record CondominioAdminInviteResponse(
        String token,
        String nome,
        String email,
        String condominio,
        LocalDateTime enviadoEm
) {
}
