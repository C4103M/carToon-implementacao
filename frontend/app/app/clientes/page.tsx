'use client'

import { useMemo, useState, useEffect, useCallback } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { Search, Users, Plus, RefreshCw, RotateCcw } from 'lucide-react'
import { clientesService } from '@/lib/services/clientes.service'
import { normalizeError } from '@/lib/error-handler'
import type { ClienteResponse } from '@/lib/types'
import { formatCpf } from '@/lib/format'
import { PageHeader } from '@/components/page-header'
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
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { toast } from 'sonner'

export default function ClientesPage() {
  const router = useRouter()
  const [clientes, setClientes] = useState<ClienteResponse[]>([])
  const [carregando, setCarregando] = useState(true)
  const [busca, setBusca] = useState('')
  const [filtroStatus, setFiltroStatus] = useState<'ativos' | 'inativos'>('ativos')
  const [reativandoId, setReativandoId] = useState<number | null>(null)

  const carregarClientes = useCallback(async () => {
    try {
      setCarregando(true)
      const data = filtroStatus === 'inativos'
        ? await clientesService.listarInativos()
        : await clientesService.listar(true)
      setClientes(Array.isArray(data) ? data : [])
    } catch (err: unknown) {
      const norm = normalizeError(err, 'Erro ao carregar lista de clientes.')
      toast.error(norm.message)
      setClientes([])
    } finally {
      setCarregando(false)
    }
  }, [filtroStatus])

  useEffect(() => {
    carregarClientes()
  }, [carregarClientes])

  const handleReativar = async (e: React.MouseEvent, cliente: ClienteResponse) => {
    e.stopPropagation()
    try {
      setReativandoId(cliente.id)
      await clientesService.ativar(cliente.id)
      toast.success(`Cliente "${cliente.nome}" reativado com sucesso!`)
      await carregarClientes()
    } catch (err: unknown) {
      const norm = normalizeError(err, 'Erro ao reativar cliente.')
      toast.error(norm.message)
    } finally {
      setReativandoId(null)
    }
  }

  const filtrados = useMemo(() => {
    const query = busca.trim().toLowerCase()
    if (!query) return clientes

    const queryLimpa = query.replace(/\D/g, '')

    return clientes.filter((c) => {
      const matchNome = c.nome.toLowerCase().includes(query)
      const matchCpf = (c.cpf || '').replace(/\D/g, '').includes(queryLimpa)
      const matchTelefone = (c.telefone || '').replace(/\D/g, '').includes(queryLimpa)
      const matchEndereco = (c.endereco || '').toLowerCase().includes(query)
      return matchNome || (queryLimpa && (matchCpf || matchTelefone)) || matchEndereco
    })
  }, [clientes, busca])

  return (
    <div>
      <PageHeader
        title="Clientes"
        description="Gerencie os clientes cadastrados e seus veículos."
        actions={
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={carregarClientes}
              disabled={carregando}
              title="Atualizar lista"
            >
              <RefreshCw className={carregando ? 'size-4 animate-spin' : 'size-4'} />
              <span className="hidden sm:inline">Atualizar</span>
            </Button>
            <Button render={<Link href="/app/clientes/novo" />}>
              <Plus data-icon="inline-start" />
              Novo Cliente
            </Button>
          </div>
        }
      />

      <Card>
        <CardContent className="flex flex-col gap-4 pt-6">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <InputGroup className="sm:max-w-xs">
              <InputGroupAddon>
                <Search />
              </InputGroupAddon>
              <InputGroupInput
                placeholder="Buscar por nome, CPF ou telefone..."
                value={busca}
                onChange={(e) => setBusca(e.target.value)}
              />
            </InputGroup>

            {/* Toggle UX Ativos / Inativos */}
            <div className="flex items-center gap-1 rounded-lg border bg-muted/40 p-1 self-start sm:self-auto">
              <button
                type="button"
                onClick={() => setFiltroStatus('ativos')}
                className={`rounded-md px-3 py-1 text-xs font-medium transition-colors ${
                  filtroStatus === 'ativos'
                    ? 'bg-background text-foreground shadow-sm'
                    : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                Ativos
              </button>
              <button
                type="button"
                onClick={() => setFiltroStatus('inativos')}
                className={`rounded-md px-3 py-1 text-xs font-medium transition-colors ${
                  filtroStatus === 'inativos'
                    ? 'bg-background text-foreground shadow-sm'
                    : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                Inativos
              </button>
            </div>
          </div>

          {carregando ? (
            <div className="flex items-center justify-center py-12 gap-2 text-muted-foreground">
              <Spinner className="size-5" />
              <span className="text-sm">Carregando clientes...</span>
            </div>
          ) : filtrados.length === 0 ? (
            <EmptyState
              icon={Users}
              title={
                filtroStatus === 'inativos'
                  ? 'Nenhum cliente inativo'
                  : 'Nenhum cliente encontrado'
              }
              description={
                filtroStatus === 'inativos'
                  ? 'Todos os clientes da oficina estão ativos no momento.'
                  : clientes.length === 0
                  ? 'Cadastre seu primeiro cliente para começar a gerar ordens de serviço.'
                  : 'Ajuste a busca para encontrar o cliente desejado.'
              }
              action={
                filtroStatus === 'ativos' && clientes.length === 0 ? (
                  <Button render={<Link href="/app/clientes/novo" />}>
                    <Plus data-icon="inline-start" />
                    Cadastrar cliente
                  </Button>
                ) : undefined
              }
            />
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Nome</TableHead>
                    <TableHead>CPF</TableHead>
                    <TableHead className="hidden md:table-cell">Telefone</TableHead>
                    <TableHead className="hidden lg:table-cell">Endereço</TableHead>
                    <TableHead>Status</TableHead>
                    {filtroStatus === 'inativos' && (
                      <TableHead className="text-right">Ação</TableHead>
                    )}
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filtrados.map((c) => (
                    <TableRow
                      key={c.id}
                      className="cursor-pointer"
                      onClick={() => router.push(`/app/clientes/${c.id}`)}
                    >
                      <TableCell className="font-medium">{c.nome}</TableCell>
                      <TableCell>{formatCpf(c.cpf)}</TableCell>
                      <TableCell className="hidden md:table-cell text-muted-foreground">
                        {c.telefone || '—'}
                      </TableCell>
                      <TableCell className="hidden text-muted-foreground lg:table-cell truncate max-w-xs">
                        {c.endereco || '—'}
                      </TableCell>
                      <TableCell>
                        <Badge variant={c.ativo !== false ? 'default' : 'secondary'}>
                          {c.ativo !== false ? 'Ativo' : 'Inativo'}
                        </Badge>
                      </TableCell>
                      {filtroStatus === 'inativos' && (
                        <TableCell className="text-right">
                          <Button
                            size="sm"
                            variant="outline"
                            disabled={reativandoId === c.id}
                            onClick={(e) => handleReativar(e, c)}
                          >
                            <RotateCcw className="size-3.5 mr-1" />
                            {reativandoId === c.id ? 'Reativando...' : 'Reativar'}
                          </Button>
                        </TableCell>
                      )}
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
