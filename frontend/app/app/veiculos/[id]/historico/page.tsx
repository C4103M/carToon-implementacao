'use client'

import { use, useEffect, useState, useCallback, Suspense } from 'react'
import { notFound, useRouter } from 'next/navigation'
import Link from 'next/link'
import { Calendar, User, FileText, ChevronRight, Car, RefreshCw, Plus } from 'lucide-react'
import { veiculosService } from '@/lib/services/veiculos.service'
import { clientesService } from '@/lib/services/clientes.service'
import { ordemServicoService } from '@/lib/services/ordem-servico.service'
import type { VeiculoResponse, ClienteResponse, OrdemServicoResumo } from '@/lib/types'
import { formatCurrency, formatDate } from '@/lib/format'
import { PageHeader } from '@/components/page-header'
import { VeiculoStatusBadge, OrcamentoStatusBadge } from '@/components/status-badge'
import { EmptyState } from '@/components/empty-state'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { Button } from '@/components/ui/button'
import { Spinner } from '@/components/ui/spinner'
import { toast } from 'sonner'

export default function VeiculoHistoricoPage({
  params,
}: {
  params: Promise<{ id: string }>
}) {
  return (
    <Suspense
      fallback={
        <div className="flex min-h-[350px] items-center justify-center">
          <Spinner />
        </div>
      }
    >
      <VeiculoHistoricoContent params={params} />
    </Suspense>
  )
}

function VeiculoHistoricoContent({
  params,
}: {
  params: Promise<{ id: string }>
}) {
  const { id } = use(params)
  const router = useRouter()

  const [veiculo, setVeiculo] = useState<VeiculoResponse | null>(null)
  const [cliente, setCliente] = useState<ClienteResponse | null>(null)
  const [orcamentos, setOrcamentos] = useState<OrdemServicoResumo[]>([])
  const [carregando, setCarregando] = useState(true)

  const carregarDados = useCallback(async () => {
    setCarregando(true)
    try {
      const numId = Number(id)
      let veic: VeiculoResponse | null = null

      if (!Number.isNaN(numId)) {
        veic = await veiculosService.buscarPorId(numId).catch(() => null)
      } else {
        veic = await veiculosService.buscarPorPlaca(id).catch(() => null)
      }

      if (!veic) {
        toast.error('Veículo não encontrado.')
        router.push('/app/clientes')
        return
      }

      setVeiculo(veic)

      // Carrega cliente proprietário e histórico de ordens em paralelo
      const [cli, histResp] = await Promise.all([
        veic.clienteId
          ? clientesService.buscarPorId(veic.clienteId).catch(() => null)
          : Promise.resolve(null),
        veiculosService
          .consultarHistorico(veic.placa)
          .then((res) => res.ordensServico || res.historico || [])
          .catch(async () => {
            // Fallback para filtro por placa/veiculoId em ordemServicoService
            const fallback = await ordemServicoService
              .listar({
                veiculoId: veic?.id,
                placa: veic?.placa,
                size: 100,
                sort: 'id,desc',
              })
              .catch(() => null)
            return fallback?.content || []
          }),
      ])

      setCliente(cli)
      setOrcamentos(histResp || [])
    } catch {
      toast.error('Erro ao carregar histórico do veículo.')
    } finally {
      setCarregando(false)
    }
  }, [id, router])

  useEffect(() => {
    carregarDados()
  }, [carregarDados])

  if (carregando) {
    return (
      <div className="flex min-h-[350px] items-center justify-center gap-2 text-muted-foreground">
        <Spinner className="size-6" />
        <span>Carregando histórico do veículo...</span>
      </div>
    )
  }

  if (!veiculo) {
    return notFound()
  }

  const vValorFipe = Number(veiculo.valorFipe || 0)

  return (
    <div className="mx-auto max-w-4xl">
      <PageHeader
        title={`Histórico: ${veiculo.modelo}`}
        description="Ficha do veículo e ordens de serviço vinculadas na oficina."
        backHref={cliente ? `/app/clientes/${cliente.id}` : '/app/clientes'}
        backLabel={cliente ? `Voltar para ${cliente.nome}` : 'Voltar para Clientes'}
        actions={
          <div className="flex items-center gap-2">
            <VeiculoStatusBadge ativo={veiculo.ativo !== false} />
            <Button
              variant="outline"
              size="sm"
              onClick={carregarDados}
              title="Atualizar dados"
            >
              <RefreshCw className="size-4" />
            </Button>
          </div>
        }
      />

      <div className="grid gap-6 md:grid-cols-3">
        {/* Card de Ficha do Veículo */}
        <Card className="md:col-span-1">
          <CardHeader>
            <CardTitle className="text-base font-semibold flex items-center gap-2">
              <Car className="size-4 text-primary" />
              Ficha do Veículo
            </CardTitle>
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
              <p className="text-sm font-medium">
                {veiculo.marca} {veiculo.modelo}
              </p>
            </div>
            <Separator />
            <div>
              <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                Ano Modelo
              </span>
              <p className="text-sm">{veiculo.anoModelo || '—'}</p>
            </div>
            {veiculo.combustivel && (
              <>
                <Separator />
                <div>
                  <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                    Combustível
                  </span>
                  <p className="text-sm">{veiculo.combustivel}</p>
                </div>
              </>
            )}
            <Separator />
            <div>
              <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                Valor FIPE de Referência
              </span>
              <p className="text-sm font-semibold font-mono text-emerald-600 dark:text-emerald-400">
                {vValorFipe > 0 ? formatCurrency(vValorFipe) : 'Não informado'}
              </p>
              {veiculo.codigoFipe && (
                <span className="text-[10px] text-muted-foreground block font-mono">
                  Cód: {veiculo.codigoFipe}
                  {veiculo.mesReferencia ? ` · ${veiculo.mesReferencia}` : ''}
                </span>
              )}
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
                    className="flex items-center gap-1.5 text-sm font-medium text-primary hover:underline mt-0.5"
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
          <CardHeader className="flex flex-row items-center justify-between">
            <div>
              <CardTitle className="text-base font-semibold">
                Histórico de Manutenções
              </CardTitle>
              <span className="text-xs text-muted-foreground">
                Todas as ordens de serviço e orçamentos deste veículo ({orcamentos.length})
              </span>
            </div>
            <Button
              size="sm"
              render={<Link href="/app/orcamentos/novo" />}
            >
              <Plus data-icon="inline-start" />
              Novo Orçamento
            </Button>
          </CardHeader>
          <CardContent>
            {orcamentos.length === 0 ? (
              <EmptyState
                icon={FileText}
                title="Sem histórico registrado"
                description="Nenhum orçamento ou ordem de serviço foi gerada para este veículo ainda."
                action={
                  <Button
                    nativeButton={false}
                    render={<Link href="/app/orcamentos/novo" />}
                  >
                    Criar Primeiro Orçamento
                  </Button>
                }
              />
            ) : (
              <div className="flex flex-col divide-y divide-border">
                {orcamentos.map((o) => {
                  const statusEfetivo = o.statusServico || o.status || 'PENDENTE'
                  const total = Number(o.valorTotal ?? o.total ?? 0)
                  return (
                    <Link
                      key={o.id}
                      href={`/app/orcamentos/${o.id}`}
                      className="flex items-center justify-between gap-4 py-4 transition-colors hover:bg-muted/40 -mx-2 px-2 rounded-md"
                    >
                      <div className="flex min-w-0 flex-col gap-1">
                        <div className="flex items-center gap-2">
                          <span className="font-medium text-sm font-mono text-muted-foreground">
                            #{o.id}
                          </span>
                          <OrcamentoStatusBadge status={statusEfetivo} />
                        </div>
                        {o.descricao && (
                          <span className="text-xs text-foreground truncate max-w-md">
                            {o.descricao}
                          </span>
                        )}
                        <div className="flex items-center gap-3 text-xs text-muted-foreground">
                          <span className="flex items-center gap-1">
                            <Calendar className="size-3" />
                            {o.dataOrcamento ? formatDate(o.dataOrcamento) : '—'}
                          </span>
                          {o.mecanicoNome && (
                            <span className="flex items-center gap-1">
                              <User className="size-3" />
                              {o.mecanicoNome}
                            </span>
                          )}
                        </div>
                      </div>

                      <div className="flex shrink-0 items-center gap-3">
                        <span className="font-semibold text-sm font-mono">
                          {formatCurrency(total)}
                        </span>
                        <ChevronRight className="size-4 text-muted-foreground" />
                      </div>
                    </Link>
                  )
                })}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
