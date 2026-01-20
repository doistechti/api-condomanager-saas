package br.com.doistech.apicondomanagersaas.dto.unidade;

public record UnidadeResponse(
        Long id,
        Long condominioId,
        Long setorId,
        String identificacao,
        String descricao
) {
}
