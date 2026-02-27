package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.dashboardcondominio.CadastroRecenteResponse;
import br.com.doistech.apicondomanagersaas.dto.dashboardcondominio.DashboardStatsResponse;
import br.com.doistech.apicondomanagersaas.dto.dashboardcondominio.ReservaRecenteResponse;
import br.com.doistech.apicondomanagersaas.service.DashboardCondominioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/condominios/{condominioId}/dashboard")
@RequiredArgsConstructor
public class DashboardCondominioController {

    private final DashboardCondominioService service;

    @GetMapping("/stats")
    public DashboardStatsResponse stats(@PathVariable Long condominioId) {
        return service.stats(condominioId);
    }

    @GetMapping("/reservas-recentes")
    public List<ReservaRecenteResponse> reservasRecentes(
            @PathVariable Long condominioId,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return service.reservasRecentes(condominioId, limit);
    }

    // ✅ adicional simples para eliminar Supabase do dashboard
    @GetMapping("/cadastros-recentes")
    public List<CadastroRecenteResponse> cadastrosRecentes(
            @PathVariable Long condominioId,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return service.cadastrosRecentes(condominioId, limit);
    }
}