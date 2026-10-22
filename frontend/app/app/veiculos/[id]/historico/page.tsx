'use client'

import { use, useMemo } from 'react'
import { useRouter, notFound } from 'next/navigation'
import Link from 'next/link'
import { Wrench, Calendar, User, FileText, ChevronRight } from 'lucide-react'
import { useStore } from '@/lib/store'
import { formatCurrency, formatDate } from '@/lib/format'
import { PageHeader } from '@/components/page-header'
import { VeiculoStatusBadge, OrcamentoStatusBadge } from '@/components/status-badge'
import { EmptyState } from '@/components/empty-state'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { Button } from '@/components/ui/button'

export default function VeiculoHistoricoPage({
  params,
}: {
  params: Promise<{ id: string }>
}) {
  const { id } = use(params)
  const { veiculos, clientes, orcamentos } = useStore()
  const router = useRouter()

  const veiculo = veiculos.find((v) => v.id === id)
  if (!veiculo) notFound()

  const cliente = clientes.find((c) => c.id === veiculo.clienteId)

  const orcamentosDoVeiculo = useMemo(() => {
    return orcamentos
      .filter((o) => o.veiculoId === veiculo.id)
      .sort((a, b) => new Date(b.data).getTime() - new Date(a.data).getTime())
  }, [orcamentos, veiculo.id])

  const totalOrcamento = (orcId: string) => {
    const orc = orcamentos.find((o) => o.id === orcId)
    if (!orc) return 0
    return orc.itens.reduce((sum, i) => sum + i.valorUnitario * i.quantidade, 0)
  }

  return (
    <div className="mx-auto max-w-4xl">
      <PageHeader
        title={`Histórico: ${veiculo.modelo}`}
        description="FIPE congelada e orçamentos vinculados a este veículo."
        backHref={cliente ? `/app/clientes/${cliente.id}` : '/app/clientes'}
        backLabel={cliente ? `Voltar para ${cliente.nome}` : 'Voltar'}
        actions={<VeiculoStatusBadge ativo={veiculo.ativo} />}
      />

      <div className="grid gap-6 md:grid-cols-3">
        {/* Card de Ficha do Veículo */}
        <Card className="md:col-span-1">
          <CardHeader>
            <CardTitle className="text-base font-semibold">Dados do Veículo</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col gap-4">
            <div>
              <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                Placa
              </span>
              <p className="font-mono text-sm font-semibold">{veiculo.placa}</p>
            </div>
            <Separator />
            <div>
              <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                Montadora / Modelo
              </span>
              <p className="text-sm font-medium">{veiculo.montadora} {veiculo.modelo}</p>
            </div>
            <Separator />
            <div>
              <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                Ano Modelo
              </span>
              <p className="text-sm">{veiculo.ano}</p>
            </div>
            <Separator />
            <div>
              <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                Valor FIPE (Congelado)
              </span>
              <p className="text-sm font-semibold font-mono text-success">
                {formatCurrency(veiculo.fipeValor)}
              </p>
              <span className="text-[10px] text-muted-foreground">
                Consultado em {formatDate(veiculo.fipeConsultadoEm)}
              </span>
            </div>
            <Separator />
            <div>
              <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                Multiplicador (Lambda)
              </span>
              <p className="text-sm font-semibold font-mono text-primary">
                {veiculo.lambda.toFixed(2)}x
              </p>
            </div>
            {cliente && (
              <>
                <Separator />
                <div>
                  <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                    Proprietário
                  </span>
                  <Link
                    href={`/app/clientes/${cliente.id}`}
                    className="flex items-center gap-1.5 text-sm font-medium text-primary hover:underline"
                  >
                    <User className="size-3.5" />
                    {cliente.nome}
                  </Link>
                </div>
              </>
            )}
          </CardContent>
        </Card>

        {/* Lista de Orçamentos do Veículo */}
        <Card className="md:col-span-2">
          <CardHeader>
            <CardTitle className="text-base font-semibold">Histórico de Orçamentos</CardTitle>
          </CardHeader>
          <CardContent>
            {orcamentosDoVeiculo.length === 0 ? (
              <EmptyState
                icon={FileText}
                title="Sem orçamentos registrados"
                description="Nenhum orçamento foi gerado para este veículo ainda."
                action={
                  <Button render={<Link href="/app/orcamentos/novo" />}>
                    Criar Novo Orçamento
                  </Button>
                }
              />
            ) : (
              <div className="flex flex-col divide-y divide-border">
                {orcamentosDoVeiculo.map((o) => (
                  <Link
                    key={o.id}
                    href={`/app/orcamentos/${o.id}`}
                    className="flex items-center justify-between gap-4 py-4 transition-colors hover:bg-muted/40 -mx-2 px-2 rounded-md"
                  >
                    <div className="flex min-w-0 flex-col gap-1">
                      <div className="flex items-center gap-2">
                        <span className="font-medium text-sm font-mono text-muted-foreground">
                          #{o.id.toUpperCase()}
                        </span>
                        <OrcamentoStatusBadge status={o.status} />
                      </div>
                      <span className="flex items-center gap-1 text-xs text-muted-foreground">
                        <Calendar className="size-3" />
                        {formatDate(o.data)}
                      </span>
                    </div>

                    <div className="flex shrink-0 items-center gap-3">
                      <span className="font-semibold text-sm font-mono">
                        {formatCurrency(totalOrcamento(o.id))}
                      </span>
                      <ChevronRight className="size-4 text-muted-foreground" />
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
