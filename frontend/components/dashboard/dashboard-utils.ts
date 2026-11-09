import type { GranularidadeDashboard, StatusServico } from '@/lib/types'

export type PresetPeriodo = '7d' | 'mes' | 'ano' | 'personalizado'

export const PRESETS: { value: PresetPeriodo; label: string }[] = [
  { value: '7d', label: 'Últimos 7 dias' },
  { value: 'mes', label: 'Mês atual' },
  { value: 'ano', label: 'Ano atual' },
  { value: 'personalizado', label: 'Personalizado' },
]

export const STATUS_LABEL: Record<StatusServico, string> = {
  PENDENTE: 'Pendente',
  ACEITO: 'Aceito',
  EM_ANDAMENTO: 'Em andamento',
  FINALIZADO: 'Finalizado',
  REJEITADO: 'Rejeitado',
}

export const STATUS_ORDEM: StatusServico[] = [
  'PENDENTE',
  'ACEITO',
  'EM_ANDAMENTO',
  'FINALIZADO',
  'REJEITADO',
]

const MESES_CURTOS = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez']

/** Data local no formato ISO (yyyy-MM-dd), sem passar por UTC. */
export function toIsoLocal(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

export function intervaloDoPreset(preset: Exclude<PresetPeriodo, 'personalizado'>, hoje = new Date()) {
  const fim = toIsoLocal(hoje)
  if (preset === '7d') {
    const inicio = new Date(hoje)
    inicio.setDate(inicio.getDate() - 6)
    return { inicio: toIsoLocal(inicio), fim }
  }
  if (preset === 'ano') {
    return { inicio: `${hoje.getFullYear()}-01-01`, fim }
  }
  return { inicio: toIsoLocal(new Date(hoje.getFullYear(), hoje.getMonth(), 1)), fim }
}

/** "2026-11-04" -> "04/11/2026" (sem conversão de fuso). */
export function formatIsoDate(iso: string): string {
  const [y, m, d] = iso.split('-')
  return d && m && y ? `${d}/${m}/${y}` : iso
}

/** Rótulo curto do eixo X para a chave da série ("yyyy-MM-dd" ou "yyyy-MM"). */
export function rotuloCurto(chave: string, granularidade: GranularidadeDashboard): string {
  const [y, m, d] = chave.split('-')
  if (granularidade === 'DIA' && d) return `${d}/${m}`
  return `${MESES_CURTOS[Number(m) - 1] ?? m}/${y?.slice(2)}`
}

/** Rótulo completo para tooltips. */
export function rotuloLongo(chave: string, granularidade: GranularidadeDashboard): string {
  const [y, m] = chave.split('-')
  if (granularidade === 'DIA') return formatIsoDate(chave)
  return `${MESES_CURTOS[Number(m) - 1] ?? m}/${y}`
}

export function formatCompactCurrency(value: number): string {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
    notation: 'compact',
    maximumFractionDigits: 1,
  }).format(value)
}

export function formatPercent(value: number, casas = 1): string {
  return `${value.toLocaleString('pt-BR', { minimumFractionDigits: casas, maximumFractionDigits: casas })}%`
}

export function formatDias(value: number | null): string {
  if (value === null || value === undefined) return '—'
  const texto = value.toLocaleString('pt-BR', { maximumFractionDigits: 1 })
  return `${texto} ${value === 1 ? 'dia' : 'dias'}`
}
