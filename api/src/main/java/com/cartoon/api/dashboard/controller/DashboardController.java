package com.cartoon.api.dashboard.controller;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.dashboard.dto.DashboardAdminResponse;
import com.cartoon.api.dashboard.service.DashboardService;
import com.cartoon.api.ordemServico.models.StatusServico;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/dashboard")
@AllArgsConstructor
@Tag(name = "Dashboard", description = "Métricas financeiras e operacionais da oficina")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @Operation(summary = "Métricas do dashboard do ADMIN",
            description = "Faturamento (somente OS finalizadas), receita por tipo, séries temporais, volume de OS e "
                    + "desempenho por mecânico. Sem período informado, usa o mês atual.")
    public DashboardAdminResponse admin(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) StatusServico status,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return dashboardService.dashboardAdmin(inicio, fim, status, solicitante);
    }
}
