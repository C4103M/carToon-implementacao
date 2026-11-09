import type { GranularidadeDashboard, StatusServico } from '@/lib/types'
import { formatIsoDate } from '@/lib/format'

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

export function rotuloCurto(chave: string, granularidade: GranularidadeDashboard): string {
  const [y, m, d] = chave.split('-')
  if (granularidade === 'DIA' && d) return `${d}/${m}`
  return `${MESES_CURTOS[Number(m) - 1] ?? m}/${y?.slice(2)}`
}

export function rotuloLongo(chave: string, granularidade: GranularidadeDashboard): string {
  const [y, m] = chave.split('-')
  if (granularidade === 'DIA') return formatIsoDate(chave)
  return `${MESES_CURTOS[Number(m) - 1] ?? m}/${y}`
}
