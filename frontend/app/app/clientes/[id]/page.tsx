'use client'

import { use, useState, useMemo, useEffect, useCallback, Suspense } from 'react'
import { useRouter, useSearchParams, notFound } from 'next/navigation'
import { toast } from 'sonner'
import {
  Car,
  Plus,
  ArrowRightLeft,
  History,
  AlertTriangle,
  RefreshCw,
  Power,
  RotateCcw,
} from 'lucide-react'
import { clientesService } from '@/lib/services/clientes.service'
import { veiculosService } from '@/lib/services/veiculos.service'
import { formatCpf, isValidCpf, formatPlaca, formatCurrency } from '@/lib/format'
import { computeLambda } from '@/lib/fipe'
import { fipeService, type FipeItemDTO } from '@/lib/services/fipe.service'
import { normalizeError } from '@/lib/error-handler'
import type { ClienteResponse, VeiculoResponse } from '@/lib/types'
import { PageHeader } from '@/components/page-header'
import { VeiculoStatusBadge } from '@/components/status-badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { SearchableSelect } from '@/components/ui/searchable-select'
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
} from '@/components/ui/card'
import {
  Field,
  FieldGroup,
  FieldLabel,
  FieldDescription,
} from '@/components/ui/field'
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
import { Spinner } from '@/components/ui/spinner'

export default function ClienteDetalhePage({
  params,
}: {
  params: Promise<{ id: string }>
}) {
  return (
    <Suspense
      fallback={
        <div className="flex min-h-[300px] items-center justify-center">
          <Spinner />
        </div>
      }
    >
      <ClienteDetalheContent params={params} />
    </Suspense>
  )
}

function ClienteDetalheContent({
  params,
}: {
  params: Promise<{ id: string }>
}) {
  const { id } = use(params)
  const router = useRouter()
  const searchParams = useSearchParams()

  const numericId = Number(id)
  const [cliente, setCliente] = useState<ClienteResponse | null>(null)
  const [veiculos, setVeiculos] = useState<VeiculoResponse[]>([])
  const [carregandoGeral, setCarregandoGeral] = useState(true)
  const [salvandoCliente, setSalvandoCliente] = useState(false)

  // --- Estados do Formulário do Cliente ---
  const [nome, setNome] = useState('')
  const [cpf, setCpf] = useState('')
  const [endereco, setEndereco] = useState('')
  const [telefone, setTelefone] = useState('')

  const [nomeErro, setNomeErro] = useState(false)
  const [cpfErro, setCpfErro] = useState<string | null>(null)
  const [enderecoErro, setEnderecoErro] = useState(false)

  // --- Estados de Veículos ---
  const [verAntigos, setVerAntigos] = useState(false)

  // --- Estados do Modal de Veículo com FIPE ---
  const [modalAberto, setModalAberto] = useState(false)
  const [modalMode, setModalMode] = useState<'add' | 'swap'>('add')
  const [veiculoAntigoId, setVeiculoAntigoId] = useState<number | null>(null)

  // Cascata FIPE
  const [marcas, setMarcas] = useState<FipeItemDTO[]>([])
  const [carregandoMarcas, setCarregandoMarcas] = useState(false)
  const [marcaCodigo, setMarcaCodigo] = useState('')
  const [marcaNome, setMarcaNome] = useState('')

  const [modelos, setModelos] = useState<FipeItemDTO[]>([])
  const [carregandoModelos, setCarregandoModelos] = useState(false)
  const [modeloCodigo, setModeloCodigo] = useState('')
  const [modeloNome, setModeloNome] = useState('')

  const [anos, setAnos] = useState<FipeItemDTO[]>([])
  const [carregandoAnos, setCarregandoAnos] = useState(false)
  const [anoCodigo, setAnoCodigo] = useState('')
  const [anoNome, setAnoNome] = useState('')

  // Campos Não Editáveis (dados retornados da FIPE)
  const [codigoFipe, setCodigoFipe] = useState('')
  const [valorFipe, setValorFipe] = useState<number | null>(null)
  const [anoModelo, setAnoModelo] = useState<number | null>(null)
  const [combustivelFipe, setCombustivelFipe] = useState('Flex')
  const [mesReferenciaFipe, setMesReferenciaFipe] = useState('')
  const [carregandoValor, setCarregandoValor] = useState(false)

  // Placa e erros
  const [placa, setPlaca] = useState('')
  const [formVeiculoErros, setFormVeiculoErros] = useState<Record<string, string>>({})
  const [fipeErro, setFipeErro] = useState<string | null>(null)
  const [salvandoVeiculo, setSalvandoVeiculo] = useState(false)
  const [atualizandoFipeId, setAtualizandoFipeId] = useState<number | null>(null)

  // Opções pesquisáveis para os campos FIPE
  const marcasOptions = useMemo(() => {
    return marcas.map((m) => ({
      value: m.codigo,
      label: m.nome,
      sublabel: `Cód: ${m.codigo}`,
    }))
  }, [marcas])

  const modelosOptions = useMemo(() => {
    return modelos.map((m) => ({
      value: m.codigo,
      label: m.nome,
      sublabel: `Cód: ${m.codigo}`,
    }))
  }, [modelos])

  const anosOptions = useMemo(() => {
    return anos.map((a) => ({
      value: a.codigo,
      label: a.nome,
    }))
  }, [anos])

  // Carrega os dados reais do cliente e seus veículos
  const carregarDados = useCallback(async () => {
    if (Number.isNaN(numericId)) {
      notFound()
      return
    }
    try {
      setCarregandoGeral(true)
      const [cli, veics] = await Promise.all([
        clientesService.buscarPorId(numericId),
        veiculosService.listarPorCliente(numericId).catch(() => []),
      ])

      setCliente(cli)
      setNome(cli.nome || '')
      setCpf(formatCpf(cli.cpf || ''))
      setEndereco(cli.endereco || '')
      setTelefone(cli.telefone || '')
      setVeiculos(Array.isArray(veics) ? veics : [])
    } catch (err: unknown) {
      toast.error('Cliente não encontrado no sistema.')
      router.push('/app/clientes')
    } finally {
      setCarregandoGeral(false)
    }
  }, [numericId, router])

  useEffect(() => {
    carregarDados()
  }, [carregarDados])

  // --- Computados ---
  const veiculosAtivos = useMemo(() => {
    return veiculos.filter((v) => v.ativo !== false)
  }, [veiculos])

  const veiculosInativos = useMemo(() => {
    return veiculos.filter((v) => v.ativo === false)
  }, [veiculos])

  // --- Ações do Cliente ---
  const handleCpfChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setCpf(formatCpf(e.target.value))
    if (cpfErro) setCpfErro(null)
  }

  const handleSalvarCliente = async (e: React.FormEvent) => {
    e.preventDefault()

    let temErro = false
    if (!nome.trim()) {
      setNomeErro(true)
      temErro = true
    } else {
      setNomeErro(false)
    }

    const cleanCpf = cpf.replace(/\D/g, '')
    if (!cleanCpf) {
      setCpfErro('CPF é obrigatório.')
      temErro = true
    } else if (!isValidCpf(cpf)) {
      setCpfErro('CPF inválido. Verifique os dígitos.')
      temErro = true
    } else {
      setCpfErro(null)
    }

    if (!endereco.trim()) {
      setEnderecoErro(true)
      temErro = true
    } else {
      setEnderecoErro(false)
    }

    if (temErro) return

    setSalvandoCliente(true)
    try {
      const atualizado = await clientesService.atualizar(numericId, {
        nome: nome.trim(),
        cpf: cleanCpf,
        endereco: endereco.trim(),
        telefone: telefone.trim() || undefined,
      })
      setCliente(atualizado)
      toast.success('Dados do cliente atualizados com sucesso!')
    } catch (err: unknown) {
      const norm = normalizeError(err, 'Erro ao salvar alterações do cliente.')
      toast.error(norm.message)
    } finally {
      setSalvandoCliente(false)
    }
  }

  // --- Carregamento de Marcas da FIPE ---
  const carregarMarcas = useCallback(async () => {
    try {
      setCarregandoMarcas(true)
      const lista = await fipeService.listarMarcas('carros')
      setMarcas(lista)
    } catch {
      toast.error('Não foi possível carregar as marcas da tabela FIPE.')
    } finally {
      setCarregandoMarcas(false)
    }
  }, [])

  // Limpa os estados do modal de veículo
  const resetFormVeiculo = () => {
    setMarcaCodigo('')
    setMarcaNome('')
    setModelos([])
    setModeloCodigo('')
    setModeloNome('')
    setAnos([])
    setAnoCodigo('')
    setAnoNome('')
    setCodigoFipe('')
    setValorFipe(null)
    setAnoModelo(null)
    setCombustivelFipe('Flex')
    setMesReferenciaFipe('')
    setPlaca('')
    setFormVeiculoErros({})
    setFipeErro(null)
    setCarregandoModelos(false)
    setCarregandoAnos(false)
    setCarregandoValor(false)
  }

  // --- Abertura do Modal de Veículo ---
  const abrirAdicionarVeiculo = useCallback(() => {
    setModalMode('add')
    setVeiculoAntigoId(null)
    resetFormVeiculo()
    setModalAberto(true)
    carregarMarcas()
  }, [carregarMarcas])

  const abrirTrocarVeiculo = (antigoId: number) => {
    setModalMode('swap')
    setVeiculoAntigoId(antigoId)
    resetFormVeiculo()
    setModalAberto(true)
    carregarMarcas()
  }

  // Abre automaticamente o modal se vier query param ?novoVeiculo=true
  useEffect(() => {
    if (searchParams.get('novoVeiculo') === 'true') {
      abrirAdicionarVeiculo()
    }
  }, [searchParams, abrirAdicionarVeiculo])

  // --- Mudança de Montadora (Marca) ---
  const handleMarcaChange = async (codigo: string | null) => {
    const cod = codigo ?? ''
    setMarcaCodigo(cod)
    const encontrada = marcas.find((m) => m.codigo === cod)
    setMarcaNome(encontrada?.nome ?? '')

    // Limpar campos dependentes
    setModeloCodigo('')
    setModeloNome('')
    setModelos([])
    setAnoCodigo('')
    setAnoNome('')
    setAnos([])
    setCodigoFipe('')
    setValorFipe(null)
    setAnoModelo(null)
    setFipeErro(null)

    if (formVeiculoErros.montadora) {
      setFormVeiculoErros((prev) => {
        const next = { ...prev }
        delete next.montadora
        return next
      })
    }

    if (!cod) return

    try {
      setCarregandoModelos(true)
      const listaModelos = await fipeService.listarModelos(cod, 'carros')
      setModelos(listaModelos)
    } catch {
      setFipeErro('Não foi possível carregar os modelos da marca na FIPE.')
    } finally {
      setCarregandoModelos(false)
    }
  }

  // --- Mudança de Modelo ---
  const handleModeloChange = async (codigo: string | null) => {
    const cod = codigo ?? ''
    setModeloCodigo(cod)
    const encontrado = modelos.find((m) => m.codigo === cod)
    setModeloNome(encontrado?.nome ?? '')

    // Limpar ano e valor
    setAnoCodigo('')
    setAnoNome('')
    setAnos([])
    setCodigoFipe('')
    setValorFipe(null)
    setAnoModelo(null)
    setFipeErro(null)

    if (formVeiculoErros.modelo) {
      setFormVeiculoErros((prev) => {
        const next = { ...prev }
        delete next.modelo
        return next
      })
    }

    if (!cod || !marcaCodigo) return

    try {
      setCarregandoAnos(true)
      const listaAnos = await fipeService.listarAnos(marcaCodigo, cod, 'carros')
      setAnos(listaAnos)
    } catch {
      setFipeErro('Não foi possível carregar os anos deste modelo na FIPE.')
    } finally {
      setCarregandoAnos(false)
    }
  }

  // --- Mudança de Código do Modelo ---
  const handleCodigoModeloChange = (valor: string) => {
    const limpo = valor.replace(/\D/g, '')
    setModeloCodigo(limpo)
    const encontrado = modelos.find((m) => m.codigo === limpo)
    if (encontrado) {
      handleModeloChange(limpo)
    } else {
      setModeloNome('')
      setAnoCodigo('')
      setAnoNome('')
      setAnos([])
      setCodigoFipe('')
      setValorFipe(null)
      setAnoModelo(null)
    }
  }

  // --- Mudança de Ano ---
  const handleAnoChange = async (codigo: string | null) => {
    const cod = codigo ?? ''
    setAnoCodigo(cod)
    const encontrado = anos.find((a) => a.codigo === cod)
    setAnoNome(encontrado?.nome ?? '')

    setCodigoFipe('')
    setValorFipe(null)
    setAnoModelo(null)
    setFipeErro(null)

    if (formVeiculoErros.ano) {
      setFormVeiculoErros((prev) => {
        const next = { ...prev }
        delete next.ano
        return next
      })
    }

    if (!cod || !marcaCodigo || !modeloCodigo) return

    try {
      setCarregandoValor(true)
      const dadosFipe = await fipeService.consultarValor(
        marcaCodigo,
        modeloCodigo,
        cod,
        'carros'
      )
      setCodigoFipe(dadosFipe.codigoFipe)
      setValorFipe(Number(dadosFipe.valorFipe))
      setAnoModelo(dadosFipe.anoModelo)
      setCombustivelFipe(dadosFipe.combustivel || 'Flex')
      setMesReferenciaFipe(dadosFipe.mesReferencia || '')
    } catch {
      setFipeErro('Não foi possível consultar o valor de mercado na tabela FIPE.')
    } finally {
      setCarregandoValor(false)
    }
  }

  const handlePlacaChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPlaca(formatPlaca(e.target.value))
    if (formVeiculoErros.placa) {
      setFormVeiculoErros((prev) => {
        const next = { ...prev }
        delete next.placa
        return next
      })
    }
  }

  const submeterVeiculo = async (e?: React.FormEvent) => {
    if (e) e.preventDefault()

    const erros: Record<string, string> = {}
    if (!marcaCodigo) erros.montadora = 'Selecione uma montadora.'
    if (!modeloCodigo) erros.modelo = 'Selecione um modelo.'
    if (!anoCodigo) erros.ano = 'Selecione o ano do veículo.'
    const cleanPlaca = placa.replace(/[^A-Z0-9]/g, '')
    if (cleanPlaca.length < 7) {
      erros.placa = 'Placa deve ter no mínimo 7 caracteres alfanuméricos.'
    }
    if (valorFipe == null) {
      erros.valorFipe = 'Aguarde a consulta do valor FIPE do veículo.'
    }

    if (Object.keys(erros).length > 0) {
      setFormVeiculoErros(erros)
      return
    }

    setFormVeiculoErros({})
    setSalvandoVeiculo(true)

    try {
      const anoCalculado =
        anoModelo || Number.parseInt(anoNome, 10) || new Date().getFullYear()

      // Se estiver no modo troca, inativa o veículo antigo
      if (modalMode === 'swap' && veiculoAntigoId) {
        await veiculosService.inativar(veiculoAntigoId).catch(() => null)
      }

      await veiculosService.criar({
        clienteId: numericId,
        placa: placa.trim().toUpperCase(),
        marca: marcaNome,
        modelo: modeloNome,
        anoModelo: anoCalculado,
        combustivel: combustivelFipe,
        codigoFipe,
        valorFipe: valorFipe ?? 0,
        mesReferencia: mesReferenciaFipe,
      })

      toast.success(
        modalMode === 'swap'
          ? 'Veículo antigo desativado e novo veículo cadastrado com sucesso!'
          : 'Veículo adicionado com sucesso!'
      )

      setModalAberto(false)
      // Atualiza lista de veículos
      const novosVeiculos = await veiculosService.listarPorCliente(numericId)
      setVeiculos(novosVeiculos)
    } catch (err: unknown) {
      const norm = normalizeError(err, 'Erro ao cadastrar veículo.')
      toast.error(norm.message)
    } finally {
      setSalvandoVeiculo(false)
    }
  }

  const handleAlternarStatusVeiculo = async (veiculo: VeiculoResponse) => {
    try {
      if (veiculo.ativo !== false) {
        await veiculosService.inativar(veiculo.id)
        toast.success(`Veículo ${veiculo.placa} inativado.`)
      } else {
        await veiculosService.reativar(veiculo.id)
        toast.success(`Veículo ${veiculo.placa} reativado.`)
      }
      const novos = await veiculosService.listarPorCliente(numericId)
      setVeiculos(novos)
    } catch (err: unknown) {
      const norm = normalizeError(err, 'Erro ao alterar status do veículo.')
      toast.error(norm.message)
    }
  }

  const handleAtualizarFipe = async (v: VeiculoResponse) => {
    try {
      setAtualizandoFipeId(v.id)
      toast.loading(`Atualizando valor FIPE de ${v.modelo}...`, { id: `fipe-${v.id}` })

      const atualizado = await veiculosService.atualizarFipe(v.id)
      const novoValor = Number(atualizado.valorFipe || 0)

      toast.success(
        `FIPE atualizada: ${formatCurrency(novoValor)} (${atualizado.mesReferencia || 'mês atual'})`,
        { id: `fipe-${v.id}` }
      )

      const novos = await veiculosService.listarPorCliente(numericId)
      setVeiculos(novos)
    } catch (err: unknown) {
      const norm = normalizeError(err, 'Erro ao atualizar valor FIPE do veículo.')
      toast.error(norm.message, { id: `fipe-${v.id}` })
    } finally {
      setAtualizandoFipeId(null)
    }
  }

  if (carregandoGeral) {
    return (
      <div className="flex min-h-[350px] items-center justify-center gap-2 text-muted-foreground">
        <Spinner className="size-6" />
        <span>Carregando dados do cliente...</span>
      </div>
    )
  }

  return (
    <div className="grid gap-6 lg:grid-cols-3">
      <div className="lg:col-span-1">
        <PageHeader
          title="Editar Cliente"
          description="Ajuste os dados cadastrais básicos do cliente."
          backHref="/app/clientes"
          backLabel="Clientes"
        />

        <Card className="mt-4">
          <CardHeader>
            <CardTitle>Dados Pessoais</CardTitle>
            <CardDescription>
              Atualize as informações de contato do cliente.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSalvarCliente}>
              <FieldGroup className="gap-4">
                <Field data-invalid={nomeErro || undefined}>
                  <FieldLabel htmlFor="nome">Nome Completo</FieldLabel>
                  <Input
                    id="nome"
                    value={nome}
                    onChange={(e) => {
                      setNome(e.target.value)
                      if (nomeErro) setNomeErro(false)
                    }}
                    required
                  />
                  {nomeErro && (
                    <FieldDescription className="text-destructive">
                      O nome é obrigatório.
                    </FieldDescription>
                  )}
                </Field>

                <Field data-invalid={!!cpfErro || undefined}>
                  <FieldLabel htmlFor="cpf">CPF</FieldLabel>
                  <Input
                    id="cpf"
                    value={cpf}
                    onChange={handleCpfChange}
                    required
                  />
                  {cpfErro && (
                    <FieldDescription className="text-destructive">
                      {cpfErro}
                    </FieldDescription>
                  )}
                </Field>

                <Field>
                  <FieldLabel htmlFor="telefone">Telefone (Opcional)</FieldLabel>
                  <Input
                    id="telefone"
                    value={telefone}
                    onChange={(e) => setTelefone(e.target.value)}
                    placeholder="(11) 99999-9999"
                  />
                </Field>

                <Field data-invalid={enderecoErro || undefined}>
                  <FieldLabel htmlFor="endereco">Endereço Completo</FieldLabel>
                  <Input
                    id="endereco"
                    value={endereco}
                    onChange={(e) => {
                      setEndereco(e.target.value)
                      if (enderecoErro) setEnderecoErro(false)
                    }}
                    required
                  />
                  {enderecoErro && (
                    <FieldDescription className="text-destructive">
                      O endereço é obrigatório.
                    </FieldDescription>
                  )}
                </Field>

                <div className="pt-2">
                  <Button type="submit" className="w-full" disabled={salvandoCliente}>
                    {salvandoCliente ? 'Salvando...' : 'Salvar Alterações'}
                  </Button>
                </div>
              </FieldGroup>
            </form>
          </CardContent>
        </Card>
      </div>

      <div className="lg:col-span-2">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <div>
              <CardTitle>Veículos do Cliente</CardTitle>
              <CardDescription>
                Frota e histórico de veículos cadastrados na oficina.
              </CardDescription>
            </div>
            <Button onClick={abrirAdicionarVeiculo}>
              <Plus data-icon="inline-start" />
              Adicionar Veículo
            </Button>
          </CardHeader>
          <CardContent className="flex flex-col gap-4">
            <div className="flex items-center justify-between border-b pb-2">
              <span className="text-sm font-medium">
                {veiculosAtivos.length}{' '}
                {veiculosAtivos.length === 1 ? 'veículo ativo' : 'veículos ativos'}
              </span>
              <button
                type="button"
                onClick={() => setVerAntigos(!verAntigos)}
                className="text-xs text-primary hover:underline"
              >
                {verAntigos
                  ? 'Ocultar inativos'
                  : `Ver inativos / substituídos (${veiculosInativos.length})`}
              </button>
            </div>

            {(verAntigos ? veiculos : veiculosAtivos).length === 0 ? (
              <div className="py-8 text-center text-muted-foreground">
                Nenhum veículo ativo cadastrado para este cliente.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Modelo / Placa</TableHead>
                      <TableHead>Montadora / Ano</TableHead>
                      <TableHead className="text-right">Valor FIPE</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead className="text-right">Ações</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {(verAntigos ? veiculos : veiculosAtivos).map((v) => {
                      const vValorFipe = Number(v.valorFipe || 0)
                      const vAtivo = v.ativo !== false

                      return (
                        <TableRow
                          key={v.id}
                          className="cursor-pointer"
                          onClick={() => router.push(`/app/veiculos/${v.id}/historico`)}
                        >
                          <TableCell>
                            <div className="flex flex-col">
                              <span className="font-medium">{v.modelo}</span>
                              <span className="text-xs text-muted-foreground font-mono">
                                {v.placa}
                              </span>
                            </div>
                          </TableCell>
                          <TableCell>
                            <div className="flex flex-col text-sm">
                              <span>{v.marca}</span>
                              <span className="text-xs text-muted-foreground">
                                Ano {v.anoModelo}
                              </span>
                            </div>
                          </TableCell>
                          <TableCell className="text-right font-mono text-sm">
                            <div className="flex flex-col">
                              <span className="font-semibold text-emerald-600 dark:text-emerald-400">
                                {vValorFipe > 0 ? formatCurrency(vValorFipe) : '—'}
                              </span>
                              {v.mesReferencia && (
                                <span className="text-[11px] text-muted-foreground font-mono">
                                  {v.mesReferencia}
                                </span>
                              )}
                            </div>
                          </TableCell>
                          <TableCell>
                            <VeiculoStatusBadge ativo={vAtivo} />
                          </TableCell>
                          <TableCell className="text-right">
                            <div className="flex items-center justify-end gap-1.5" onClick={(e) => e.stopPropagation()}>
                              {vAtivo ? (
                                <>
                                  <Button
                                    size="sm"
                                    variant="outline"
                                    disabled={atualizandoFipeId === v.id}
                                    onClick={() => handleAtualizarFipe(v)}
                                    title="Consultar novamente o valor oficial na Tabela FIPE e atualizar o veículo"
                                  >
                                    <RefreshCw className={atualizandoFipeId === v.id ? 'size-3.5 mr-1 animate-spin' : 'size-3.5 mr-1'} />
                                    {atualizandoFipeId === v.id ? 'Atualizando...' : 'Atualizar FIPE'}
                                  </Button>
                                  <Button
                                    size="sm"
                                    variant="outline"
                                    onClick={() => abrirTrocarVeiculo(v.id)}
                                    title="Substituir por outro veículo na oficina"
                                  >
                                    <ArrowRightLeft className="size-3.5 mr-1" />
                                    Trocar
                                  </Button>
                                  <Button
                                    size="sm"
                                    variant="ghost"
                                    onClick={() => handleAlternarStatusVeiculo(v)}
                                    title="Desativar veículo"
                                  >
                                    <Power className="size-3.5 text-muted-foreground hover:text-destructive" />
                                  </Button>
                                </>
                              ) : (
                                <Button
                                  size="sm"
                                  variant="outline"
                                  onClick={() => handleAlternarStatusVeiculo(v)}
                                  title="Reativar veículo"
                                >
                                  <RotateCcw className="size-3.5 mr-1" />
                                  Reativar
                                </Button>
                              )}
                            </div>
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

      {/* --- Modal de Adicionar / Trocar Veículo com Cascata FIPE --- */}
      <Dialog open={modalAberto} onOpenChange={setModalAberto}>
        <DialogContent className="sm:max-w-xl">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Car className="size-5 text-primary" />
              {modalMode === 'swap' ? 'Substituir Veículo' : 'Cadastrar Novo Veículo'}
            </DialogTitle>
            <DialogDescription>
              {modalMode === 'swap'
                ? 'O veículo atual será desativado e o novo será vinculado a este cliente com o valor FIPE atualizado.'
                : 'Selecione a montadora, o modelo e o ano para congelar o valor FIPE oficial deste veículo.'}
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={submeterVeiculo}>
            <FieldGroup className="gap-4 py-2">
              {fipeErro && (
                <div className="flex items-center gap-2 rounded-md bg-destructive/10 p-3 text-sm text-destructive">
                  <AlertTriangle className="size-4 shrink-0" />
                  <span>{fipeErro}</span>
                </div>
              )}

              {/* 1. Montadora (Marca) - Proporção 80% (Nome com busca) / 20% (Código numérico não editável) */}
              <div className="grid grid-cols-1 sm:grid-cols-5 gap-3">
                <div className="sm:col-span-4">
                  <Field data-invalid={!!formVeiculoErros.montadora || undefined}>
                    <FieldLabel>Montadora</FieldLabel>
                    <SearchableSelect
                      value={marcaCodigo}
                      onChange={handleMarcaChange}
                      options={marcasOptions}
                      placeholder={
                        carregandoMarcas ? 'Carregando marcas FIPE...' : 'Selecione a montadora'
                      }
                      searchPlaceholder="Pesquisar montadora (ex: Fiat, Chevrolet, Volkswagen)..."
                      disabled={carregandoMarcas || salvandoVeiculo}
                      loading={carregandoMarcas}
                      error={!!formVeiculoErros.montadora}
                    />
                    {formVeiculoErros.montadora && (
                      <FieldDescription className="text-destructive">
                        {formVeiculoErros.montadora}
                      </FieldDescription>
                    )}
                  </Field>
                </div>
                <div className="sm:col-span-1">
                  <Field>
                    <FieldLabel>Cód. Montadora</FieldLabel>
                    <Input
                      type="number"
                      readOnly
                      value={marcaCodigo}
                      placeholder="Cód."
                      className="bg-muted font-mono cursor-not-allowed select-none text-center"
                      tabIndex={-1}
                      title="Código numérico da montadora na tabela FIPE (não pode ser alterado)"
                    />
                  </Field>
                </div>
              </div>

              {/* 2. Modelo - Modelo vem ANTES do código modelo; Proporção 80% (Nome do modelo com busca) / 20% (Código numérico) */}
              <div className="grid grid-cols-1 sm:grid-cols-5 gap-3">
                <div className="sm:col-span-4">
                  <Field data-invalid={!!formVeiculoErros.modelo || undefined}>
                    <FieldLabel>Modelo</FieldLabel>
                    <SearchableSelect
                      value={modeloCodigo}
                      onChange={handleModeloChange}
                      options={modelosOptions}
                      placeholder={
                        !marcaCodigo
                          ? 'Aguardando montadora'
                          : carregandoModelos
                            ? 'Buscando modelos...'
                            : 'Selecione o modelo'
                      }
                      searchPlaceholder="Pesquisar modelo (ex: Gol, Onix, Civic, Corolla)..."
                      disabled={!marcaCodigo || carregandoModelos || salvandoVeiculo}
                      loading={carregandoModelos}
                      error={!!formVeiculoErros.modelo}
                    />
                    {formVeiculoErros.modelo && (
                      <FieldDescription className="text-destructive">
                        {formVeiculoErros.modelo}
                      </FieldDescription>
                    )}
                  </Field>
                </div>
                <div className="sm:col-span-1">
                  <Field>
                    <FieldLabel>Cód. Modelo</FieldLabel>
                    <Input
                      type="number"
                      placeholder="Cód."
                      value={modeloCodigo}
                      onChange={(e) => handleCodigoModeloChange(e.target.value)}
                      disabled={!marcaCodigo || carregandoModelos || salvandoVeiculo}
                      className="font-mono text-center"
                      title="Código numérico do modelo na tabela FIPE"
                    />
                  </Field>
                </div>
              </div>

              {/* 3. Ano Modelo - Com pesquisa rápida */}
              <Field data-invalid={!!formVeiculoErros.ano || undefined}>
                <FieldLabel>Ano Modelo</FieldLabel>
                <SearchableSelect
                  value={anoCodigo}
                  onChange={handleAnoChange}
                  options={anosOptions}
                  placeholder={
                    !modeloCodigo
                      ? 'Aguardando modelo'
                      : carregandoAnos
                        ? 'Buscando anos...'
                        : 'Selecione o ano'
                  }
                  searchPlaceholder="Pesquisar ano (ex: 2021, 2022, 2023)..."
                  disabled={!modeloCodigo || carregandoAnos || salvandoVeiculo}
                  loading={carregandoAnos}
                  error={!!formVeiculoErros.ano}
                />
                {formVeiculoErros.ano && (
                  <FieldDescription className="text-destructive">
                    {formVeiculoErros.ano}
                  </FieldDescription>
                )}
              </Field>

              {/* 4. Placa */}
              <Field data-invalid={!!formVeiculoErros.placa || undefined}>
                <FieldLabel htmlFor="modal-placa">Placa do Veículo</FieldLabel>
                <Input
                  id="modal-placa"
                  placeholder="Ex: ABC-1234 ou BRA2E19"
                  value={placa}
                  onChange={handlePlacaChange}
                  maxLength={8}
                  className="font-mono uppercase"
                  disabled={salvandoVeiculo}
                />
                {formVeiculoErros.placa && (
                  <FieldDescription className="text-destructive">
                    {formVeiculoErros.placa}
                  </FieldDescription>
                )}
              </Field>

              {/* 5. Dados FIPE Congelados */}
              <div className="rounded-lg border bg-muted/40 p-3">
                <span className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Referência FIPE Oficial
                </span>
                <div className="mt-2 grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <span className="text-xs text-muted-foreground">Código FIPE</span>
                    <p className="font-mono font-medium">
                      {carregandoValor ? 'Consultando...' : codigoFipe || '—'}
                    </p>
                  </div>
                  <div>
                    <span className="text-xs text-muted-foreground">Valor de Mercado</span>
                    <p className="font-mono font-semibold text-emerald-600 dark:text-emerald-400">
                      {carregandoValor ? (
                        'Consultando...'
                      ) : valorFipe != null ? (
                        formatCurrency(valorFipe)
                      ) : (
                        '—'
                      )}
                    </p>
                  </div>
                </div>
              </div>
            </FieldGroup>

            <DialogFooter className="mt-4">
              <Button
                type="button"
                variant="outline"
                onClick={() => setModalAberto(false)}
                disabled={salvandoVeiculo}
              >
                Cancelar
              </Button>
              <Button
                type="submit"
                disabled={
                  salvandoVeiculo ||
                  carregandoValor ||
                  valorFipe == null ||
                  !placa.trim()
                }
              >
                {salvandoVeiculo
                  ? 'Salvando...'
                  : modalMode === 'swap'
                    ? 'Confirmar Substituição'
                    : 'Cadastrar Veículo'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  )
}
