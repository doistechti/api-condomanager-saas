package br.com.doistech.apicondomanagersaas.dto.auth;

import java.time.LocalDateTime;

public record MoradorInviteResponse(
        String token,
        String nome,
        String email,
        String condominio,
        String unidade,
        LocalDateTime conviteEnviadoEm
) {
}
