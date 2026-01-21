package br.com.doistech.apicondomanagersaas.dto.auth;

import java.util.List;

public record MeResponse(
        Long id,
        String nome,
        String email,
        Long condominioId,
        List<String> roles
) {}
