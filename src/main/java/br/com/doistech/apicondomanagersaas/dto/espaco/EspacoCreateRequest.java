package br.com.doistech.apicondomanagersaas.dto.espaco;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EspacoCreateRequest(
        @NotNull Long condominioId,
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
