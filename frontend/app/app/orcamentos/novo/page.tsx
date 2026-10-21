'use client'

import { useState, useMemo } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { toast } from 'sonner'
import { FilePlus2, User, Car, ArrowRight, AlertTriangle } from 'lucide-react'
import { useStore } from '@/lib/store'
import { formatCurrency, formatCpf } from '@/lib/format'
import { PageHeader } from '@/components/page-header'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
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
  const { clientes, veiculos, createOrcamento } = useStore()
  const router = useRouter()

  const [clienteId, setClienteId] = useState('')
  const [veiculoId, setVeiculoId] = useState('')

  // Computado: Cliente selecionado
  const clienteSelecionado = useMemo(() => {
    return clientes.find((c) => c.id === clienteId)
  }, [clientes, clienteId])

  // Computado: Veículos ativos do cliente selecionado
  const veiculosAtivosDoCliente = useMemo(() => {
    if (!clienteId) return []
    return veiculos.filter((v) => v.clienteId === clienteId && v.ativo)
  }, [veiculos, clienteId])

  // Computado: Veículo selecionado
  const veiculoSelecionado = useMemo(() => {
    return veiculosAtivosDoCliente.find((v) => v.id === veiculoId)
  }, [veiculosAtivosDoCliente, veiculoId])

  const handleClienteChange = (val: string | null) => {
    setClienteId(val ?? '')
    setVeiculoId('') // Reseta veículo ao trocar de cliente
  }

  const handleCriarOrcamento = () => {
    if (!clienteId || !veiculoId) return

    const orcamento = createOrcamento(clienteId, veiculoId)
    toast.success('Orçamento iniciado!')
    router.push(`/app/orcamentos/${orcamento.id}`)
  }

  return (
    <div className="mx-auto max-w-xl">
      <PageHeader
        title="Iniciar Orçamento"
        description="Selecione o cliente e o veículo ativo para começar a montar o orçamento."
        backHref="/app/orcamentos"
        backLabel="Voltar para orçamentos"
      />

      <Card>
        <CardContent className="flex flex-col gap-6 pt-6">
          {/* Passo 1: Selecionar Cliente */}
          <div className="flex flex-col gap-2">
            <Label htmlFor="cliente-select" className="text-sm font-medium">
              1. Selecionar Cliente
            </Label>
            <Select value={clienteId} onValueChange={handleClienteChange}>
              <SelectTrigger id="cliente-select" className="w-full">
                <SelectValue placeholder="Selecione um cliente..." />
              </SelectTrigger>
              <SelectContent>
                <SelectGroup>
                  {clientes.map((c) => (
                    <SelectItem key={c.id} value={c.id}>
                      {c.nome} ({formatCpf(c.cpf)})
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
          </div>

          {/* Passo 2: Selecionar Veículo */}
          {clienteId && (
            <div className="flex flex-col gap-2">
              <Label htmlFor="veiculo-select" className="text-sm font-medium">
                2. Selecionar Veículo Ativo
              </Label>
              {veiculosAtivosDoCliente.length === 0 ? (
                <div className="flex flex-col gap-3 rounded-lg border border-warning/30 bg-warning/10 p-4 text-warning-foreground text-sm">
                  <div className="flex gap-2">
                    <AlertTriangle className="size-5 shrink-0 mt-0.5" />
                    <div className="flex flex-col gap-1">
                      <span className="font-semibold">Nenhum veículo ativo</span>
                      <span>
                        Este cliente não possui veículos ativos cadastrados no momento. É
                        necessário ter um veículo ativo para gerar o orçamento.
                      </span>
                    </div>
                  </div>
                  <Button
                    size="sm"
                    className="w-fit"
                    render={<Link href={`/app/clientes/${clienteId}`} />}
                  >
                    Gerenciar Veículos do Cliente
                  </Button>
                </div>
              ) : (
                <>
                  <Select value={veiculoId} onValueChange={(val) => setVeiculoId(val ?? '')}>
                    <SelectTrigger id="veiculo-select" className="w-full">
                      <SelectValue placeholder="Selecione um veículo ativo..." />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectGroup>
                        {veiculosAtivosDoCliente.map((v) => (
                          <SelectItem key={v.id} value={v.id}>
                            {v.modelo} · {v.placa} ({v.ano})
                          </SelectItem>
                        ))}
                      </SelectGroup>
                    </SelectContent>
                  </Select>

                  {/* Resumo FIPE e Lambda do veículo selecionado */}
                  {veiculoSelecionado && (
                    <div className="mt-2 rounded-lg border border-border bg-muted/30 p-4 text-sm flex flex-col gap-2.5">
                      <div className="flex items-center gap-2 text-muted-foreground">
                        <Car className="size-4 text-primary" />
                        <span className="font-medium text-foreground">
                          {veiculoSelecionado.montadora} {veiculoSelecionado.modelo}
                        </span>
                      </div>
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <span className="text-xs text-muted-foreground uppercase">
                            Valor FIPE (Congelado)
                          </span>
                          <p className="font-mono font-semibold text-success mt-0.5">
                            {formatCurrency(veiculoSelecionado.fipeValor)}
                          </p>
                        </div>
                        <div>
                          <span className="text-xs text-muted-foreground uppercase">
                            Multiplicador (Lambda)
                          </span>
                          <p className="font-mono font-semibold text-primary mt-0.5">
                            {veiculoSelecionado.lambda.toFixed(2)}x
                          </p>
                        </div>
                      </div>
                    </div>
                  )}
                </>
              )}
            </div>
          )}

          <div className="flex justify-end gap-3 pt-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => router.push('/app/orcamentos')}
            >
              Cancelar
            </Button>
            <Button
              type="button"
              disabled={!clienteId || !veiculoId}
              onClick={handleCriarOrcamento}
            >
              Iniciar Orçamento
              <ArrowRight data-icon="inline-end" />
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
