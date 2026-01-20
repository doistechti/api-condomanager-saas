package br.com.doistech.apicondomanagersaas.dto.setor;

public record SetorResponse(
        Long id,
        Long condominioId,
        String nome,
        String tipo,
        String descricao
) {
}
