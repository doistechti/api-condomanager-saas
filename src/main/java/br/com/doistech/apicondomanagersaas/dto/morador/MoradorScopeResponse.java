package br.com.doistech.apicondomanagersaas.dto.morador;

import java.util.List;

public record MoradorScopeResponse(
        Long usuarioId,
        Long condominioId,
        String condominioNome,
        Long pessoaId,
        Long vinculoPrincipalId,
        List<Long> unidadeIds
) {
}