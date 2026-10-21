'use client'

import { useMemo, useState } from 'react'
import { useRouter } from 'next/navigation'
import { Search, Building2 } from 'lucide-react'
import { useStore } from '@/lib/store'
import type { OficinaStatus } from '@/lib/types'
import { formatDate } from '@/lib/format'
import { PageHeader } from '@/components/page-header'
import { OficinaStatusBadge } from '@/components/status-badge'
import { EmptyState } from '@/components/empty-state'
import { Card, CardContent } from '@/components/ui/card'
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

type Filtro = OficinaStatus | 'todas'

export default function AdminOficinasPage() {
  const { oficinas } = useStore()
  const router = useRouter()
  const [busca, setBusca] = useState('')
  const [filtro, setFiltro] = useState<Filtro>('todas')

  const filtradas = useMemo(() => {
    return oficinas.filter((o) => {
      const matchBusca = o.nome.toLowerCase().includes(busca.trim().toLowerCase())
      const matchStatus = filtro === 'todas' || o.status === filtro
      return matchBusca && matchStatus
    })
  }, [oficinas, busca, filtro])

  const contadores = useMemo(
    () => ({
      total: oficinas.length,
      ativas: oficinas.filter((o) => o.status === 'ativa').length,
      pendentes: oficinas.filter((o) => o.status === 'pendente').length,
      suspensas: oficinas.filter((o) => o.status === 'suspensa').length,
    }),
    [oficinas],
  )

  return (
    <div>
      <PageHeader
        title="Oficinas"
        description="Aprove, suspenda e monitore as contas de oficina da plataforma."
      />

      <div className="mb-6 grid grid-cols-2 gap-3 md:grid-cols-4">
        <StatCard label="Total" value={contadores.total} />
        <StatCard label="Ativas" value={contadores.ativas} tone="success" />
        <StatCard label="Pendentes" value={contadores.pendentes} tone="warning" />
        <StatCard label="Suspensas" value={contadores.suspensas} tone="destructive" />
      </div>

      <Card>
        <CardContent className="flex flex-col gap-4 pt-6">
          <div className="flex flex-col gap-3 sm:flex-row">
            <InputGroup className="sm:max-w-xs">
              <InputGroupAddon>
                <Search />
              </InputGroupAddon>
              <InputGroupInput
                placeholder="Buscar por nome"
                value={busca}
                onChange={(e) => setBusca(e.target.value)}
              />
            </InputGroup>
            <Select value={filtro} onValueChange={(v) => setFiltro(v as Filtro)}>
              <SelectTrigger className="sm:w-48">
                <SelectValue placeholder="Status" />
              </SelectTrigger>
              <SelectContent>
                <SelectGroup>
                  <SelectItem value="todas">Todos os status</SelectItem>
                  <SelectItem value="ativa">Ativas</SelectItem>
                  <SelectItem value="pendente">Pendentes</SelectItem>
                  <SelectItem value="suspensa">Suspensas</SelectItem>
                </SelectGroup>
              </SelectContent>
            </Select>
          </div>

          {filtradas.length === 0 ? (
            <EmptyState
              icon={Building2}
              title="Nenhuma oficina encontrada"
              description="Ajuste a busca ou o filtro de status para ver resultados."
            />
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Oficina</TableHead>
                    <TableHead className="hidden md:table-cell">E-mail</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="hidden sm:table-cell">Cadastro</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filtradas.map((o) => (
                    <TableRow
                      key={o.id}
                      className="cursor-pointer"
                      onClick={() => router.push(`/admin/oficinas/${o.id}`)}
                    >
                      <TableCell className="font-medium">{o.nome}</TableCell>
                      <TableCell className="hidden text-muted-foreground md:table-cell">
                        {o.email}
                      </TableCell>
                      <TableCell>
                        <OficinaStatusBadge status={o.status} />
                      </TableCell>
                      <TableCell className="hidden text-muted-foreground sm:table-cell">
                        {formatDate(o.dataCadastro)}
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
  )
}

function StatCard({
  label,
  value,
  tone = 'default',
}: {
  label: string
  value: number
  tone?: 'default' | 'success' | 'warning' | 'destructive'
}) {
  const toneClass = {
    default: 'text-foreground',
    success: 'text-success',
    warning: 'text-warning-foreground',
    destructive: 'text-destructive',
  }[tone]
  return (
    <Card>
      <CardContent className="flex flex-col gap-1 py-4">
        <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
          {label}
        </span>
        <span className={`text-2xl font-semibold ${toneClass}`}>{value}</span>
      </CardContent>
    </Card>
  )
}
