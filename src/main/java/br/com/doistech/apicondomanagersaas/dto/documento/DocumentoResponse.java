package br.com.doistech.apicondomanagersaas.dto.documento;

import java.time.LocalDateTime;

public record DocumentoResponse(
        Long id,
        Long condominioId,
        String nome,
        String descricao,
        String arquivoUrl,
        String arquivoNome,
        String categoria,
        Boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
