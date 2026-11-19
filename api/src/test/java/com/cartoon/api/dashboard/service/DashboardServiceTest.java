package com.cartoon.api.dashboard.service;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.compartilhado.exceptions.RegraDeNegocioException;
import com.cartoon.api.dashboard.dto.projecao.ContagemMecanicoProjecao;
import com.cartoon.api.dashboard.dto.projecao.OsAberturaProjecao;
import com.cartoon.api.dashboard.dto.projecao.OsFinalizadaProjecao;
import com.cartoon.api.dashboard.dto.projecao.SomaPorOsProjecao;
import com.cartoon.api.dashboard.dto.response.DashboardAdminResponse.DesempenhoMecanico;
import com.cartoon.api.dashboard.dto.response.DashboardAdminResponse.Granularidade;
import com.cartoon.api.dashboard.dto.response.DashboardAdminResponse.PontoQuantidade;
import com.cartoon.api.dashboard.dto.response.DashboardAdminResponse.PontoValor;
import com.cartoon.api.dashboard.dto.response.DashboardAdminResponse.QuantidadePorStatus;
import com.cartoon.api.dashboard.dto.response.DashboardAdminResponse;
import com.cartoon.api.dashboard.repository.DashboardRepository;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.seguranca.TenantGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardServiceTest {

    private static final Integer OFICINA_ID = 7;
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 9);

    @Mock
    private DashboardRepository repository;

    @Mock
    private TenantGuard tenantGuard;

    private DashboardService service;
    private final UsuarioAutenticado admin = new UsuarioAutenticado(1L, "admin@oficina.com", "ROLE_ADMIN");

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(HOJE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new DashboardService(repository, tenantGuard, new CalculadoraFaturamento(),
                new CalculadoraOrdens(), new CalculadoraDesempenhoMecanico(), clock);

        Oficina oficina = new Oficina();
        oficina.setId(OFICINA_ID);
        when(tenantGuard.obterOficinaDoSolicitante(admin)).thenReturn(oficina);
        when(repository.somarTotalPorStatus(OFICINA_ID, StatusServico.FINALIZADO)).thenReturn(new BigDecimal("10000.00"));
        when(repository.listarFinalizadas(eq(OFICINA_ID), any(), any())).thenReturn(List.of());
        when(repository.listarAbertas(eq(OFICINA_ID), any(), any())).thenReturn(List.of());
        when(repository.contarPorMecanico(OFICINA_ID, StatusServico.EM_ANDAMENTO)).thenReturn(List.of());
        when(repository.somarPecasDasFinalizadas(eq(OFICINA_ID), any(), any())).thenReturn(List.of());
        when(repository.somarServicosDasFinalizadas(eq(OFICINA_ID), any(), any())).thenReturn(List.of());
    }

    private static OsFinalizadaProjecao os(int id, String inicio, String fim, String total, int mecId, String mecNome) {
        return new OsFinalizadaProjecao(id, inicio == null ? null : LocalDate.parse(inicio), LocalDate.parse(fim),
                new BigDecimal(total), mecId, mecNome);
    }

    @Test
    @DisplayName("Sem período informado usa o mês atual, granularidade diária e período anterior de mesmo tamanho")
    void periodoPadrao_MesAtual() {
        DashboardAdminResponse resp = service.dashboardAdmin(null, null, null, admin);

        assertThat(resp.periodo().inicio()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(resp.periodo().fim()).isEqualTo(HOJE);
        assertThat(resp.periodo().fimAnterior()).isEqualTo(LocalDate.of(2026, 10, 31));
        assertThat(resp.periodo().inicioAnterior()).isEqualTo(LocalDate.of(2026, 10, 23));
        assertThat(resp.periodo().granularidade()).isEqualTo(Granularidade.DIA);
        assertThat(resp.evolucaoFaturamento()).hasSize(9);
        assertThat(resp.faturamentoMensal()).hasSize(12);
        assertThat(resp.faturamentoMensal().get(11).chave()).isEqualTo("2026-11");
        assertThat(resp.faturamentoMensal().get(0).chave()).isEqualTo("2025-12");
        assertThat(resp.faturamento().totalAcumulado()).isEqualByComparingTo("10000.00");
        assertThat(resp.faturamento().variacaoPercentual()).isNull();
    }

    @Test
    @DisplayName("Faturamento do período, comparação com o período anterior e ticket médio usam apenas OS finalizadas")
    void faturamento_PeriodoEAnterior() {
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fim = LocalDate.of(2026, 11, 9);
        when(repository.listarFinalizadas(eq(OFICINA_ID), any(), eq(fim))).thenReturn(List.of(
                os(1, "2026-11-01", "2026-11-03", "300.00", 10, "Ana"),
                os(2, "2026-11-02", "2026-11-05", "500.00", 11, "Bruno"),
                os(3, "2026-10-20", "2026-10-25", "400.00", 10, "Ana"),
                os(4, "2026-06-01", "2026-06-10", "999.00", 11, "Bruno")
        ));

        DashboardAdminResponse resp = service.dashboardAdmin(inicio, fim, null, admin);

        assertThat(resp.faturamento().periodo()).isEqualByComparingTo("800.00");
        assertThat(resp.faturamento().periodoAnterior()).isEqualByComparingTo("400.00");
        assertThat(resp.faturamento().variacaoPercentual()).isEqualByComparingTo("100.0");
        assertThat(resp.faturamento().osFinalizadas()).isEqualTo(2);
        assertThat(resp.faturamento().ticketMedio()).isEqualByComparingTo("400.00");

        assertThat(resp.faturamentoMensal())
                .extracting(PontoValor::chave, p -> p.valor().toPlainString())
                .contains(
                        org.assertj.core.groups.Tuple.tuple("2026-06", "999.00"),
                        org.assertj.core.groups.Tuple.tuple("2026-10", "400.00"),
                        org.assertj.core.groups.Tuple.tuple("2026-11", "800.00"));

        PontoValor dia3 = resp.evolucaoFaturamento().stream().filter(p -> p.chave().equals("2026-11-03")).findFirst().orElseThrow();
        assertThat(dia3.valor()).isEqualByComparingTo("300.00");
    }

    @Test
    @DisplayName("Receita por tipo rateia o desconto da OS proporcionalmente entre peças e serviços")
    void receitaPorTipo_RateiaDesconto() {
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fim = LocalDate.of(2026, 11, 9);
        when(repository.listarFinalizadas(eq(OFICINA_ID), any(), eq(fim))).thenReturn(List.of(
                os(1, "2026-11-01", "2026-11-02", "900.00", 10, "Ana"),
                os(2, "2026-11-01", "2026-11-02", "50.00", 10, "Ana")
        ));
        when(repository.somarPecasDasFinalizadas(OFICINA_ID, inicio, fim))
                .thenReturn(List.of(new SomaPorOsProjecao(1, new BigDecimal("600.00"))));
        when(repository.somarServicosDasFinalizadas(OFICINA_ID, inicio, fim))
                .thenReturn(List.of(new SomaPorOsProjecao(1, new BigDecimal("400.00"))));

        DashboardAdminResponse resp = service.dashboardAdmin(inicio, fim, null, admin);

        assertThat(resp.receitaPorTipo().pecas()).isEqualByComparingTo("540.00");
        assertThat(resp.receitaPorTipo().servicos()).isEqualByComparingTo("410.00");
        assertThat(resp.receitaPorTipo().pecas().add(resp.receitaPorTipo().servicos()))
                .isEqualByComparingTo(resp.faturamento().periodo());
    }

    @Test
    @DisplayName("Desempenho por mecânico: ranking por faturamento, OS concluídas, em andamento e tempo médio em dias")
    void desempenhoMecanicos_Ranking() {
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fim = LocalDate.of(2026, 11, 9);
        when(repository.listarFinalizadas(eq(OFICINA_ID), any(), eq(fim))).thenReturn(List.of(
                os(1, "2026-11-01", "2026-11-03", "300.00", 10, "Ana"),
                os(2, "2026-11-02", "2026-11-03", "200.00", 10, "Ana"),
                os(3, "2026-11-04", "2026-11-04", "800.00", 11, "Bruno"),
                os(4, null, "2026-11-05", "100.00", 11, "Bruno")
        ));
        when(repository.contarPorMecanico(OFICINA_ID, StatusServico.EM_ANDAMENTO)).thenReturn(List.of(
                new ContagemMecanicoProjecao(10, "Ana", 2L),
                new ContagemMecanicoProjecao(12, "Carla", 1L)
        ));

        List<DesempenhoMecanico> mecs = service.dashboardAdmin(inicio, fim, null, admin).mecanicos();

        assertThat(mecs).extracting(DesempenhoMecanico::nome).containsExactly("Bruno", "Ana", "Carla");

        DesempenhoMecanico bruno = mecs.get(0);
        assertThat(bruno.faturamento()).isEqualByComparingTo("900.00");
        assertThat(bruno.osConcluidas()).isEqualTo(2);
        assertThat(bruno.osEmAndamento()).isZero();
        assertThat(bruno.tempoMedioDias()).isEqualByComparingTo("0.0");
        assertThat(bruno.participacaoPercentual()).isEqualByComparingTo("64.3");

        DesempenhoMecanico ana = mecs.get(1);
        assertThat(ana.osConcluidas()).isEqualTo(2);
        assertThat(ana.osEmAndamento()).isEqualTo(2);
        assertThat(ana.tempoMedioDias()).isEqualByComparingTo("1.5");
        assertThat(ana.ticketMedio()).isEqualByComparingTo("250.00");

        DesempenhoMecanico carla = mecs.get(2);
        assertThat(carla.osConcluidas()).isZero();
        assertThat(carla.osEmAndamento()).isEqualTo(1);
        assertThat(carla.tempoMedioDias()).isNull();
    }

    @Test
    @DisplayName("Volume de OS respeita o filtro de status; a distribuição por status traz todos os status")
    void volumeOrdens_FiltroStatus() {
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fim = LocalDate.of(2026, 11, 3);
        when(repository.listarAbertas(OFICINA_ID, inicio, fim)).thenReturn(List.of(
                new OsAberturaProjecao(LocalDate.of(2026, 11, 1), StatusServico.PENDENTE),
                new OsAberturaProjecao(LocalDate.of(2026, 11, 1), StatusServico.FINALIZADO),
                new OsAberturaProjecao(LocalDate.of(2026, 11, 3), StatusServico.PENDENTE)
        ));

        DashboardAdminResponse resp = service.dashboardAdmin(inicio, fim, StatusServico.PENDENTE, admin);

        assertThat(resp.volumeOrdens()).extracting(PontoQuantidade::chave, PontoQuantidade::quantidade)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("2026-11-01", 1L),
                        org.assertj.core.groups.Tuple.tuple("2026-11-02", 0L),
                        org.assertj.core.groups.Tuple.tuple("2026-11-03", 1L));
        assertThat(resp.ordensPorStatus()).hasSize(StatusServico.values().length);
        assertThat(resp.ordensPorStatus()).filteredOn(q -> q.status() == StatusServico.PENDENTE)
                .extracting(QuantidadePorStatus::quantidade).containsExactly(2L);
        assertThat(resp.periodo().status()).isEqualTo(StatusServico.PENDENTE);
    }

    @Test
    @DisplayName("Períodos longos são agrupados por mês")
    void granularidadeMensal_ParaPeriodosLongos() {
        DashboardAdminResponse resp = service.dashboardAdmin(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 11, 9), null, admin);

        assertThat(resp.periodo().granularidade()).isEqualTo(Granularidade.MES);
        assertThat(resp.evolucaoFaturamento()).hasSize(11);
        assertThat(resp.volumeOrdens().get(0).chave()).isEqualTo("2026-01");
    }

    @Test
    @DisplayName("Todas as consultas usam a oficina do usuário logado (isolamento multi-tenant)")
    void consultas_FiltradasPelaOficinaDoSolicitante() {
        service.dashboardAdmin(null, null, null, admin);

        verify(repository).somarTotalPorStatus(OFICINA_ID, StatusServico.FINALIZADO);
        verify(repository).listarFinalizadas(eq(OFICINA_ID), any(), any());
        verify(repository).listarAbertas(eq(OFICINA_ID), any(), any());
        verify(repository).contarPorMecanico(OFICINA_ID, StatusServico.EM_ANDAMENTO);
        verify(repository, never()).somarPecasDasFinalizadas(any(), any(), any());
    }

    @Test
    @DisplayName("Usuário sem oficina tem acesso negado e nenhuma consulta é executada")
    void semOficina_AcessoNegado() {
        UsuarioAutenticado semOficina = new UsuarioAutenticado(99L, "x@x.com", "ROLE_ADMIN");
        when(tenantGuard.obterOficinaDoSolicitante(semOficina))
                .thenThrow(new AccessDeniedException("Seu usuário não está vinculado a uma oficina."));

        assertThatThrownBy(() -> service.dashboardAdmin(null, null, null, semOficina))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Período inválido (início após o fim ou maior que 5 anos) gera erro de regra de negócio")
    void periodoInvalido() {
        assertThatThrownBy(() -> service.dashboardAdmin(LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 1), null, admin))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("data inicial");
        assertThatThrownBy(() -> service.dashboardAdmin(LocalDate.of(2015, 1, 1), LocalDate.of(2026, 11, 1), null, admin))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("5 anos");
    }
}
