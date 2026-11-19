'use client'

import Link from 'next/link'
import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  ArrowDownRight,
  ArrowRight,
  ArrowUpRight,
  BarChart3,
  CheckCircle2,
  ClipboardList,
  FilePlus2,
  FileText,
  Minus,
  Receipt,
  TrendingUp,
  Users,
  Wallet,
} from 'lucide-react'
import { dashboardService } from '@/lib/services'
import type {
  DashboardAdminResponse,
  OrdemServicoResumo,
  StatusServico,
} from '@/lib/types'
import {
  formatCurrency,
  formatDate,
  formatDias,
  formatIsoDate,
  formatPercent,
} from '@/lib/format'
import { getErrorMessage } from '@/lib/error-handler'
import { cn } from '@/lib/utils'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { Spinner } from '@/components/ui/spinner'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
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
import { EmptyState } from '@/components/empty-state'
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

interface AdminDashboardProps {
  pendentes?: OrdemServicoResumo[]
  carregandoPendentes?: boolean
}

export function AdminDashboard({
  pendentes = [],
  carregandoPendentes = false,
}: AdminDashboardProps) {
  const [preset, setPreset] = useState<PresetPeriodo>('mes')
  const [personalizado, setPersonalizado] = useState(() =>
    intervaloDoPreset('mes'),
  )
  const [status, setStatus] = useState<FiltroStatus>('TODOS')
  const [dados, setDados] = useState<DashboardAdminResponse | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)

  const intervalo = useMemo(
    () =>
      preset === 'personalizado' ? personalizado : intervaloDoPreset(preset),
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
      setErro(
        getErrorMessage(
          e,
          'Não foi possível carregar as métricas do dashboard.',
        ),
      )
    } finally {
      setCarregando(false)
    }
  }, [intervalo.inicio, intervalo.fim, status, intervaloInvalido])

  useEffect(() => {
    carregar()
  }, [carregar])

  return (
    <section aria-label="Painel gerencial da oficina" className="flex flex-col gap-6">
      {/* Barra de Filtros Integrada e Limpa */}
      <div className="flex flex-col gap-3 rounded-xl border border-border/80 bg-card p-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex flex-col gap-0.5">
          <span className="text-sm font-semibold text-foreground">
            Período e filtro de análise
          </span>
          <span className="text-xs text-muted-foreground">
            O faturamento considera ordens finalizadas dentro do intervalo selecionado.
          </span>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
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
            <SelectTrigger className="w-[155px]" aria-label="Período">
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
                className="w-[145px]"
                value={personalizado.inicio}
                max={personalizado.fim || undefined}
                onChange={(e) =>
                  setPersonalizado((p) => ({ ...p, inicio: e.target.value }))
                }
              />
              <span className="text-xs font-medium text-muted-foreground">
                até
              </span>
              <Input
                type="date"
                aria-label="Data final"
                className="w-[145px]"
                value={personalizado.fim}
                min={personalizado.inicio || undefined}
                onChange={(e) =>
                  setPersonalizado((p) => ({ ...p, fim: e.target.value }))
                }
              />
            </div>
          )}

          <Select
            items={STATUS_ITEMS}
            value={status}
            onValueChange={(v) => setStatus(v as FiltroStatus)}
          >
            <SelectTrigger
              className="w-[165px]"
              aria-label="Status das ordens de serviço"
            >
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
          <AlertDescription>
            A data inicial deve ser anterior ou igual à data final.
          </AlertDescription>
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
        <div
          className={cn(
            'flex flex-col gap-6 transition-opacity',
            carregando && 'opacity-60',
          )}
          aria-busy={carregando}
        >
          {/* 1. Resumo Executivo (4 indicadores-chave no topo) */}
          <Kpis dados={dados} totalPendentes={pendentes.length} />

          {/* 2. Navegação em Abas para separar Pátio, Financeiro e Equipe */}
          <Tabs defaultValue="patio" className="gap-5">
            <div className="flex flex-col gap-3 border-b border-border pb-3 sm:flex-row sm:items-center sm:justify-between">
              <TabsList className="h-10 p-1">
                <TabsTrigger value="patio" className="px-3.5 py-1.5">
                  <ClipboardList className="size-4" />
                  <span>Pátio & Ordens</span>
                  {pendentes.length > 0 && (
                    <span className="ml-1 rounded-full bg-primary/15 px-2 py-0.5 font-mono text-xs font-bold text-primary tabular-nums">
                      {pendentes.length}
                    </span>
                  )}
                </TabsTrigger>
                <TabsTrigger value="financeiro" className="px-3.5 py-1.5">
                  <BarChart3 className="size-4" />
                  <span>Financeiro</span>
                </TabsTrigger>
                <TabsTrigger value="equipe" className="px-3.5 py-1.5">
                  <Users className="size-4" />
                  <span>Mecânicos</span>
                </TabsTrigger>
              </TabsList>

              <span className="text-xs font-medium text-muted-foreground tabular-nums">
                Recorte: {formatIsoDate(dados.periodo.inicio)} a{' '}
                {formatIsoDate(dados.periodo.fim)}
              </span>
            </div>

            {/* ABA 1: PÁTIO & ORDENS (Operação do dia a dia + distribuição de status) */}
            <TabsContent value="patio" className="flex flex-col gap-6">
              <div className="grid gap-6 xl:grid-cols-12">
                <div className="xl:col-span-7">
                  <ListaOrcamentosPendentes
                    pendentes={pendentes}
                    carregando={carregandoPendentes}
                  />
                </div>

                <div className="flex flex-col gap-6 xl:col-span-5">
                  <OrdensPorStatusCard
                    dados={dados}
                    destaque={dados.periodo.status}
                  />

                  <Card>
                    <CardHeader>
                      <CardTitle>Ritmo de abertura de ordens</CardTitle>
                      <CardDescription>
                        Quantidade de OS abertas{' '}
                        {dados.periodo.granularidade === 'DIA'
                          ? 'por dia'
                          : 'por mês'}{' '}
                        no período
                        {dados.periodo.status
                          ? ` (${STATUS_LABEL[dados.periodo.status]})`
                          : ''}
                        .
                      </CardDescription>
                    </CardHeader>
                    <CardContent>
                      <VolumeOrdensChart
                        dados={dados.volumeOrdens}
                        granularidade={dados.periodo.granularidade}
                      />
                    </CardContent>
                  </Card>
                </div>
              </div>
            </TabsContent>

            {/* ABA 2: FINANCEIRO (Gráficos de faturamento e composição de receita) */}
            <TabsContent value="financeiro" className="flex flex-col gap-6">
              <div className="grid gap-6 xl:grid-cols-12">
                <Card className="xl:col-span-8">
                  <CardHeader>
                    <CardTitle>Evolução do faturamento</CardTitle>
                    <CardDescription>
                      Receita diária/mensal de ordens finalizadas entre{' '}
                      {formatIsoDate(dados.periodo.inicio)} e{' '}
                      {formatIsoDate(dados.periodo.fim)}.
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <EvolucaoFaturamentoChart
                      dados={dados.evolucaoFaturamento}
                      granularidade={dados.periodo.granularidade}
                    />
                  </CardContent>
                </Card>

                <div className="xl:col-span-4">
                  <ReceitaPorTipoCard dados={dados} />
                </div>
              </div>

              <Card>
                <CardHeader>
                  <CardTitle>Histórico dos últimos 12 meses</CardTitle>
                  <CardDescription>
                    Comparativo mensal até {formatIsoDate(dados.periodo.fim)}.
                    Passe o cursor nas barras para ver a variação percentual.
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <FaturamentoMensalChart dados={dados.faturamentoMensal} />
                </CardContent>
              </Card>
            </TabsContent>

            {/* ABA 3: MECÂNICOS (Produtividade e participação no faturamento) */}
            <TabsContent value="equipe" className="flex flex-col gap-6">
              <DesempenhoMecanicos dados={dados} />
            </TabsContent>
          </Tabs>
        </div>
      ) : null}
    </section>
  )
}

function Kpis({
  dados,
  totalPendentes,
}: {
  dados: DashboardAdminResponse
  totalPendentes: number
}) {
  const f = dados.faturamento
  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <KpiCard
        icon={TrendingUp}
        titulo="Faturamento no período"
        valor={formatCurrency(f.periodo)}
        rodape={
          <Variacao
            variacao={f.variacaoPercentual}
            anterior={f.periodoAnterior}
          />
        }
      />
      <KpiCard
        icon={Receipt}
        titulo="Ticket médio"
        valor={formatCurrency(f.ticketMedio)}
        rodape={`Acima de ${f.osFinalizadas} OS finalizadas no período`}
      />
      <KpiCard
        icon={Wallet}
        titulo="Faturamento acumulado"
        valor={formatCurrency(f.totalAcumulado)}
        rodape="Soma histórica da oficina"
      />
      <KpiCard
        icon={ClipboardList}
        titulo="Aguardando aprovação"
        valor={String(totalPendentes)}
        destaque={totalPendentes > 0}
        rodape={
          totalPendentes === 1
            ? '1 orçamento pendente de decisão'
            : `${totalPendentes} orçamentos pendentes de decisão`
        }
      />
    </div>
  )
}

function KpiCard({
  icon: Icon,
  titulo,
  valor,
  rodape,
  destaque = false,
}: {
  icon: React.ComponentType<{ className?: string }>
  titulo: string
  valor: string
  rodape: React.ReactNode
  destaque?: boolean
}) {
  return (
    <Card className={cn(destaque && 'border-primary/35 bg-primary/5')}>
      <CardContent className="flex flex-col gap-2.5 py-1">
        <div className="flex items-center justify-between text-xs font-semibold uppercase tracking-wider text-muted-foreground">
          <span>{titulo}</span>
          <span
            className={cn(
              'flex size-8 items-center justify-center rounded-lg',
              destaque
                ? 'bg-primary text-primary-foreground'
                : 'bg-secondary text-foreground',
            )}
          >
            <Icon className="size-4" />
          </span>
        </div>
        <span className="font-display text-2xl font-extrabold tracking-tight text-foreground tabular-nums">
          {valor}
        </span>
        <div className="text-xs font-medium text-muted-foreground">{rodape}</div>
      </CardContent>
    </Card>
  )
}

function Variacao({
  variacao,
  anterior,
}: {
  variacao: number | null
  anterior: number
}) {
  if (variacao === null) {
    return <span>Sem faturamento no período anterior</span>
  }
  const Icone =
    variacao > 0 ? ArrowUpRight : variacao < 0 ? ArrowDownRight : Minus
  return (
    <span className="inline-flex flex-wrap items-center gap-1">
      <span
        className={cn(
          'inline-flex items-center gap-0.5 font-semibold',
          variacao > 0 && 'text-success',
          variacao < 0 && 'text-destructive',
        )}
      >
        <Icone className="size-3.5" aria-hidden />
        {variacao > 0 ? '+' : ''}
        {formatPercent(variacao)}
      </span>
      <span>vs. anterior ({formatCurrency(anterior)})</span>
    </span>
  )
}

export function ListaOrcamentosPendentes({
  pendentes,
  carregando,
}: {
  pendentes: OrdemServicoResumo[]
  carregando: boolean
}) {
  return (
    <Card className="h-full">
      <CardHeader className="flex-row items-center justify-between border-b border-border/60 pb-4">
        <div className="flex flex-col gap-1">
          <CardTitle>Orçamentos aguardando cliente</CardTitle>
          <CardDescription>
            Ordens com status pendente que precisam de aprovação para iniciar.
          </CardDescription>
        </div>
        <Button
          variant="outline"
          size="sm"
          nativeButton={false}
          render={<Link href="/app/orcamentos" />}
        >
          Ver todas as OS
          <ArrowRight data-icon="inline-end" />
        </Button>
      </CardHeader>
      <CardContent className="pt-2">
        {carregando ? (
          <div className="flex items-center justify-center gap-2 py-12 text-muted-foreground">
            <Spinner className="size-5" />
            <span className="text-sm font-medium">
              Carregando orçamentos...
            </span>
          </div>
        ) : pendentes.length === 0 ? (
          <EmptyState
            icon={FileText}
            title="Nenhum orçamento pendente"
            description="Todos os orçamentos já foram respondidos. Abra um novo orçamento quando o próximo veículo chegar."
            action={
              <Button
                nativeButton={false}
                render={<Link href="/app/orcamentos/novo" />}
              >
                <FilePlus2 data-icon="inline-start" />
                Novo orçamento
              </Button>
            }
          />
        ) : (
          <div className="flex flex-col divide-y divide-border/70">
            {pendentes.map((o) => {
              const statusEfetivo = o.statusServico || o.status || 'PENDENTE'
              const total = Number(o.valorTotal ?? o.total ?? 0)
              const veiculoLabel =
                o.veiculoMarca || o.veiculoModelo
                  ? `${o.veiculoMarca || ''} ${o.veiculoModelo || ''}`.trim()
                  : 'Veículo'

              return (
                <Link
                  key={o.id}
                  href={`/app/orcamentos/${o.id}`}
                  className="group flex flex-col justify-between gap-3 py-3.5 transition-colors hover:bg-secondary/50 -mx-3 px-3 rounded-lg sm:flex-row sm:items-center"
                >
                  <div className="flex min-w-0 flex-col gap-1">
                    <div className="flex items-center gap-2">
                      <span className="font-mono text-xs font-bold text-primary">
                        #{o.id}
                      </span>
                      <span className="truncate font-display text-sm font-bold text-foreground group-hover:text-primary transition-colors">
                        {o.clienteNome || o.nomeCliente || 'Cliente'}
                      </span>
                    </div>
                    <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                      <span className="font-medium text-foreground/80">
                        {veiculoLabel}
                      </span>
                      {o.placa && (
                        <>
                          <span>·</span>
                          <span className="rounded bg-secondary px-1.5 py-0.5 font-mono text-[11px] font-semibold text-foreground">
                            {o.placa}
                          </span>
                        </>
                      )}
                      {o.dataOrcamento && (
                        <>
                          <span>·</span>
                          <span>Aberto em {formatDate(o.dataOrcamento)}</span>
                        </>
                      )}
                    </div>
                  </div>
                  <div className="flex shrink-0 items-center justify-between gap-4 sm:justify-end">
                    <span className="font-mono text-sm font-bold text-foreground tabular-nums">
                      {formatCurrency(total)}
                    </span>
                    <OrcamentoStatusBadge status={statusEfetivo} />
                  </div>
                </Link>
              )
            })}
          </div>
        )}
      </CardContent>
    </Card>
  )
}

function ReceitaPorTipoCard({ dados }: { dados: DashboardAdminResponse }) {
  const { servicos, pecas } = dados.receitaPorTipo
  const total = servicos + pecas
  const pctServicos = total > 0 ? (servicos / total) * 100 : 0
  const pctPecas = total > 0 ? 100 - pctServicos : 0
  const itens = [
    {
      chave: 'servicos',
      label: 'Mão de obra (Serviços)',
      valor: servicos,
      pct: pctServicos,
      cor: 'var(--chart-servicos)',
    },
    {
      chave: 'pecas',
      label: 'Peças aplicadas',
      valor: pecas,
      pct: pctPecas,
      cor: 'var(--chart-pecas)',
    },
  ]

  return (
    <Card className="h-full">
      <CardHeader>
        <CardTitle>Composição da receita</CardTitle>
        <CardDescription>
          Divisão entre mão de obra e peças nas OS finalizadas (com desconto rateado).
        </CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col justify-between gap-6">
        {total === 0 ? (
          <p className="py-8 text-center text-sm text-muted-foreground">
            Nenhuma OS finalizada no período.
          </p>
        ) : (
          <>
            <div className="flex flex-col gap-2">
              <span className="text-xs font-medium text-muted-foreground">
                Receita líquida total
              </span>
              <span className="font-display text-2xl font-extrabold text-foreground tabular-nums">
                {formatCurrency(total)}
              </span>
              <div
                className="mt-2 flex h-3.5 w-full gap-1 overflow-hidden rounded-full bg-secondary p-0.5"
                role="img"
                aria-label={`Serviços ${formatPercent(pctServicos)}, peças ${formatPercent(pctPecas)}`}
              >
                {itens
                  .filter((i) => i.pct > 0)
                  .map((i) => (
                    <div
                      key={i.chave}
                      className="h-full first:rounded-l-full last:rounded-r-full transition-all"
                      style={{ width: `${i.pct}%`, background: i.cor }}
                      title={`${i.label}: ${formatCurrency(i.valor)} (${formatPercent(i.pct)})`}
                    />
                  ))}
              </div>
            </div>

            <ul className="flex flex-col divide-y divide-border/60 border-t border-border/60 pt-2">
              {itens.map((i) => (
                <li
                  key={i.chave}
                  className="flex items-center justify-between gap-3 py-3 text-sm"
                >
                  <span className="flex items-center gap-2.5 font-medium text-foreground">
                    <span
                      className="size-3 shrink-0 rounded-sm"
                      style={{ background: i.cor }}
                      aria-hidden
                    />
                    {i.label}
                  </span>
                  <div className="flex flex-col items-end">
                    <span className="font-mono font-bold text-foreground tabular-nums">
                      {formatCurrency(i.valor)}
                    </span>
                    <span className="text-xs text-muted-foreground tabular-nums">
                      {formatPercent(i.pct)}
                    </span>
                  </div>
                </li>
              ))}
            </ul>
          </>
        )}
      </CardContent>
    </Card>
  )
}

const STATUS_VISUAL: Record<
  StatusServico,
  { corBarra: string; corTexto: string; corPonto: string }
> = {
  PENDENTE: {
    corBarra: 'bg-warning',
    corTexto: 'text-warning',
    corPonto: 'bg-warning',
  },
  ACEITO: {
    corBarra: 'bg-success',
    corTexto: 'text-success',
    corPonto: 'bg-success',
  },
  EM_ANDAMENTO: {
    corBarra: 'bg-primary',
    corTexto: 'text-primary',
    corPonto: 'bg-primary',
  },
  FINALIZADO: {
    corBarra: 'bg-info',
    corTexto: 'text-info',
    corPonto: 'bg-info',
  },
  REJEITADO: {
    corBarra: 'bg-destructive',
    corTexto: 'text-destructive',
    corPonto: 'bg-destructive',
  },
}

function OrdensPorStatusCard({
  dados,
  destaque,
}: {
  dados: DashboardAdminResponse
  destaque: StatusServico | null
}) {
  const porStatus = new Map(
    dados.ordensPorStatus.map((s) => [s.status, s.quantidade]),
  )
  const totalOrdens = dados.ordensPorStatus.reduce(
    (acc, s) => acc + s.quantidade,
    0,
  )
  const maximo = Math.max(1, ...dados.ordensPorStatus.map((s) => s.quantidade))

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Distribuição por status</CardTitle>
          <span className="rounded-md bg-secondary px-2 py-0.5 font-mono text-xs font-semibold text-foreground tabular-nums">
            {totalOrdens} {totalOrdens === 1 ? 'OS no período' : 'OS no período'}
          </span>
        </div>
        <CardDescription>
          Situação atual das ordens abertas dentro do período selecionado.
        </CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-5">
        {/* Barra proporcional segmentada */}
        {totalOrdens > 0 && (
          <div
            className="flex h-3 w-full gap-1 overflow-hidden rounded-full bg-secondary p-0.5"
            role="img"
            aria-label="Proporção de ordens de serviço por status"
          >
            {STATUS_ORDEM.map((s) => {
              const qtd = porStatus.get(s) ?? 0
              if (qtd === 0) return null
              const pct = (qtd / totalOrdens) * 100
              const visual = STATUS_VISUAL[s]
              return (
                <div
                  key={s}
                  className={cn(
                    'h-full first:rounded-l-full last:rounded-r-full transition-all',
                    visual.corBarra,
                    destaque && destaque !== s && 'opacity-35',
                  )}
                  style={{ width: `${pct}%` }}
                  title={`${STATUS_LABEL[s]}: ${qtd} (${formatPercent(pct)})`}
                />
              )
            })}
          </div>
        )}

        {/* Lista limpa com indicadores geométricos e barras semânticas */}
        <ul className="flex flex-col divide-y divide-border/50">
          {STATUS_ORDEM.map((s) => {
            const qtd = porStatus.get(s) ?? 0
            const pct = totalOrdens > 0 ? (qtd / totalOrdens) * 100 : 0
            const visual = STATUS_VISUAL[s]
            return (
              <li
                key={s}
                className={cn(
                  'flex flex-col gap-1.5 py-2.5 first:pt-0 last:pb-0',
                  destaque && destaque !== s && 'opacity-45',
                )}
              >
                <div className="flex items-center justify-between gap-3 text-sm">
                  <span className="flex items-center gap-2.5 font-medium text-foreground">
                    <span
                      className={cn('size-2.5 shrink-0 rounded-[3px]', visual.corPonto)}
                      aria-hidden="true"
                    />
                    <span>{STATUS_LABEL[s]}</span>
                  </span>
                  <div className="flex items-baseline gap-2 font-mono tabular-nums">
                    <span className="text-sm font-bold text-foreground">
                      {qtd}
                    </span>
                    <span className="w-12 text-right text-xs text-muted-foreground">
                      {formatPercent(pct)}
                    </span>
                  </div>
                </div>
                <div className="h-1.5 w-full overflow-hidden rounded-full bg-secondary">
                  <div
                    className={cn('h-full rounded-full transition-all', visual.corBarra)}
                    style={{ width: `${(qtd / maximo) * 100}%` }}
                  />
                </div>
              </li>
            )
          })}
        </ul>
      </CardContent>
    </Card>
  )
}

function DesempenhoMecanicos({ dados }: { dados: DashboardAdminResponse }) {
  const mecanicos = dados.mecanicos
  return (
    <Card>
      <CardHeader>
        <CardTitle>Produtividade e faturamento por mecânico</CardTitle>
        <CardDescription>
          Ranking por faturamento concluído no período e carga atual de ordens em andamento.
        </CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-6">
        {mecanicos.length === 0 ? (
          <p className="py-8 text-center text-sm text-muted-foreground">
            Nenhuma OS finalizada ou em andamento para exibir neste período.
          </p>
        ) : (
          <>
            {mecanicos.some((m) => m.faturamento > 0) && (
              <div className="border-b border-border/60 pb-6">
                <RankingMecanicosChart dados={mecanicos} />
              </div>
            )}
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
                      <TableCell className="font-mono text-muted-foreground tabular-nums">
                        {i + 1}
                      </TableCell>
                      <TableCell className="font-semibold text-foreground">
                        {m.nome}
                      </TableCell>
                      <TableCell className="text-right font-mono font-semibold tabular-nums">
                        {formatCurrency(m.faturamento)}
                      </TableCell>
                      <TableCell className="text-right tabular-nums">
                        {formatPercent(m.participacaoPercentual)}
                      </TableCell>
                      <TableCell className="text-right tabular-nums">
                        {m.osConcluidas}
                      </TableCell>
                      <TableCell className="text-right tabular-nums">
                        {m.osEmAndamento}
                      </TableCell>
                      <TableCell className="text-right tabular-nums">
                        {formatDias(m.tempoMedioDias)}
                      </TableCell>
                      <TableCell className="text-right font-mono tabular-nums">
                        {m.osConcluidas > 0
                          ? formatCurrency(m.ticketMedio)
                          : '—'}
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
    <div
      className="flex flex-col gap-6"
      aria-busy
      aria-label="Carregando métricas"
    >
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <Skeleton key={i} className="h-[112px] rounded-xl" />
        ))}
      </div>
      <Skeleton className="h-[380px] rounded-xl" />
    </div>
  )
}
