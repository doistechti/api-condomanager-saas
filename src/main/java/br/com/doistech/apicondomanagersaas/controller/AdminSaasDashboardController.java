package br.com.doistech.apicondomanagersaas.controller;

import br.com.doistech.apicondomanagersaas.dto.adminsaas.DashboardStatsResponse;
import br.com.doistech.apicondomanagersaas.dto.adminsaas.RecentCondominioResponse;
import br.com.doistech.apicondomanagersaas.dto.adminsaas.RecentPaymentResponse;
import br.com.doistech.apicondomanagersaas.service.AdminSaasDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin-saas/dashboard")
@PreAuthorize("hasRole('ADMIN_SAAS')")
public class AdminSaasDashboardController {

    private final AdminSaasDashboardService service;

    public AdminSaasDashboardController(AdminSaasDashboardService service) {
        this.service = service;
    }

    @GetMapping("/stats")
    public DashboardStatsResponse stats() {
        return service.getStats();
    }

    @GetMapping("/recent-condominios")
    public List<RecentCondominioResponse> recentCondominios(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return service.getRecentCondominios(limit);
    }

    @GetMapping("/recent-payments")
    public List<RecentPaymentResponse> recentPayments(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return service.getRecentPayments(limit);
    }
}

