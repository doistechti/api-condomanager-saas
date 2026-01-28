package br.com.doistech.apicondomanagersaas.dto.assinatura;

import br.com.doistech.apicondomanagersaas.domain.assinatura.AssinaturaStatus;

import java.time.LocalDate;

public record AssinaturaUpdateRequest(
        Long planoId,
        AssinaturaStatus status,
        LocalDate dataVencimento,
        String mercadoPagoId
) {}

