package br.com.doistech.apicondomanagersaas.dto.assinatura;
import br.com.doistech.apicondomanagersaas.domain.assinatura.AssinaturaStatus;

import java.time.LocalDate;

public record AssinaturaResponse(
        Long id,
        Long condominioId,
        String condominioNome,
        Long planoId,
        String planoNome,
        java.math.BigDecimal planoPreco,
        AssinaturaStatus status,
        LocalDate dataInicio,
        LocalDate dataVencimento,
        String mercadoPagoId
) {}

