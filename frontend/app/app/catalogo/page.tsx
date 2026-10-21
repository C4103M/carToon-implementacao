'use client'

import { useMemo, useState } from 'react'
import { Search, Plus, Edit, Package, Wrench, AlertCircle } from 'lucide-react'
import { useStore } from '@/lib/store'
import type { Servico, Peca } from '@/lib/types'
import { formatCurrency } from '@/lib/format'
import { PageHeader } from '@/components/page-header'
import { EmptyState } from '@/components/empty-state'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
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
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { toast } from 'sonner'

export default function CatalogoPage() {
  const {
    servicos,
    addServico,
    updateServico,
    pecas,
    addPeca,
    updatePeca,
  } = useStore()

  const [activeTab, setActiveTab] = useState('servicos')

  // --- Filtros de Busca ---
  const [buscaServico, setBuscaServico] = useState('')
  const [buscaPeca, setBuscaPeca] = useState('')

  // --- Modal de Serviço ---
  const [servicoModalAberto, setServicoModalAberto] = useState(false)
  const [editandoServico, setEditandoServico] = useState<Servico | null>(null)
  const [servicoNome, setServicoNome] = useState('')
  const [servicoValorBase, setServicoValorBase] = useState('')
  const [servicoAtivo, setServicoAtivo] = useState(true)
  const [servicoPecaIds, setServicoPecaIds] = useState<string[]>([])
  const [servicoErros, setServicoErros] = useState<Record<string, string>>({})

  // --- Modal de Peça ---
  const [pecaModalAberto, setPecaModalAberto] = useState(false)
  const [editandoPeca, setEditandoPeca] = useState<Peca | null>(null)
  const [pecaNome, setPecaNome] = useState('')
  const [pecaValor, setPecaValor] = useState('')
  const [pecaAtivo, setPecaAtivo] = useState(true)
  const [pecaErros, setPecaErros] = useState<Record<string, string>>({})

  // --- Computados: Serviços Filtrados ---
  const servicosFiltrados = useMemo(() => {
    const q = buscaServico.toLowerCase().trim()
    return servicos.filter((s) => s.nome.toLowerCase().includes(q))
  }, [servicos, buscaServico])

  // --- Computados: Peças Filtradas ---
  const pecasFiltradas = useMemo(() => {
    const q = buscaPeca.toLowerCase().trim()
    return pecas.filter((p) => p.nome.toLowerCase().includes(q))
  }, [pecas, buscaPeca])

  // Map para buscar nome das peças rapidamente
  const pecaMap = useMemo(() => {
    return new Map(pecas.map((p) => [p.id, p]))
  }, [pecas])

  // --- Handlers: Modal Serviço ---
  const abrirNovoServico = () => {
    setEditandoServico(null)
    setServicoNome('')
    setServicoValorBase('')
    setServicoAtivo(true)
    setServicoPecaIds([])
    setServicoErros({})
    setServicoModalAberto(true)
  }

  const abrirEditarServico = (servico: Servico) => {
    setEditandoServico(servico)
    setServicoNome(servico.nome)
    setServicoValorBase(servico.valorBase.toString())
    setServicoAtivo(servico.ativo)
    setServicoPecaIds(servico.pecaIds)
    setServicoErros({})
    setServicoModalAberto(true)
  }

  const handleSalvarServico = (e: React.FormEvent) => {
    e.preventDefault()

    const erros: Record<string, string> = {}
    if (!servicoNome.trim()) erros.nome = 'Nome do serviço é obrigatório.'
    const valor = Number.parseFloat(servicoValorBase)
    if (!servicoValorBase || Number.isNaN(valor) || valor < 0) {
      erros.valorBase = 'Valor base deve ser um número maior ou igual a zero.'
    }

    if (Object.keys(erros).length > 0) {
      setServicoErros(erros)
      return
    }

    const dados = {
      nome: servicoNome.trim(),
      valorBase: valor,
      pecaIds: servicoPecaIds,
      ativo: servicoAtivo,
    }

    if (editandoServico) {
      updateServico(editandoServico.id, dados)
      toast.success('Serviço atualizado com sucesso!')
    } else {
      addServico(dados)
      toast.success('Serviço criado com sucesso!')
    }

    setServicoModalAberto(false)
  }

  const toggleServicoPeca = (id: string) => {
    setServicoPecaIds((prev) =>
      prev.includes(id) ? prev.filter((pid) => pid !== id) : [...prev, id],
    )
  }

  // --- Handlers: Modal Peça ---
  const abrirNovaPeca = () => {
    setEditandoPeca(null)
    setPecaNome('')
    setPecaValor('')
    setPecaAtivo(true)
    setPecaErros({})
    setPecaModalAberto(true)
  }

  const abrirEditarPeca = (peca: Peca) => {
    setEditandoPeca(peca)
    setPecaNome(peca.nome)
    setPecaValor(peca.valor.toString())
    setPecaAtivo(peca.ativo)
    setPecaErros({})
    setPecaModalAberto(true)
  }

  const handleSalvarPeca = (e: React.FormEvent) => {
    e.preventDefault()

    const erros: Record<string, string> = {}
    if (!pecaNome.trim()) erros.nome = 'Nome da peça é obrigatório.'
    const valor = Number.parseFloat(pecaValor)
    if (!pecaValor || Number.isNaN(valor) || valor <= 0) {
      erros.valor = 'Valor deve ser um número positivo maior que zero.'
    }

    if (Object.keys(erros).length > 0) {
      setPecaErros(erros)
      return
    }

    const dados = {
      nome: pecaNome.trim(),
      valor,
      ativo: pecaAtivo,
    }

    if (editandoPeca) {
      updatePeca(editandoPeca.id, dados)
      toast.success('Peça atualizada com sucesso!')
    } else {
      addPeca(dados)
      toast.success('Peça cadastrada com sucesso!')
    }

    setPecaModalAberto(false)
  }

  return (
    <div>
      <PageHeader
        title="Catálogo"
        description="Mantenha a lista de peças e receitas de serviços da oficina."
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

              {servicosFiltrados.length === 0 ? (
                <EmptyState
                  icon={Wrench}
                  title="Nenhum serviço encontrado"
                  description="Cadastre um novo serviço ou altere os termos de busca."
                  action={
                    servicos.length === 0 ? (
                      <Button onClick={abrirNovoServico}>
                        <Plus data-icon="inline-start" />
                        Cadastrar serviço
                      </Button>
                    ) : undefined
                  }
                />
              ) : (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Serviço</TableHead>
                        <TableHead>Receita (Peças Vinculadas)</TableHead>
                        <TableHead className="text-right">Mão de Obra Base</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead className="text-right">Ações</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {servicosFiltrados.map((s) => (
                        <TableRow key={s.id}>
                          <TableCell className="font-medium">{s.nome}</TableCell>
                          <TableCell>
                            {s.pecaIds.length === 0 ? (
                              <span className="text-xs text-muted-foreground">
                                Apenas mão de obra
                              </span>
                            ) : (
                              <div className="flex flex-wrap gap-1.5 max-w-md">
                                {s.pecaIds.map((pid) => {
                                  const p = pecaMap.get(pid)
                                  return (
                                    <Badge
                                      key={pid}
                                      variant="secondary"
                                      className="text-[10px] font-normal"
                                    >
                                      {p ? p.nome : 'Peça desconhecida'}
                                    </Badge>
                                  )
                                })}
                              </div>
                            )}
                          </TableCell>
                          <TableCell className="text-right font-mono font-medium">
                            {formatCurrency(s.valorBase)}
                          </TableCell>
                          <TableCell>
                            <Badge
                              variant="outline"
                              className={
                                s.ativo
                                  ? 'bg-success/15 text-success border-success/40'
                                  : 'bg-muted text-muted-foreground border-border'
                              }
                            >
                              {s.ativo ? 'Ativo' : 'Inativo'}
                            </Badge>
                          </TableCell>
                          <TableCell className="text-right">
                            <Button
                              size="sm"
                              variant="ghost"
                              onClick={() => abrirEditarServico(s)}
                            >
                              <Edit className="size-3.5" />
                              Editar
                            </Button>
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
                  placeholder="Buscar peças..."
                  value={buscaPeca}
                  onChange={(e) => setBuscaPeca(e.target.value)}
                />
              </InputGroup>

              {pecasFiltradas.length === 0 ? (
                <EmptyState
                  icon={Package}
                  title="Nenhuma peça encontrada"
                  description="Cadastre uma nova peça ou altere os termos de busca."
                  action={
                    pecas.length === 0 ? (
                      <Button onClick={abrirNovaPeca}>
                        <Plus data-icon="inline-start" />
                        Cadastrar peça
                      </Button>
                    ) : undefined
                  }
                />
              ) : (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Peça</TableHead>
                        <TableHead className="text-right">Valor Unitário</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead className="text-right">Ações</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {pecasFiltradas.map((p) => (
                        <TableRow key={p.id}>
                          <TableCell className="font-medium">{p.nome}</TableCell>
                          <TableCell className="text-right font-mono font-medium">
                            {formatCurrency(p.valor)}
                          </TableCell>
                          <TableCell>
                            <Badge
                              variant="outline"
                              className={
                                p.ativo
                                  ? 'bg-success/15 text-success border-success/40'
                                  : 'bg-muted text-muted-foreground border-border'
                              }
                            >
                              {p.ativo ? 'Ativa' : 'Inativa'}
                            </Badge>
                          </TableCell>
                          <TableCell className="text-right">
                            <Button
                              size="sm"
                              variant="ghost"
                              onClick={() => abrirEditarPeca(p)}
                            >
                              <Edit className="size-3.5" />
                              Editar
                            </Button>
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
        <DialogContent className="sm:max-w-lg max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>
              {editandoServico ? 'Editar Serviço' : 'Novo Serviço'}
            </DialogTitle>
            <DialogDescription>
              Preencha os dados e selecione as peças que compõem a receita do serviço.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSalvarServico}>
            <div className="flex flex-col gap-5 py-4">
              <div className="flex flex-col gap-2">
                <Label htmlFor="srv-nome">Nome do Serviço</Label>
                <Input
                  id="srv-nome"
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
                <Label htmlFor="srv-valor-base">Mão de Obra Base (R$)</Label>
                <Input
                  id="srv-valor-base"
                  type="number"
                  step="0.01"
                  placeholder="0.00"
                  value={servicoValorBase}
                  onChange={(e) => {
                    setServicoValorBase(e.target.value)
                    if (servicoErros.valorBase) {
                      setServicoErros((prev) => {
                        const n = { ...prev }
                        delete n.valorBase
                        return n
                      })
                    }
                  }}
                  required
                />
                <span className="text-[11px] text-muted-foreground">
                  *Este valor será multiplicado pelo multiplicador (lambda) do veículo no
                  momento de gerar o orçamento. Peças avulsas podem ser cadastradas com valor de mão de obra zero.
                </span>
                {servicoErros.valorBase && (
                  <p className="text-xs text-destructive">{servicoErros.valorBase}</p>
                )}
              </div>

              {editandoServico && (
                <div className="flex items-center gap-3 rounded-lg border border-border p-3">
                  <input
                    type="checkbox"
                    id="srv-ativo"
                    checked={servicoAtivo}
                    onChange={(e) => setServicoAtivo(e.target.checked)}
                    className="size-4 rounded border-input"
                  />
                  <Label htmlFor="srv-ativo" className="cursor-pointer font-normal">
                    Serviço Ativo (indisponibiliza para novos orçamentos se desmarcado)
                  </Label>
                </div>
              )}

              {/* Checklist de Peças */}
              <div className="flex flex-col gap-2.5">
                <Label className="text-sm font-medium">Receita de Peças</Label>
                <span className="text-[11px] text-muted-foreground -mt-1.5">
                  Selecione as peças que são trocadas/usadas sempre neste serviço.
                </span>

                {pecas.filter((p) => p.ativo || servicoPecaIds.includes(p.id)).length === 0 ? (
                  <div className="rounded-lg border border-dashed border-border bg-muted/40 p-4 text-center text-xs text-muted-foreground flex items-center justify-center gap-1.5">
                    <AlertCircle className="size-4" />
                    Nenhuma peça ativa no catálogo para vincular.
                  </div>
                ) : (
                  <div className="max-h-48 overflow-y-auto border border-input rounded-md p-2.5 flex flex-col gap-2">
                    {pecas
                      .filter((p) => p.ativo || servicoPecaIds.includes(p.id))
                      .map((p) => (
                        <label
                          key={p.id}
                          className="flex items-center gap-2.5 py-1 text-xs cursor-pointer hover:bg-muted/40 px-1 rounded"
                        >
                          <input
                            type="checkbox"
                            checked={servicoPecaIds.includes(p.id)}
                            onChange={() => toggleServicoPeca(p.id)}
                            className="size-4 rounded border-input text-primary"
                          />
                          <div className="flex flex-1 justify-between gap-2">
                            <span className="font-medium text-foreground">{p.nome}</span>
                            <span className="font-mono text-muted-foreground">
                              {formatCurrency(p.valor)}
                            </span>
                          </div>
                        </label>
                      ))}
                  </div>
                )}
              </div>
            </div>

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => setServicoModalAberto(false)}
              >
                Cancelar
              </Button>
              <Button type="submit">Salvar Serviço</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* --- Dialog: Cadastro / Edição de Peça --- */}
      <Dialog open={pecaModalAberto} onOpenChange={setPecaModalAberto}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>
              {editandoPeca ? 'Editar Peça' : 'Nova Peça'}
            </DialogTitle>
            <DialogDescription>
              Forneça o nome e o valor de custo unitário padrão da peça no catálogo.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSalvarPeca}>
            <div className="flex flex-col gap-4 py-4">
              <div className="flex flex-col gap-2">
                <Label htmlFor="peca-nome">Nome da Peça</Label>
                <Input
                  id="peca-nome"
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
                <Label htmlFor="peca-valor">Valor Unitário (R$)</Label>
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

              {editandoPeca && (
                <div className="flex items-center gap-3 rounded-lg border border-border p-3">
                  <input
                    type="checkbox"
                    id="peca-ativo"
                    checked={pecaAtivo}
                    onChange={(e) => setPecaAtivo(e.target.checked)}
                    className="size-4 rounded border-input"
                  />
                  <Label htmlFor="peca-ativo" className="cursor-pointer font-normal">
                    Peça Ativa (indisponibiliza para novas receitas se desmarcado)
                  </Label>
                </div>
              )}
            </div>

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => setPecaModalAberto(false)}
              >
                Cancelar
              </Button>
              <Button type="submit">Salvar Peça</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  )
}
