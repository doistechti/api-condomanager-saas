package br.com.doistech.apicondomanagersaas.dto.condominio;

import br.com.doistech.apicondomanagersaas.domain.assinatura.AssinaturaStatus;

public record CondominioResponse(
        Long id,
        String nome,
        String cnpj,
        String responsavel,
        String email,
        String telefone,
        String endereco,
        String tipoSetor,
        String logoUrl,
        Long planoId,
        String planoNome,
        Long unidadesCount,
        AssinaturaStatus assinaturaStatus
) {
}
