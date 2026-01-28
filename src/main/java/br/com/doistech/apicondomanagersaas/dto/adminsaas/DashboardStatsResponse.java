package br.com.doistech.apicondomanagersaas.dto.adminsaas;

import java.math.BigDecimal;

public record DashboardStatsResponse(
        long totalCondominios,
        BigDecimal receitaMensal,
        long assinaturasAtivas,
        long assinaturasInadimplentes,
        long totalUsuarios
) {}

