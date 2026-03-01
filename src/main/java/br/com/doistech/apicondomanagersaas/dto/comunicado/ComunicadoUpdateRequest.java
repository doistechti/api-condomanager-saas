package br.com.doistech.apicondomanagersaas.dto.comunicado;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ComunicadoUpdateRequest(
        @NotBlank String titulo,
        String conteudo,
        String imagemUrl,
        @NotBlank String tipo,
        @NotNull Boolean ativo,
        // ✅ novo campo
        Boolean destaque,
        @NotNull LocalDateTime dataPublicacao,
        LocalDateTime dataExpiracao
) {}