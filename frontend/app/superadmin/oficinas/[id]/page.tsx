'use client'

import { use, useState } from 'react'
import { notFound } from 'next/navigation'
import { toast } from 'sonner'
import { Building2, Mail, MapPin, CalendarDays, CheckCircle2, Ban, RotateCcw } from 'lucide-react'
import { useStore } from '@/lib/store'
import type { OficinaStatus } from '@/lib/types'
import { formatDate } from '@/lib/format'
import { PageHeader } from '@/components/page-header'
import { OficinaStatusBadge } from '@/components/status-badge'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog'

export default function OficinaDetalhePage({
  params,
}: {
  params: Promise<{ id: string }>
}) {
  const { id } = use(params)
  const { oficinas, setOficinaStatus } = useStore()
  const oficina = oficinas.find((o) => o.id === id)
  const [confirmSuspender, setConfirmSuspender] = useState(false)

  if (!oficina) notFound()

  const alterarStatus = (status: OficinaStatus, mensagem: string) => {
    setOficinaStatus(oficina.id, status)
    toast.success(mensagem)
  }

  return (
    <div>
      <PageHeader
        title={oficina.nome}
        description="Dados da conta e controle de acesso."
        backHref="/superadmin"
        backLabel="Todas as oficinas"
        actions={<OficinaStatusBadge status={oficina.status} />}
      />

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Dados cadastrais</CardTitle>
            <CardDescription>
              Informações fornecidas pela oficina no momento do cadastro.
            </CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col gap-4">
            <InfoRow icon={Building2} label="Nome" value={oficina.nome} />
            <Separator />
            <InfoRow icon={Mail} label="E-mail de contato" value={oficina.email} />
            <Separator />
            <InfoRow icon={MapPin} label="Endereço" value={oficina.endereco} />
            <Separator />
            <InfoRow
              icon={CalendarDays}
              label="Data de cadastro"
              value={formatDate(oficina.dataCadastro)}
            />
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Ações</CardTitle>
            <CardDescription>Gerencie o acesso desta conta.</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col gap-3">
            {oficina.status === 'pendente' && (
              <Button
                className="w-full"
                onClick={() => alterarStatus('ativa', 'Oficina aprovada com sucesso.')}
              >
                <CheckCircle2 data-icon="inline-start" />
                Aprovar oficina
              </Button>
            )}

            {oficina.status === 'ativa' && (
              <AlertDialog open={confirmSuspender} onOpenChange={setConfirmSuspender}>
                <AlertDialogTrigger
                  render={
                    <Button variant="destructive" className="w-full">
                      <Ban data-icon="inline-start" />
                      Suspender oficina
                    </Button>
                  }
                />
                <AlertDialogContent>
                  <AlertDialogHeader>
                    <AlertDialogTitle>Suspender esta oficina?</AlertDialogTitle>
                    <AlertDialogDescription>
                      A oficina perderá o acesso ao sistema imediatamente e não
                      conseguirá entrar até ser reativada. Esta ação bloqueia o login.
                    </AlertDialogDescription>
                  </AlertDialogHeader>
                  <AlertDialogFooter>
                    <AlertDialogCancel>Cancelar</AlertDialogCancel>
                    <AlertDialogAction
                      onClick={() =>
                        alterarStatus('suspensa', 'Oficina suspensa.')
                      }
                      className="bg-destructive text-white hover:bg-destructive/90"
                    >
                      Suspender
                    </AlertDialogAction>
                  </AlertDialogFooter>
                </AlertDialogContent>
              </AlertDialog>
            )}

            {oficina.status === 'suspensa' && (
              <Button
                className="w-full"
                onClick={() => alterarStatus('ativa', 'Oficina reativada.')}
              >
                <RotateCcw data-icon="inline-start" />
                Reativar oficina
              </Button>
            )}

            <p className="text-xs text-muted-foreground">
              {oficina.status === 'pendente' &&
                'Esta conta aguarda aprovação e ainda não pode acessar o sistema.'}
              {oficina.status === 'ativa' &&
                'Esta conta está ativa e com acesso liberado ao sistema.'}
              {oficina.status === 'suspensa' &&
                'Esta conta está suspensa e não consegue acessar o sistema.'}
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

function InfoRow({
  icon: Icon,
  label,
  value,
}: {
  icon: typeof Building2
  label: string
  value: string
}) {
  return (
    <div className="flex items-start gap-3">
      <div className="flex size-9 shrink-0 items-center justify-center rounded-md bg-muted text-muted-foreground">
        <Icon className="size-4" />
      </div>
      <div className="flex flex-col">
        <span className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
          {label}
        </span>
        <span className="text-sm text-pretty">{value}</span>
      </div>
    </div>
  )
}
