package br.com.doistech.apicondomanagersaas.dto.dashboardcondominio;

import java.time.LocalDate;

public record ReservaRecenteResponse(
        Long id,
        String espacoNome,
        String moradorNome,
        LocalDate dataReserva
) {}