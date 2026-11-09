'use client'

import { Suspense, use, useEffect, useState } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { toast } from 'sonner'
import {
  Building2,
  Mail,
  MapPin,
  CalendarDays,
  CheckCircle2,
  Ban,
  RotateCcw,
  Phone,
  Plus,
  UserCheck,
  UserX,
  Shield,
  ShieldCheck,
  Pencil,
  AlertCircle,
} from 'lucide-react'
import type { Oficina, OficinaStatus, UsuarioResponse } from '@/lib/types'
import { formatDate, formatTelefone, isValidTelefone } from '@/lib/format'
import { oficinasService, usuariosService } from '@/lib/services'
import { normalizeError } from '@/lib/error-handler'
import { PageHeader } from '@/components/page-header'
import { OficinaStatusBadge } from '@/components/status-badge'
import { EmptyState } from '@/components/empty-state'
import { Button } from '@/components/ui/button'
import { Spinner } from '@/components/ui/spinner'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Field, FieldLabel, FieldDescription } from '@/components/ui/field'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Separator } from '@/components/ui/separator'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog'
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
  return (
    <Suspense
      fallback={
        <div className="flex min-h-[300px] items-center justify-center">
          <Spinner />
        </div>
      }
    >
      <OficinaDetalheContent params={params} />
    </Suspense>
  )
}

function OficinaDetalheContent({
  params,
}: {
  params: Promise<{ id: string }>
}) {
  const { id } = use(params)
  const router = useRouter()
  const searchParams = useSearchParams()

  // Oficina em exibição
  const [oficina, setOficina] = useState<Oficina | null>(null)
  const [carregandoOficina, setCarregandoOficina] = useState(true)

  // Administradores vinculados à oficina
  const [admins, setAdmins] = useState<UsuarioResponse[]>([])
  const [carregandoAdmins, setCarregandoAdmins] = useState(true)

  // Modais de Administrador
  const [modalAdminAberto, setModalAdminAberto] = useState(false)
  const [salvandoAdmin, setSalvandoAdmin] = useState(false)
  const [adminNome, setAdminNome] = useState('')
  const [adminEmail, setAdminEmail] = useState('')
  const [adminSenha, setAdminSenha] = useState('')
  const [adminConfirmarSenha, setAdminConfirmarSenha] = useState('')
  const [adminNomeErro, setAdminNomeErro] = useState<string | null>(null)
  const [adminEmailErro, setAdminEmailErro] = useState<string | null>(null)
  const [adminSenhaErro, setAdminSenhaErro] = useState<string | null>(null)
  const [adminConfirmarSenhaErro, setAdminConfirmarSenhaErro] = useState<string | null>(null)
  const [adminErroGeral, setAdminErroGeral] = useState<string | null>(null)

  // Modal de Edição de Dados da Oficina
  const [modalEdicaoOficinaAberto, setModalEdicaoOficinaAberto] = useState(false)
  const [salvandoEdicaoOficina, setSalvandoEdicaoOficina] = useState(false)
  const [editNome, setEditNome] = useState('')
  const [editTelefone, setEditTelefone] = useState('')
  const [editEndereco, setEditEndereco] = useState('')
  const [editEmail, setEditEmail] = useState('')

  // Diálogo de confirmação de suspensão / ativação de oficina
  const [confirmSuspender, setConfirmSuspender] = useState(false)
  const [alterandoStatusOficina, setAlterandoStatusOficina] = useState(false)

  // 1. Carrega dados da oficina via API (com fallback ao Store)
  useEffect(() => {
    let isMounted = true
    const numericId = Number(id)

    if (!Number.isNaN(numericId)) {
      oficinasService
        .buscarPorId(numericId)
        .then((res) => {
          if (!isMounted) return
          const dados: Oficina = {
            id: String(res.id),
            nome: res.nome,
            endereco: res.endereco,
            telefone: res.telefone,
            email: res.email || '',
            dataCadastro: new Date().toISOString().slice(0, 10),
            status: res.ativo ? 'ativa' : 'suspensa',
          }
          setOficina(dados)
        })
        .catch(() => {
          // Mantém o store local caso falhe ou API esteja offline
        })
        .finally(() => {
          if (isMounted) setCarregandoOficina(false)
        })
    } else {
      setCarregandoOficina(false)
    }

    return () => {
      isMounted = false
    }
  }, [id])

  // 2. Carrega lista de administradores desta oficina
  useEffect(() => {
    let isMounted = true
    const numericId = Number(id)

    if (!Number.isNaN(numericId)) {
      usuariosService
        .listar({ oficinaId: numericId, role: 'ADMIN' })
        .then((data) => {
          if (!isMounted) return
          const administradores = data.filter((u) => u.role === 'ADMIN')
          setAdmins(administradores)
        })
        .catch(() => {
          // Modo offline: inicia lista vazia
        })
        .finally(() => {
          if (isMounted) setCarregandoAdmins(false)
        })
    } else {
      setCarregandoAdmins(false)
    }

    return () => {
      isMounted = false
    }
  }, [id])

  // 3. Abre automaticamente o modal se o query param "novoAdmin" vier como true
  useEffect(() => {
    if (searchParams.get('novoAdmin') === 'true') {
      setModalAdminAberto(true)
    }
  }, [searchParams])

  // Sincroniza campos para edição quando a oficina é carregada
  useEffect(() => {
    if (oficina) {
      setEditNome(oficina.nome)
      setEditTelefone(oficina.telefone || '')
      setEditEndereco(oficina.endereco)
      setEditEmail(oficina.email || '')
    }
  }, [oficina])

  const resetAdminForm = () => {
    setAdminNome('')
    setAdminEmail('')
    setAdminSenha('')
    setAdminConfirmarSenha('')
    setAdminNomeErro(null)
    setAdminEmailErro(null)
    setAdminSenhaErro(null)
    setAdminConfirmarSenhaErro(null)
    setAdminErroGeral(null)
  }

  const handleAbrirModalAdmin = () => {
    resetAdminForm()
    setModalAdminAberto(true)
  }

  // Submissão do cadastro de Administrador
  const handleCriarAdmin = async (e: React.FormEvent) => {
    e.preventDefault()
    setAdminErroGeral(null)

    let temErro = false

    if (!adminNome.trim()) {
      setAdminNomeErro('O nome do administrador é obrigatório.')
      temErro = true
    } else {
      setAdminNomeErro(null)
    }

    if (!adminEmail.trim()) {
      setAdminEmailErro('O e-mail é obrigatório.')
      temErro = true
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(adminEmail.trim())) {
      setAdminEmailErro('Informe um e-mail válido.')
      temErro = true
    } else {
      setAdminEmailErro(null)
    }

    if (!adminSenha.trim()) {
      setAdminSenhaErro('A senha é obrigatória.')
      temErro = true
    } else if (adminSenha.length < 8) {
      setAdminSenhaErro('A senha deve ter no mínimo 8 caracteres.')
      temErro = true
    } else {
      setAdminSenhaErro(null)
    }

    if (!adminConfirmarSenha.trim()) {
      setAdminConfirmarSenhaErro('A confirmação de senha é obrigatória.')
      temErro = true
    } else if (adminSenha !== adminConfirmarSenha) {
      setAdminConfirmarSenhaErro('As senhas não coincidem.')
      temErro = true
    } else {
      setAdminConfirmarSenhaErro(null)
    }

    if (temErro) return

    setSalvandoAdmin(true)
    const numericId = Number(id)

    try {
      const res = await usuariosService.cadastrarAdmin({
        nome: adminNome.trim(),
        email: adminEmail.trim(),
        senha: adminSenha,
        confirmarSenha: adminConfirmarSenha,
        oficinaId: Number.isNaN(numericId) ? 1 : numericId,
      })

      setAdmins((prev) => [res, ...prev.filter((a) => a.id !== res.id)])
      toast.success('Administrador cadastrado com sucesso! Acesso da oficina liberado.')
      setModalAdminAberto(false)
      resetAdminForm()
    } catch (err: unknown) {
      const normalized = normalizeError(err)

      if (normalized.isNetworkError) {
        // Fallback offline para demonstração
        const mockAdmin: UsuarioResponse = {
          id: Date.now(),
          nome: adminNome.trim(),
          email: adminEmail.trim(),
          role: 'ADMIN',
          ativo: true,
          oficinaId: Number.isNaN(numericId) ? 1 : numericId,
        }
        setAdmins((prev) => [mockAdmin, ...prev])
        toast.success('Administrador cadastrado localmente (servidor backend offline).')
        setModalAdminAberto(false)
        resetAdminForm()
        return
      }

      const msgLower = (normalized.message || '').toLowerCase()
      if (msgLower.includes('e-mail') || msgLower.includes('email')) {
        setAdminEmailErro(normalized.message)
      } else if (msgLower.includes('confirmar') || msgLower.includes('coincidem')) {
        setAdminConfirmarSenhaErro(normalized.message)
      } else if (msgLower.includes('senha')) {
        setAdminSenhaErro(normalized.message)
      } else {
        setAdminErroGeral(normalized.message)
      }

      toast.error(normalized.title, {
        description: normalized.message,
      })
    } finally {
      setSalvandoAdmin(false)
    }
  }

  // Ativar / Desativar Administrador
  const handleToggleAdminStatus = async (admin: UsuarioResponse) => {
    try {
      if (admin.ativo) {
        await usuariosService.desativar(admin.id)
        setAdmins((prev) =>
          prev.map((a) => (a.id === admin.id ? { ...a, ativo: false } : a)),
        )
        toast.success(`Acesso de ${admin.nome} foi desativado.`)
      } else {
        await usuariosService.ativar(admin.id)
        setAdmins((prev) =>
          prev.map((a) => (a.id === admin.id ? { ...a, ativo: true } : a)),
        )
        toast.success(`Acesso de ${admin.nome} foi reativado.`)
      }
    } catch (err: unknown) {
      const normalized = normalizeError(err)
      toast.error(normalized.title, { description: normalized.message })
    }
  }

  // Alteração de status da Oficina (Ativar / Suspender)
  const alterarStatusOficina = async (novoStatus: OficinaStatus) => {
    if (!oficina) return
    const numericId = Number(oficina.id)
    setAlterandoStatusOficina(true)

    try {
      if (!Number.isNaN(numericId)) {
        if (novoStatus === 'ativa') {
          await oficinasService.ativar(numericId)
        } else {
          await oficinasService.desativar(numericId)
        }
      }

      setOficina((prev) => (prev ? { ...prev, status: novoStatus } : null))
      toast.success(
        novoStatus === 'ativa'
          ? 'Oficina ativada com sucesso!'
          : 'Oficina suspensa com sucesso.',
      )
      setConfirmSuspender(false)
    } catch (err: unknown) {
      const normalized = normalizeError(err)
      toast.error(normalized.title, {
        description: normalized.message,
      })
    } finally {
      setAlterandoStatusOficina(false)
    }
  }

  // Salvar edição dos dados cadastrais da Oficina
  const handleSalvarEdicaoOficina = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!oficina) return

    setSalvandoEdicaoOficina(true)
    const numericId = Number(oficina.id)

    try {
      if (!Number.isNaN(numericId)) {
        const res = await oficinasService.atualizar(numericId, {
          nome: editNome.trim(),
          endereco: editEndereco.trim(),
          telefone: editTelefone.trim(),
          email: editEmail.trim(),
        })

        const atualizada: Oficina = {
          ...oficina,
          nome: res.nome,
          endereco: res.endereco,
          telefone: res.telefone,
          email: res.email || editEmail.trim(),
        }

        setOficina(atualizada)
      } else {
        const atualizada: Oficina = {
          ...oficina,
          nome: editNome.trim(),
          endereco: editEndereco.trim(),
          telefone: editTelefone.trim(),
          email: editEmail.trim(),
        }
        setOficina(atualizada)
      }

      toast.success('Dados da oficina atualizados com sucesso!')
      setModalEdicaoOficinaAberto(false)
    } catch (err: unknown) {
      const normalized = normalizeError(err)
      toast.error(normalized.title, { description: normalized.message })
    } finally {
      setSalvandoEdicaoOficina(false)
    }
  }

  if (carregandoOficina) {
    return (
      <div className="flex min-h-[350px] items-center justify-center">
        <Spinner />
      </div>
    )
  }

  if (!oficina) {
    return (
      <div className="py-12">
        <EmptyState
          icon={Building2}
          title="Oficina não encontrada"
          description="A oficina solicitada não existe ou foi removida da plataforma."
          action={
            <Button onClick={() => router.push('/superadmin')}>
              Voltar para o Painel
            </Button>
          }
        />
      </div>
    )
  }

  const iniciais = (nome: string) => {
    const partes = nome.trim().split(' ')
    if (partes.length >= 2) {
      return `${partes[0][0]}${partes[1][0]}`.toUpperCase()
    }
    return nome.slice(0, 2).toUpperCase()
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title={oficina.nome}
        description="Dados da conta, administradores e controle de acesso."
        backHref="/superadmin"
        backLabel="Todas as oficinas"
        actions={
          <div className="flex items-center gap-2">
            <OficinaStatusBadge status={oficina.status} />
            <Button
              variant="outline"
              size="sm"
              onClick={() => setModalEdicaoOficinaAberto(true)}
            >
              <Pencil data-icon="inline-start" />
              Editar Dados
            </Button>
          </div>
        }
      />

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Card: Dados Cadastrais */}
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
            {oficina.email && (
              <>
                <InfoRow icon={Mail} label="E-mail de contato" value={oficina.email} />
                <Separator />
              </>
            )}
            {oficina.telefone && (
              <>
                <InfoRow icon={Phone} label="Telefone" value={oficina.telefone} />
                <Separator />
              </>
            )}
            <InfoRow icon={MapPin} label="Endereço" value={oficina.endereco} />
            <Separator />
            <InfoRow
              icon={CalendarDays}
              label="Data de cadastro"
              value={formatDate(oficina.dataCadastro)}
            />
          </CardContent>
        </Card>

        {/* Card: Ações da Oficina */}
        <Card>
          <CardHeader>
            <CardTitle>Controle de Acesso</CardTitle>
            <CardDescription>Gerencie o status desta unidade.</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col gap-3">
            {oficina.status === 'pendente' && (
              <Button
                className="w-full"
                onClick={() => alterarStatusOficina('ativa')}
                disabled={alterandoStatusOficina}
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
                      A oficina e seus colaboradores perderão o acesso ao sistema
                      imediatamente até que ela seja reativada.
                    </AlertDialogDescription>
                  </AlertDialogHeader>
                  <AlertDialogFooter>
                    <AlertDialogCancel disabled={alterandoStatusOficina}>
                      Cancelar
                    </AlertDialogCancel>
                    <AlertDialogAction
                      onClick={() => alterarStatusOficina('suspensa')}
                      disabled={alterandoStatusOficina}
                      className="bg-destructive text-white hover:bg-destructive/90"
                    >
                      {alterandoStatusOficina ? (
                        <>
                          <Spinner className="size-4" />
                          Suspendendo...
                        </>
                      ) : (
                        'Suspender Oficina'
                      )}
                    </AlertDialogAction>
                  </AlertDialogFooter>
                </AlertDialogContent>
              </AlertDialog>
            )}

            {oficina.status === 'suspensa' && (
              <Button
                className="w-full"
                onClick={() => alterarStatusOficina('ativa')}
                disabled={alterandoStatusOficina}
              >
                <RotateCcw data-icon="inline-start" />
                {alterandoStatusOficina ? 'Reativando...' : 'Reativar oficina'}
              </Button>
            )}

            <p className="text-xs text-muted-foreground">
              {oficina.status === 'pendente' &&
                'Esta conta aguarda aprovação e ainda não pode acessar o sistema.'}
              {oficina.status === 'ativa' &&
                'Esta conta está ativa e com acesso liberado aos administradores e mecânicos.'}
              {oficina.status === 'suspensa' &&
                'Esta conta está suspensa e bloqueia o login de todos os colaboradores.'}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Seção: Administradores da Oficina */}
      <Card>
        <CardHeader className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <CardTitle className="flex items-center gap-2">
              <ShieldCheck className="size-5 text-primary" />
              Administradores da Oficina
            </CardTitle>
            <CardDescription>
              Gestores responsáveis pelo acesso administrativo e operações desta unidade.
            </CardDescription>
          </div>
          <Button onClick={handleAbrirModalAdmin} size="sm">
            <Plus data-icon="inline-start" />
            Novo Administrador
          </Button>
        </CardHeader>

        <CardContent>
          {carregandoAdmins ? (
            <div className="flex items-center justify-center py-8">
              <Spinner />
            </div>
          ) : admins.length === 0 ? (
            <EmptyState
              icon={Shield}
              title="Nenhum administrador cadastrado"
              description="Esta oficina ainda não possui administradores vinculados. Cadastre o primeiro gestor para liberar o login e a operação da oficina."
              action={
                <Button onClick={handleAbrirModalAdmin}>
                  <Plus data-icon="inline-start" />
                  Cadastrar Primeiro Administrador
                </Button>
              }
            />
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Administrador</TableHead>
                    <TableHead>E-mail de Acesso</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Ações</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {admins.map((adm) => (
                    <TableRow key={adm.id}>
                      <TableCell>
                        <div className="flex items-center gap-3">
                          <Avatar size="sm">
                            <AvatarFallback>{iniciais(adm.nome)}</AvatarFallback>
                          </Avatar>
                          <span className="font-medium">{adm.nome}</span>
                        </div>
                      </TableCell>
                      <TableCell className="text-muted-foreground">
                        {adm.email}
                      </TableCell>
                      <TableCell>
                        <Badge
                          variant="outline"
                          className={
                            adm.ativo
                              ? 'border-success/40 bg-success/15 font-medium text-success'
                              : 'border-border bg-muted text-muted-foreground'
                          }
                        >
                          {adm.ativo ? 'Ativo' : 'Inativo'}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-right">
                        {adm.ativo ? (
                          <Button
                            variant="ghost"
                            size="sm"
                            className="text-destructive hover:bg-destructive/10 hover:text-destructive"
                            onClick={() => handleToggleAdminStatus(adm)}
                          >
                            <UserX data-icon="inline-start" />
                            Desativar
                          </Button>
                        ) : (
                          <Button
                            variant="ghost"
                            size="sm"
                            className="text-success hover:bg-success/10 hover:text-success"
                            onClick={() => handleToggleAdminStatus(adm)}
                          >
                            <UserCheck data-icon="inline-start" />
                            Reativar
                          </Button>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Modal: Cadastrar Administrador */}
      <Dialog open={modalAdminAberto} onOpenChange={setModalAdminAberto}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <ShieldCheck className="size-5 text-primary" />
              Novo Administrador da Oficina
            </DialogTitle>
            <DialogDescription>
              Crie a conta de acesso do gestor responsável pela oficina{' '}
              <strong className="text-foreground">{oficina.nome}</strong>.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleCriarAdmin} className="space-y-4 pt-2">
            {adminErroGeral && (
              <Alert variant="destructive">
                <AlertCircle className="size-4" />
                <AlertDescription>{adminErroGeral}</AlertDescription>
              </Alert>
            )}

            <Field data-invalid={!!adminNomeErro || undefined}>
              <FieldLabel htmlFor="admin-nome">Nome Completo *</FieldLabel>
              <Input
                id="admin-nome"
                placeholder="Ex: Carlos Eduardo de Oliveira"
                value={adminNome}
                onChange={(e) => {
                  setAdminNome(e.target.value)
                  if (adminNomeErro) setAdminNomeErro(null)
                  if (adminErroGeral) setAdminErroGeral(null)
                }}
                required
              />
              {adminNomeErro && (
                <FieldDescription className="text-destructive">
                  {adminNomeErro}
                </FieldDescription>
              )}
            </Field>

            <Field data-invalid={!!adminEmailErro || undefined}>
              <FieldLabel htmlFor="admin-email">E-mail de Acesso *</FieldLabel>
              <Input
                id="admin-email"
                type="email"
                placeholder="gestor@oficina.com.br"
                value={adminEmail}
                onChange={(e) => {
                  setAdminEmail(e.target.value)
                  if (adminEmailErro) setAdminEmailErro(null)
                  if (adminErroGeral) setAdminErroGeral(null)
                }}
                required
              />
              {adminEmailErro && (
                <FieldDescription className="text-destructive">
                  {adminEmailErro}
                </FieldDescription>
              )}
            </Field>

            <Field data-invalid={!!adminSenhaErro || undefined}>
              <FieldLabel htmlFor="admin-senha">Senha Inicial *</FieldLabel>
              <Input
                id="admin-senha"
                type="password"
                placeholder="Mínimo de 8 caracteres"
                value={adminSenha}
                onChange={(e) => {
                  setAdminSenha(e.target.value)
                  if (adminSenhaErro) setAdminSenhaErro(null)
                  if (adminErroGeral) setAdminErroGeral(null)
                }}
                minLength={8}
                required
              />
              {adminSenhaErro ? (
                <FieldDescription className="text-destructive">
                  {adminSenhaErro}
                </FieldDescription>
              ) : (
                <FieldDescription>
                  O gestor utilizará este e-mail e senha para acessar o painel da
                  oficina.
                </FieldDescription>
              )}
            </Field>

            <Field data-invalid={!!adminConfirmarSenhaErro || undefined}>
              <FieldLabel htmlFor="admin-confirmar-senha">Confirmar Senha *</FieldLabel>
              <Input
                id="admin-confirmar-senha"
                type="password"
                placeholder="Repita a senha digitada"
                value={adminConfirmarSenha}
                onChange={(e) => {
                  setAdminConfirmarSenha(e.target.value)
                  if (adminConfirmarSenhaErro) setAdminConfirmarSenhaErro(null)
                  if (adminErroGeral) setAdminErroGeral(null)
                }}
                minLength={8}
                required
              />
              {adminConfirmarSenhaErro && (
                <FieldDescription className="text-destructive">
                  {adminConfirmarSenhaErro}
                </FieldDescription>
              )}
            </Field>

            <DialogFooter className="pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => setModalAdminAberto(false)}
                disabled={salvandoAdmin}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={salvandoAdmin}>
                {salvandoAdmin ? (
                  <>
                    <Spinner className="size-4" />
                    Cadastrando...
                  </>
                ) : (
                  <>
                    <Plus data-icon="inline-start" />
                    Cadastrar Administrador
                  </>
                )}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Modal: Editar Dados Cadastrais da Oficina */}
      <Dialog
        open={modalEdicaoOficinaAberto}
        onOpenChange={setModalEdicaoOficinaAberto}
      >
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Pencil className="size-5 text-primary" />
              Editar Dados da Oficina
            </DialogTitle>
            <DialogDescription>
              Atualize as informações cadastrais e de contato desta oficina.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSalvarEdicaoOficina} className="space-y-4 pt-2">
            <Field>
              <FieldLabel htmlFor="edit-nome">Nome da Oficina *</FieldLabel>
              <Input
                id="edit-nome"
                value={editNome}
                onChange={(e) => setEditNome(e.target.value)}
                maxLength={250}
                required
              />
            </Field>

            <Field>
              <FieldLabel htmlFor="edit-email">E-mail de Contato *</FieldLabel>
              <Input
                id="edit-email"
                type="email"
                value={editEmail}
                onChange={(e) => setEditEmail(e.target.value)}
                required
              />
            </Field>

            <Field>
              <FieldLabel htmlFor="edit-telefone">Telefone / WhatsApp *</FieldLabel>
              <Input
                id="edit-telefone"
                value={editTelefone}
                onChange={(e) => setEditTelefone(formatTelefone(e.target.value))}
                maxLength={15}
                required
              />
            </Field>

            <Field>
              <FieldLabel htmlFor="edit-endereco">Endereço Completo *</FieldLabel>
              <Input
                id="edit-endereco"
                value={editEndereco}
                onChange={(e) => setEditEndereco(e.target.value)}
                maxLength={250}
                required
              />
            </Field>

            <DialogFooter className="pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => setModalEdicaoOficinaAberto(false)}
                disabled={salvandoEdicaoOficina}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={salvandoEdicaoOficina}>
                {salvandoEdicaoOficina ? (
                  <>
                    <Spinner className="size-4" />
                    Salvando...
                  </>
                ) : (
                  'Salvar Alterações'
                )}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
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
