package br.com.doistech.apicondomanagersaas.dto.morador;

public record MoradorUnidadeResponse(
        Long id,
        Long condominioId,
        Long setorId,
        String setorNome,
        String identificacao,
        String descricao
) {
}