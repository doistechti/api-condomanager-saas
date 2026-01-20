package br.com.doistech.apicondomanagersaas.dto.veiculo;

public record VeiculoResponse(
        Long id,
        Long condominioId,
        Long pessoaId,
        String placa,
        String modelo,
        String cor,
        String tipo
) {
}
