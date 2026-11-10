'use client'

import Link from 'next/link'
import { useEffect, useState, useCallback } from 'react'
import {
  FilePlus2,
  UserPlus,
  ClipboardList,
} from 'lucide-react'
import { useStore } from '@/lib/store'
import { ordemServicoService } from '@/lib/services'
import type { OrdemServicoResumo } from '@/lib/types'
import { PageHeader } from '@/components/page-header'
import {
  AdminDashboard,
  ListaOrcamentosPendentes,
} from '@/components/dashboard'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'

export default function DashboardPage() {
  const { user } = useStore()
  const isAdmin = user?.role?.toUpperCase().replace(/^ROLE_/, '') === 'ADMIN'
  const [pendentes, setPendentes] = useState<OrdemServicoResumo[]>([])
  const [carregando, setCarregando] = useState(true)

  const carregarPendentes = useCallback(async () => {
    setCarregando(true)
    try {
      const resp = await ordemServicoService.listar({
        statusServico: 'PENDENTE',
        size: 20,
        sort: 'id,desc',
      })
      setPendentes(resp.content || [])
    } catch {
      setPendentes([])
    } finally {
      setCarregando(false)
    }
  }, [])

  useEffect(() => {
    carregarPendentes()
  }, [carregarPendentes])

  return (
    <div className="flex flex-col gap-2">
      <PageHeader
        title={`Olá, ${user?.nome ?? ''}`}
        description={
          isAdmin
            ? 'Visão consolidada do pátio, faturamento e produtividade da equipe.'
            : 'Resumo operacional dos orçamentos e ordens que precisam de atenção hoje.'
        }
        actions={
          <>
            <Button
              variant="outline"
              nativeButton={false}
              render={<Link href="/app/clientes/novo" />}
            >
              <UserPlus data-icon="inline-start" />
              Novo Cliente
            </Button>
            <Button
              nativeButton={false}
              render={<Link href="/app/orcamentos/novo" />}
            >
              <FilePlus2 data-icon="inline-start" />
              Novo Orçamento
            </Button>
          </>
        }
      />

      {isAdmin ? (
        <AdminDashboard
          pendentes={pendentes}
          carregandoPendentes={carregando}
        />
      ) : (
        <div className="flex flex-col gap-6">
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <Card className="border-primary/25 bg-primary/5">
              <CardContent className="flex items-center justify-between py-5">
                <div className="flex flex-col gap-0.5">
                  <span className="font-display text-3xl font-extrabold tracking-tight text-foreground tabular-nums">
                    {pendentes.length}
                  </span>
                  <span className="text-sm font-medium text-muted-foreground">
                    Orçamentos pendentes
                  </span>
                </div>
                <div className="flex size-11 items-center justify-center rounded-xl border border-primary/20 bg-primary/15 text-primary">
                  <ClipboardList className="size-5" />
                </div>
              </CardContent>
            </Card>

            <Link
              href="/app/orcamentos/novo"
              className="group rounded-xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            >
              <Card className="h-full transition-all group-hover:border-primary/40 group-hover:shadow-sm">
                <CardContent className="flex items-center justify-between py-5">
                  <div className="flex flex-col gap-0.5">
                    <span className="font-display text-base font-bold text-foreground transition-colors group-hover:text-primary">
                      Novo Orçamento
                    </span>
                    <span className="text-sm font-medium text-muted-foreground">
                      Montar orçamento com tabela FIPE
                    </span>
                  </div>
                  <div className="flex size-11 items-center justify-center rounded-xl bg-secondary text-foreground transition-colors group-hover:bg-primary group-hover:text-primary-foreground">
                    <FilePlus2 className="size-5" />
                  </div>
                </CardContent>
              </Card>
            </Link>

            <Link
              href="/app/clientes/novo"
              className="group rounded-xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            >
              <Card className="h-full transition-all group-hover:border-primary/40 group-hover:shadow-sm">
                <CardContent className="flex items-center justify-between py-5">
                  <div className="flex flex-col gap-0.5">
                    <span className="font-display text-base font-bold text-foreground transition-colors group-hover:text-primary">
                      Novo Cliente
                    </span>
                    <span className="text-sm font-medium text-muted-foreground">
                      Cadastrar proprietário e veículo
                    </span>
                  </div>
                  <div className="flex size-11 items-center justify-center rounded-xl bg-secondary text-foreground transition-colors group-hover:bg-primary group-hover:text-primary-foreground">
                    <UserPlus className="size-5" />
                  </div>
                </CardContent>
              </Card>
            </Link>
          </div>

          <ListaOrcamentosPendentes
            pendentes={pendentes}
            carregando={carregando}
          />
        </div>
      )}
    </div>
  )
}
