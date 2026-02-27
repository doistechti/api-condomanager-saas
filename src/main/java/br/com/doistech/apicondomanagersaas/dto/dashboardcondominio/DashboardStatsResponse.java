package br.com.doistech.apicondomanagersaas.dto.dashboardcondominio;

public record DashboardStatsResponse(
        long unidades,
        long moradores,
        long veiculos,
        long reservasPendentes
) {}