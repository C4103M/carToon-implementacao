'use client'

import { useMemo, useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { Search, FileText, Plus, ChevronRight } from 'lucide-react'
import { useStore } from '@/lib/store'
import type { OrcamentoStatus } from '@/lib/types'
import { formatCurrency, formatDate } from '@/lib/format'
import { PageHeader } from '@/components/page-header'
import { OrcamentoStatusBadge } from '@/components/status-badge'
import { EmptyState } from '@/components/empty-state'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
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

type FiltroStatus = OrcamentoStatus | 'todos'

export default function OrcamentosPage() {
  const { orcamentos, clientes, veiculos } = useStore()
  const router = useRouter()

  const [busca, setBusca] = useState('')
  const [statusFiltro, setStatusFiltro] = useState<FiltroStatus>('todos')

  // Maps para buscas eficientes
  const clienteMap = useMemo(() => new Map(clientes.map((c) => [c.id, c])), [clientes])
  const veiculoMap = useMemo(() => new Map(veiculos.map((v) => [v.id, v])), [veiculos])

  const totalOrcamento = (itens: any[]) => {
    return itens.reduce((sum, i) => sum + i.valorUnitario * i.quantidade, 0)
  }

  const filtrados = useMemo(() => {
    return orcamentos
      .filter((o) => {
        const cliente = clienteMap.get(o.clienteId)
        const matchCliente = cliente
          ? cliente.nome.toLowerCase().includes(busca.toLowerCase().trim())
          : false

        const matchStatus = statusFiltro === 'todos' || o.status === statusFiltro

        return matchCliente && matchStatus
      })
      .sort((a, b) => new Date(b.data).getTime() - new Date(a.data).getTime())
  }, [orcamentos, busca, statusFiltro, clienteMap])

  return (
    <div>
      <PageHeader
        title="Orçamentos"
        description="Acompanhe e gerencie todos os orçamentos da oficina."
        actions={
          <Button render={<Link href="/app/orcamentos/novo" />}>
            <Plus data-icon="inline-start" />
            Novo Orçamento
          </Button>
        }
      />

      <Card>
        <CardContent className="flex flex-col gap-4 pt-6">
          <div className="flex flex-col gap-3 sm:flex-row">
            <InputGroup className="sm:max-w-xs flex-1">
              <InputGroupAddon>
                <Search />
              </InputGroupAddon>
              <InputGroupInput
                placeholder="Buscar por cliente..."
                value={busca}
                onChange={(e) => setBusca(e.target.value)}
              />
            </InputGroup>

            <Select
              value={statusFiltro}
              onValueChange={(v) => setStatusFiltro(v as FiltroStatus)}
            >
              <SelectTrigger className="sm:w-48">
                <SelectValue placeholder="Filtrar por status" />
              </SelectTrigger>
              <SelectContent>
                <SelectGroup>
                  <SelectItem value="todos">Todos os status</SelectItem>
                  <SelectItem value="pendente">Pendente</SelectItem>
                  <SelectItem value="aceito">Aceito</SelectItem>
                  <SelectItem value="rejeitado">Rejeitado</SelectItem>
                  <SelectItem value="finalizado">Finalizado</SelectItem>
                </SelectGroup>
              </SelectContent>
            </Select>
          </div>

          {filtrados.length === 0 ? (
            <EmptyState
              icon={FileText}
              title="Nenhum orçamento encontrado"
              description={
                orcamentos.length === 0
                  ? "Crie o primeiro orçamento para iniciar as atividades."
                  : "Ajuste os filtros de busca para encontrar o orçamento desejado."
              }
              action={
                orcamentos.length === 0 ? (
                  <Button render={<Link href="/app/orcamentos/novo" />}>
                    <Plus data-icon="inline-start" />
                    Criar orçamento
                  </Button>
                ) : undefined
              }
            />
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Cliente</TableHead>
                    <TableHead>Veículo</TableHead>
                    <TableHead className="hidden sm:table-cell">Data</TableHead>
                    <TableHead className="text-right">Valor Total</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="w-10"></TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filtrados.map((o) => {
                    const c = clienteMap.get(o.clienteId)
                    const v = veiculoMap.get(o.veiculoId)
                    return (
                      <TableRow
                        key={o.id}
                        className="cursor-pointer"
                        onClick={() => router.push(`/app/orcamentos/${o.id}`)}
                      >
                        <TableCell className="font-medium">
                          {c ? c.nome : 'Cliente Desconhecido'}
                        </TableCell>
                        <TableCell>
                          {v ? (
                            <div className="flex flex-col">
                              <span>{v.modelo}</span>
                              <span className="text-xs text-muted-foreground font-mono">
                                {v.placa}
                              </span>
                            </div>
                          ) : (
                            'Veículo Desconhecido'
                          )}
                        </TableCell>
                        <TableCell className="hidden text-muted-foreground sm:table-cell">
                          {formatDate(o.data)}
                        </TableCell>
                        <TableCell className="text-right font-mono font-semibold">
                          {formatCurrency(totalOrcamento(o.itens))}
                        </TableCell>
                        <TableCell>
                          <OrcamentoStatusBadge status={o.status} />
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
