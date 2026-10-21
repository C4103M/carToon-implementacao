'use client'

import { use, useState, useMemo } from 'react'
import { useRouter, notFound } from 'next/navigation'
import { toast } from 'sonner'
import {
  Car,
  Plus,
  ArrowRightLeft,
  History,
  AlertTriangle,
  RefreshCw,
} from 'lucide-react'
import { useStore } from '@/lib/store'
import { formatCpf, isValidCpf, formatPlaca } from '@/lib/format'
import { consultarFipe, type FipeResult } from '@/lib/fipe'
import { PageHeader } from '@/components/page-header'
import { VeiculoStatusBadge } from '@/components/status-badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
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
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Spinner } from '@/components/ui/spinner'

const MONTADORAS = [
  'Volkswagen',
  'Chevrolet',
  'Fiat',
  'Ford',
  'Toyota',
  'Honda',
  'Hyundai',
  'Renault',
  'Jeep',
  'Nissan',
]

export default function ClienteDetalhePage({
  params,
}: {
  params: Promise<{ id: string }>
}) {
  const { id } = use(params)
  const {
    clientes,
    updateCliente,
    veiculos,
    addVeiculo,
    trocarVeiculo,
  } = useStore()
  const router = useRouter()

  const cliente = clientes.find((c) => c.id === id)
  if (!cliente) notFound()

  // --- Estados do Cliente ---
  const [nome, setNome] = useState(cliente.nome)
  const [cpf, setCpf] = useState(formatCpf(cliente.cpf))
  const [endereco, setEndereco] = useState(cliente.endereco)

  const [nomeErro, setNomeErro] = useState(false)
  const [cpfErro, setCpfErro] = useState<string | null>(null)
  const [enderecoErro, setEnderecoErro] = useState(false)

  // --- Estados de Veículos ---
  const [verAntigos, setVerAntigos] = useState(false)

  // --- Estados do Modal de Veículo ---
  const [modalAberto, setModalAberto] = useState(false)
  const [modalMode, setModalMode] = useState<'add' | 'swap'>('add')
  const [veiculoAntigoId, setVeiculoAntigoId] = useState<string | null>(null)

  const [montadora, setMontadora] = useState('')
  const [modelo, setModelo] = useState('')
  const [ano, setAno] = useState('')
  const [placa, setPlaca] = useState('')

  const [formVeiculoErros, setFormVeiculoErros] = useState<Record<string, string>>({})

  // FIPE loading & error states
  const [fipeLoading, setFipeLoading] = useState(false)
  const [fipeErro, setFipeErro] = useState<string | null>(null)

  // --- Computados ---
  const veiculosDoCliente = useMemo(() => {
    return veiculos.filter((v) => v.clienteId === cliente.id)
  }, [veiculos, cliente.id])

  const veiculosAtivos = useMemo(() => {
    return veiculosDoCliente.filter((v) => v.ativo)
  }, [veiculosDoCliente])

  const veiculosInativos = useMemo(() => {
    return veiculosDoCliente.filter((v) => !v.ativo)
  }, [veiculosDoCliente])

  // --- Ações do Cliente ---
  const handleCpfChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setCpf(formatCpf(e.target.value))
    if (cpfErro) setCpfErro(null)
  }

  const handleSalvarCliente = (e: React.FormEvent) => {
    e.preventDefault()

    let temErro = false
    if (!nome.trim()) {
      setNomeErro(true)
      temErro = true
    } else {
      setNomeErro(false)
    }

    if (!cpf.trim()) {
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

    updateCliente(cliente.id, {
      nome: nome.trim(),
      cpf: cpf.trim(),
      endereco: endereco.trim(),
    })

    toast.success('Dados do cliente atualizados!')
  }

  // --- Modal e Veículo Actions ---
  const abrirAdicionarVeiculo = () => {
    setModalMode('add')
    setVeiculoAntigoId(null)
    setMontadora('')
    setModelo('')
    setAno('')
    setPlaca('')
    setFormVeiculoErros({})
    setFipeErro(null)
    setFipeLoading(false)
    setModalAberto(true)
  }

  const abrirTrocarVeiculo = (antigoId: string) => {
    setModalMode('swap')
    setVeiculoAntigoId(antigoId)
    setMontadora('')
    setModelo('')
    setAno('')
    setPlaca('')
    setFormVeiculoErros({})
    setFipeErro(null)
    setFipeLoading(false)
    setModalAberto(true)
  }

  const handlePlacaChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPlaca(formatPlaca(e.target.value))
  }

  const submeterVeiculo = async (e?: React.FormEvent) => {
    if (e) e.preventDefault()

    // Validação
    const erros: Record<string, string> = {}
    if (!montadora) erros.montadora = 'Selecione uma montadora.'
    if (!modelo.trim()) erros.modelo = 'Modelo é obrigatório.'
    if (!ano) {
      erros.ano = 'Ano é obrigatório.'
    } else {
      const numAno = Number.parseInt(ano, 10)
      if (Number.isNaN(numAno) || numAno < 1900 || numAno > new Date().getFullYear() + 2) {
        erros.ano = 'Informe um ano válido.'
      }
    }
    const cleanPlaca = placa.replace(/[^A-Z0-9]/g, '')
    if (cleanPlaca.length < 7) {
      erros.placa = 'Placa deve ter no mínimo 7 caracteres alfanuméricos.'
    }

    if (Object.keys(erros).length > 0) {
      setFormVeiculoErros(erros)
      return
    }

    setFormVeiculoErros({})
    setFipeErro(null)
    setFipeLoading(true)

    try {
      const fipeResult = await consultarFipe({
        montadora,
        modelo: modelo.trim(),
        ano: Number.parseInt(ano, 10),
      })

      const veiculoData = {
        clienteId: cliente.id,
        placa: placa.trim().toUpperCase(),
        modelo: modelo.trim(),
        montadora,
        ano: Number.parseInt(ano, 10),
      }

      if (modalMode === 'swap' && veiculoAntigoId) {
        trocarVeiculo(veiculoAntigoId, veiculoData, fipeResult)
        toast.success('Veículo antigo desativado e novo veículo cadastrado!')
      } else {
        addVeiculo(veiculoData, fipeResult)
        toast.success('Veículo adicionado com sucesso!')
      }

      setModalAberto(false)
    } catch (err: any) {
      setFipeErro(err.message || 'Falha ao consultar FIPE.')
    } finally {
      setFipeLoading(false)
    }
  }

  return (
    <div className="grid gap-6 lg:grid-cols-3">
      <div className="lg:col-span-1">
        <PageHeader
          title="Editar Cliente"
          description="Ajuste os dados cadastrais básico do cliente."
          backHref="/app/clientes"
          backLabel="Clientes"
        />

        <Card>
          <CardContent className="pt-6">
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

                <Button type="submit" className="w-full mt-2">
                  Salvar Cliente
                </Button>
              </FieldGroup>
            </form>
          </CardContent>
        </Card>
      </div>

      <div className="lg:col-span-2">
        <div className="mb-6 flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
          <div className="flex flex-col gap-1">
            <h2 className="text-pretty text-2xl font-semibold tracking-tight">
              Veículos
            </h2>
            <p className="text-pretty text-sm text-muted-foreground">
              Gerencie a frota ativa e antiga deste cliente.
            </p>
          </div>
          <Button onClick={abrirAdicionarVeiculo}>
            <Plus data-icon="inline-start" />
            Adicionar Veículo
          </Button>
        </div>

        <Card>
          <CardContent className="pt-6">
            <div className="mb-4 flex items-center justify-between">
              <span className="text-sm font-medium text-muted-foreground">
                Exibindo {verAntigos ? veiculosDoCliente.length : veiculosAtivos.length}{' '}
                veículo(s)
              </span>
              <button
                type="button"
                onClick={() => setVerAntigos(!verAntigos)}
                className="inline-flex items-center gap-1.5 text-xs font-medium text-muted-foreground underline-offset-4 hover:text-foreground hover:underline"
              >
                <History className="size-3.5" />
                {verAntigos ? 'Ocultar veículos antigos' : 'Ver veículos antigos'}
              </button>
            </div>

            {((verAntigos ? veiculosDoCliente : veiculosAtivos).length === 0) ? (
              <div className="py-8 text-center text-muted-foreground">
                Nenhum veículo ativo cadastrado.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Modelo / Placa</TableHead>
                      <TableHead>Montadora / Ano</TableHead>
                      <TableHead className="text-right">FIPE / Lambda</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead className="text-right">Ações</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {(verAntigos ? veiculosDoCliente : veiculosAtivos).map((v) => (
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
                            <span>{v.montadora}</span>
                            <span className="text-xs text-muted-foreground">
                              Ano {v.ano}
                            </span>
                          </div>
                        </TableCell>
                        <TableCell className="text-right font-mono text-sm">
                          <div className="flex flex-col">
                            <span>
                              {new Intl.NumberFormat('pt-BR', {
                                style: 'currency',
                                currency: 'BRL',
                              }).format(v.fipeValor)}
                            </span>
                            <span className="text-xs text-muted-foreground">
                              {v.lambda.toFixed(2)}x
                            </span>
                          </div>
                        </TableCell>
                        <TableCell>
                          <VeiculoStatusBadge ativo={v.ativo} />
                        </TableCell>
                        <TableCell className="text-right">
                          {v.ativo && (
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={(e) => {
                                e.stopPropagation()
                                abrirTrocarVeiculo(v.id)
                              }}
                              title="Trocar por um novo veículo"
                            >
                              <ArrowRightLeft className="size-3.5 mr-1" />
                              Trocar
                            </Button>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Modal / Dialog de Cadastro/Substituição de Veículo */}
      <Dialog open={modalAberto} onOpenChange={setModalAberto}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>
              {modalMode === 'add' ? 'Adicionar Veículo' : 'Trocar Veículo'}
            </DialogTitle>
            <DialogDescription>
              {modalMode === 'add'
                ? 'Insira as informações do veículo para consultar a tabela FIPE.'
                : 'Insira o novo veículo. O veículo anterior será marcado como inativo.'}
            </DialogDescription>
          </DialogHeader>

          {fipeErro && (
            <div className="flex items-start gap-3 rounded-lg border border-destructive/50 bg-destructive/10 p-3 text-destructive">
              <AlertTriangle className="size-5 shrink-0 mt-0.5" />
              <div className="flex flex-col gap-1.5 text-sm">
                <span className="font-semibold">Erro na consulta FIPE</span>
                <span>{fipeErro}</span>
                <Button
                  size="sm"
                  variant="destructive"
                  onClick={() => submeterVeiculo()}
                  className="w-fit"
                >
                  <RefreshCw className="size-3.5 mr-1.5" />
                  Tentar Novamente
                </Button>
              </div>
            </div>
          )}

          <form onSubmit={submeterVeiculo}>
            <FieldGroup className="gap-4">
              <Field data-invalid={!!formVeiculoErros.montadora || undefined}>
                <FieldLabel htmlFor="montadora">Montadora</FieldLabel>
                <Select
                  value={montadora}
                  onValueChange={(v) => {
                    setMontadora(v ?? '')
                    if (formVeiculoErros.montadora) {
                      setFormVeiculoErros((prev) => {
                        const next = { ...prev }
                        delete next.montadora
                        return next
                      })
                    }
                  }}
                  disabled={fipeLoading}
                >
                  <SelectTrigger id="montadora" className="w-full">
                    <SelectValue placeholder="Selecione..." />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      {MONTADORAS.map((m) => (
                        <SelectItem key={m} value={m}>
                          {m}
                        </SelectItem>
                      ))}
                    </SelectGroup>
                  </SelectContent>
                </Select>
                {formVeiculoErros.montadora && (
                  <FieldDescription className="text-destructive">
                    {formVeiculoErros.montadora}
                  </FieldDescription>
                )}
              </Field>

              <Field data-invalid={!!formVeiculoErros.modelo || undefined}>
                <FieldLabel htmlFor="modelo">Modelo</FieldLabel>
                <Input
                  id="modelo"
                  placeholder="Ex: Corolla 2.0 XEi"
                  value={modelo}
                  onChange={(e) => {
                    setModelo(e.target.value)
                    if (formVeiculoErros.modelo) {
                      setFormVeiculoErros((prev) => {
                        const next = { ...prev }
                        delete next.modelo
                        return next
                      })
                    }
                  }}
                  disabled={fipeLoading}
                  required
                />
                {formVeiculoErros.modelo && (
                  <FieldDescription className="text-destructive">
                    {formVeiculoErros.modelo}
                  </FieldDescription>
                )}
              </Field>

              <div className="grid grid-cols-2 gap-4">
                <Field data-invalid={!!formVeiculoErros.ano || undefined}>
                  <FieldLabel htmlFor="ano">Ano</FieldLabel>
                  <Input
                    id="ano"
                    type="number"
                    placeholder="Ex: 2020"
                    value={ano}
                    onChange={(e) => {
                      setAno(e.target.value)
                      if (formVeiculoErros.ano) {
                        setFormVeiculoErros((prev) => {
                          const next = { ...prev }
                          delete next.ano
                          return next
                        })
                      }
                    }}
                    disabled={fipeLoading}
                    required
                  />
                  {formVeiculoErros.ano && (
                    <FieldDescription className="text-destructive">
                      {formVeiculoErros.ano}
                    </FieldDescription>
                  )}
                </Field>

                <Field data-invalid={!!formVeiculoErros.placa || undefined}>
                  <FieldLabel htmlFor="placa">Placa</FieldLabel>
                  <Input
                    id="placa"
                    placeholder="ABC-1234"
                    value={placa}
                    onChange={handlePlacaChange}
                    disabled={fipeLoading}
                    required
                  />
                  {formVeiculoErros.placa && (
                    <FieldDescription className="text-destructive">
                      {formVeiculoErros.placa}
                    </FieldDescription>
                  )}
                </Field>
              </div>

              <DialogFooter className="pt-4">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setModalAberto(false)}
                  disabled={fipeLoading}
                >
                  Cancelar
                </Button>
                <Button type="submit" disabled={fipeLoading}>
                  {fipeLoading && <Spinner data-icon="inline-start" />}
                  {fipeLoading ? 'Consultando FIPE...' : 'Salvar Veículo'}
                </Button>
              </DialogFooter>
            </FieldGroup>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  )
}
