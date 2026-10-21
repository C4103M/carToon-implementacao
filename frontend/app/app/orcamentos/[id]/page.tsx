'use client'

import { use, useState, useMemo } from 'react'
import { useRouter, notFound } from 'next/navigation'
import Link from 'next/link'
import { toast } from 'sonner'
import {
  FileText,
  User,
  Car,
  Calendar,
  CheckCircle2,
  XCircle,
  Play,
  Trash2,
  Plus,
  Share2,
  Copy,
  Info,
} from 'lucide-react'
import { useStore } from '@/lib/store'
import type { OrcamentoStatus, Servico } from '@/lib/types'
import { formatCurrency, formatDate } from '@/lib/format'
import { PageHeader } from '@/components/page-header'
import { OrcamentoStatusBadge } from '@/components/status-badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Label } from '@/components/ui/label'
import { Input } from '@/components/ui/input'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
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

export default function OrcamentoDetalhePage({
  params,
}: {
  params: Promise<{ id: string }>
}) {
  const { id } = use(params)
  const {
    orcamentos,
    clientes,
    veiculos,
    servicos,
    pecas,
    addOrcamentoItem,
    removeOrcamentoItem,
    setOrcamentoStatus,
  } = useStore()
  const router = useRouter()

  const orcamento = orcamentos.find((o) => o.id === id)
  if (!orcamento) notFound()

  const cliente = clientes.find((c) => c.id === orcamento.clienteId)
  const veiculo = veiculos.find((v) => v.id === orcamento.veiculoId)

  // --- Estados do Formulário de Adicionar Item ---
  const [selecionadoServicoId, setSelecionadoServicoId] = useState('')
  const [quantidade, setQuantidade] = useState('1')
  const [erroItem, setErroItem] = useState<string | null>(null)

  // --- Estado de Confirmação de Transição de Status ---
  const [statusConfirmar, setStatusConfirmar] = useState<OrcamentoStatus | null>(null)

  // --- Computados ---
  const servicosAtivos = useMemo(() => {
    return servicos.filter((s) => s.ativo)
  }, [servicos])

  const servicoSelecionado = useMemo(() => {
    return servicos.find((s) => s.id === selecionadoServicoId)
  }, [servicos, selecionadoServicoId])

  const precoUnitarioPrevisto = useMemo(() => {
    if (!servicoSelecionado || !veiculo) return 0
    return Math.round(servicoSelecionado.valorBase * veiculo.lambda * 100) / 100
  }, [servicoSelecionado, veiculo])

  const totalGeral = useMemo(() => {
    return orcamento.itens.reduce((sum, item) => sum + item.valorUnitario * item.quantidade, 0)
  }, [orcamento.itens])

  const pecaMap = useMemo(() => new Map(pecas.map((p) => [p.id, p])), [pecas])
  const servicoMap = useMemo(() => new Map(servicos.map((s) => [s.id, s])), [servicos])

  // --- Handlers de Ações ---
  const handleAdicionarItem = (e: React.FormEvent) => {
    e.preventDefault()
    setErroItem(null)

    if (!selecionadoServicoId) {
      setErroItem('Selecione um serviço.')
      return
    }

    const qty = Number.parseInt(quantidade, 10)
    if (Number.isNaN(qty) || qty <= 0) {
      setErroItem('A quantidade deve ser um número maior que zero.')
      return
    }

    if (!servicoSelecionado || !veiculo) return

    addOrcamentoItem(orcamento.id, servicoSelecionado, qty, veiculo.lambda)
    toast.success('Item adicionado ao orçamento!')

    // Limpa campos
    setSelecionadoServicoId('')
    setQuantidade('1')
  }

  const handleRemoverItem = (itemId: string) => {
    removeOrcamentoItem(orcamento.id, itemId)
    toast.success('Item removido do orçamento.')
  }

  const confirmarStatusChange = (status: OrcamentoStatus) => {
    setStatusConfirmar(status)
  }

  const executarStatusChange = () => {
    if (!statusConfirmar) return
    setOrcamentoStatus(orcamento.id, statusConfirmar)

    let msg = ''
    if (statusConfirmar === 'aceito') msg = 'Orçamento marcado como ACEITO.'
    if (statusConfirmar === 'rejeitado') msg = 'Orçamento marcado como REJEITADO.'
    if (statusConfirmar === 'finalizado') msg = 'Orçamento finalizado com sucesso.'

    toast.success(msg)
    setStatusConfirmar(null)
  }

  const copiarResumoWhatsApp = () => {
    if (!cliente || !veiculo) return

    let itensTexto = ''
    orcamento.itens.forEach((item, index) => {
      const valorTotalItem = item.valorUnitario * item.quantidade
      itensTexto += `\n*${index + 1}. ${item.servicoNome}*\n`
      itensTexto += `   Qtd: ${item.quantidade} × ${formatCurrency(item.valorUnitario)} = *${formatCurrency(valorTotalItem)}*\n`

      // Lista as peças vinculadas para transparência
      const s = servicoMap.get(item.servicoId)
      if (s && s.pecaIds.length > 0) {
        const pecasNomes = s.pecaIds
          .map((pid) => pecaMap.get(pid)?.nome)
          .filter(Boolean)
          .join(', ')
        if (pecasNomes) {
          itensTexto += `   _(Peças inclusas: ${pecasNomes})_\n`
        }
      }
    })

    const statusTraduzido = {
      pendente: 'PENDENTE DE DECISÃO',
      aceito: 'ACEITO (EM EXECUÇÃO)',
      rejeitado: 'REJEITADO',
      finalizado: 'CONCLUÍDO',
    }[orcamento.status]

    const texto = `🛠️ *ORÇAMENTO - OFICINAPRO* 🛠️
-----------------------------------
*Status:* ${statusTraduzido}
*Data:* ${formatDate(orcamento.data)}
*Cliente:* ${cliente.nome}
*Veículo:* ${veiculo.montadora} ${veiculo.modelo}
*Placa:* ${veiculo.placa} (FIPE: ${formatCurrency(veiculo.fipeValor)})
-----------------------------------
*ITENS DO ORÇAMENTO:*
${itensTexto}
-----------------------------------
*VALOR TOTAL:* *${formatCurrency(totalGeral)}*
-----------------------------------
Ficamos no aguardo da sua aprovação! Se tiver dúvidas, entre em contato.`

    navigator.clipboard.writeText(texto)
    toast.success('Resumo do orçamento copiado para a área de transferência!')
  }

  return (
    <div>
      <PageHeader
        title={`Orçamento #${orcamento.id.toUpperCase().slice(-5)}`}
        description="Visualize e edite os itens deste orçamento."
        backHref="/app/orcamentos"
        backLabel="Todos os orçamentos"
        actions={<OrcamentoStatusBadge status={orcamento.status} />}
      />

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Painel do Cliente e Veículo */}
        <div className="flex flex-col gap-6 lg:col-span-1">
          <Card>
            <CardHeader>
              <CardTitle className="text-base font-semibold">Dados Básicos</CardTitle>
            </CardHeader>
            <CardContent className="flex flex-col gap-4">
              {cliente && (
                <div>
                  <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                    Cliente
                  </span>
                  <div className="flex items-center gap-2 mt-1">
                    <User className="size-4 text-muted-foreground" />
                    <Link
                      href={`/app/clientes/${cliente.id}`}
                      className="font-medium text-sm text-primary hover:underline"
                    >
                      {cliente.nome}
                    </Link>
                  </div>
                </div>
              )}

              <Separator />

              {veiculo && (
                <div>
                  <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                    Veículo
                  </span>
                  <div className="flex items-center gap-2 mt-1">
                    <Car className="size-4 text-muted-foreground" />
                    <Link
                      href={`/app/veiculos/${veiculo.id}/historico`}
                      className="font-medium text-sm text-primary hover:underline"
                    >
                      {veiculo.montadora} {veiculo.modelo}
                    </Link>
                  </div>
                  <div className="mt-1.5 flex flex-col gap-1 text-xs text-muted-foreground font-mono">
                    <span>Placa: {veiculo.placa}</span>
                    <span>FIPE: {formatCurrency(veiculo.fipeValor)}</span>
                    <span>Multiplicador (Lambda): {veiculo.lambda.toFixed(2)}x</span>
                  </div>
                  <div className="mt-3 flex items-center gap-1.5 rounded-md bg-muted/60 border border-border p-2 text-[10px] text-muted-foreground">
                    <Info className="size-3.5 text-primary shrink-0" />
                    <span>
                      O valor FIPE e o multiplicador foram congelados no momento do cadastro
                      deste veículo.
                    </span>
                  </div>
                </div>
              )}

              <Separator />

              <div>
                <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                  Data de Criação
                </span>
                <div className="flex items-center gap-2 mt-1 text-sm">
                  <Calendar className="size-4 text-muted-foreground" />
                  <span>{formatDate(orcamento.data)}</span>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Card de Controle de Status */}
          <Card>
            <CardHeader>
              <CardTitle className="text-base font-semibold">Status do Orçamento</CardTitle>
              <CardDescription>
                Altere o status conforme o andamento da negociação.
              </CardDescription>
            </CardHeader>
            <CardContent className="flex flex-col gap-3">
              {orcamento.status === 'pendente' && (
                <>
                  <Button
                    onClick={() => confirmarStatusChange('aceito')}
                    className="w-full bg-success text-white hover:bg-success/95"
                  >
                    <CheckCircle2 data-icon="inline-start" />
                    Aprovar (Marcar como Aceito)
                  </Button>
                  <Button
                    variant="destructive"
                    onClick={() => confirmarStatusChange('rejeitado')}
                    className="w-full"
                  >
                    <XCircle data-icon="inline-start" />
                    Rejeitar Orçamento
                  </Button>
                </>
              )}

              {orcamento.status === 'aceito' && (
                <Button
                  onClick={() => confirmarStatusChange('finalizado')}
                  className="w-full bg-info text-white hover:bg-info/95"
                >
                  <CheckCircle2 data-icon="inline-start" />
                  Finalizar Serviço
                </Button>
              )}

              {orcamento.status === 'rejeitado' && (
                <div className="rounded-md bg-destructive/10 border border-destructive/30 p-3 text-center text-xs text-destructive font-medium">
                  Este orçamento foi REJEITADO pelo cliente e está congelado.
                </div>
              )}

              {orcamento.status === 'finalizado' && (
                <div className="rounded-md bg-info/10 border border-info/30 p-3 text-center text-xs text-info font-medium">
                  Este orçamento foi FINALIZADO e concluído.
                </div>
              )}

              <Button
                variant="outline"
                onClick={copiarResumoWhatsApp}
                className="w-full mt-2"
              >
                <Share2 data-icon="inline-start" />
                Copiar para WhatsApp
              </Button>
            </CardContent>
          </Card>
        </div>

        {/* Detalhamento dos Itens */}
        <div className="flex flex-col gap-6 lg:col-span-2">
          {/* Adicionar Novo Item se Pendente */}
          {orcamento.status === 'pendente' && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base font-semibold">Adicionar Item ao Orçamento</CardTitle>
                <CardDescription>
                  Selecione um serviço ativo do catálogo. O preço da mão de obra será
                  multiplicado pelo lambda do veículo.
                </CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleAdicionarItem} className="flex flex-col gap-4">
                  {erroItem && <div className="text-xs text-destructive">{erroItem}</div>}
                  <div className="grid gap-4 sm:grid-cols-3">
                    <div className="flex flex-col gap-1.5 sm:col-span-2">
                      <Label htmlFor="item-select">Serviço</Label>
                      <Select
                        value={selecionadoServicoId}
                        onValueChange={(v) => {
                          setSelecionadoServicoId(v ?? '')
                          setErroItem(null)
                        }}
                      >
                        <SelectTrigger id="item-select" className="w-full">
                          <SelectValue placeholder="Selecione um serviço..." />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectGroup>
                            {servicosAtivos.map((s) => (
                              <SelectItem key={s.id} value={s.id}>
                                {s.nome} (Mão de Obra Base: {formatCurrency(s.valorBase)})
                              </SelectItem>
                            ))}
                          </SelectGroup>
                        </SelectContent>
                      </Select>
                    </div>

                    <div className="flex flex-col gap-1.5 sm:col-span-1">
                      <Label htmlFor="item-qtd">Quantidade</Label>
                      <Input
                        id="item-qtd"
                        type="number"
                        min="1"
                        value={quantidade}
                        onChange={(e) => {
                          setQuantidade(e.target.value)
                          setErroItem(null)
                        }}
                        required
                      />
                    </div>
                  </div>

                  {/* Visualização de Prévia de Preço */}
                  {servicoSelecionado && veiculo && (
                    <div className="rounded-lg border border-border bg-muted/40 p-3.5 text-xs flex flex-col gap-2">
                      <div className="flex justify-between items-center">
                        <span className="text-muted-foreground font-medium">
                          Mão de Obra Ajustada ({veiculo.lambda.toFixed(2)}x):
                        </span>
                        <span className="font-semibold font-mono text-sm">
                          {formatCurrency(precoUnitarioPrevisto)}
                        </span>
                      </div>
                      {servicoSelecionado.pecaIds.length > 0 && (
                        <div className="text-muted-foreground flex flex-col gap-1">
                          <span className="font-medium text-[10px] uppercase">
                            Peças embutidas neste serviço:
                          </span>
                          <div className="flex flex-wrap gap-1">
                            {servicoSelecionado.pecaIds.map((pid) => {
                              const p = pecaMap.get(pid)
                              return p ? (
                                <Badge
                                  key={pid}
                                  variant="secondary"
                                  className="text-[9px] font-normal"
                                >
                                  {p.nome}
                                </Badge>
                              ) : null
                            })}
                          </div>
                        </div>
                      )}
                    </div>
                  )}

                  <div className="flex justify-end pt-2">
                    <Button type="submit">
                      <Plus data-icon="inline-start" />
                      Adicionar Item
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          )}

          {/* Tabela de Itens Adicionados */}
          <Card>
            <CardHeader className="flex-row items-center justify-between">
              <div>
                <CardTitle className="text-base font-semibold">Itens Adicionados</CardTitle>
                <CardDescription>
                  Resumo detalhado dos serviços e mão de obra contratada.
                </CardDescription>
              </div>
              <div className="flex flex-col items-end">
                <span className="text-[10px] text-muted-foreground uppercase tracking-wide">
                  Valor Total
                </span>
                <span className="text-xl font-bold font-mono text-success">
                  {formatCurrency(totalGeral)}
                </span>
              </div>
            </CardHeader>
            <CardContent>
              {orcamento.itens.length === 0 ? (
                <div className="py-12 text-center text-muted-foreground text-sm border border-dashed rounded-lg border-border">
                  Nenhum item adicionado a este orçamento. Adicione serviços acima.
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Serviço / Peças Inclusas</TableHead>
                        <TableHead className="text-right">Mão de Obra Base</TableHead>
                        <TableHead className="text-right">Preço Unitário (c/ Lambda)</TableHead>
                        <TableHead className="text-center w-16">Qtd</TableHead>
                        <TableHead className="text-right">Total Item</TableHead>
                        {orcamento.status === 'pendente' && <TableHead className="w-10"></TableHead>}
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {orcamento.itens.map((item) => {
                        const originalServico = servicoMap.get(item.servicoId)
                        const totalItem = item.valorUnitario * item.quantidade

                        return (
                          <TableRow key={item.id}>
                            <TableCell>
                              <div className="flex flex-col gap-1">
                                <span className="font-medium">{item.servicoNome}</span>
                                {originalServico && originalServico.pecaIds.length > 0 && (
                                  <div className="flex flex-wrap gap-1">
                                    {originalServico.pecaIds.map((pid) => {
                                      const p = pecaMap.get(pid)
                                      return p ? (
                                        <Badge
                                          key={pid}
                                          variant="secondary"
                                          className="text-[9px] font-normal"
                                        >
                                          {p.nome}
                                        </Badge>
                                      ) : null
                                    })}
                                  </div>
                                )}
                              </div>
                            </TableCell>
                            <TableCell className="text-right font-mono text-xs text-muted-foreground">
                              {formatCurrency(item.valorBaseServico)}
                            </TableCell>
                            <TableCell className="text-right font-mono text-sm">
                              {formatCurrency(item.valorUnitario)}
                            </TableCell>
                            <TableCell className="text-center font-medium">
                              {item.quantidade}
                            </TableCell>
                            <TableCell className="text-right font-mono font-semibold text-foreground">
                              {formatCurrency(totalItem)}
                            </TableCell>
                            {orcamento.status === 'pendente' && (
                              <TableCell className="text-right">
                                <Button
                                  size="sm"
                                  variant="ghost"
                                  className="text-destructive hover:bg-destructive/10"
                                  onClick={() => handleRemoverItem(item.id)}
                                  title="Remover item"
                                >
                                  <Trash2 className="size-3.5" />
                                </Button>
                              </TableCell>
                            )}
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
      </div>

      {/* AlertDialog para Confirmação de Alteração de Status */}
      <AlertDialog
        open={statusConfirmar !== null}
        onOpenChange={(aberto) => !aberto && setStatusConfirmar(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Alterar status do orçamento?</AlertDialogTitle>
            <AlertDialogDescription>
              {statusConfirmar === 'aceito' && (
                <span>
                  O orçamento será marcado como <strong>ACEITO</strong>. O catálogo de itens
                  ficará travado para novas alterações e não poderá ser editado.
                </span>
              )}
              {statusConfirmar === 'rejeitado' && (
                <span>
                  O orçamento será marcado como <strong>REJEITADO</strong>. Esta ação é
                  irreversível e travará o orçamento como somente-leitura.
                </span>
              )}
              {statusConfirmar === 'finalizado' && (
                <span>
                  O serviço será finalizado e o orçamento será marcado como{' '}
                  <strong>CONCLUÍDO</strong>.
                </span>
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancelar</AlertDialogCancel>
            <AlertDialogAction onClick={executarStatusChange}>
              Confirmar Alteração
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
