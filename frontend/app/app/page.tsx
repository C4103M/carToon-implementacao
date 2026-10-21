'use client'

import Link from 'next/link'
import { useMemo } from 'react'
import { FilePlus2, UserPlus, FileText, ArrowRight, ClipboardList } from 'lucide-react'
import { useStore } from '@/lib/store'
import { formatCurrency, formatDate } from '@/lib/format'
import { PageHeader } from '@/components/page-header'
import { OrcamentoStatusBadge } from '@/components/status-badge'
import { EmptyState } from '@/components/empty-state'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

export default function DashboardPage() {
  const { user, orcamentos, clientes, veiculos } = useStore()

  const pendentes = useMemo(
    () =>
      orcamentos
        .filter((o) => o.status === 'pendente')
        .sort((a, b) => new Date(b.data).getTime() - new Date(a.data).getTime()),
    [orcamentos],
  )

  const totalOrcamento = (id: string) => {
    const orc = orcamentos.find((o) => o.id === id)
    if (!orc) return 0
    return orc.itens.reduce((sum, i) => sum + i.valorUnitario * i.quantidade, 0)
  }

  const clienteNome = (id: string) => clientes.find((c) => c.id === id)?.nome ?? '—'
  const veiculoLabel = (id: string) => {
    const v = veiculos.find((x) => x.id === id)
    return v ? `${v.modelo} · ${v.placa}` : '—'
  }

  return (
    <div>
      <PageHeader
        title={`Olá, ${user?.nome ?? ''}`}
        description="Aqui está um resumo do que precisa de atenção hoje."
      />

      <div className="mb-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <Card className="bg-primary/10 border-primary/30">
          <CardContent className="flex items-center justify-between py-5">
            <div className="flex flex-col">
              <span className="text-3xl font-semibold">{pendentes.length}</span>
              <span className="text-sm text-muted-foreground">
                Orçamentos pendentes
              </span>
            </div>
            <div className="flex size-11 items-center justify-center rounded-full bg-primary/20 text-primary">
              <ClipboardList className="size-5" />
            </div>
          </CardContent>
        </Card>

        <Link href="/app/orcamentos/novo" className="group">
          <Card className="h-full transition-colors group-hover:border-primary/50">
            <CardContent className="flex items-center justify-between py-5">
              <div className="flex flex-col">
                <span className="font-medium">Novo Orçamento</span>
                <span className="text-sm text-muted-foreground">
                  Montar um orçamento
                </span>
              </div>
              <div className="flex size-11 items-center justify-center rounded-full bg-muted text-foreground transition-colors group-hover:bg-primary group-hover:text-primary-foreground">
                <FilePlus2 className="size-5" />
              </div>
            </CardContent>
          </Card>
        </Link>

        <Link href="/app/clientes/novo" className="group">
          <Card className="h-full transition-colors group-hover:border-primary/50">
            <CardContent className="flex items-center justify-between py-5">
              <div className="flex flex-col">
                <span className="font-medium">Novo Cliente</span>
                <span className="text-sm text-muted-foreground">
                  Cadastrar um cliente
                </span>
              </div>
              <div className="flex size-11 items-center justify-center rounded-full bg-muted text-foreground transition-colors group-hover:bg-primary group-hover:text-primary-foreground">
                <UserPlus className="size-5" />
              </div>
            </CardContent>
          </Card>
        </Link>
      </div>

      <Card>
        <CardHeader className="flex-row items-center justify-between">
          <div className="flex flex-col gap-1.5">
            <CardTitle>Orçamentos pendentes</CardTitle>
            <CardDescription>
              Aguardando decisão do cliente. Atualize o status quando houver retorno.
            </CardDescription>
          </div>
          <Button variant="outline" size="sm" render={<Link href="/app/orcamentos" />}>
            Ver todos
            <ArrowRight data-icon="inline-end" />
          </Button>
        </CardHeader>
        <CardContent>
          {pendentes.length === 0 ? (
            <EmptyState
              icon={FileText}
              title="Nenhum orçamento pendente"
              description="Todos os orçamentos já foram decididos. Crie um novo quando precisar."
              action={
                <Button render={<Link href="/app/orcamentos/novo" />}>
                  <FilePlus2 data-icon="inline-start" />
                  Novo orçamento
                </Button>
              }
            />
          ) : (
            <div className="flex flex-col divide-y divide-border">
              {pendentes.map((o) => (
                <Link
                  key={o.id}
                  href={`/app/orcamentos/${o.id}`}
                  className="flex items-center justify-between gap-4 py-3 transition-colors hover:bg-muted/40 -mx-2 px-2 rounded-md"
                >
                  <div className="flex min-w-0 flex-col">
                    <span className="truncate font-medium">{clienteNome(o.clienteId)}</span>
                    <span className="truncate text-sm text-muted-foreground">
                      {veiculoLabel(o.veiculoId)} · {formatDate(o.data)}
                    </span>
                  </div>
                  <div className="flex shrink-0 items-center gap-4">
                    <span className="hidden font-medium sm:inline">
                      {formatCurrency(totalOrcamento(o.id))}
                    </span>
                    <OrcamentoStatusBadge status={o.status} />
                  </div>
                </Link>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
