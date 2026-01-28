package br.com.doistech.apicondomanagersaas.dto.adminsaas;

import java.math.BigDecimal;

public record RecentPaymentResponse(
        Long id,
        String condominioNome,
        BigDecimal valor,
        String status
) {}

