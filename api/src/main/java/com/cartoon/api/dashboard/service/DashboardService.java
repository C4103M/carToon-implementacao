package com.cartoon.api.dashboard.service;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.compartilhado.exceptions.RegraDeNegocioException;
import com.cartoon.api.dashboard.dto.DashboardAdminResponse;
import com.cartoon.api.dashboard.dto.DashboardAdminResponse.DesempenhoMecanico;
import com.cartoon.api.dashboard.dto.DashboardAdminResponse.Faturamento;
import com.cartoon.api.dashboard.dto.DashboardAdminResponse.Granularidade;
import com.cartoon.api.dashboard.dto.DashboardAdminResponse.Periodo;
import com.cartoon.api.dashboard.dto.DashboardAdminResponse.PontoQuantidade;
import com.cartoon.api.dashboard.dto.DashboardAdminResponse.PontoValor;
import com.cartoon.api.dashboard.dto.DashboardAdminResponse.QuantidadePorStatus;
import com.cartoon.api.dashboard.dto.DashboardAdminResponse.ReceitaPorTipo;
import com.cartoon.api.dashboard.dto.projecao.ContagemMecanicoProjecao;
import com.cartoon.api.dashboard.dto.projecao.OsAberturaProjecao;
import com.cartoon.api.dashboard.dto.projecao.OsFinalizadaProjecao;
import com.cartoon.api.dashboard.dto.projecao.SomaPorOsProjecao;
import com.cartoon.api.dashboard.repository.DashboardRepository;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.seguranca.TenantGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DashboardService {

    static final int LIMITE_DIAS_GRANULARIDADE_DIARIA = 62;
    static final int LIMITE_DIAS_PERIODO = 366 * 5;
    static final int MESES_FATURAMENTO_MENSAL = 12;

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final DashboardRepository dashboardRepository;
    private final TenantGuard tenantGuard;
    private final Clock clock;

    @Autowired
    public DashboardService(DashboardRepository dashboardRepository, TenantGuard tenantGuard) {
        this(dashboardRepository, tenantGuard, Clock.systemDefaultZone());
    }

    DashboardService(DashboardRepository dashboardRepository, TenantGuard tenantGuard, Clock clock) {
        this.dashboardRepository = dashboardRepository;
        this.tenantGuard = tenantGuard;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardAdminResponse dashboardAdmin(LocalDate inicio, LocalDate fim, StatusServico status,
                                                 UsuarioAutenticado solicitante) {
        Oficina oficina = tenantGuard.obterOficinaDoSolicitante(solicitante);
        Integer oficinaId = oficina.getId();

        LocalDate hoje = LocalDate.now(clock);
        LocalDate fimEfetivo = fim != null ? fim : hoje;
        LocalDate inicioEfetivo = inicio != null ? inicio : fimEfetivo.withDayOfMonth(1);
        validarPeriodo(inicioEfetivo, fimEfetivo);

        long dias = ChronoUnit.DAYS.between(inicioEfetivo, fimEfetivo) + 1;
        LocalDate fimAnterior = inicioEfetivo.minusDays(1);
        LocalDate inicioAnterior = inicioEfetivo.minusDays(dias);
        Granularidade granularidade = dias > LIMITE_DIAS_GRANULARIDADE_DIARIA ? Granularidade.MES : Granularidade.DIA;

        YearMonth ultimoMes = YearMonth.from(fimEfetivo);
        YearMonth primeiroMes = ultimoMes.minusMonths(MESES_FATURAMENTO_MENSAL - 1L);
        LocalDate inicioConsulta = min(inicioAnterior, primeiroMes.atDay(1));

        List<OsFinalizadaProjecao> finalizadas = dashboardRepository.listarFinalizadas(oficinaId, inicioConsulta, fimEfetivo);
        List<OsFinalizadaProjecao> noPeriodo = filtrar(finalizadas, inicioEfetivo, fimEfetivo);
        List<OsFinalizadaProjecao> noAnterior = filtrar(finalizadas, inicioAnterior, fimAnterior);

        BigDecimal faturamentoPeriodo = somar(noPeriodo);
        BigDecimal faturamentoAnterior = somar(noAnterior);
        BigDecimal totalAcumulado = nz(dashboardRepository.somarTotalPorStatus(oficinaId, StatusServico.FINALIZADO));

        Faturamento faturamento = new Faturamento(
                totalAcumulado,
                faturamentoPeriodo,
                faturamentoAnterior,
                variacaoPercentual(faturamentoPeriodo, faturamentoAnterior),
                dividir(faturamentoPeriodo, noPeriodo.size()),
                noPeriodo.size()
        );

        ReceitaPorTipo receitaPorTipo = receitaPorTipo(oficinaId, inicioEfetivo, fimEfetivo, noPeriodo);

        List<PontoValor> faturamentoMensal = faturamentoMensal(finalizadas, primeiroMes, ultimoMes, fimEfetivo);
        List<PontoValor> evolucao = evolucaoFaturamento(noPeriodo, inicioEfetivo, fimEfetivo, granularidade);

        List<OsAberturaProjecao> abertas = dashboardRepository.listarAbertas(oficinaId, inicioEfetivo, fimEfetivo);
        List<PontoQuantidade> volume = volumeOrdens(abertas, status, inicioEfetivo, fimEfetivo, granularidade);
        List<QuantidadePorStatus> porStatus = ordensPorStatus(abertas);

        List<ContagemMecanicoProjecao> emAndamento =
                dashboardRepository.contarPorMecanico(oficinaId, StatusServico.EM_ANDAMENTO);
        List<DesempenhoMecanico> mecanicos = desempenhoMecanicos(noPeriodo, emAndamento, faturamentoPeriodo);

        Periodo periodo = new Periodo(inicioEfetivo, fimEfetivo, inicioAnterior, fimAnterior, granularidade, status);
        return new DashboardAdminResponse(periodo, faturamento, receitaPorTipo, faturamentoMensal, evolucao,
                volume, porStatus, mecanicos);
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio.isAfter(fim)) {
            throw new RegraDeNegocioException("A data inicial do período não pode ser posterior à data final.");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) + 1 > LIMITE_DIAS_PERIODO) {
            throw new RegraDeNegocioException("O período informado não pode ultrapassar 5 anos.");
        }
    }

    private ReceitaPorTipo receitaPorTipo(Integer oficinaId, LocalDate inicio, LocalDate fim,
                                          List<OsFinalizadaProjecao> noPeriodo) {
        if (noPeriodo.isEmpty()) {
            return new ReceitaPorTipo(zero(), zero());
        }
        Map<Integer, BigDecimal> pecasPorOs = paraMapa(dashboardRepository.somarPecasDasFinalizadas(oficinaId, inicio, fim));
        Map<Integer, BigDecimal> servicosPorOs = paraMapa(dashboardRepository.somarServicosDasFinalizadas(oficinaId, inicio, fim));

        BigDecimal totalPecas = BigDecimal.ZERO;
        BigDecimal totalServicos = BigDecimal.ZERO;
        for (OsFinalizadaProjecao os : noPeriodo) {
            BigDecimal liquido = nz(os.total());
            BigDecimal pecas = pecasPorOs.getOrDefault(os.id(), BigDecimal.ZERO);
            BigDecimal servicos = servicosPorOs.getOrDefault(os.id(), BigDecimal.ZERO);
            BigDecimal bruto = pecas.add(servicos);
            if (bruto.signum() == 0) {
                totalServicos = totalServicos.add(liquido);
                continue;
            }
            BigDecimal pecasLiquido = liquido.multiply(pecas).divide(bruto, 2, RoundingMode.HALF_UP);
            totalPecas = totalPecas.add(pecasLiquido);
            totalServicos = totalServicos.add(liquido.subtract(pecasLiquido));
        }
        return new ReceitaPorTipo(escala(totalServicos), escala(totalPecas));
    }

    private List<PontoValor> faturamentoMensal(List<OsFinalizadaProjecao> finalizadas, YearMonth primeiro,
                                               YearMonth ultimo, LocalDate fim) {
        Map<YearMonth, BigDecimal> porMes = new LinkedHashMap<>();
        for (YearMonth m = primeiro; !m.isAfter(ultimo); m = m.plusMonths(1)) {
            porMes.put(m, BigDecimal.ZERO);
        }
        for (OsFinalizadaProjecao os : finalizadas) {
            LocalDate data = os.dataFinalizacao();
            if (data == null || data.isAfter(fim)) {
                continue;
            }
            porMes.computeIfPresent(YearMonth.from(data), (k, v) -> v.add(nz(os.total())));
        }
        List<PontoValor> pontos = new ArrayList<>();
        porMes.forEach((mes, valor) -> pontos.add(new PontoValor(mes.toString(), escala(valor))));
        return pontos;
    }

    private List<PontoValor> evolucaoFaturamento(List<OsFinalizadaProjecao> noPeriodo, LocalDate inicio,
                                                 LocalDate fim, Granularidade granularidade) {
        Map<String, BigDecimal> serie = new LinkedHashMap<>();
        chaves(inicio, fim, granularidade).forEach(c -> serie.put(c, BigDecimal.ZERO));
        for (OsFinalizadaProjecao os : noPeriodo) {
            serie.computeIfPresent(chave(os.dataFinalizacao(), granularidade), (k, v) -> v.add(nz(os.total())));
        }
        List<PontoValor> pontos = new ArrayList<>();
        serie.forEach((k, v) -> pontos.add(new PontoValor(k, escala(v))));
        return pontos;
    }

    private List<PontoQuantidade> volumeOrdens(List<OsAberturaProjecao> abertas, StatusServico status,
                                               LocalDate inicio, LocalDate fim, Granularidade granularidade) {
        Map<String, Long> serie = new LinkedHashMap<>();
        chaves(inicio, fim, granularidade).forEach(c -> serie.put(c, 0L));
        for (OsAberturaProjecao os : abertas) {
            if (os.dataOrcamento() == null || (status != null && status != os.status())) {
                continue;
            }
            serie.computeIfPresent(chave(os.dataOrcamento(), granularidade), (k, v) -> v + 1);
        }
        List<PontoQuantidade> pontos = new ArrayList<>();
        serie.forEach((k, v) -> pontos.add(new PontoQuantidade(k, v)));
        return pontos;
    }

    private List<QuantidadePorStatus> ordensPorStatus(List<OsAberturaProjecao> abertas) {
        Map<StatusServico, Long> contagem = new EnumMap<>(StatusServico.class);
        for (StatusServico s : StatusServico.values()) {
            contagem.put(s, 0L);
        }
        for (OsAberturaProjecao os : abertas) {
            if (os.status() != null) {
                contagem.merge(os.status(), 1L, Long::sum);
            }
        }
        List<QuantidadePorStatus> lista = new ArrayList<>();
        contagem.forEach((s, q) -> lista.add(new QuantidadePorStatus(s, q)));
        return lista;
    }

    private List<DesempenhoMecanico> desempenhoMecanicos(List<OsFinalizadaProjecao> noPeriodo,
                                                         List<ContagemMecanicoProjecao> emAndamento,
                                                         BigDecimal faturamentoPeriodo) {
        Map<Integer, Acumulador> porMecanico = new LinkedHashMap<>();
        for (OsFinalizadaProjecao os : noPeriodo) {
            Acumulador acc = porMecanico.computeIfAbsent(os.mecanicoId(), id -> new Acumulador(id, os.mecanicoNome()));
            acc.faturamento = acc.faturamento.add(nz(os.total()));
            acc.concluidas++;
            if (os.dataInicio() != null && os.dataFinalizacao() != null) {
                acc.somaDias += Math.max(0, ChronoUnit.DAYS.between(os.dataInicio(), os.dataFinalizacao()));
                acc.comTempo++;
            }
        }
        for (ContagemMecanicoProjecao c : emAndamento) {
            Acumulador acc = porMecanico.computeIfAbsent(c.mecanicoId(), id -> new Acumulador(id, c.mecanicoNome()));
            acc.emAndamento = c.quantidade() != null ? c.quantidade() : 0L;
        }

        return porMecanico.values().stream()
                .map(acc -> new DesempenhoMecanico(
                        acc.id,
                        acc.nome,
                        escala(acc.faturamento),
                        acc.concluidas,
                        acc.emAndamento,
                        acc.comTempo == 0 ? null
                                : BigDecimal.valueOf(acc.somaDias).divide(BigDecimal.valueOf(acc.comTempo), 1, RoundingMode.HALF_UP),
                        dividir(acc.faturamento, acc.concluidas),
                        faturamentoPeriodo.signum() == 0 ? zero()
                                : acc.faturamento.multiply(CEM).divide(faturamentoPeriodo, 1, RoundingMode.HALF_UP)
                ))
                .sorted(Comparator.comparing(DesempenhoMecanico::faturamento).reversed()
                        .thenComparing(Comparator.comparingLong(DesempenhoMecanico::osConcluidas).reversed())
                        .thenComparing(DesempenhoMecanico::nome, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    private static List<String> chaves(LocalDate inicio, LocalDate fim, Granularidade granularidade) {
        List<String> chaves = new ArrayList<>();
        if (granularidade == Granularidade.DIA) {
            for (LocalDate d = inicio; !d.isAfter(fim); d = d.plusDays(1)) {
                chaves.add(d.toString());
            }
        } else {
            for (YearMonth m = YearMonth.from(inicio); !m.isAfter(YearMonth.from(fim)); m = m.plusMonths(1)) {
                chaves.add(m.toString());
            }
        }
        return chaves;
    }

    private static String chave(LocalDate data, Granularidade granularidade) {
        if (data == null) {
            return "";
        }
        return granularidade == Granularidade.DIA ? data.toString() : YearMonth.from(data).toString();
    }

    private static List<OsFinalizadaProjecao> filtrar(List<OsFinalizadaProjecao> lista, LocalDate de, LocalDate ate) {
        return lista.stream()
                .filter(os -> os.dataFinalizacao() != null
                        && !os.dataFinalizacao().isBefore(de)
                        && !os.dataFinalizacao().isAfter(ate))
                .toList();
    }

    private static BigDecimal somar(List<OsFinalizadaProjecao> lista) {
        return escala(lista.stream().map(os -> nz(os.total())).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static Map<Integer, BigDecimal> paraMapa(List<SomaPorOsProjecao> somas) {
        Map<Integer, BigDecimal> mapa = new HashMap<>();
        for (SomaPorOsProjecao s : somas) {
            mapa.merge(s.ordemServicoId(), nz(s.valor()), BigDecimal::add);
        }
        return mapa;
    }

    static BigDecimal variacaoPercentual(BigDecimal atual, BigDecimal anterior) {
        if (anterior == null || anterior.signum() == 0) {
            return null;
        }
        return atual.subtract(anterior).multiply(CEM).divide(anterior, 1, RoundingMode.HALF_UP);
    }

    private static BigDecimal dividir(BigDecimal valor, long quantidade) {
        return quantidade == 0 ? zero() : valor.divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.HALF_UP);
    }

    private static LocalDate min(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }

    private static BigDecimal nz(BigDecimal v) {
        return Objects.requireNonNullElse(v, BigDecimal.ZERO);
    }

    private static BigDecimal escala(BigDecimal v) {
        return nz(v).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2);
    }

    private static final class Acumulador {
        final Integer id;
        final String nome;
        BigDecimal faturamento = BigDecimal.ZERO;
        long concluidas;
        long emAndamento;
        long somaDias;
        long comTempo;

        Acumulador(Integer id, String nome) {
            this.id = id;
            this.nome = nome;
        }
    }
}
