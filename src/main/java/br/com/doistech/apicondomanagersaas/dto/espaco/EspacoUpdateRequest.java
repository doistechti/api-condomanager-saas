package br.com.doistech.apicondomanagersaas.dto.espaco;

import jakarta.validation.constraints.NotBlank;

public record EspacoUpdateRequest(
        @NotBlank String nome,
        String descricao,
        String regras,
        Integer capacidade,
        boolean necessitaAprovacao,
        boolean ativo,
        String tipoReserva,
        Integer prazoAntecedencia
) {
}
