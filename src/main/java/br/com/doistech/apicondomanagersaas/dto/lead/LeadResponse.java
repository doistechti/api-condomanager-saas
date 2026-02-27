package br.com.doistech.apicondomanagersaas.dto.lead;

import br.com.doistech.apicondomanagersaas.domain.lead.LeadStatus;

import java.time.Instant;

public record LeadResponse(
        Long id,
        String nomeCondominio,
        String cnpj,
        String responsavel,
        String email,
        String telefone,
        Integer unidadesEstimadas,
        String mensagem,
        LeadStatus status,
        Instant createdAt
) {}

