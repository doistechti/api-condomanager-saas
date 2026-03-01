package br.com.doistech.apicondomanagersaas.dto.linkutil;

import java.time.LocalDateTime;

public record LinkUtilResponse(
        Long id,
        Long condominioId,
        String titulo,
        String descricao,
        String url,
        String categoria,
        Integer ordem,
        Boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}