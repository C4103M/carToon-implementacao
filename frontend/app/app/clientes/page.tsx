'use client'

import { useMemo, useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { Search, Users, Plus } from 'lucide-react'
import { useStore } from '@/lib/store'
import { formatCpf } from '@/lib/format'
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

export default function ClientesPage() {
  const { clientes } = useStore()
  const router = useRouter()
  const [busca, setBusca] = useState('')

  const filtrados = useMemo(() => {
    const query = busca.trim().toLowerCase()
    if (!query) return clientes

    // Remove pontos e traço se o usuário digitar formatado ou apenas dígitos
    const queryLimpa = query.replace(/\D/g, '')

    return clientes.filter((c) => {
      const matchNome = c.nome.toLowerCase().includes(query)
      const matchCpf = c.cpf.replace(/\D/g, '').includes(queryLimpa)
      return matchNome || (queryLimpa && matchCpf)
    })
  }, [clientes, busca])

  return (
    <div>
      <PageHeader
        title="Clientes"
        description="Gerencie os clientes cadastrados e seus veículos."
        actions={
          <Button render={<Link href="/app/clientes/novo" />}>
            <Plus data-icon="inline-start" />
            Novo Cliente
          </Button>
        }
      />

      <Card>
        <CardContent className="flex flex-col gap-4 pt-6">
          <InputGroup className="sm:max-w-xs">
            <InputGroupAddon>
              <Search />
            </InputGroupAddon>
            <InputGroupInput
              placeholder="Buscar por nome ou CPF"
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
            />
          </InputGroup>

          {filtrados.length === 0 ? (
            <EmptyState
              icon={Users}
              title="Nenhum cliente encontrado"
              description={
                clientes.length === 0
                  ? "Cadastre seu primeiro cliente para começar."
                  : "Ajuste a busca para encontrar o cliente desejado."
              }
              action={
                clientes.length === 0 ? (
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
                    <TableHead className="hidden sm:table-cell">Endereço</TableHead>
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
                      <TableCell className="hidden text-muted-foreground sm:table-cell">
                        {c.endereco}
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
