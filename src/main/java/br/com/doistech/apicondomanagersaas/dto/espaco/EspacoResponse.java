package br.com.doistech.apicondomanagersaas.dto.espaco;

public record EspacoResponse(
        Long id,
        Long condominioId,
        String nome,
        String descricao,
        String regras,
        Integer capacidade,
        boolean necessitaAprovacao,
        boolean ativo,
        String tipoReserva,
        Integer prazoAntecedencia
) {
}
