'use client'

import { useState, useEffect, useCallback } from 'react'
import {
  Search,
  Plus,
  Edit,
  Trash2,
  Package,
  Wrench,
  Clock,
} from 'lucide-react'
import { catalogoService } from '@/lib/services'
import { handleError, normalizeError } from '@/lib/error-handler'
import type {
  ServicoResponse,
  ServicoRequest,
  PecaResponse,
  PecaRequest,
} from '@/lib/types'
import { formatCurrency } from '@/lib/format'
import { PageHeader } from '@/components/page-header'
import { EmptyState } from '@/components/empty-state'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Spinner } from '@/components/ui/spinner'
import {
  InputGroup,
  InputGroupAddon,
  InputGroupInput,
} from '@/components/ui/input-group'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
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
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { toast } from 'sonner'
import { PESO_SERVICO_OPCOES } from '@/lib/types'

/**
 * Converte qualquer entrada de tempo (ex: "1h30", "1:30", "01:30", "45min", "1.5")
 * para o formato HH:mm padrão exigido pelo backend (ou null se inválido).
 */
function normalizarTempoParaEnvio(input: string): string | null {
  const str = input.trim()
  if (!str) return null

  // 1. Formato HH:mm ou H:mm ou HH:mm:ss
  const colonMatch = str.match(/^(\d{1,2}):(\d{1,2})(?::\d{1,2})?$/)
  if (colonMatch) {
    const h = parseInt(colonMatch[1], 10)
    const m = parseInt(colonMatch[2], 10)
    if (h === 0 && m === 0) return null
    if (m > 59) return null
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
  }

  // 2. Formato 1h30, 1h30m, 1h, 1H
  const horaMinMatch = str.match(/^(\d+)\s*[hH]\s*(\d+)?\s*(?:[mM])?$/)
  if (horaMinMatch) {
    const h = parseInt(horaMinMatch[1], 10)
    const m = horaMinMatch[2] ? parseInt(horaMinMatch[2], 10) : 0
    if (h === 0 && m === 0) return null
    if (m > 59) return null
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
  }

  // 3. Formato de minutos apenas: 30m, 45min
  const minMatch = str.match(/^(\d+)\s*(?:m|min|minutos)$/i)
  if (minMatch) {
    const totalMin = parseInt(minMatch[1], 10)
    if (totalMin <= 0) return null
    const h = Math.floor(totalMin / 60)
    const m = totalMin % 60
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
  }

  // 4. Formato decimal: 1.5 ou 1,5
  const num = parseFloat(str.replace(',', '.'))
  if (!isNaN(num) && num > 0) {
    const h = Math.floor(num)
    const m = Math.round((num - h) * 60)
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
  }

  return null
}

/**
 * Formata o tempo para exibição amigável:
 * - "01:30" ou 1.5 -> "1h30"
 * - "02:00" ou 2.0 -> "2h"
 * - "00:45" ou 0.75 -> "45min"
 */
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

export default function CatalogoPage() {
  const [activeTab, setActiveTab] = useState('servicos')

  // --- Filtros de Busca ---
  const [buscaServico, setBuscaServico] = useState('')
  const [buscaPeca, setBuscaPeca] = useState('')

  // --- Estado de Serviços (API Spring Boot: /servicos) ---
  const [servicos, setServicos] = useState<ServicoResponse[]>([])
  const [carregandoServicos, setCarregandoServicos] = useState(true)
  const [salvandoServico, setSalvandoServico] = useState(false)
  const [servicoParaExcluir, setServicoParaExcluir] = useState<ServicoResponse | null>(null)
  const [excluindoServico, setExcluindoServico] = useState(false)

  // --- Estado de Peças (API Spring Boot: /pecas) ---
  const [pecas, setPecas] = useState<PecaResponse[]>([])
  const [carregandoPecas, setCarregandoPecas] = useState(true)
  const [salvandoPeca, setSalvandoPeca] = useState(false)
  const [pecaParaExcluir, setPecaParaExcluir] = useState<PecaResponse | null>(null)
  const [excluindoPeca, setExcluindoPeca] = useState(false)

  // --- Modal de Serviço ---
  const [servicoModalAberto, setServicoModalAberto] = useState(false)
  const [editandoServico, setEditandoServico] = useState<ServicoResponse | null>(null)
  const [servicoNome, setServicoNome] = useState('')
  const [servicoTempo, setServicoTempo] = useState('')
  const [servicoDescricao, setServicoDescricao] = useState('')
  const [servicoPeso, setServicoPeso] = useState<number>(0.5)
  const [servicoErros, setServicoErros] = useState<Record<string, string>>({})

  // --- Modal de Peça ---
  const [pecaModalAberto, setPecaModalAberto] = useState(false)
  const [editandoPeca, setEditandoPeca] = useState<PecaResponse | null>(null)
  const [pecaNome, setPecaNome] = useState('')
  const [pecaFabricante, setPecaFabricante] = useState('')
  const [pecaValor, setPecaValor] = useState('')
  const [pecaErros, setPecaErros] = useState<Record<string, string>>({})

  // --- Carregamento de Serviços do Backend ---
  const carregarServicos = useCallback(async (termoBusca?: string) => {
    setCarregandoServicos(true)
    try {
      const resp = await catalogoService.listarServicos({
        nome: termoBusca !== undefined ? (termoBusca.trim() || undefined) : undefined,
        size: 100,
      })
      setServicos(resp.content || [])
    } catch (err) {
      console.error('Erro ao carregar serviços:', err)
      toast.error('Erro ao carregar lista de serviços.')
    } finally {
      setCarregandoServicos(false)
    }
  }, [])

  // --- Carregamento de Peças do Backend ---
  const carregarPecas = useCallback(async (termoBusca?: string) => {
    setCarregandoPecas(true)
    try {
      const resp = await catalogoService.listarPecas({
        busca: termoBusca !== undefined ? (termoBusca.trim() || undefined) : undefined,
        size: 100,
      })
      setPecas(resp.content || [])
    } catch (err) {
      console.error('Erro ao carregar peças:', err)
      toast.error('Erro ao carregar lista de peças.')
    } finally {
      setCarregandoPecas(false)
    }
  }, [])

  useEffect(() => {
    const timer = setTimeout(() => {
      carregarServicos(buscaServico)
    }, 300)
    return () => clearTimeout(timer)
  }, [buscaServico, carregarServicos])

  useEffect(() => {
    const timer = setTimeout(() => {
      carregarPecas(buscaPeca)
    }, 300)
    return () => clearTimeout(timer)
  }, [buscaPeca, carregarPecas])

  // --- Handlers: Modal Serviço ---
  const abrirNovoServico = () => {
    setEditandoServico(null)
    setServicoNome('')
    setServicoTempo('')
    setServicoDescricao('')
    setServicoPeso(0.5)
    setServicoErros({})
    setServicoModalAberto(true)
  }

  const abrirEditarServico = (servico: ServicoResponse) => {
    setEditandoServico(servico)
    setServicoNome(servico.nome)
    if (servico.tempo) {
      setServicoTempo(formatarTempoExibicao(servico.tempo, servico.quantidadeHoras))
    } else if (servico.quantidadeHoras) {
      setServicoTempo(formatarTempoExibicao(undefined, servico.quantidadeHoras))
    } else {
      setServicoTempo('')
    }
    setServicoDescricao(servico.descricao || '')
    setServicoPeso(servico.pesoServico != null ? Number(servico.pesoServico) : 0.5)
    setServicoErros({})
    setServicoModalAberto(true)
  }

  const handleSalvarServico = async (e: React.FormEvent) => {
    e.preventDefault()

    const erros: Record<string, string> = {}
    if (!servicoNome.trim()) {
      erros.nome = 'Nome do serviço é obrigatório.'
    }
    const tempoFormatado = normalizarTempoParaEnvio(servicoTempo)
    if (!tempoFormatado) {
      erros.tempo = 'Informe um tempo válido (ex: 1h30, 1:30, 45min ou 2h).'
    }

    if (Object.keys(erros).length > 0) {
      setServicoErros(erros)
      return
    }

    setSalvandoServico(true)
    try {
      const payload: ServicoRequest = {
        nome: servicoNome.trim(),
        tempo: tempoFormatado!,
        descricao: servicoDescricao.trim() || undefined,
        pesoServico: servicoPeso,
      }

      if (editandoServico) {
        await catalogoService.atualizarServico(editandoServico.id, payload)
        toast.success('Serviço atualizado com sucesso!')
      } else {
        await catalogoService.criarServico(payload)
        toast.success('Serviço criado com sucesso!')
      }

      setServicoModalAberto(false)
      carregarServicos(buscaServico)
    } catch (err) {
      handleError(err, { fallbackMessage: 'Erro ao salvar serviço.' })
    } finally {
      setSalvandoServico(false)
    }
  }

  const handleConfirmarExclusao = async () => {
    if (!servicoParaExcluir) return
    setExcluindoServico(true)
    try {
      await catalogoService.excluirServico(servicoParaExcluir.id)
      toast.success('Serviço excluído com sucesso!')
      setServicoParaExcluir(null)
      carregarServicos(buscaServico)
    } catch (err) {
      handleError(err, { fallbackMessage: 'Erro ao excluir serviço.' })
    } finally {
      setExcluindoServico(false)
    }
  }

  // --- Handlers: Modal Peça ---
  const abrirNovaPeca = () => {
    setEditandoPeca(null)
    setPecaNome('')
    setPecaFabricante('')
    setPecaValor('')
    setPecaErros({})
    setPecaModalAberto(true)
  }

  const abrirEditarPeca = (peca: PecaResponse) => {
    setEditandoPeca(peca)
    setPecaNome(peca.nome)
    setPecaFabricante(peca.fabricante || '')
    setPecaValor(peca.valorBase != null ? peca.valorBase.toString() : '')
    setPecaErros({})
    setPecaModalAberto(true)
  }

  const handleSalvarPeca = async (e: React.FormEvent) => {
    e.preventDefault()

    const erros: Record<string, string> = {}
    if (!pecaNome.trim()) erros.nome = 'Nome da peça é obrigatório.'
    if (!pecaFabricante.trim()) erros.fabricante = 'Fabricante da peça é obrigatório.'
    const valor = Number.parseFloat(pecaValor.replace(',', '.'))
    if (!pecaValor || Number.isNaN(valor) || valor <= 0) {
      erros.valor = 'Valor base deve ser um número positivo maior que zero.'
    }

    if (Object.keys(erros).length > 0) {
      setPecaErros(erros)
      return
    }

    setSalvandoPeca(true)
    try {
      const payload: PecaRequest = {
        nome: pecaNome.trim(),
        fabricante: pecaFabricante.trim(),
        valorBase: valor,
      }

      if (editandoPeca) {
        await catalogoService.atualizarPeca(editandoPeca.id, payload)
        toast.success('Peça atualizada com sucesso!')
      } else {
        await catalogoService.criarPeca(payload)
        toast.success('Peça cadastrada com sucesso!')
      }

      setPecaModalAberto(false)
      carregarPecas(buscaPeca)
    } catch (err) {
      handleError(err, { fallbackMessage: 'Erro ao salvar peça.' })
    } finally {
      setSalvandoPeca(false)
    }
  }

  const handleConfirmarExclusaoPeca = async () => {
    if (!pecaParaExcluir) return
    setExcluindoPeca(true)
    try {
      await catalogoService.excluirPeca(pecaParaExcluir.id)
      toast.success('Peça excluída com sucesso!')
      setPecaParaExcluir(null)
      carregarPecas(buscaPeca)
    } catch (err) {
      handleError(err, { fallbackMessage: 'Erro ao excluir peça.' })
    } finally {
      setExcluindoPeca(false)
    }
  }

  return (
    <div>
      <PageHeader
        title="Catálogo"
        description="Mantenha a lista de peças e serviços da sua oficina."
      />

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <div className="mb-4 flex items-center justify-between">
          <TabsList>
            <TabsTrigger value="servicos">
              <Wrench className="size-4" />
              Serviços
            </TabsTrigger>
            <TabsTrigger value="pecas">
              <Package className="size-4" />
              Peças
            </TabsTrigger>
          </TabsList>

          {activeTab === 'servicos' ? (
            <Button onClick={abrirNovoServico}>
              <Plus data-icon="inline-start" />
              Novo Serviço
            </Button>
          ) : (
            <Button onClick={abrirNovaPeca}>
              <Plus data-icon="inline-start" />
              Nova Peça
            </Button>
          )}
        </div>

        {/* --- ABA SERVIÇOS --- */}
        <TabsContent value="servicos">
          <Card>
            <CardContent className="flex flex-col gap-4 pt-6">
              <InputGroup className="sm:max-w-xs">
                <InputGroupAddon>
                  <Search />
                </InputGroupAddon>
                <InputGroupInput
                  placeholder="Buscar serviços..."
                  value={buscaServico}
                  onChange={(e) => setBuscaServico(e.target.value)}
                />
              </InputGroup>

              {carregandoServicos ? (
                <div className="flex flex-col items-center justify-center py-12 gap-3 text-muted-foreground">
                  <Spinner className="size-6" />
                  <span className="text-sm">Carregando serviços...</span>
                </div>
              ) : servicos.length === 0 ? (
                <EmptyState
                  icon={Wrench}
                  title="Nenhum serviço encontrado"
                  description={
                    buscaServico
                      ? 'Nenhum serviço corresponde aos termos de busca.'
                      : 'Cadastre seu primeiro serviço informando nome, tempo estimado e descrição.'
                  }
                  action={
                    <Button onClick={abrirNovoServico}>
                      <Plus data-icon="inline-start" />
                      Cadastrar serviço
                    </Button>
                  }
                />
              ) : (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Serviço</TableHead>
                        <TableHead className="w-36">Tempo Estimado</TableHead>
                        <TableHead className="w-44">Risco / Peso</TableHead>
                        <TableHead>Descrição</TableHead>
                        <TableHead className="text-right w-28">Ações</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {servicos.map((s) => (
                        <TableRow key={s.id}>
                          <TableCell className="font-medium">{s.nome}</TableCell>
                          <TableCell>
                            <Badge variant="outline" className="font-mono bg-muted/50 gap-1.5 py-1">
                              <Clock className="size-3.5 text-muted-foreground" />
                              {formatarTempoExibicao(s.tempo, s.quantidadeHoras)}
                            </Badge>
                          </TableCell>
                          <TableCell>
                            {Number(s.pesoServico ?? 0.5) === 0 ? (
                              <Badge variant="outline" className="border-emerald-500/30 text-emerald-600 dark:text-emerald-400 bg-emerald-500/10 font-medium whitespace-nowrap">
                                0,0 — Sem risco
                              </Badge>
                            ) : Number(s.pesoServico ?? 0.5) === 1 ? (
                              <Badge variant="outline" className="border-amber-500/30 text-amber-600 dark:text-amber-400 bg-amber-500/10 font-medium whitespace-nowrap">
                                1,0 — Alto risco
                              </Badge>
                            ) : (
                              <Badge variant="outline" className="border-blue-500/30 text-blue-600 dark:text-blue-400 bg-blue-500/10 font-medium whitespace-nowrap">
                                0,5 — Moderado
                              </Badge>
                            )}
                          </TableCell>
                          <TableCell className="text-muted-foreground text-sm max-w-md truncate">
                            {s.descricao || '-'}
                          </TableCell>
                          <TableCell className="text-right">
                            <div className="flex items-center justify-end gap-1">
                              <Button
                                size="sm"
                                variant="ghost"
                                onClick={() => abrirEditarServico(s)}
                                title="Editar serviço"
                              >
                                <Edit className="size-3.5" />
                                <span className="sr-only">Editar</span>
                              </Button>
                              <Button
                                size="sm"
                                variant="ghost"
                                className="text-destructive hover:text-destructive hover:bg-destructive/10"
                                onClick={() => setServicoParaExcluir(s)}
                                title="Excluir serviço"
                              >
                                <Trash2 className="size-3.5" />
                                <span className="sr-only">Excluir</span>
                              </Button>
                            </div>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* --- ABA PEÇAS --- */}
        <TabsContent value="pecas">
          <Card>
            <CardContent className="flex flex-col gap-4 pt-6">
              <InputGroup className="sm:max-w-xs">
                <InputGroupAddon>
                  <Search />
                </InputGroupAddon>
                <InputGroupInput
                  placeholder="Buscar peças por nome ou fabricante..."
                  value={buscaPeca}
                  onChange={(e) => setBuscaPeca(e.target.value)}
                />
              </InputGroup>

              {carregandoPecas ? (
                <div className="flex flex-col items-center justify-center py-12 gap-3 text-muted-foreground">
                  <Spinner className="size-6" />
                  <span className="text-sm">Carregando peças...</span>
                </div>
              ) : pecas.length === 0 ? (
                <EmptyState
                  icon={Package}
                  title="Nenhuma peça encontrada"
                  description={
                    buscaPeca
                      ? 'Nenhuma peça corresponde aos termos de busca.'
                      : 'Cadastre uma nova peça informando nome, fabricante e valor base.'
                  }
                  action={
                    <Button onClick={abrirNovaPeca}>
                      <Plus data-icon="inline-start" />
                      Cadastrar peça
                    </Button>
                  }
                />
              ) : (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Peça</TableHead>
                        <TableHead>Fabricante</TableHead>
                        <TableHead className="text-right">Valor Base</TableHead>
                        <TableHead className="text-right w-28">Ações</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {pecas.map((p) => (
                        <TableRow key={p.id}>
                          <TableCell className="font-medium">{p.nome}</TableCell>
                          <TableCell className="text-muted-foreground">
                            {p.fabricante || '-'}
                          </TableCell>
                          <TableCell className="text-right font-mono font-medium">
                            {formatCurrency(p.valorBase)}
                          </TableCell>
                          <TableCell className="text-right">
                            <div className="flex items-center justify-end gap-1">
                              <Button
                                size="sm"
                                variant="ghost"
                                onClick={() => abrirEditarPeca(p)}
                                title="Editar peça"
                              >
                                <Edit className="size-3.5" />
                                <span className="sr-only">Editar</span>
                              </Button>
                              <Button
                                size="sm"
                                variant="ghost"
                                className="text-destructive hover:text-destructive hover:bg-destructive/10"
                                onClick={() => setPecaParaExcluir(p)}
                                title="Excluir peça"
                              >
                                <Trash2 className="size-3.5" />
                                <span className="sr-only">Excluir</span>
                              </Button>
                            </div>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* --- Dialog: Cadastro / Edição de Serviço --- */}
      <Dialog open={servicoModalAberto} onOpenChange={setServicoModalAberto}>
        <DialogContent className="sm:max-w-lg">
          <DialogHeader>
            <DialogTitle>
              {editandoServico ? 'Editar Serviço' : 'Novo Serviço'}
            </DialogTitle>
            <DialogDescription>
              Informe o nome, o tempo estimado de execução (ex: 1h30 ou 1:30) e a descrição do serviço.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSalvarServico}>
            <div className="flex flex-col gap-4 py-4">
              <div className="flex flex-col gap-2">
                <Label htmlFor="srv-nome">Nome do Serviço</Label>
                <Input
                  id="srv-nome"
                  placeholder="Ex: Alinhamento e Balanceamento"
                  value={servicoNome}
                  onChange={(e) => {
                    setServicoNome(e.target.value)
                    if (servicoErros.nome) {
                      setServicoErros((prev) => {
                        const n = { ...prev }
                        delete n.nome
                        return n
                      })
                    }
                  }}
                  required
                />
                {servicoErros.nome && (
                  <p className="text-xs text-destructive">{servicoErros.nome}</p>
                )}
              </div>

              <div className="flex flex-col gap-2">
                <Label htmlFor="srv-tempo">Tempo Estimado (ex: 1h30 ou 1:30)</Label>
                <Input
                  id="srv-tempo"
                  type="text"
                  placeholder="Ex: 1h30 ou 1:30"
                  value={servicoTempo}
                  onChange={(e) => {
                    setServicoTempo(e.target.value)
                    if (servicoErros.tempo) {
                      setServicoErros((prev) => {
                        const n = { ...prev }
                        delete n.tempo
                        return n
                      })
                    }
                  }}
                  required
                />
                <span className="text-[11px] text-muted-foreground">
                  *Aceita formatos como 1h30, 1:30, 2h ou 45min. O sistema converte automaticamente para o cálculo do valor da mão de obra.
                </span>
                {servicoErros.tempo && (
                  <p className="text-xs text-destructive">{servicoErros.tempo}</p>
                )}
              </div>

              <div className="flex flex-col gap-2">
                <Label htmlFor="srv-peso">Nível de Risco do Serviço (Peso Padrão)</Label>
                <Select
                  value={String(servicoPeso)}
                  onValueChange={(val) => setServicoPeso(Number(val))}
                >
                  <SelectTrigger id="srv-peso" className="w-full">
                    <SelectValue placeholder="Selecione o nível de risco..." />
                  </SelectTrigger>
                  <SelectContent>
                    {PESO_SERVICO_OPCOES.map((opcao) => (
                      <SelectItem key={opcao.valor} value={String(opcao.valor)}>
                        <div className="flex flex-col text-left py-0.5">
                          <span className="font-medium text-foreground">{opcao.label}</span>
                          <span className="text-[11px] text-muted-foreground">{opcao.descricao}</span>
                        </div>
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <span className="text-[11px] text-muted-foreground">
                  Multiplicador na fórmula de precificação sobre a FIPE: Horas × Valor_Hora × (1 + FIPE/100000 × Taxa_Risco × Peso). Pode ser ajustado em cada orçamento individual.
                </span>
              </div>

              <div className="flex flex-col gap-2">
                <Label htmlFor="srv-descricao">Descrição (Opcional)</Label>
                <Textarea
                  id="srv-descricao"
                  placeholder="Detalhes ou procedimentos do serviço..."
                  rows={3}
                  value={servicoDescricao}
                  onChange={(e) => setServicoDescricao(e.target.value)}
                />
              </div>
            </div>

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => setServicoModalAberto(false)}
                disabled={salvandoServico}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={salvandoServico}>
                {salvandoServico && <Spinner className="size-4 mr-2" />}
                {editandoServico ? 'Salvar Alterações' : 'Cadastrar Serviço'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* --- AlertDialog: Confirmação de Exclusão de Serviço --- */}
      <AlertDialog
        open={!!servicoParaExcluir}
        onOpenChange={(open) => !open && setServicoParaExcluir(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Excluir Serviço</AlertDialogTitle>
            <AlertDialogDescription>
              Tem certeza que deseja excluir o serviço &quot;{servicoParaExcluir?.nome}&quot;? Esta ação não pode ser desfeita.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={excluindoServico}>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleConfirmarExclusao}
              disabled={excluindoServico}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {excluindoServico ? 'Excluindo...' : 'Excluir'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* --- Dialog: Cadastro / Edição de Peça --- */}
      <Dialog open={pecaModalAberto} onOpenChange={setPecaModalAberto}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>
              {editandoPeca ? 'Editar Peça' : 'Nova Peça'}
            </DialogTitle>
            <DialogDescription>
              Forneça o nome, o fabricante e o valor de custo base da peça no catálogo da oficina.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSalvarPeca}>
            <div className="flex flex-col gap-4 py-4">
              <div className="flex flex-col gap-2">
                <Label htmlFor="peca-nome">Nome da Peça</Label>
                <Input
                  id="peca-nome"
                  placeholder="Ex: Filtro de Óleo"
                  value={pecaNome}
                  onChange={(e) => {
                    setPecaNome(e.target.value)
                    if (pecaErros.nome) {
                      setPecaErros((prev) => {
                        const n = { ...prev }
                        delete n.nome
                        return n
                      })
                    }
                  }}
                  required
                />
                {pecaErros.nome && (
                  <p className="text-xs text-destructive">{pecaErros.nome}</p>
                )}
              </div>

              <div className="flex flex-col gap-2">
                <Label htmlFor="peca-fabricante">Fabricante</Label>
                <Input
                  id="peca-fabricante"
                  placeholder="Ex: Bosch"
                  value={pecaFabricante}
                  onChange={(e) => {
                    setPecaFabricante(e.target.value)
                    if (pecaErros.fabricante) {
                      setPecaErros((prev) => {
                        const n = { ...prev }
                        delete n.fabricante
                        return n
                      })
                    }
                  }}
                  required
                />
                {pecaErros.fabricante && (
                  <p className="text-xs text-destructive">{pecaErros.fabricante}</p>
                )}
              </div>

              <div className="flex flex-col gap-2">
                <Label htmlFor="peca-valor">Valor Base (R$)</Label>
                <Input
                  id="peca-valor"
                  type="number"
                  step="0.01"
                  placeholder="0.00"
                  value={pecaValor}
                  onChange={(e) => {
                    setPecaValor(e.target.value)
                    if (pecaErros.valor) {
                      setPecaErros((prev) => {
                        const n = { ...prev }
                        delete n.valor
                        return n
                      })
                    }
                  }}
                  required
                />
                {pecaErros.valor && (
                  <p className="text-xs text-destructive">{pecaErros.valor}</p>
                )}
              </div>
            </div>

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => setPecaModalAberto(false)}
                disabled={salvandoPeca}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={salvandoPeca}>
                {salvandoPeca && <Spinner className="size-4 mr-2" />}
                {editandoPeca ? 'Salvar Alterações' : 'Salvar Peça'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* --- AlertDialog: Confirmação de Exclusão de Peça --- */}
      <AlertDialog
        open={!!pecaParaExcluir}
        onOpenChange={(open) => !open && setPecaParaExcluir(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Excluir Peça</AlertDialogTitle>
            <AlertDialogDescription>
              Tem certeza que deseja excluir a peça &quot;{pecaParaExcluir?.nome}&quot;? Esta ação não pode ser desfeita.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={excluindoPeca}>Cancelar</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleConfirmarExclusaoPeca}
              disabled={excluindoPeca}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {excluindoPeca ? 'Excluindo...' : 'Excluir'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
