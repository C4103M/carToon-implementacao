'use client'

import { useState, useMemo, useEffect, useCallback } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { toast } from 'sonner'
import {
  Car,
  ArrowRight,
  AlertTriangle,
  Percent,
  Wrench,
} from 'lucide-react'
import {
  clientesService,
  veiculosService,
  usuariosService,
  ordemServicoService,
} from '@/lib/services'
import { handleError, normalizeError } from '@/lib/error-handler'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { AlertCircle } from 'lucide-react'
import type {
  ClienteResponse,
  VeiculoResponse,
  UsuarioResponse,
  Desconto,
} from '@/lib/types'
import { formatCurrency, formatCpf } from '@/lib/format'
import { PageHeader } from '@/components/page-header'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Spinner } from '@/components/ui/spinner'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Label } from '@/components/ui/label'

export default function NovoOrcamentoPage() {
  const router = useRouter()

  const [clientes, setClientes] = useState<ClienteResponse[]>([])
  const [carregandoClientes, setCarregandoClientes] = useState(true)

  const [veiculosDoCliente, setVeiculosDoCliente] = useState<VeiculoResponse[]>([])
  const [carregandoVeiculos, setCarregandoVeiculos] = useState(false)

  const [mecanicos, setMecanicos] = useState<UsuarioResponse[]>([])

  const [clienteId, setClienteId] = useState('')
  const [veiculoId, setVeiculoId] = useState('')
  const [mecanicoId, setMecanicoId] = useState('')
  const [descricao, setDescricao] = useState('')
  const [desconto, setDesconto] = useState<Desconto>('SEM_DESCONTO')
  const [salvando, setSalvando] = useState(false)
  const [erroSubmit, setErroSubmit] = useState<string | null>(null)

  const carregarDadosIniciais = useCallback(async () => {
    setCarregandoClientes(true)
    try {
      const [listaClientes, listaUsuarios] = await Promise.all([
        clientesService.listar(),
        usuariosService.listar().catch(() => [] as UsuarioResponse[]),
      ])
      setClientes(listaClientes || [])
      const ativos = (listaUsuarios || []).filter(
        (u) => u.ativo && u.role === 'MECANICO'
      )
      setMecanicos(ativos)
      if (ativos.length > 0) {
        setMecanicoId(String(ativos[0].id))
      }
    } catch (err) {
      toast.error('Erro ao carregar lista de clientes.')
    } finally {
      setCarregandoClientes(false)
    }
  }, [])

  useEffect(() => {
    carregarDadosIniciais()
  }, [carregarDadosIniciais])

  const handleClienteChange = async (val: string | null) => {
    const id = val ?? ''
    setClienteId(id)
    setVeiculoId('')
    setVeiculosDoCliente([])

    if (!id) return

    setCarregandoVeiculos(true)
    try {
      const lista = await veiculosService.listarPorCliente(Number(id))
      const ativos = (lista || []).filter((v) => v.ativo !== false)
      setVeiculosDoCliente(ativos)
      if (ativos.length === 1) {
        setVeiculoId(String(ativos[0].id))
      }
    } catch (err) {
      toast.error('Erro ao carregar os veículos deste cliente.')
    } finally {
      setCarregandoVeiculos(false)
    }
  }

  const clienteSelecionado = useMemo(() => {
    return clientes.find((c) => String(c.id) === clienteId)
  }, [clientes, clienteId])

  const veiculoSelecionado = useMemo(() => {
    return veiculosDoCliente.find((v) => String(v.id) === veiculoId)
  }, [veiculosDoCliente, veiculoId])

  const mecanicoSelecionado = useMemo(() => {
    return mecanicos.find((m) => String(m.id) === mecanicoId)
  }, [mecanicos, mecanicoId])

  const DESCONTO_LABELS: Record<Desconto, string> = {
    SEM_DESCONTO: 'Sem desconto (0%)',
    CINCO_PORCENTO: '5% de desconto',
    DEZ_PORCENTO: '10% de desconto',
    QUINZE_PORCENTO: '15% de desconto',
  }

  const handleCriarOrcamento = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!clienteId || !veiculoId || !mecanicoId) {
      toast.error('Selecione o cliente, o veículo e o mecânico responsável.')
      return
    }

    setErroSubmit(null)
    setSalvando(true)
    try {
      const criada = await ordemServicoService.criar({
        veiculoId: Number(veiculoId),
        mecanicoId: Number(mecanicoId),
        descricao: descricao.trim() || 'Orçamento de manutenção',
        desconto,
      })
      toast.success('Orçamento iniciado com sucesso!')
      router.push(`/app/orcamentos/${criada.id}`)
    } catch (err) {
      const norm = handleError(err, { fallbackMessage: 'Erro ao iniciar orçamento.' })
      setErroSubmit(norm.message)
    } finally {
      setSalvando(false)
    }
  }

  return (
    <div className="mx-auto max-w-2xl w-full min-w-0">
      <PageHeader
        title="Iniciar Novo Orçamento"
        description="Selecione o cliente, o veículo ativo e configure os dados iniciais da Ordem de Serviço."
        backHref="/app/orcamentos"
        backLabel="Voltar para orçamentos"
      />

      <Card className="w-full min-w-0 overflow-hidden">
        <CardContent className="pt-6 w-full min-w-0">
          <form onSubmit={handleCriarOrcamento} className="flex flex-col gap-6 w-full min-w-0">
            {erroSubmit && (
              <Alert variant="destructive">
                <AlertCircle className="size-4" />
                <AlertDescription className="flex items-center justify-between gap-2">
                  <span>{erroSubmit}</span>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => setErroSubmit(null)}
                    className="h-6 text-xs px-2 hover:bg-destructive/20"
                  >
                    Fechar
                  </Button>
                </AlertDescription>
              </Alert>
            )}
            {/* Passo 1: Selecionar Cliente */}
            <div className="flex flex-col gap-2">
              <div className="flex items-center justify-between">
                <Label htmlFor="cliente-select" className="text-sm font-medium">
                  1. Selecionar Cliente
                </Label>
                <Link
                  href="/app/clientes/novo"
                  className="text-xs text-primary hover:underline font-medium"
                >
                  + Cadastrar novo cliente
                </Link>
              </div>
              <Select
                value={clienteId}
                onValueChange={handleClienteChange}
                disabled={carregandoClientes || salvando}
              >
                <SelectTrigger id="cliente-select" className="w-full">
                  <SelectValue
                    placeholder={
                      carregandoClientes
                        ? 'Carregando clientes...'
                        : 'Selecione um cliente...'
                    }
                  >
                    {clienteSelecionado
                      ? clienteSelecionado.nome
                      : carregandoClientes
                        ? 'Carregando clientes...'
                        : 'Selecione um cliente...'}
                  </SelectValue>
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    {clientes.map((c) => (
                      <SelectItem key={c.id} value={String(c.id)}>
                        {c.nome}
                      </SelectItem>
                    ))}
                  </SelectGroup>
                </SelectContent>
              </Select>
            </div>

            {/* Passo 2: Selecionar Veículo */}
            {clienteId && (
              <div className="flex flex-col gap-2">
                <div className="flex items-center justify-between">
                  <Label htmlFor="veiculo-select" className="text-sm font-medium">
                    2. Selecionar Veículo Ativo
                  </Label>
                  <Link
                    href={`/app/clientes/${clienteId}?novoVeiculo=true`}
                    className="text-xs text-primary hover:underline font-medium"
                  >
                    + Adicionar veículo ao cliente
                  </Link>
                </div>

                {carregandoVeiculos ? (
                  <div className="flex items-center gap-2 py-3 text-sm text-muted-foreground">
                    <Spinner className="size-4" />
                    <span>Buscando veículos do cliente...</span>
                  </div>
                ) : veiculosDoCliente.length === 0 ? (
                  <div className="flex flex-col gap-3 rounded-lg border border-warning/30 bg-warning/10 p-4 text-warning-foreground text-sm">
                    <div className="flex gap-2">
                      <AlertTriangle className="size-5 shrink-0 mt-0.5" />
                      <div className="flex flex-col gap-1">
                        <span className="font-semibold">Nenhum veículo ativo</span>
                        <span>
                          Este cliente não possui veículos ativos cadastrados no momento. É
                          necessário cadastrar um veículo com consulta FIPE para gerar o orçamento.
                        </span>
                      </div>
                    </div>
                    <Button
                      size="sm"
                      className="w-fit"
                      nativeButton={false}
                      render={<Link href={`/app/clientes/${clienteId}?novoVeiculo=true`} />}
                    >
                      Cadastrar Veículo do Cliente
                    </Button>
                  </div>
                ) : (
                  <>
                    <Select
                      value={veiculoId}
                      onValueChange={(val) => setVeiculoId(val ?? '')}
                      disabled={salvando}
                    >
                      <SelectTrigger id="veiculo-select" className="w-full">
                        <SelectValue placeholder="Selecione um veículo ativo...">
                          {veiculoSelecionado
                            ? `${veiculoSelecionado.marca} ${veiculoSelecionado.modelo} (${veiculoSelecionado.anoModelo})`
                            : 'Selecione um veículo ativo...'}
                        </SelectValue>
                      </SelectTrigger>
                      <SelectContent>
                        <SelectGroup>
                          {veiculosDoCliente.map((v) => (
                            <SelectItem key={v.id} value={String(v.id)}>
                              {v.marca} {v.modelo} ({v.anoModelo})
                            </SelectItem>
                          ))}
                        </SelectGroup>
                      </SelectContent>
                    </Select>

                    {veiculoSelecionado && (
                      <div className="mt-2 rounded-lg border border-border bg-muted/30 p-4 text-sm flex flex-col gap-2.5">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <Car className="size-4 text-primary" />
                            <span className="font-medium text-foreground">
                              {veiculoSelecionado.marca} {veiculoSelecionado.modelo} (
                              {veiculoSelecionado.anoModelo})
                            </span>
                          </div>
                          <span className="font-mono text-xs bg-muted px-2 py-0.5 rounded border">
                            {veiculoSelecionado.placa}
                          </span>
                        </div>
                        <div className="grid grid-cols-2 gap-4 pt-1">
                          <div>
                            <span className="text-xs text-muted-foreground uppercase">
                              Valor FIPE Cadastrado
                            </span>
                            <p className="font-mono font-semibold text-success mt-0.5">
                              {veiculoSelecionado.valorFipe != null
                                ? formatCurrency(Number(veiculoSelecionado.valorFipe))
                                : 'Não informado'}
                            </p>
                          </div>
                          <div>
                            <span className="text-xs text-muted-foreground uppercase">
                              Código FIPE
                            </span>
                            <p className="font-mono font-semibold text-primary mt-0.5">
                              {veiculoSelecionado.codigoFipe || '—'}
                            </p>
                          </div>
                        </div>
                      </div>
                    )}
                  </>
                )}
              </div>
            )}

            {/* Passo 3: Mecânico Responsável e Desconto Inicial */}
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="flex flex-col gap-2">
                <Label htmlFor="mecanico-select" className="text-sm font-medium flex items-center gap-1.5">
                  <Wrench className="size-3.5 text-muted-foreground" />
                  3. Responsável Técnico
                </Label>
                <Select
                  value={mecanicoId}
                  onValueChange={(val) => setMecanicoId(val ?? '')}
                  disabled={salvando || mecanicos.length === 0}
                >
                  <SelectTrigger id="mecanico-select" className="w-full">
                    <SelectValue placeholder="Selecione o responsável técnico...">
                      {mecanicoSelecionado
                        ? mecanicoSelecionado.nome
                        : 'Selecione o responsável técnico...'}
                    </SelectValue>
                  </SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      {mecanicos.map((m) => (
                        <SelectItem key={m.id} value={String(m.id)}>
                          {m.nome}
                        </SelectItem>
                      ))}
                    </SelectGroup>
                  </SelectContent>
                </Select>
              </div>

              <div className="flex flex-col gap-2">
                <Label htmlFor="desconto-select" className="text-sm font-medium flex items-center gap-1.5">
                  <Percent className="size-3.5 text-muted-foreground" />
                  4. Desconto Inicial (Opcional)
                </Label>
                <Select
                  value={desconto}
                  onValueChange={(val) => setDesconto((val as Desconto) || 'SEM_DESCONTO')}
                  disabled={salvando}
                >
                  <SelectTrigger id="desconto-select" className="w-full">
                    <SelectValue placeholder="Sem desconto (0%)">
                      {DESCONTO_LABELS[desconto] || 'Sem desconto (0%)'}
                    </SelectValue>
                  </SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      <SelectItem value="SEM_DESCONTO">Sem desconto (0%)</SelectItem>
                      <SelectItem value="CINCO_PORCENTO">5% de desconto</SelectItem>
                      <SelectItem value="DEZ_PORCENTO">10% de desconto</SelectItem>
                      <SelectItem value="QUINZE_PORCENTO">15% de desconto</SelectItem>
                    </SelectGroup>
                  </SelectContent>
                </Select>
              </div>
            </div>

            {/* Passo 5: Descrição / Observações */}
            <div className="flex flex-col gap-2">
              <Label htmlFor="descricao-os" className="text-sm font-medium">
                5. Descrição / Relato do Problema
              </Label>
              <Textarea
                id="descricao-os"
                placeholder="Ex: Revisão de 40.000 km, barulho na suspensão dianteira e troca de óleo..."
                rows={3}
                value={descricao}
                onChange={(e) => setDescricao(e.target.value)}
                disabled={salvando}
              />
            </div>

            <div className="flex justify-end gap-3 pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => router.push('/app/orcamentos')}
                disabled={salvando}
              >
                Cancelar
              </Button>
              <Button
                type="submit"
                disabled={!clienteId || !veiculoId || salvando}
              >
                {salvando && <Spinner className="size-4 mr-2" />}
                Iniciar Orçamento
                <ArrowRight data-icon="inline-end" />
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
