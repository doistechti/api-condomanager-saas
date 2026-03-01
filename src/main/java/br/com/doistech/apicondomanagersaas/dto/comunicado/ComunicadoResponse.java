package br.com.doistech.apicondomanagersaas.dto.comunicado;

import java.time.LocalDateTime;

public record ComunicadoResponse(
        Long id,
        Long condominioId,
        String titulo,
        String conteudo,
        String imagemUrl,
        String tipo,
        Boolean ativo,
        // ✅ novo campo
        Boolean destaque,
        LocalDateTime dataPublicacao,
        LocalDateTime dataExpiracao,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}