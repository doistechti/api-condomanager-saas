package br.com.doistech.apicondomanagersaas.dto.dashboardcondominio;

import java.time.LocalDateTime;

public record CadastroRecenteResponse(
        String id,
        String tipo,         // "Morador" | "Veículo"
        String nome,         // ex: Nome morador ou "Modelo - Placa"
        String unidade,      // ex: "Bloco A - 101" (se existir)
        LocalDateTime createdAt
) {}