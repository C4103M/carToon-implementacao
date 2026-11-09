package com.cartoon.api.dashboard.service;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.dashboard.dto.projecao.OsAberturaProjecao;
import com.cartoon.api.dashboard.dto.projecao.OsFinalizadaProjecao;
import com.cartoon.api.dashboard.dto.response.DashboardAdminResponse.Faturamento;
import com.cartoon.api.dashboard.dto.response.DashboardAdminResponse.ReceitaPorTipo;
import com.cartoon.api.dashboard.dto.response.DashboardAdminResponse;
import com.cartoon.api.dashboard.repository.DashboardRepository;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.seguranca.TenantGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final TenantGuard tenantGuard;
    private final CalculadoraFaturamento calculadoraFaturamento;
    private final CalculadoraOrdens calculadoraOrdens;
    private final CalculadoraDesempenhoMecanico calculadoraDesempenho;
    private final Clock clock;

    @Transactional(readOnly = true)
    public DashboardAdminResponse dashboardAdmin(LocalDate inicio, LocalDate fim, StatusServico status,
                                                 UsuarioAutenticado solicitante) {
        Integer oficinaId = tenantGuard.obterOficinaDoSolicitante(solicitante).getId();
        PeriodoDashboard periodo = PeriodoDashboard.resolver(inicio, fim, LocalDate.now(clock));

        List<OsFinalizadaProjecao> finalizadas =
                dashboardRepository.listarFinalizadas(oficinaId, periodo.inicioConsulta(), periodo.fim());
        List<OsFinalizadaProjecao> noPeriodo = finalizadas.stream()
                .filter(os -> periodo.contem(os.dataFinalizacao()))
                .toList();
        List<OsFinalizadaProjecao> noAnterior = finalizadas.stream()
                .filter(os -> periodo.contemNoAnterior(os.dataFinalizacao()))
                .toList();

        Faturamento faturamento = calculadoraFaturamento.resumo(
                dashboardRepository.somarTotalPorStatus(oficinaId, StatusServico.FINALIZADO), noPeriodo, noAnterior);
        ReceitaPorTipo receitaPorTipo = receitaPorTipo(oficinaId, periodo, noPeriodo);

        List<OsAberturaProjecao> abertas = dashboardRepository.listarAbertas(oficinaId, periodo.inicio(), periodo.fim());

        return new DashboardAdminResponse(
                periodo.paraResposta(status),
                faturamento,
                receitaPorTipo,
                calculadoraFaturamento.mensal(finalizadas, periodo),
                calculadoraFaturamento.evolucao(noPeriodo, periodo),
                calculadoraOrdens.volume(abertas, status, periodo),
                calculadoraOrdens.porStatus(abertas),
                calculadoraDesempenho.calcular(
                        noPeriodo,
                        dashboardRepository.contarPorMecanico(oficinaId, StatusServico.EM_ANDAMENTO),
                        faturamento.periodo())
        );
    }

    private ReceitaPorTipo receitaPorTipo(Integer oficinaId, PeriodoDashboard periodo,
                                          List<OsFinalizadaProjecao> noPeriodo) {
        if (noPeriodo.isEmpty()) {
            return calculadoraFaturamento.receitaPorTipo(noPeriodo, List.of(), List.of());
        }
        return calculadoraFaturamento.receitaPorTipo(
                noPeriodo,
                dashboardRepository.somarPecasDasFinalizadas(oficinaId, periodo.inicio(), periodo.fim()),
                dashboardRepository.somarServicosDasFinalizadas(oficinaId, periodo.inicio(), periodo.fim())
        );
    }
}
