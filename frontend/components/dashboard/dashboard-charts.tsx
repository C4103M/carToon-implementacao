'use client'

import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { formatCurrency } from '@/lib/format'
import type {
  DashboardDesempenhoMecanico,
  DashboardPontoQuantidade,
  DashboardPontoValor,
  GranularidadeDashboard,
} from '@/lib/types'
import {
  formatCompactCurrency,
  formatPercent,
  rotuloCurto,
  rotuloLongo,
} from './dashboard-utils'

const EIXO = {
  stroke: 'var(--muted-foreground)',
  fontSize: 12,
  tickLine: false,
  axisLine: false,
} as const

const GRID = {
  stroke: 'var(--border)',
  strokeDasharray: '0',
  vertical: false,
} as const

const CURSOR_BARRA = { fill: 'var(--muted)', opacity: 0.6 }

interface TooltipLinha {
  label: string
  valor: string
  detalhe?: string
}

function TooltipBox({ titulo, linhas }: { titulo: string; linhas: TooltipLinha[] }) {
  return (
    <div className="min-w-40 rounded-lg border bg-popover px-3 py-2 text-xs text-popover-foreground shadow-md">
      <div className="mb-1 font-medium">{titulo}</div>
      {linhas.map((l) => (
        <div key={l.label} className="flex items-baseline justify-between gap-4">
          <span className="text-muted-foreground">{l.label}</span>
          <span className="font-mono font-medium tabular-nums">{l.valor}</span>
        </div>
      ))}
      {linhas.map((l) =>
        l.detalhe ? (
          <div key={`${l.label}-d`} className="mt-1 text-muted-foreground">
            {l.detalhe}
          </div>
        ) : null,
      )}
    </div>
  )
}

export function EvolucaoFaturamentoChart({
  dados,
  granularidade,
}: {
  dados: DashboardPontoValor[]
  granularidade: GranularidadeDashboard
}) {
  return (
    <div className="h-64 w-full" role="img" aria-label="Gráfico de evolução do faturamento no período">
      <ResponsiveContainer width="100%" height="100%">
        <AreaChart data={dados} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
          <defs>
            <linearGradient id="grad-faturamento" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="var(--primary)" stopOpacity={0.25} />
              <stop offset="100%" stopColor="var(--primary)" stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid {...GRID} />
          <XAxis
            dataKey="chave"
            {...EIXO}
            tickFormatter={(c: string) => rotuloCurto(c, granularidade)}
            minTickGap={16}
          />
          <YAxis {...EIXO} width={76} tickFormatter={(v: number) => formatCompactCurrency(v)} />
          <Tooltip
            cursor={{ stroke: 'var(--muted-foreground)', strokeWidth: 1, strokeDasharray: '4 4' }}
            content={({ active, payload, label }) =>
              active && payload?.length ? (
                <TooltipBox
                  titulo={rotuloLongo(String(label), granularidade)}
                  linhas={[{ label: 'Faturamento', valor: formatCurrency(Number(payload[0].value)) }]}
                />
              ) : null
            }
          />
          <Area
            type="linear"
            dataKey="valor"
            stroke="var(--primary)"
            strokeWidth={2}
            fill="url(#grad-faturamento)"
            activeDot={{ r: 4, strokeWidth: 2, stroke: 'var(--card)' }}
            isAnimationActive={false}
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  )
}

export function FaturamentoMensalChart({ dados }: { dados: DashboardPontoValor[] }) {
  const comVariacao = dados.map((p, i) => {
    const anterior = i > 0 ? dados[i - 1].valor : null
    const variacao = anterior ? ((p.valor - anterior) / anterior) * 100 : null
    return { ...p, variacao }
  })

  return (
    <div className="h-64 w-full" role="img" aria-label="Gráfico de faturamento mensal dos últimos 12 meses">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={comVariacao} margin={{ top: 8, right: 8, left: 0, bottom: 0 }} barCategoryGap="20%">
          <CartesianGrid {...GRID} />
          <XAxis dataKey="chave" {...EIXO} tickFormatter={(c: string) => rotuloCurto(c, 'MES')} minTickGap={8} />
          <YAxis {...EIXO} width={76} tickFormatter={(v: number) => formatCompactCurrency(v)} />
          <Tooltip
            cursor={CURSOR_BARRA}
            content={({ active, payload }) => {
              if (!active || !payload?.length) return null
              const p = payload[0].payload as (typeof comVariacao)[number]
              return (
                <TooltipBox
                  titulo={rotuloLongo(p.chave, 'MES')}
                  linhas={[
                    {
                      label: 'Faturamento',
                      valor: formatCurrency(p.valor),
                      detalhe:
                        p.variacao === null
                          ? 'Sem base de comparação com o mês anterior'
                          : `${p.variacao >= 0 ? '+' : ''}${formatPercent(p.variacao)} vs. mês anterior`,
                    },
                  ]}
                />
              )
            }}
          />
          <Bar
            dataKey="valor"
            fill="var(--primary)"
            radius={[4, 4, 0, 0]}
            maxBarSize={40}
            isAnimationActive={false}
          />
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}

export function VolumeOrdensChart({
  dados,
  granularidade,
}: {
  dados: DashboardPontoQuantidade[]
  granularidade: GranularidadeDashboard
}) {
  return (
    <div className="h-56 w-full" role="img" aria-label="Gráfico de volume de ordens de serviço no período">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={dados} margin={{ top: 8, right: 8, left: 0, bottom: 0 }} barCategoryGap="20%">
          <CartesianGrid {...GRID} />
          <XAxis
            dataKey="chave"
            {...EIXO}
            tickFormatter={(c: string) => rotuloCurto(c, granularidade)}
            minTickGap={16}
          />
          <YAxis {...EIXO} width={32} allowDecimals={false} />
          <Tooltip
            cursor={CURSOR_BARRA}
            content={({ active, payload, label }) =>
              active && payload?.length ? (
                <TooltipBox
                  titulo={rotuloLongo(String(label), granularidade)}
                  linhas={[{ label: 'Ordens abertas', valor: String(payload[0].value) }]}
                />
              ) : null
            }
          />
          <Bar
            dataKey="quantidade"
            fill="var(--muted-foreground)"
            radius={[4, 4, 0, 0]}
            maxBarSize={32}
            isAnimationActive={false}
          />
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}

export function RankingMecanicosChart({ dados }: { dados: DashboardDesempenhoMecanico[] }) {
  const top = dados.slice(0, 8)
  const altura = Math.max(120, top.length * 40 + 16)
  return (
    <div style={{ height: altura }} className="w-full" role="img" aria-label="Gráfico de faturamento por mecânico">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={top} layout="vertical" margin={{ top: 0, right: 16, left: 0, bottom: 0 }} barCategoryGap={8}>
          <CartesianGrid stroke="var(--border)" horizontal={false} />
          <XAxis type="number" {...EIXO} tickFormatter={(v: number) => formatCompactCurrency(v)} />
          <YAxis
            type="category"
            dataKey="nome"
            {...EIXO}
            width={140}
            tickFormatter={(n: string) => (n.length > 18 ? `${n.slice(0, 17)}…` : n)}
          />
          <Tooltip
            cursor={CURSOR_BARRA}
            content={({ active, payload }) => {
              if (!active || !payload?.length) return null
              const m = payload[0].payload as DashboardDesempenhoMecanico
              return (
                <TooltipBox
                  titulo={m.nome}
                  linhas={[
                    { label: 'Faturamento', valor: formatCurrency(m.faturamento) },
                    { label: 'Participação', valor: formatPercent(m.participacaoPercentual) },
                    { label: 'OS concluídas', valor: String(m.osConcluidas) },
                  ]}
                />
              )
            }}
          />
          <Bar
            dataKey="faturamento"
            fill="var(--primary)"
            radius={[0, 4, 4, 0]}
            maxBarSize={24}
            isAnimationActive={false}
          />
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}
