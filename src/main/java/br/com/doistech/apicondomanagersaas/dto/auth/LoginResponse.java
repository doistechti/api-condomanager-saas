package br.com.doistech.apicondomanagersaas.dto.auth;

import java.util.List;

public record LoginResponse(
        String token,
        UsuarioMeResponse usuario
) {
    public record UsuarioMeResponse(
            Long id,
            String nome,
            String email,
            Long condominioId,
            List<String> roles
    ) {}
}
