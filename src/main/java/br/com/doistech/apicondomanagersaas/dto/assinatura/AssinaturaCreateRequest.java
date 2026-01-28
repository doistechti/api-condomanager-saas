package br.com.doistech.apicondomanagersaas.dto.assinatura;

import br.com.doistech.apicondomanagersaas.domain.assinatura.AssinaturaStatus;

import java.time.LocalDate;

public record AssinaturaCreateRequest(
        Long condominioId,
        Long planoId,
        AssinaturaStatus status,
        LocalDate dataInicio,
        LocalDate dataVencimento,
        String mercadoPagoId
) {}

