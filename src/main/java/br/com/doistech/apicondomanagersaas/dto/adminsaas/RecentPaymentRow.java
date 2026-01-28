package br.com.doistech.apicondomanagersaas.dto.adminsaas;

import br.com.doistech.apicondomanagersaas.domain.assinatura.AssinaturaStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Linha retornada por query JPQL (para facilitar join e ordenação).
 * O Service converte isso para RecentPaymentResponse (status em label).
 */
public record RecentPaymentRow(
        Long id,
        String condominioNome,
        BigDecimal valor,
        AssinaturaStatus status,
        Instant updatedAt
) {}

