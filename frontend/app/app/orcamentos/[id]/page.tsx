'use client'

import { use, useState, useMemo, useEffect, useCallback } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { toast } from 'sonner'
import {
  User,
  Car,
  Calendar,
  CheckCircle2,
  XCircle,
  Play,
  Flag,
  Trash2,
  Plus,
  Share2,
  Info,
  Wrench,
  Package,
  Clock,
  Percent,
  Receipt,
  Sparkles,
} from 'lucide-react'
import { ordemServicoService, catalogoService } from '@/lib/services'
import { handleError, normalizeError } from '@/lib/error-handler'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { AlertCircle } from 'lucide-react'
import type {
  OrdemServicoResponse,
  StatusServico,
  Desconto,
  ServicoResponse,
  PecaResponse,
} from '@/lib/types'
import { formatCurrency, formatDate, formatCpf } from '@/lib/format'
import { PageHeader } from '@/components/page-header'
import { OrcamentoStatusBadge } from '@/components/status-badge'
import { Button } from '@/components/ui/button'
import { Spinner } from '@/components/ui/spinner'
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Label } from '@/components/ui/label'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'
import { Badge } from '@/components/ui/badge'

const DESCONTO_OPCOES: { value: Desconto; label: string; pct: number }[] = [
  { value: 'SEM_DESCONTO', label: 'Sem Desconto (0%)', pct: 0 },
  { value: 'CINCO_PORCENTO', label: '5% de Desconto', pct: 5 },
  { value: 'DEZ_PORCENTO', label: '10% de Desconto', pct: 10 },
  { value: 'QUINZE_PORCENTO', label: '15% de Desconto', pct: 15 },
]

function normalizarTempoParaEnvio(input: string): string | null {
  const str = input.trim()
  if (!str) return null

  const colonMatch = str.match(/^(\d{1,2}):(\d{1,2})(?::\d{1,2})?$/)
  if (colonMatch) {
    const h = parseInt(colonMatch[1], 10)
    const m = parseInt(colonMatch[2], 10)
    if (h === 0 && m === 0) return null
    if (m > 59) return null
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
  }

  const horaMinMatch = str.match(/^(\d+)\s*[hH]\s*(\d+)?\s*(?:[mM])?$/)
  if (horaMinMatch) {
    const h = parseInt(horaMinMatch[1], 10)
    const m = horaMinMatch[2] ? parseInt(horaMinMatch[2], 10) : 0
    if (h === 0 && m === 0) return null
    if (m > 59) return null
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
  }

  const minMatch = str.match(/^(\d+)\s*(?:m|min|minutos)$/i)
  if (minMatch) {
    const totalMin = parseInt(minMatch[1], 10)
    if (totalMin <= 0) return null
    const h = Math.floor(totalMin / 60)
    const m = totalMin % 60
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
  }

  const num = parseFloat(str.replace(',', '.'))
  if (!isNaN(num) && num > 0) {
    const h = Math.floor(num)
    const m = Math.round((num - h) * 60)
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
  }

  return null
}

function formatarTempoExibicao(tempo?: string, quantidadeHoras?: number): string {
  if (tempo) {
    const [hStr, mStr] = tempo.split(':')
    const h = parseInt(hStr, 10)
    const m = parseInt(mStr, 10)
    if (!isNaN(h) && !isNaN(m)) {
      if (h > 0 && m > 0) return `${h}h${String(m).padStart(2, '0')}`
      if (h > 0) return `${h}h`
      return `${m}min`
    }
  }
  if (quantidadeHoras != null) {
    const h = Math.floor(quantidadeHoras)
    const m = Math.round((quantidadeHoras - h) * 60)
    if (h > 0 && m > 0) return `${h}h${String(m).padStart(2, '0')}`
    if (h > 0) return `${h}h`
    return `${m}min`
  }
  return '-'
}

export default function OrcamentoDetalhePage({
  params,
}: {
  params: Promise<{ id: string }>
}) {
  const { id } = use(params)
  const router = useRouter()
  const osId = Number(id)

  const [os, setOs] = useState<OrdemServicoResponse | null>(null)
  const [carregando, setCarregando] = useState(true)

  // --- Catálogo de Serviços e Peças ---
  const [servicosCatalogo, setServicosCatalogo] = useState<ServicoResponse[]>([])
  const [pecasCatalogo, setPecasCatalogo] = useState<PecaResponse[]>([])

  // --- Adicionar Serviço na OS ---
  const [abaAdicionar, setAbaAdicionar] = useState<'servico' | 'peca'>('servico')
  const [selecionadoServicoId, setSelecionadoServicoId] = useState('')
  const [qtdServico, setQtdServico] = useState('1')
  const [adicionandoServico, setAdicionandoServico] = useState(false)

  // --- Adicionar Peça na OS ---
  const [selecionadoPecaId, setSelecionadoPecaId] = useState('')
  const [qtdPeca, setQtdPeca] = useState('1')
  const [adicionandoPeca, setAdicionandoPeca] = useState(false)

  // --- Modal de Cadastro Rápido de Novo Serviço (caso não esteja na lista) ---
  const [novoServicoModalAberto, setNovoServicoModalAberto] = useState(false)
  const [novoServicoNome, setNovoServicoNome] = useState('')
  const [novoServicoTempo, setNovoServicoTempo] = useState('')
  const [novoServicoDescricao, setNovoServicoDescricao] = useState('')
  const [salvandoNovoServico, setSalvandoNovoServico] = useState(false)

  // --- Modal de Cadastro Rápido de Nova Peça (caso não esteja na lista) ---
  const [novaPecaModalAberto, setNovaPecaModalAberto] = useState(false)
  const [novaPecaNome, setNovaPecaNome] = useState('')
  const [novaPecaFabricante, setNovaPecaFabricante] = useState('')
  const [novaPecaValor, setNovaPecaValor] = useState('')
  const [salvandoNovaPeca, setSalvandoNovaPeca] = useState(false)

  // --- Desconto e Status ---
  const [atualizandoDesconto, setAtualizandoDesconto] = useState(false)
  const [acaoStatusConfirmar, setAcaoStatusConfirmar] = useState<
    'aceitar' | 'iniciar' | 'finalizar' | 'rejeitar' | null
  >(null)
  const [executandoStatus, setExecutandoStatus] = useState(false)
  const [erroAcao, setErroAcao] = useState<string | null>(null)

  const carregarTudo = useCallback(async () => {
    if (Number.isNaN(osId)) {
      setCarregando(false)
      return
    }
    setCarregando(true)
    try {
      const [dadosOs, pageServicos, pagePecas] = await Promise.all([
        ordemServicoService.buscarPorId(osId),
        catalogoService.listarServicos({ size: 200 }),
        catalogoService.listarPecas({ size: 200 }),
      ])
      setOs(dadosOs)
      setServicosCatalogo(pageServicos.content || [])
      setPecasCatalogo(pagePecas.content || [])
    } catch (err) {
      console.error('Erro ao carregar Ordem de Serviço:', err)
      toast.error('Não foi possível carregar os detalhes do orçamento.')
    } finally {
      setCarregando(false)
    }
  }, [osId])

  useEffect(() => {
    carregarTudo()
  }, [carregarTudo])

  // --- Computados Financeiros e de Previsão ---
  const podeEditar = useMemo(() => {
    if (!os) return false
    return os.status !== 'FINALIZADO' && os.status !== 'REJEITADO'
  }, [os])

  const servicoSelecionado = useMemo(() => {
    return servicosCatalogo.find((s) => String(s.id) === selecionadoServicoId)
  }, [servicosCatalogo, selecionadoServicoId])

  const pecaSelecionada = useMemo(() => {
    return pecasCatalogo.find((p) => String(p.id) === selecionadoPecaId)
  }, [pecasCatalogo, selecionadoPecaId])

  // Simulação transparente do preço unitário do serviço conforme CalculadoraOrcamento
  const previaServico = useMemo(() => {
    if (!servicoSelecionado || !os) return null
    const horas = servicoSelecionado.quantidadeHoras ?? 0
    const valorHora = Number(os.oficinaValorHora ?? 100)
    const valorFipe = Number(os.veiculoValorFipe ?? 0)
    const taxaRisco = Number(os.oficinaTaxaRisco ?? 0)

    const maoDeObraBase = horas * valorHora
    const multiplicador = 1 + (valorFipe / 100000) * taxaRisco
    const valorUnitario = Math.round(maoDeObraBase * multiplicador * 100) / 100
    const qtd = Math.max(1, parseInt(qtdServico, 10) || 1)
    const subtotal = Math.round(valorUnitario * qtd * 100) / 100

    return {
      horas,
      valorHora,
      maoDeObraBase,
      multiplicador,
      valorUnitario,
      qtd,
      subtotal,
    }
  }, [servicoSelecionado, os, qtdServico])

  const previaPeca = useMemo(() => {
    if (!pecaSelecionada) return null
    const valorUnitario = Number(pecaSelecionada.valorBase ?? 0)
    const qtd = Math.max(1, parseInt(qtdPeca, 10) || 1)
    const subtotal = Math.round(valorUnitario * qtd * 100) / 100
    return { valorUnitario, qtd, subtotal }
  }, [pecaSelecionada, qtdPeca])

  const valoresResumo = useMemo(() => {
    if (!os) {
      return {
        totalServicos: 0,
        totalPecas: 0,
        valorBruto: 0,
        valorDesconto: 0,
        total: 0,
        desconto: 'SEM_DESCONTO' as Desconto,
      }
    }
    const totalServicos =
      os.totalServicos != null
        ? Number(os.totalServicos)
        : (os.itensServico || []).reduce((acc, i) => acc + Number(i.subtotal || 0), 0)

    const totalPecas =
      os.totalPecas != null
        ? Number(os.totalPecas)
        : (os.itensPeca || []).reduce((acc, i) => acc + Number(i.subtotal || 0), 0)

    const valorBruto =
      os.valorBruto != null ? Number(os.valorBruto) : totalServicos + totalPecas

    const valorDesconto = os.valorDesconto != null ? Number(os.valorDesconto) : 0
    const total = os.total != null ? Number(os.total) : valorBruto - valorDesconto
    const desconto: Desconto = os.desconto || 'SEM_DESCONTO'

    return {
      totalServicos,
      totalPecas,
      valorBruto,
      valorDesconto,
      total,
      desconto,
    }
  }, [os])

  // --- Handlers: Adicionar / Remover Serviço ---
  const handleAdicionarServico = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!os || !selecionadoServicoId) {
      toast.error('Selecione um serviço para adicionar.')
      return
    }
    const quantidade = parseInt(qtdServico, 10)
    if (Number.isNaN(quantidade) || quantidade <= 0) {
      toast.error('A quantidade deve ser maior que zero.')
      return
    }

    setAdicionandoServico(true)
    try {
      const atualizada = await ordemServicoService.adicionarItemServico(os.id, {
        servicoId: Number(selecionadoServicoId),
        quantidade,
      })
      setOs(atualizada)
      setSelecionadoServicoId('')
      setQtdServico('1')
      toast.success('Serviço adicionado ao orçamento!')
      setErroAcao(null)
    } catch (err) {
      const norm = handleError(err, { fallbackMessage: 'Erro ao adicionar serviço.' })
      setErroAcao(norm.message)
    } finally {
      setAdicionandoServico(false)
    }
  }

  const handleRemoverServico = async (itemId: number) => {
    if (!os) return
    try {
      const atualizada = await ordemServicoService.removerItemServico(os.id, itemId)
      setOs(atualizada)
      toast.success('Serviço removido do orçamento.')
      setErroAcao(null)
    } catch (err) {
      const norm = handleError(err, { fallbackMessage: 'Erro ao remover serviço.' })
      setErroAcao(norm.message)
    }
  }

  // --- Handlers: Adicionar / Remover Peça ---
  const handleAdicionarPeca = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!os || !selecionadoPecaId) {
      toast.error('Selecione uma peça para adicionar.')
      return
    }
    const quantidade = parseInt(qtdPeca, 10)
    if (Number.isNaN(quantidade) || quantidade <= 0) {
      toast.error('A quantidade deve ser maior que zero.')
      return
    }

    setAdicionandoPeca(true)
    try {
      const atualizada = await ordemServicoService.adicionarItemPeca(os.id, {
        pecaId: Number(selecionadoPecaId),
        quantidade,
      })
      setOs(atualizada)
      setSelecionadoPecaId('')
      setQtdPeca('1')
      toast.success('Peça adicionada ao orçamento!')
      setErroAcao(null)
    } catch (err) {
      const norm = handleError(err, { fallbackMessage: 'Erro ao adicionar peça.' })
      setErroAcao(norm.message)
    } finally {
      setAdicionandoPeca(false)
    }
  }

  const handleRemoverPeca = async (itemId: number) => {
    if (!os) return
    try {
      const atualizada = await ordemServicoService.removerItemPeca(os.id, itemId)
      setOs(atualizada)
      toast.success('Peça removida do orçamento.')
      setErroAcao(null)
    } catch (err) {
      const norm = handleError(err, { fallbackMessage: 'Erro ao remover peça.' })
      setErroAcao(norm.message)
    }
  }

  // --- Handlers: Cadastrar Novo Serviço no Catálogo e Selecionar ---
  const handleCriarNovoServicoCatalogo = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!novoServicoNome.trim()) {
      toast.error('Informe o nome do serviço.')
      return
    }
    const tempoFormatado = normalizarTempoParaEnvio(novoServicoTempo)
    if (!tempoFormatado) {
      toast.error('Informe um tempo válido (ex: 1h30, 1:30 ou 45min).')
      return
    }

    setSalvandoNovoServico(true)
    try {
      const criado = await catalogoService.criarServico({
        nome: novoServicoNome.trim(),
        tempo: tempoFormatado,
        descricao: novoServicoDescricao.trim() || undefined,
      })
      setServicosCatalogo((prev) => [criado, ...prev])
      setSelecionadoServicoId(String(criado.id))
      setAbaAdicionar('servico')
      setNovoServicoModalAberto(false)
      setNovoServicoNome('')
      setNovoServicoTempo('')
      setNovoServicoDescricao('')
      toast.success('Serviço cadastrado no catálogo e selecionado!')
    } catch (err) {
      toast.error(normalizeError(err).message || 'Erro ao cadastrar serviço.')
    } finally {
      setSalvandoNovoServico(false)
    }
  }

  // --- Handlers: Cadastrar Nova Peça no Catálogo e Selecionar ---
  const handleCriarNovaPecaCatalogo = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!novaPecaNome.trim() || !novaPecaFabricante.trim()) {
      toast.error('Preencha o nome e o fabricante da peça.')
      return
    }
    const valorBase = Number.parseFloat(novaPecaValor.replace(',', '.'))
    if (Number.isNaN(valorBase) || valorBase <= 0) {
      toast.error('Informe um valor base válido maior que zero.')
      return
    }

    setSalvandoNovaPeca(true)
    try {
      const criada = await catalogoService.criarPeca({
        nome: novaPecaNome.trim(),
        fabricante: novaPecaFabricante.trim(),
        valorBase,
      })
      setPecasCatalogo((prev) => [criada, ...prev])
      setSelecionadoPecaId(String(criada.id))
      setAbaAdicionar('peca')
      setNovaPecaModalAberto(false)
      setNovaPecaNome('')
      setNovaPecaFabricante('')
      setNovaPecaValor('')
      toast.success('Peça cadastrada no catálogo e selecionada!')
    } catch (err) {
      toast.error(normalizeError(err).message || 'Erro ao cadastrar peça.')
    } finally {
      setSalvandoNovaPeca(false)
    }
  }

  // --- Handler: Aplicar Desconto (Desconto.java) ---
  const handleAlterarDesconto = async (novoDesconto: string | null) => {
    if (!os || !novoDesconto) return
    setAtualizandoDesconto(true)
    try {
      const atualizada = await ordemServicoService.aplicarDesconto(
        os.id,
        novoDesconto as Desconto,
      )
      setOs(atualizada)
      toast.success('Desconto atualizado no orçamento!')
      setErroAcao(null)
    } catch (err) {
      const norm = handleError(err, { fallbackMessage: 'Erro ao aplicar desconto.' })
      setErroAcao(norm.message)
    } finally {
      setAtualizandoDesconto(false)
    }
  }

  // --- Handler: Transição de Status (StatusServico.java) ---
  const executarMudancaStatus = async () => {
    if (!os || !acaoStatusConfirmar) return
    setExecutandoStatus(true)
    try {
      let atualizada: OrdemServicoResponse
      if (acaoStatusConfirmar === 'aceitar') {
        atualizada = await ordemServicoService.aceitar(os.id)
        toast.success('Orçamento marcado como ACEITO!')
      } else if (acaoStatusConfirmar === 'iniciar') {
        atualizada = await ordemServicoService.iniciar(os.id)
        toast.success('Execução iniciada! Status atualizado para EM ANDAMENTO.')
      } else if (acaoStatusConfirmar === 'finalizar') {
        atualizada = await ordemServicoService.finalizar(os.id)
        toast.success('Serviço FINALIZADO com sucesso!')
      } else {
        atualizada = await ordemServicoService.rejeitar(os.id)
        toast.success('Orçamento marcado como REJEITADO.')
      }
      setOs(atualizada)
      setAcaoStatusConfirmar(null)
      setErroAcao(null)
    } catch (err) {
      const norm = handleError(err, { fallbackMessage: 'Erro ao atualizar status da OS.' })
      setErroAcao(norm.message)
    } finally {
      setExecutandoStatus(false)
    }
  }

  // --- Copiar Resumo Transparente para WhatsApp ---
  const copiarResumoWhatsApp = () => {
    if (!os) return

    let servicosTexto = ''
    if (os.itensServico && os.itensServico.length > 0) {
      os.itensServico.forEach((s, i) => {
        const tempoStr = formatarTempoExibicao(s.tempo, s.quantidadeHoras)
        servicosTexto += `  ${i + 1}. ${s.servicoNome} (${tempoStr}) — ${s.quantidade}x ${formatCurrency(Number(s.valorUnitario))} = *${formatCurrency(Number(s.subtotal))}*\n`
      })
    } else {
      servicosTexto = '  Nenhum serviço lançado.\n'
    }

    let pecasTexto = ''
    if (os.itensPeca && os.itensPeca.length > 0) {
      os.itensPeca.forEach((p, i) => {
        const nome = p.pecaNome || p.nomePeca || 'Peça'
        const fab = p.fabricante ? ` (${p.fabricante})` : ''
        pecasTexto += `  ${i + 1}. ${nome}${fab} — ${p.quantidade}x ${formatCurrency(Number(p.valorUnitario))} = *${formatCurrency(Number(p.subtotal))}*\n`
      })
    } else {
      pecasTexto = '  Nenhuma peça lançada.\n'
    }

    const descPct =
      DESCONTO_OPCOES.find((d) => d.value === valoresResumo.desconto)?.pct ?? 0

    const texto = `🛠️ *ORÇAMENTO #${os.id} - ${os.oficinaNome || 'OFICINA'}* 🛠️
-----------------------------------
*Status:* ${os.status}
*Data:* ${os.dataOrcamento ? formatDate(os.dataOrcamento) : '—'}
*Cliente:* ${os.clienteNome || '—'}
*Veículo:* ${os.veiculoMarca || ''} ${os.veiculoModelo || ''} · Placa: ${os.placa}
*Descrição:* ${os.descricao || '—'}
-----------------------------------
🔧 *MÃO DE OBRA (SERVIÇOS):*
${servicosTexto}*Subtotal Mão de Obra:* ${formatCurrency(valoresResumo.totalServicos)}

📦 *PEÇAS E COMPONENTES:*
${pecasTexto}*Subtotal Peças:* ${formatCurrency(valoresResumo.totalPecas)}
-----------------------------------
*Valor Bruto:* ${formatCurrency(valoresResumo.valorBruto)}
*Desconto (${descPct}%):* -${formatCurrency(valoresResumo.valorDesconto)}
*VALOR TOTAL FINAL:* *${formatCurrency(valoresResumo.total)}*
-----------------------------------`

    navigator.clipboard.writeText(texto)
    toast.success('Resumo detalhado copiado para a área de transferência!')
  }

  if (carregando) {
    return (
      <div className="flex flex-col items-center justify-center py-24 gap-3 text-muted-foreground">
        <Spinner className="size-8" />
        <span className="text-sm">Carregando detalhes do orçamento...</span>
      </div>
    )
  }

  if (!os) {
    return (
      <div className="py-16 text-center flex flex-col items-center gap-4">
        <p className="text-muted-foreground">Orçamento não encontrado.</p>
        <Button variant="outline" onClick={() => router.push('/app/orcamentos')}>
          Voltar para Orçamentos
        </Button>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title={`Orçamento / OS #${os.id}`}
        description={os.descricao || 'Detalhamento transparente de serviços, peças e valores.'}
        backHref="/app/orcamentos"
        backLabel="Todos os orçamentos"
        actions={
          <div className="flex items-center gap-2">
            <OrcamentoStatusBadge status={os.status} />
            <Button variant="outline" size="sm" onClick={copiarResumoWhatsApp}>
              <Share2 data-icon="inline-start" />
              Copiar p/ WhatsApp
            </Button>
          </div>
        }
      />

      {/* Barra de Progresso do Fluxo de Status (StatusServico.java) */}
      <Card>
        <CardContent className="py-4">
          <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div className="flex flex-wrap items-center gap-2 text-xs sm:text-sm">
              {(
                [
                  { key: 'PENDENTE', label: '1. Pendente', date: os.dataOrcamento },
                  { key: 'ACEITO', label: '2. Aceito', date: null },
                  { key: 'EM_ANDAMENTO', label: '3. Em Andamento', date: os.dataInicio },
                  { key: 'FINALIZADO', label: '4. Finalizado', date: os.dataFinalizacao },
                ] as const
              ).map((step, idx) => {
                const ordemStatus: StatusServico[] = [
                  'PENDENTE',
                  'ACEITO',
                  'EM_ANDAMENTO',
                  'FINALIZADO',
                ]
                const idxAtual = ordemStatus.indexOf(os.status)
                const isAtivo = os.status === step.key
                const isConcluido = idxAtual > idx && os.status !== 'REJEITADO'

                return (
                  <div key={step.key} className="flex items-center gap-2">
                    <div
                      className={`flex flex-col rounded-lg border px-3 py-1.5 transition-colors ${
                        isAtivo
                          ? 'border-primary bg-primary/15 text-primary font-semibold'
                          : isConcluido
                          ? 'border-success/40 bg-success/10 text-success'
                          : 'border-border bg-muted/40 text-muted-foreground'
                      }`}
                    >
                      <span>{step.label}</span>
                      {step.date && (
                        <span className="text-[10px] font-mono opacity-80">
                          {formatDate(step.date)}
                        </span>
                      )}
                    </div>
                    {idx < 3 && <span className="text-muted-foreground">→</span>}
                  </div>
                )
              })}

              {os.status === 'REJEITADO' && (
                <div className="flex flex-col rounded-lg border border-destructive/40 bg-destructive/15 px-3 py-1.5 text-destructive font-semibold">
                  <span>Orçamento Rejeitado</span>
                  {os.dataRejeicao && (
                    <span className="text-[10px] font-mono">
                      {formatDate(os.dataRejeicao)}
                    </span>
                  )}
                </div>
              )}
            </div>

            {/* Botões de Ação de acordo com StatusServico.java */}
            <div className="flex flex-wrap items-center gap-2">
              {os.status === 'PENDENTE' && (
                <>
                  <Button
                    size="sm"
                    onClick={() => setAcaoStatusConfirmar('aceitar')}
                    className="bg-success text-white hover:bg-success/90"
                  >
                    <CheckCircle2 data-icon="inline-start" />
                    Aceitar Orçamento
                  </Button>
                  <Button
                    size="sm"
                    variant="destructive"
                    onClick={() => setAcaoStatusConfirmar('rejeitar')}
                  >
                    <XCircle data-icon="inline-start" />
                    Rejeitar
                  </Button>
                </>
              )}

              {os.status === 'ACEITO' && (
                <>
                  <Button
                    size="sm"
                    onClick={() => setAcaoStatusConfirmar('iniciar')}
                  >
                    <Play data-icon="inline-start" />
                    Iniciar Execução (Em Andamento)
                  </Button>
                  <Button
                    size="sm"
                    variant="destructive"
                    onClick={() => setAcaoStatusConfirmar('rejeitar')}
                  >
                    <XCircle data-icon="inline-start" />
                    Rejeitar
                  </Button>
                </>
              )}

              {os.status === 'EM_ANDAMENTO' && (
                <Button
                  size="sm"
                  onClick={() => setAcaoStatusConfirmar('finalizar')}
                  className="bg-info text-white hover:bg-info/90"
                >
                  <Flag data-icon="inline-start" />
                  Finalizar Serviço
                </Button>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Coluna Esquerda: Dados do Cliente/Veículo + Resumo Financeiro Transparente */}
        <div className="flex flex-col gap-6 lg:col-span-1">
          {/* Card: Resumo Financeiro Transparente e Desconto */}
          <Card className="border-primary/30">
            <CardHeader className="pb-3">
              <CardTitle className="text-base font-semibold flex items-center gap-2">
                <Receipt className="size-4 text-primary" />
                Transparência de Valores
              </CardTitle>
              <CardDescription>
                Separação clara entre Mão de Obra, Peças e Desconto aplicado.
              </CardDescription>
            </CardHeader>
            <CardContent className="flex flex-col gap-4">
              <div className="flex flex-col gap-2.5 rounded-lg border border-border bg-muted/30 p-3.5 text-sm">
                <div className="flex items-center justify-between">
                  <span className="flex items-center gap-1.5 text-muted-foreground">
                    <Wrench className="size-3.5 text-primary" />
                    Mão de Obra ({os.itensServico?.length || 0}{' '}
                    {(os.itensServico?.length || 0) === 1 ? 'serviço' : 'serviços'})
                  </span>
                  <span className="font-mono font-semibold">
                    {formatCurrency(valoresResumo.totalServicos)}
                  </span>
                </div>

                <div className="flex items-center justify-between">
                  <span className="flex items-center gap-1.5 text-muted-foreground">
                    <Package className="size-3.5 text-info" />
                    Peças ({os.itensPeca?.length || 0}{' '}
                    {(os.itensPeca?.length || 0) === 1 ? 'item' : 'itens'})
                  </span>
                  <span className="font-mono font-semibold">
                    {formatCurrency(valoresResumo.totalPecas)}
                  </span>
                </div>

                <Separator />

                <div className="flex items-center justify-between font-medium">
                  <span>Valor Bruto (Peças + Mão de Obra)</span>
                  <span className="font-mono">
                    {formatCurrency(valoresResumo.valorBruto)}
                  </span>
                </div>

                {valoresResumo.valorDesconto > 0 && (
                  <div className="flex items-center justify-between text-success font-medium">
                    <span className="flex items-center gap-1">
                      <Percent className="size-3.5" />
                      Desconto Aplicado
                    </span>
                    <span className="font-mono">
                      - {formatCurrency(valoresResumo.valorDesconto)}
                    </span>
                  </div>
                )}

                <Separator />

                <div className="flex items-center justify-between pt-1">
                  <span className="text-base font-bold">Total Final</span>
                  <span className="text-xl font-bold font-mono text-success">
                    {formatCurrency(valoresResumo.total)}
                  </span>
                </div>
              </div>

              {/* Seletor de Desconto (Desconto.java) */}
              <div className="flex flex-col gap-2">
                <Label htmlFor="select-desconto" className="text-xs font-medium uppercase tracking-wide text-muted-foreground flex items-center gap-1.5">
                  <Percent className="size-3.5 text-primary" />
                  Conceder Desconto no Orçamento
                </Label>
                <Select
                  value={valoresResumo.desconto}
                  onValueChange={handleAlterarDesconto}
                  disabled={!podeEditar || atualizandoDesconto}
                >
                  <SelectTrigger id="select-desconto" className="w-full">
                    <SelectValue placeholder="Selecione o desconto...">
                      {DESCONTO_OPCOES.find((op) => op.value === valoresResumo.desconto)?.label ||
                        'Selecione o desconto...'}
                    </SelectValue>
                  </SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      {DESCONTO_OPCOES.map((op) => (
                        <SelectItem key={op.value} value={op.value}>
                          {op.label}
                        </SelectItem>
                      ))}
                    </SelectGroup>
                  </SelectContent>
                </Select>
                <span className="text-[11px] text-muted-foreground">
                  O desconto incide sobre o valor bruto total (limite máximo de 15%).
                </span>
              </div>
            </CardContent>
          </Card>

          {/* Card: Dados do Cliente, Veículo e Parâmetros da Oficina */}
          <Card>
            <CardHeader>
              <CardTitle className="text-base font-semibold">
                Dados do Veículo e Parâmetros
              </CardTitle>
            </CardHeader>
            <CardContent className="flex flex-col gap-4 text-sm">
              <div>
                <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                  Cliente
                </span>
                <div className="flex items-center gap-2 mt-1">
                  <User className="size-4 text-muted-foreground" />
                  {os.clienteId ? (
                    <Link
                      href={`/app/clientes/${os.clienteId}`}
                      className="font-medium text-primary hover:underline"
                    >
                      {os.clienteNome || `Cliente #${os.clienteId}`}
                    </Link>
                  ) : (
                    <span className="font-medium">{os.clienteNome || '—'}</span>
                  )}
                </div>
                {os.clienteCpf && (
                  <span className="text-xs text-muted-foreground font-mono">
                    CPF: {formatCpf(os.clienteCpf)}
                  </span>
                )}
              </div>

              <Separator />

              <div>
                <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                  Veículo
                </span>
                <div className="flex items-center gap-2 mt-1">
                  <Car className="size-4 text-muted-foreground" />
                  <span className="font-medium">
                    {os.veiculoMarca || os.veiculoModelo
                      ? `${os.veiculoMarca || ''} ${os.veiculoModelo || ''} ${
                          os.veiculoAno ? `(${os.veiculoAno})` : ''
                        }`.trim()
                      : `Placa ${os.placa}`}
                  </span>
                </div>
                <div className="mt-2 flex flex-col gap-1 text-xs text-muted-foreground font-mono bg-muted/40 p-2.5 rounded-md border">
                  <div className="flex justify-between">
                    <span>Placa:</span>
                    <span className="font-semibold text-foreground">{os.placa}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Valor FIPE:</span>
                    <span className="font-semibold text-success">
                      {os.veiculoValorFipe != null
                        ? formatCurrency(Number(os.veiculoValorFipe))
                        : '—'}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span>Valor Hora da Oficina:</span>
                    <span className="font-semibold text-foreground">
                      {os.oficinaValorHora != null
                        ? `${formatCurrency(Number(os.oficinaValorHora))}/h`
                        : '—'}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span>Taxa de Risco da Oficina:</span>
                    <span className="font-semibold text-foreground">
                      {os.oficinaTaxaRisco != null ? Number(os.oficinaTaxaRisco) : '—'}
                    </span>
                  </div>
                </div>
                <div className="mt-2 flex items-start gap-1.5 rounded-md bg-muted/60 border border-border p-2 text-[11px] text-muted-foreground">
                  <Info className="size-3.5 text-primary shrink-0 mt-0.5" />
                  <span>
                    Fórmula da mão de obra: <strong>Horas × Valor/Hora × [1 + (FIPE / 100.000) × Taxa Risco]</strong>.
                  </span>
                </div>
              </div>

              <Separator />

              <div className="grid grid-cols-2 gap-3 text-xs">
                <div>
                  <span className="text-muted-foreground uppercase">Responsável</span>
                  <p className="font-medium text-sm mt-0.5">{os.mecanicoNome || '—'}</p>
                </div>
                <div>
                  <span className="text-muted-foreground uppercase">Data Orçamento</span>
                  <p className="font-medium text-sm mt-0.5 flex items-center gap-1">
                    <Calendar className="size-3.5 text-muted-foreground" />
                    {os.dataOrcamento ? formatDate(os.dataOrcamento) : '—'}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Coluna Direita: Adicionar Serviço/Peça + Tabelas Separadas de Mão de Obra e Peças */}
        <div className="flex flex-col gap-6 lg:col-span-2">
          {/* Painel para Adicionar Serviços (Mão de Obra) e Peças */}
          {podeEditar && (
            <Card>
              <CardHeader className="pb-3">
                <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                  <div>
                    <CardTitle className="text-base font-semibold">
                      Adicionar Itens ao Orçamento
                    </CardTitle>
                    <CardDescription>
                      Inclua serviços (mão de obra) e peças utilizadas. Se o item não estiver na lista, cadastre-o na hora.
                    </CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <Tabs
                  value={abaAdicionar}
                  onValueChange={(v) => setAbaAdicionar(v as 'servico' | 'peca')}
                >
                  <TabsList className="mb-4">
                    <TabsTrigger value="servico">
                      <Wrench className="size-4" />
                      Adicionar Serviço (Mão de Obra)
                    </TabsTrigger>
                    <TabsTrigger value="peca">
                      <Package className="size-4" />
                      Adicionar Peça
                    </TabsTrigger>
                  </TabsList>

                  {/* ABA: ADICIONAR SERVIÇO */}
                  <TabsContent value="servico">
                    <form onSubmit={handleAdicionarServico} className="flex flex-col gap-4">
                      <div className="grid gap-4 sm:grid-cols-4 items-end">
                        <div className="flex flex-col gap-1.5 sm:col-span-3">
                          <div className="flex items-center justify-between">
                            <Label htmlFor="select-servico">Serviço do Catálogo</Label>
                            <button
                              type="button"
                              onClick={() => setNovoServicoModalAberto(true)}
                              className="text-xs font-medium text-primary hover:underline inline-flex items-center gap-1"
                            >
                              <Sparkles className="size-3" />
                              Não está na lista? Cadastrar novo serviço
                            </button>
                          </div>
                          <Select
                            value={selecionadoServicoId}
                            onValueChange={(v) => setSelecionadoServicoId(v ?? '')}
                            disabled={adicionandoServico}
                          >
                            <SelectTrigger id="select-servico" className="w-full">
                              <SelectValue placeholder="Selecione um serviço da oficina...">
                                {servicoSelecionado
                                  ? servicoSelecionado.nome
                                  : 'Selecione um serviço da oficina...'}
                              </SelectValue>
                            </SelectTrigger>
                            <SelectContent>
                              <SelectGroup>
                                {servicosCatalogo.map((s) => (
                                  <SelectItem key={s.id} value={String(s.id)}>
                                    {s.nome} ({formatarTempoExibicao(s.tempo, s.quantidadeHoras)})
                                  </SelectItem>
                                ))}
                              </SelectGroup>
                            </SelectContent>
                          </Select>
                        </div>

                        <div className="flex flex-col gap-1.5 sm:col-span-1">
                          <Label htmlFor="qtd-servico">Quantidade</Label>
                          <Input
                            id="qtd-servico"
                            type="number"
                            min="1"
                            value={qtdServico}
                            onChange={(e) => setQtdServico(e.target.value)}
                            disabled={adicionandoServico}
                            required
                          />
                        </div>
                      </div>

                      {/* Prévia Transparente do Cálculo da Mão de Obra */}
                      {servicoSelecionado && previaServico && (
                        <div className="rounded-lg border border-primary/30 bg-primary/5 p-3.5 text-xs flex flex-col gap-2">
                          <div className="font-semibold text-foreground flex items-center justify-between">
                            <span>
                              Simulação da Mão de Obra ({servicoSelecionado.nome}):
                            </span>
                            <Badge variant="outline" className="font-mono">
                              <Clock className="size-3 mr-1" />
                              {formatarTempoExibicao(
                                servicoSelecionado.tempo,
                                servicoSelecionado.quantidadeHoras,
                              )}{' '}
                              ({previaServico.horas.toFixed(2)}h)
                            </Badge>
                          </div>
                          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 pt-1 font-mono">
                            <div className="rounded bg-background/80 p-2 border">
                              <span className="text-[10px] text-muted-foreground block">
                                Base (Horas × Valor/h)
                              </span>
                              <span className="font-semibold">
                                {formatCurrency(previaServico.maoDeObraBase)}
                              </span>
                            </div>
                            <div className="rounded bg-background/80 p-2 border">
                              <span className="text-[10px] text-muted-foreground block">
                                Fator FIPE/Risco
                              </span>
                              <span className="font-semibold text-primary">
                                {previaServico.multiplicador.toFixed(4)}x
                              </span>
                            </div>
                            <div className="rounded bg-background/80 p-2 border">
                              <span className="text-[10px] text-muted-foreground block">
                                Unitário Calculado
                              </span>
                              <span className="font-semibold">
                                {formatCurrency(previaServico.valorUnitario)}
                              </span>
                            </div>
                            <div className="rounded bg-background/80 p-2 border">
                              <span className="text-[10px] text-muted-foreground block">
                                Subtotal ({previaServico.qtd}x)
                              </span>
                              <span className="font-bold text-success">
                                {formatCurrency(previaServico.subtotal)}
                              </span>
                            </div>
                          </div>
                        </div>
                      )}

                      <div className="flex items-center justify-between pt-1">
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={() => setNovoServicoModalAberto(true)}
                        >
                          <Plus data-icon="inline-start" />
                          Novo Serviço no Catálogo
                        </Button>
                        <Button
                          type="submit"
                          disabled={!selecionadoServicoId || adicionandoServico}
                        >
                          {adicionandoServico && <Spinner className="size-4 mr-2" />}
                          <Plus data-icon="inline-start" />
                          Adicionar Serviço ao Orçamento
                        </Button>
                      </div>
                    </form>
                  </TabsContent>

                  {/* ABA: ADICIONAR PEÇA */}
                  <TabsContent value="peca">
                    <form onSubmit={handleAdicionarPeca} className="flex flex-col gap-4">
                      <div className="grid gap-4 sm:grid-cols-4 items-end">
                        <div className="flex flex-col gap-1.5 sm:col-span-3">
                          <div className="flex items-center justify-between">
                            <Label htmlFor="select-peca">Peça do Catálogo</Label>
                            <button
                              type="button"
                              onClick={() => setNovaPecaModalAberto(true)}
                              className="text-xs font-medium text-primary hover:underline inline-flex items-center gap-1"
                            >
                              <Sparkles className="size-3" />
                              Não está na lista? Cadastrar nova peça
                            </button>
                          </div>
                          <Select
                            value={selecionadoPecaId}
                            onValueChange={(v) => setSelecionadoPecaId(v ?? '')}
                            disabled={adicionandoPeca}
                          >
                            <SelectTrigger id="select-peca" className="w-full">
                              <SelectValue placeholder="Selecione uma peça da oficina...">
                                {pecaSelecionada
                                  ? pecaSelecionada.nome
                                  : 'Selecione uma peça da oficina...'}
                              </SelectValue>
                            </SelectTrigger>
                            <SelectContent>
                              <SelectGroup>
                                {pecasCatalogo.map((p) => (
                                  <SelectItem key={p.id} value={String(p.id)}>
                                    {p.nome} {p.fabricante ? `· ${p.fabricante}` : ''} (
                                    {formatCurrency(Number(p.valorBase))})
                                  </SelectItem>
                                ))}
                              </SelectGroup>
                            </SelectContent>
                          </Select>
                        </div>

                        <div className="flex flex-col gap-1.5 sm:col-span-1">
                          <Label htmlFor="qtd-peca">Quantidade</Label>
                          <Input
                            id="qtd-peca"
                            type="number"
                            min="1"
                            value={qtdPeca}
                            onChange={(e) => setQtdPeca(e.target.value)}
                            disabled={adicionandoPeca}
                            required
                          />
                        </div>
                      </div>

                      {pecaSelecionada && previaPeca && (
                        <div className="rounded-lg border border-info/30 bg-info/5 p-3.5 text-xs flex items-center justify-between">
                          <div>
                            <span className="font-semibold text-foreground">
                              {pecaSelecionada.nome}
                            </span>
                            {pecaSelecionada.fabricante && (
                              <span className="text-muted-foreground ml-2">
                                Fabricante: {pecaSelecionada.fabricante}
                              </span>
                            )}
                          </div>
                          <div className="font-mono text-sm">
                            {previaPeca.qtd}x {formatCurrency(previaPeca.valorUnitario)} ={' '}
                            <span className="font-bold text-success">
                              {formatCurrency(previaPeca.subtotal)}
                            </span>
                          </div>
                        </div>
                      )}

                      <div className="flex items-center justify-between pt-1">
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={() => setNovaPecaModalAberto(true)}
                        >
                          <Plus data-icon="inline-start" />
                          Nova Peça no Catálogo
                        </Button>
                        <Button
                          type="submit"
                          disabled={!selecionadoPecaId || adicionandoPeca}
                        >
                          {adicionandoPeca && <Spinner className="size-4 mr-2" />}
                          <Plus data-icon="inline-start" />
                          Adicionar Peça ao Orçamento
                        </Button>
                      </div>
                    </form>
                  </TabsContent>
                </Tabs>
              </CardContent>
            </Card>
          )}

          {/* Tabela 1: Mão de Obra (Serviços) */}
          <Card>
            <CardHeader className="flex-row items-center justify-between pb-3">
              <div className="flex items-center gap-2">
                <div className="flex size-8 items-center justify-center rounded-md bg-primary/15 text-primary">
                  <Wrench className="size-4" />
                </div>
                <div>
                  <CardTitle className="text-base font-semibold">
                    1. Mão de Obra (Serviços)
                  </CardTitle>
                  <CardDescription>
                    Valores calculados com base no tempo estimado, valor/hora da oficina e fator FIPE.
                  </CardDescription>
                </div>
              </div>
              <div className="text-right">
                <span className="text-[10px] uppercase tracking-wide text-muted-foreground block">
                  Subtotal Mão de Obra
                </span>
                <span className="text-lg font-bold font-mono text-primary">
                  {formatCurrency(valoresResumo.totalServicos)}
                </span>
              </div>
            </CardHeader>
            <CardContent>
              {!os.itensServico || os.itensServico.length === 0 ? (
                <div className="py-8 text-center text-muted-foreground text-sm border border-dashed rounded-lg">
                  Nenhum serviço (mão de obra) adicionado a este orçamento.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Serviço</TableHead>
                        <TableHead>Tempo</TableHead>
                        <TableHead className="text-right hidden sm:table-cell">
                          Base (Horas × Valor/h)
                        </TableHead>
                        <TableHead className="text-right">Unitário (c/ FIPE)</TableHead>
                        <TableHead className="text-center w-16">Qtd</TableHead>
                        <TableHead className="text-right">Subtotal</TableHead>
                        {podeEditar && <TableHead className="w-10"></TableHead>}
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {os.itensServico.map((item) => (
                        <TableRow key={item.id}>
                          <TableCell className="font-medium">{item.servicoNome}</TableCell>
                          <TableCell>
                            <Badge variant="outline" className="font-mono text-xs gap-1">
                              <Clock className="size-3 text-muted-foreground" />
                              {formatarTempoExibicao(item.tempo, item.quantidadeHoras)}
                            </Badge>
                          </TableCell>
                          <TableCell className="text-right font-mono text-xs text-muted-foreground hidden sm:table-cell">
                            {item.valorHoraBase != null
                              ? formatCurrency(Number(item.valorHoraBase))
                              : '—'}
                            {item.multiplicadorFipe != null && (
                              <span className="block text-[10px] text-primary">
                                × {Number(item.multiplicadorFipe).toFixed(4)}
                              </span>
                            )}
                          </TableCell>
                          <TableCell className="text-right font-mono text-sm">
                            {formatCurrency(Number(item.valorUnitario))}
                          </TableCell>
                          <TableCell className="text-center font-medium">
                            {item.quantidade}
                          </TableCell>
                          <TableCell className="text-right font-mono font-semibold">
                            {formatCurrency(Number(item.subtotal))}
                          </TableCell>
                          {podeEditar && (
                            <TableCell className="text-right">
                              <Button
                                size="sm"
                                variant="ghost"
                                className="text-destructive hover:bg-destructive/10"
                                onClick={() => handleRemoverServico(item.id)}
                                title="Remover serviço"
                              >
                                <Trash2 className="size-3.5" />
                              </Button>
                            </TableCell>
                          )}
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Tabela 2: Peças e Componentes */}
          <Card>
            <CardHeader className="flex-row items-center justify-between pb-3">
              <div className="flex items-center gap-2">
                <div className="flex size-8 items-center justify-center rounded-md bg-info/15 text-info">
                  <Package className="size-4" />
                </div>
                <div>
                  <CardTitle className="text-base font-semibold">
                    2. Peças e Componentes
                  </CardTitle>
                  <CardDescription>
                    Peças aplicadas na manutenção pelo valor base unitário do catálogo.
                  </CardDescription>
                </div>
              </div>
              <div className="text-right">
                <span className="text-[10px] uppercase tracking-wide text-muted-foreground block">
                  Subtotal Peças
                </span>
                <span className="text-lg font-bold font-mono text-info">
                  {formatCurrency(valoresResumo.totalPecas)}
                </span>
              </div>
            </CardHeader>
            <CardContent>
              {!os.itensPeca || os.itensPeca.length === 0 ? (
                <div className="py-8 text-center text-muted-foreground text-sm border border-dashed rounded-lg">
                  Nenhuma peça adicionada a este orçamento.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Peça</TableHead>
                        <TableHead>Fabricante</TableHead>
                        <TableHead className="text-right">Valor Unitário</TableHead>
                        <TableHead className="text-center w-16">Qtd</TableHead>
                        <TableHead className="text-right">Subtotal</TableHead>
                        {podeEditar && <TableHead className="w-10"></TableHead>}
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {os.itensPeca.map((item) => (
                        <TableRow key={item.id}>
                          <TableCell className="font-medium">
                            {item.pecaNome || item.nomePeca || `Peça #${item.pecaId}`}
                          </TableCell>
                          <TableCell className="text-muted-foreground text-sm">
                            {item.fabricante || '—'}
                          </TableCell>
                          <TableCell className="text-right font-mono text-sm">
                            {formatCurrency(Number(item.valorUnitario))}
                          </TableCell>
                          <TableCell className="text-center font-medium">
                            {item.quantidade}
                          </TableCell>
                          <TableCell className="text-right font-mono font-semibold">
                            {formatCurrency(Number(item.subtotal))}
                          </TableCell>
                          {podeEditar && (
                            <TableCell className="text-right">
                              <Button
                                size="sm"
                                variant="ghost"
                                className="text-destructive hover:bg-destructive/10"
                                onClick={() => handleRemoverPeca(item.id)}
                                title="Remover peça"
                              >
                                <Trash2 className="size-3.5" />
                              </Button>
                            </TableCell>
                          )}
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Dialog: Cadastrar Novo Serviço no Catálogo (sem sair do orçamento) */}
      <Dialog open={novoServicoModalAberto} onOpenChange={setNovoServicoModalAberto}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Cadastrar Novo Serviço no Catálogo</DialogTitle>
            <DialogDescription>
              O serviço será salvo no catálogo da sua oficina e selecionado automaticamente para este orçamento.
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleCriarNovoServicoCatalogo}>
            <div className="flex flex-col gap-4 py-4">
              <div className="flex flex-col gap-2">
                <Label htmlFor="modal-srv-nome">Nome do Serviço</Label>
                <Input
                  id="modal-srv-nome"
                  placeholder="Ex: Troca da Correia Dentada"
                  value={novoServicoNome}
                  onChange={(e) => setNovoServicoNome(e.target.value)}
                  required
                />
              </div>
              <div className="flex flex-col gap-2">
                <Label htmlFor="modal-srv-tempo">Tempo Estimado (ex: 1h30 ou 1:30)</Label>
                <Input
                  id="modal-srv-tempo"
                  placeholder="Ex: 1h30 ou 01:30"
                  value={novoServicoTempo}
                  onChange={(e) => setNovoServicoTempo(e.target.value)}
                  required
                />
              </div>
              <div className="flex flex-col gap-2">
                <Label htmlFor="modal-srv-desc">Descrição (Opcional)</Label>
                <Textarea
                  id="modal-srv-desc"
                  rows={2}
                  placeholder="Procedimentos inclusos..."
                  value={novoServicoDescricao}
                  onChange={(e) => setNovoServicoDescricao(e.target.value)}
                />
              </div>
            </div>
            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => setNovoServicoModalAberto(false)}
                disabled={salvandoNovoServico}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={salvandoNovoServico}>
                {salvandoNovoServico && <Spinner className="size-4 mr-2" />}
                Cadastrar e Selecionar
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Dialog: Cadastrar Nova Peça no Catálogo (sem sair do orçamento) */}
      <Dialog open={novaPecaModalAberto} onOpenChange={setNovaPecaModalAberto}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Cadastrar Nova Peça no Catálogo</DialogTitle>
            <DialogDescription>
              A peça será salva no catálogo da sua oficina e selecionada automaticamente para este orçamento.
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleCriarNovaPecaCatalogo}>
            <div className="flex flex-col gap-4 py-4">
              <div className="flex flex-col gap-2">
                <Label htmlFor="modal-peca-nome">Nome da Peça</Label>
                <Input
                  id="modal-peca-nome"
                  placeholder="Ex: Jogo de Pastilhas Dianteiras"
                  value={novaPecaNome}
                  onChange={(e) => setNovaPecaNome(e.target.value)}
                  required
                />
              </div>
              <div className="flex flex-col gap-2">
                <Label htmlFor="modal-peca-fab">Fabricante</Label>
                <Input
                  id="modal-peca-fab"
                  placeholder="Ex: Cobreq / Bosch"
                  value={novaPecaFabricante}
                  onChange={(e) => setNovaPecaFabricante(e.target.value)}
                  required
                />
              </div>
              <div className="flex flex-col gap-2">
                <Label htmlFor="modal-peca-valor">Valor Base Unitário (R$)</Label>
                <Input
                  id="modal-peca-valor"
                  type="number"
                  step="0.01"
                  placeholder="0.00"
                  value={novaPecaValor}
                  onChange={(e) => setNovaPecaValor(e.target.value)}
                  required
                />
              </div>
            </div>
            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => setNovaPecaModalAberto(false)}
                disabled={salvandoNovaPeca}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={salvandoNovaPeca}>
                {salvandoNovaPeca && <Spinner className="size-4 mr-2" />}
                Cadastrar e Selecionar
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* AlertDialog para Confirmação de Transição de Status */}
      <AlertDialog
        open={acaoStatusConfirmar !== null}
        onOpenChange={(aberto) => !aberto && setAcaoStatusConfirmar(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Confirmar alteração de status?</AlertDialogTitle>
            <AlertDialogDescription>
              {acaoStatusConfirmar === 'aceitar' &&
                'O orçamento será marcado como ACEITO pelo cliente, habilitando o início da execução.'}
              {acaoStatusConfirmar === 'iniciar' &&
                'A Ordem de Serviço mudará para EM ANDAMENTO e a data de início da manutenção será registrada.'}
              {acaoStatusConfirmar === 'finalizar' &&
                'A Ordem de Serviço será marcada como FINALIZADA. Após finalizar, não será mais possível adicionar/remover itens ou alterar descontos.'}
              {acaoStatusConfirmar === 'rejeitar' &&
                'O orçamento será marcado como REJEITADO e a data de rejeição será registrada.'}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={executandoStatus}>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              onClick={executarMudancaStatus}
              disabled={executandoStatus}
            >
              {executandoStatus ? 'Processando...' : 'Confirmar'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
