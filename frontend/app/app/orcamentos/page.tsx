'use client'

import { useMemo, useState, useEffect, useCallback, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import Link from 'next/link'
import {
  Search,
  FileText,
  Plus,
  ChevronRight,
  Wrench,
  Package,
  Percent,
  X,
  RefreshCw,
} from 'lucide-react'
import { ordemServicoService } from '@/lib/services'
import { handleError } from '@/lib/error-handler'
import type { OrdemServicoResumo, StatusServico } from '@/lib/types'
import { formatCurrency, formatDate } from '@/lib/format'
import { PageHeader } from '@/components/page-header'
import { OrcamentoStatusBadge } from '@/components/status-badge'
import { EmptyState } from '@/components/empty-state'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Spinner } from '@/components/ui/spinner'
import { Badge } from '@/components/ui/badge'
import {
  InputGroup,
  InputGroupAddon,
  InputGroupInput,
} from '@/components/ui/input-group'
import {
  Select,
  SelectContent,
  SelectGroup,
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

type FiltroStatus = StatusServico | 'todos'

const DESCONTO_LABELS: Record<string, string> = {
  SEM_DESCONTO: '0%',
  CINCO_PORCENTO: '5%',
  DEZ_PORCENTO: '10%',
  QUINZE_PORCENTO: '15%',
}

export default function OrcamentosPage() {
  return (
    <Suspense
      fallback={
        <div className="flex flex-col items-center justify-center min-h-[400px] gap-3 text-muted-foreground">
          <Spinner className="size-8 text-primary" />
          <span className="text-sm">Carregando orçamentos...</span>
        </div>
      }
    >
      <OrcamentosContent />
    </Suspense>
  )
}

function OrcamentosContent() {
  const router = useRouter()
  const searchParams = useSearchParams()

  const initialStatus = (searchParams.get('status') as FiltroStatus) || 'todos'
  const initialBusca = searchParams.get('busca') || ''

  const [orcamentos, setOrcamentos] = useState<OrdemServicoResumo[]>([])
  const [carregando, setCarregando] = useState(true)
  const [busca, setBusca] = useState(initialBusca)
  const [statusFiltro, setStatusFiltro] = useState<FiltroStatus>(initialStatus)

  // Atualiza query params na URL mantendo sincronização de navegação
  const sincronizarUrl = useCallback((novoStatus: FiltroStatus, novaBusca: string) => {
    const params = new URLSearchParams()
    if (novoStatus && novoStatus !== 'todos') {
      params.set('status', novoStatus)
    }
    if (novaBusca.trim()) {
      params.set('busca', novaBusca.trim())
    }
    const query = params.toString()
    router.replace(query ? `/app/orcamentos?${query}` : '/app/orcamentos', { scroll: false })
  }, [router])

  const carregarOrcamentos = useCallback(async (status?: FiltroStatus) => {
    setCarregando(true)
    try {
      const resp = await ordemServicoService.listar({
        statusServico: status && status !== 'todos' ? status : undefined,
        size: 100,
        sort: 'id,desc',
      })
      setOrcamentos(resp.content || [])
    } catch (err) {
      handleError(err, { fallbackMessage: 'Não foi possível carregar os orçamentos.' })
    } finally {
      setCarregando(false)
    }
  }, [])

  useEffect(() => {
    carregarOrcamentos(statusFiltro)
  }, [statusFiltro, carregarOrcamentos])

  const handleMudarStatus = (v: string | null) => {
    const novo = (v as FiltroStatus) || 'todos'
    setStatusFiltro(novo)
    sincronizarUrl(novo, busca)
  }

  const handleMudarBusca = (e: React.ChangeEvent<HTMLInputElement>) => {
    const nova = e.target.value
    setBusca(nova)
    sincronizarUrl(statusFiltro, nova)
  }

  const limparFiltros = () => {
    setBusca('')
    setStatusFiltro('todos')
    sincronizarUrl('todos', '')
  }

  const temFiltroAtivo = busca.trim() !== '' || statusFiltro !== 'todos'

  const filtrados = useMemo(() => {
    const q = busca.toLowerCase().trim()
    if (!q) return orcamentos

    return orcamentos.filter((o) => {
      const cliente = (o.clienteNome || o.nomeCliente || '').toLowerCase()
      const placa = (o.placa || o.placaVeiculo || '').toLowerCase()
      const modelo = `${o.veiculoMarca || ''} ${o.veiculoModelo || ''}`.toLowerCase()
      const desc = (o.descricao || '').toLowerCase()
      const idStr = `#${o.id}`
      return (
        cliente.includes(q) ||
        placa.includes(q) ||
        modelo.includes(q) ||
        desc.includes(q) ||
        idStr.includes(q)
      )
    })
  }, [orcamentos, busca])

  return (
    <div className="flex flex-col gap-6 w-full min-w-0 max-w-full">
      <PageHeader
        title="Orçamentos e Ordens de Serviço"
        description="Acompanhe o fluxo de status, valores de mão de obra, peças e descontos."
        actions={
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => carregarOrcamentos(statusFiltro)}
              disabled={carregando}
              title="Recarregar orçamentos"
            >
              <RefreshCw className={`size-4 ${carregando ? 'animate-spin' : ''}`} />
            </Button>
            <Button nativeButton={false} render={<Link href="/app/orcamentos/novo" />}>
              <Plus data-icon="inline-start" />
              Novo Orçamento
            </Button>
          </div>
        }
      />

      <Card className="w-full min-w-0 max-w-full overflow-hidden">
        <CardContent className="flex flex-col gap-4 pt-6 w-full min-w-0">
          <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between w-full min-w-0">
            <div className="flex flex-1 flex-col gap-3 sm:flex-row sm:items-center w-full min-w-0">
              <InputGroup className="w-full sm:max-w-sm flex-1 min-w-0">
                <InputGroupAddon>
                  <Search />
                </InputGroupAddon>
                <InputGroupInput
                  placeholder="Buscar por cliente, placa, veículo ou #OS..."
                  value={busca}
                  onChange={handleMudarBusca}
                />
              </InputGroup>

              <Select
                value={statusFiltro}
                onValueChange={handleMudarStatus}
              >
                <SelectTrigger className="w-full sm:w-56 min-w-0">
                  <SelectValue placeholder="Filtrar por status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    <SelectItem value="todos">Todos os status</SelectItem>
                    <SelectItem value="PENDENTE">Pendente</SelectItem>
                    <SelectItem value="ACEITO">Aceito</SelectItem>
                    <SelectItem value="EM_ANDAMENTO">Em Andamento</SelectItem>
                    <SelectItem value="REJEITADO">Rejeitado</SelectItem>
                    <SelectItem value="FINALIZADO">Finalizado</SelectItem>
                  </SelectGroup>
                </SelectContent>
              </Select>

              {temFiltroAtivo && (
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={limparFiltros}
                  className="text-xs text-muted-foreground hover:text-foreground h-9 gap-1 shrink-0"
                >
                  <X className="size-3.5" />
                  Limpar filtros
                </Button>
              )}
            </div>

            <Badge variant="secondary" className="self-start lg:self-auto px-2.5 py-1 text-xs shrink-0">
              {filtrados.length} {filtrados.length === 1 ? 'orçamento' : 'orçamentos'}
            </Badge>
          </div>

          {carregando ? (
            <div className="flex flex-col items-center justify-center py-12 gap-3 text-muted-foreground">
              <Spinner className="size-6 text-primary" />
              <span className="text-sm">Carregando orçamentos...</span>
            </div>
          ) : filtrados.length === 0 ? (
            <EmptyState
              icon={FileText}
              title="Nenhum orçamento encontrado"
              description={
                orcamentos.length === 0 && statusFiltro === 'todos' && !busca
                  ? 'Crie o primeiro orçamento selecionando um cliente e veículo.'
                  : 'Ajuste os filtros de busca ou de status para encontrar o orçamento desejado.'
              }
              action={
                temFiltroAtivo ? (
                  <Button variant="outline" onClick={limparFiltros}>
                    <X data-icon="inline-start" />
                    Limpar filtros
                  </Button>
                ) : (
                  <Button nativeButton={false} render={<Link href="/app/orcamentos/novo" />}>
                    <Plus data-icon="inline-start" />
                    Criar orçamento
                  </Button>
                )
              }
            />
          ) : (
            <div
              tabIndex={0}
              role="region"
              aria-label="Tabela de orçamentos e ordens de serviço"
              className="w-full max-w-full overflow-x-auto rounded-md border border-border focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
            >
              <Table className="w-full min-w-[720px] md:min-w-full text-sm">
                <TableHeader>
                  <TableRow>
                    <TableHead className="w-20">OS</TableHead>
                    <TableHead>Cliente / Descrição</TableHead>
                    <TableHead>Veículo / Placa</TableHead>
                    <TableHead className="hidden md:table-cell">Data</TableHead>
                    <TableHead className="text-right hidden lg:table-cell">
                      <span className="inline-flex items-center gap-1">
                        <Wrench className="size-3.5 text-muted-foreground" />
                        Mão de Obra
                      </span>
                    </TableHead>
                    <TableHead className="text-right hidden lg:table-cell">
                      <span className="inline-flex items-center gap-1">
                        <Package className="size-3.5 text-muted-foreground" />
                        Peças
                      </span>
                    </TableHead>
                    <TableHead className="text-right hidden xl:table-cell">
                      <span className="inline-flex items-center gap-1">
                        <Percent className="size-3.5 text-muted-foreground" />
                        Desconto
                      </span>
                    </TableHead>
                    <TableHead className="text-right">Total Final</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="w-10"></TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filtrados.map((o) => {
                    const statusEfetivo = o.statusServico || o.status || 'PENDENTE'
                    const totalFinal = Number(o.valorTotal ?? o.total ?? 0)
                    const totalServicos = Number(o.totalServicos ?? 0)
                    const totalPecas = Number(o.totalPecas ?? 0)
                    const valorDesconto = Number(o.valorDesconto ?? 0)
                    const descontoKey = o.desconto || 'SEM_DESCONTO'

                    return (
                      <TableRow
                        key={o.id}
                        className="cursor-pointer hover:bg-muted/40 transition-colors"
                        onClick={() => router.push(`/app/orcamentos/${o.id}`)}
                      >
                        <TableCell className="font-mono font-semibold text-muted-foreground">
                          #{o.id}
                        </TableCell>
                        <TableCell>
                          <div className="flex flex-col">
                            <span className="font-medium text-foreground">
                              {o.clienteNome || o.nomeCliente || 'Cliente vinculado'}
                            </span>
                            {o.descricao && (
                              <span className="text-xs text-muted-foreground truncate max-w-xs">
                                {o.descricao}
                              </span>
                            )}
                          </div>
                        </TableCell>
                        <TableCell>
                          <div className="flex flex-col">
                            <span className="font-medium text-foreground">
                              {o.veiculoMarca || o.veiculoModelo
                                ? `${o.veiculoMarca || ''} ${o.veiculoModelo || ''}`.trim()
                                : 'Veículo'}
                            </span>
                            <span className="text-xs text-muted-foreground font-mono">
                              {o.placa || o.placaVeiculo || '—'}
                            </span>
                          </div>
                        </TableCell>
                        <TableCell className="hidden text-muted-foreground md:table-cell">
                          {o.dataOrcamento ? formatDate(o.dataOrcamento) : '—'}
                        </TableCell>
                        <TableCell className="text-right font-mono text-sm hidden lg:table-cell">
                          {formatCurrency(totalServicos)}
                        </TableCell>
                        <TableCell className="text-right font-mono text-sm hidden lg:table-cell">
                          {formatCurrency(totalPecas)}
                        </TableCell>
                        <TableCell className="text-right font-mono text-xs hidden xl:table-cell">
                          {valorDesconto > 0 ? (
                            <Badge variant="outline" className="font-mono text-success border-success/40 bg-success/10">
                              -{formatCurrency(valorDesconto)} ({DESCONTO_LABELS[descontoKey] || '0%'})
                            </Badge>
                          ) : (
                            <span className="text-muted-foreground">0%</span>
                          )}
                        </TableCell>
                        <TableCell className="text-right font-mono font-semibold text-foreground">
                          {formatCurrency(totalFinal)}
                        </TableCell>
                        <TableCell>
                          <OrcamentoStatusBadge status={statusEfetivo} />
                        </TableCell>
                        <TableCell>
                          <ChevronRight className="size-4 text-muted-foreground" />
                        </TableCell>
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
