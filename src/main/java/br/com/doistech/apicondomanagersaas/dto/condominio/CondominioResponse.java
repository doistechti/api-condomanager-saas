package br.com.doistech.apicondomanagersaas.dto.condominio;

public record CondominioResponse(
        Long id,
        String nome,
        String cnpj,
        String responsavel,
        String email,
        String telefone,
        String endereco,
        String tipoSetor,
        String logoUrl
) {
}
