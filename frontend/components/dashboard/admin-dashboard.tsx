'use client'

import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  ArrowDownRight,
  ArrowUpRight,
  CheckCircle2,
  Minus,
  Receipt,
  TrendingUp,
  Wallet,
} from 'lucide-react'
import { dashboardService } from '@/lib/services'
import type { DashboardAdminResponse, StatusServico } from '@/lib/types'
import { formatCurrency, formatDias, formatIsoDate, formatPercent } from '@/lib/format'
import { getErrorMessage } from '@/lib/error-handler'
import { cn } from '@/lib/utils'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { OrcamentoStatusBadge } from '@/components/status-badge'
import {
  EvolucaoFaturamentoChart,
  FaturamentoMensalChart,
  RankingMecanicosChart,
  VolumeOrdensChart,
} from './dashboard-charts'
import {
  PRESETS,
  STATUS_LABEL,
  STATUS_ORDEM,
  intervaloDoPreset,
  type PresetPeriodo,
} from './dashboard-utils'

type FiltroStatus = StatusServico | 'TODOS'

const STATUS_ITEMS: { value: FiltroStatus; label: string }[] = [
  { value: 'TODOS', label: 'Todos os status' },
  ...STATUS_ORDEM.map((s) => ({ value: s, label: STATUS_LABEL[s] })),
]

export function AdminDashboard() {
  const [preset, setPreset] = useState<PresetPeriodo>('mes')
  const [personalizado, setPersonalizado] = useState(() => intervaloDoPreset('mes'))
  const [status, setStatus] = useState<FiltroStatus>('TODOS')
  const [dados, setDados] = useState<DashboardAdminResponse | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)

  const intervalo = useMemo(
    () => (preset === 'personalizado' ? personalizado : intervaloDoPreset(preset)),
    [preset, personalizado],
  )
  const intervaloInvalido =
    !intervalo.inicio || !intervalo.fim || intervalo.inicio > intervalo.fim

  const carregar = useCallback(async () => {
    if (intervaloInvalido) return
    setCarregando(true)
    setErro(null)
    try {
      const resp = await dashboardService.admin({
        inicio: intervalo.inicio,
        fim: intervalo.fim,
        status: status === 'TODOS' ? undefined : status,
      })
      setDados(resp)
    } catch (e) {
      setErro(getErrorMessage(e, 'Não foi possível carregar as métricas do dashboard.'))
    } finally {
      setCarregando(false)
    }
  }, [intervalo.inicio, intervalo.fim, status, intervaloInvalido])

  useEffect(() => {
    carregar()
  }, [carregar])

  return (
    <section aria-labelledby="titulo-financeiro" className="mb-8 flex flex-col gap-4">
      <div className="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
        <div className="flex flex-col gap-1">
          <h2 id="titulo-financeiro" className="text-lg font-semibold tracking-tight">
            Visão financeira
          </h2>
          <p className="text-sm text-muted-foreground">
            Faturamento considera apenas ordens de serviço finalizadas, pela data de finalização.
          </p>
        </div>

        <div className="flex flex-wrap items-end gap-2">
          <Select
            items={PRESETS}
            value={preset}
            onValueChange={(v) => {
              const novo = v as PresetPeriodo
              if (novo === 'personalizado' && preset !== 'personalizado') {
                setPersonalizado(intervalo)
              }
              setPreset(novo)
            }}
          >
            <SelectTrigger className="w-[160px]" aria-label="Período">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {PRESETS.map((p) => (
                <SelectItem key={p.value} value={p.value}>
                  {p.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          {preset === 'personalizado' && (
            <div className="flex items-center gap-2">
              <Input
                type="date"
                aria-label="Data inicial"
                className="w-[150px]"
                value={personalizado.inicio}
                max={personalizado.fim || undefined}
                onChange={(e) => setPersonalizado((p) => ({ ...p, inicio: e.target.value }))}
              />
              <span className="text-sm text-muted-foreground">até</span>
              <Input
                type="date"
                aria-label="Data final"
                className="w-[150px]"
                value={personalizado.fim}
                min={personalizado.inicio || undefined}
                onChange={(e) => setPersonalizado((p) => ({ ...p, fim: e.target.value }))}
              />
            </div>
          )}

          <Select
            items={STATUS_ITEMS}
            value={status}
            onValueChange={(v) => setStatus(v as FiltroStatus)}
          >
            <SelectTrigger className="w-[170px]" aria-label="Status das ordens de serviço">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {STATUS_ITEMS.map((s) => (
                <SelectItem key={s.value} value={s.value}>
                  {s.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {intervaloInvalido && (
        <Alert variant="destructive">
          <AlertTitle>Período inválido</AlertTitle>
          <AlertDescription>A data inicial deve ser anterior ou igual à data final.</AlertDescription>
        </Alert>
      )}

      {erro && !intervaloInvalido && (
        <Alert variant="destructive">
          <AlertTitle>Erro ao carregar métricas</AlertTitle>
          <AlertDescription>{erro}</AlertDescription>
        </Alert>
      )}

      {!dados && carregando ? (
        <DashboardSkeleton />
      ) : dados ? (
        <div className={cn('flex flex-col gap-4 transition-opacity', carregando && 'opacity-60')} aria-busy={carregando}>
          <Kpis dados={dados} />

          <div className="grid gap-4 xl:grid-cols-3">
            <Card className="xl:col-span-2">
              <CardHeader>
                <CardTitle>Evolução do faturamento</CardTitle>
                <CardDescription>
                  {formatIsoDate(dados.periodo.inicio)} a {formatIsoDate(dados.periodo.fim)} ·{' '}
                  {dados.periodo.granularidade === 'DIA' ? 'por dia' : 'por mês'}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <EvolucaoFaturamentoChart
                  dados={dados.evolucaoFaturamento}
                  granularidade={dados.periodo.granularidade}
                />
              </CardContent>
            </Card>

            <ReceitaPorTipoCard dados={dados} />
          </div>

          <div className="grid gap-4 xl:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Faturamento mensal</CardTitle>
                <CardDescription>
                  Últimos 12 meses até {formatIsoDate(dados.periodo.fim)}. Passe o mouse para comparar com o mês anterior.
                </CardDescription>
              </CardHeader>
              <CardContent>
                <FaturamentoMensalChart dados={dados.faturamentoMensal} />
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Volume de ordens de serviço</CardTitle>
                <CardDescription>
                  Ordens abertas no período
                  {dados.periodo.status ? ` com status "${STATUS_LABEL[dados.periodo.status]}"` : ''}.
                </CardDescription>
              </CardHeader>
              <CardContent className="flex flex-col gap-4">
                <VolumeOrdensChart
                  dados={dados.volumeOrdens}
                  granularidade={dados.periodo.granularidade}
                />
                <OrdensPorStatus dados={dados} destaque={dados.periodo.status} />
              </CardContent>
            </Card>
          </div>

          <DesempenhoMecanicos dados={dados} />
        </div>
      ) : null}
    </section>
  )
}

function Kpis({ dados }: { dados: DashboardAdminResponse }) {
  const f = dados.faturamento
  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <KpiCard
        icon={Wallet}
        titulo="Faturamento total"
        valor={formatCurrency(f.totalAcumulado)}
        rodape="Acumulado de todas as OS finalizadas"
      />
      <KpiCard
        icon={TrendingUp}
        titulo="Faturamento no período"
        valor={formatCurrency(f.periodo)}
        rodape={<Variacao variacao={f.variacaoPercentual} anterior={f.periodoAnterior} />}
      />
      <KpiCard
        icon={CheckCircle2}
        titulo="OS finalizadas"
        valor={String(f.osFinalizadas)}
        rodape="Concluídas no período"
      />
      <KpiCard
        icon={Receipt}
        titulo="Ticket médio"
        valor={formatCurrency(f.ticketMedio)}
        rodape="Por OS finalizada no período"
      />
    </div>
  )
}

function KpiCard({
  icon: Icon,
  titulo,
  valor,
  rodape,
}: {
  icon: React.ComponentType<{ className?: string }>
  titulo: string
  valor: string
  rodape: React.ReactNode
}) {
  return (
    <Card>
      <CardContent className="flex flex-col gap-2">
        <div className="flex items-center justify-between text-sm text-muted-foreground">
          <span>{titulo}</span>
          <Icon className="size-4" />
        </div>
        <span className="font-mono text-2xl font-semibold tabular-nums">{valor}</span>
        <div className="text-xs text-muted-foreground">{rodape}</div>
      </CardContent>
    </Card>
  )
}

function Variacao({ variacao, anterior }: { variacao: number | null; anterior: number }) {
  if (variacao === null) {
    return <span>Sem faturamento no período anterior</span>
  }
  const Icone = variacao > 0 ? ArrowUpRight : variacao < 0 ? ArrowDownRight : Minus
  return (
    <span className="inline-flex flex-wrap items-center gap-1">
      <span
        className={cn(
          'inline-flex items-center gap-0.5 font-medium',
          variacao > 0 && 'text-success',
          variacao < 0 && 'text-destructive',
        )}
      >
        <Icone className="size-3.5" aria-hidden />
        {variacao > 0 ? '+' : ''}
        {formatPercent(variacao)}
      </span>
      <span>vs. período anterior ({formatCurrency(anterior)})</span>
    </span>
  )
}

function ReceitaPorTipoCard({ dados }: { dados: DashboardAdminResponse }) {
  const { servicos, pecas } = dados.receitaPorTipo
  const total = servicos + pecas
  const pctServicos = total > 0 ? (servicos / total) * 100 : 0
  const pctPecas = total > 0 ? 100 - pctServicos : 0
  const itens = [
    { chave: 'servicos', label: 'Serviços', valor: servicos, pct: pctServicos, cor: 'var(--chart-servicos)' },
    { chave: 'pecas', label: 'Peças', valor: pecas, pct: pctPecas, cor: 'var(--chart-pecas)' },
  ]

  return (
    <Card>
      <CardHeader>
        <CardTitle>Receita por tipo</CardTitle>
        <CardDescription>Serviços vs. peças no período, com desconto rateado.</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-5">
        {total === 0 ? (
          <p className="py-8 text-center text-sm text-muted-foreground">
            Nenhuma OS finalizada no período.
          </p>
        ) : (
          <>
            <div
              className="flex h-3 w-full gap-0.5 overflow-hidden rounded-full"
              role="img"
              aria-label={`Serviços ${formatPercent(pctServicos)}, peças ${formatPercent(pctPecas)}`}
            >
              {itens
                .filter((i) => i.pct > 0)
                .map((i) => (
                  <div
                    key={i.chave}
                    className="h-full first:rounded-l-full last:rounded-r-full"
                    style={{ width: `${i.pct}%`, background: i.cor }}
                    title={`${i.label}: ${formatCurrency(i.valor)} (${formatPercent(i.pct)})`}
                  />
                ))}
            </div>
            <ul className="flex flex-col gap-3">
              {itens.map((i) => (
                <li key={i.chave} className="flex items-center justify-between gap-3 text-sm">
                  <span className="flex items-center gap-2">
                    <span className="size-2.5 rounded-sm" style={{ background: i.cor }} aria-hidden />
                    {i.label}
                  </span>
                  <span className="flex items-baseline gap-2">
                    <span className="font-mono font-medium tabular-nums">{formatCurrency(i.valor)}</span>
                    <span className="w-14 text-right text-xs text-muted-foreground tabular-nums">
                      {formatPercent(i.pct)}
                    </span>
                  </span>
                </li>
              ))}
            </ul>
          </>
        )}
      </CardContent>
    </Card>
  )
}

function OrdensPorStatus({
  dados,
  destaque,
}: {
  dados: DashboardAdminResponse
  destaque: StatusServico | null
}) {
  const porStatus = new Map(dados.ordensPorStatus.map((s) => [s.status, s.quantidade]))
  const maximo = Math.max(1, ...dados.ordensPorStatus.map((s) => s.quantidade))
  return (
    <div className="flex flex-col gap-2 border-t pt-4">
      <span className="text-xs font-medium text-muted-foreground">Distribuição por status (OS abertas no período)</span>
      <ul className="flex flex-col gap-1.5">
        {STATUS_ORDEM.map((s) => {
          const qtd = porStatus.get(s) ?? 0
          return (
            <li
              key={s}
              className={cn(
                'grid grid-cols-[7.5rem_1fr_2.5rem] items-center gap-3 text-sm',
                destaque && destaque !== s && 'opacity-50',
              )}
            >
              <OrcamentoStatusBadge status={s} />
              <div className="h-2 rounded-full bg-muted">
                <div
                  className="h-full rounded-full bg-muted-foreground/60"
                  style={{ width: `${(qtd / maximo) * 100}%` }}
                />
              </div>
              <span className="text-right font-mono tabular-nums">{qtd}</span>
            </li>
          )
        })}
      </ul>
    </div>
  )
}

function DesempenhoMecanicos({ dados }: { dados: DashboardAdminResponse }) {
  const mecanicos = dados.mecanicos
  return (
    <Card>
      <CardHeader>
        <CardTitle>Desempenho por mecânico</CardTitle>
        <CardDescription>
          Ranking por faturamento no período. &ldquo;Em andamento&rdquo; mostra a carga atual de cada profissional.
        </CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-6">
        {mecanicos.length === 0 ? (
          <p className="py-8 text-center text-sm text-muted-foreground">
            Nenhuma OS finalizada ou em andamento para exibir.
          </p>
        ) : (
          <>
            {mecanicos.some((m) => m.faturamento > 0) && <RankingMecanicosChart dados={mecanicos} />}
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="w-10">#</TableHead>
                    <TableHead>Mecânico</TableHead>
                    <TableHead className="text-right">Faturamento</TableHead>
                    <TableHead className="text-right">Participação</TableHead>
                    <TableHead className="text-right">Concluídas</TableHead>
                    <TableHead className="text-right">Em andamento</TableHead>
                    <TableHead className="text-right">Tempo médio</TableHead>
                    <TableHead className="text-right">Ticket médio</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {mecanicos.map((m, i) => (
                    <TableRow key={m.mecanicoId}>
                      <TableCell className="text-muted-foreground tabular-nums">{i + 1}</TableCell>
                      <TableCell className="font-medium">{m.nome}</TableCell>
                      <TableCell className="text-right font-mono tabular-nums">{formatCurrency(m.faturamento)}</TableCell>
                      <TableCell className="text-right tabular-nums">{formatPercent(m.participacaoPercentual)}</TableCell>
                      <TableCell className="text-right tabular-nums">{m.osConcluidas}</TableCell>
                      <TableCell className="text-right tabular-nums">{m.osEmAndamento}</TableCell>
                      <TableCell className="text-right tabular-nums">{formatDias(m.tempoMedioDias)}</TableCell>
                      <TableCell className="text-right font-mono tabular-nums">
                        {m.osConcluidas > 0 ? formatCurrency(m.ticketMedio) : '—'}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </>
        )}
      </CardContent>
    </Card>
  )
}

function DashboardSkeleton() {
  return (
    <div className="flex flex-col gap-4" aria-busy aria-label="Carregando métricas">
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <Skeleton key={i} className="h-[108px] rounded-xl" />
        ))}
      </div>
      <div className="grid gap-4 xl:grid-cols-3">
        <Skeleton className="h-[340px] rounded-xl xl:col-span-2" />
        <Skeleton className="h-[340px] rounded-xl" />
      </div>
    </div>
  )
}
