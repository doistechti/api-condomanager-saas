package br.com.doistech.apicondomanagersaas.dto.pessoa;

public record PessoaResponse(
        Long id,
        Long condominioId,
        String nome,
        String documento,
        String email,
        String telefone
) {
}
