package br.com.doistech.apicondomanagersaas.dto.morador;


import java.util.List;

/**
 * ✅ Escopo mínimo do morador para filtrar dados no backend.
 * - Resolve TUDO pelo usuário logado (JWT).
 */
public record MoradorScopeDto(
        Long usuarioId,
        Long condominioId,
        Long pessoaId,
        List<Long> unidadeIds
) {}
