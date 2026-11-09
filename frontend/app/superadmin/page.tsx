'use client'

import { useEffect, useMemo, useState, useCallback } from 'react'
import { useRouter } from 'next/navigation'
import { Search, Building2, Plus, AlertCircle } from 'lucide-react'
import { toast } from 'sonner'
import type { Oficina, OficinaStatus } from '@/lib/types'
import { formatDate, formatTelefone, isValidTelefone } from '@/lib/format'
import { oficinasService } from '@/lib/services'
import { normalizeError } from '@/lib/error-handler'
import { PageHeader } from '@/components/page-header'
import { OficinaStatusBadge } from '@/components/status-badge'
import { EmptyState } from '@/components/empty-state'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Spinner } from '@/components/ui/spinner'
import { Input } from '@/components/ui/input'
import { Field, FieldLabel, FieldDescription } from '@/components/ui/field'
import { Alert, AlertDescription } from '@/components/ui/alert'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog'
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
  const [oficinas, setOficinas] = useState<Oficina[]>([])
  const [carregando, setCarregando] = useState(true)
  const router = useRouter()
  const [busca, setBusca] = useState('')
  const [filtro, setFiltro] = useState<Filtro>('todas')

  // Estado do Modal de Cadastro de Oficina
  const [modalAberto, setModalAberto] = useState(false)
  const [salvando, setSalvando] = useState(false)
  const [nome, setNome] = useState('')
  const [telefone, setTelefone] = useState('')
  const [endereco, setEndereco] = useState('')
  const [email, setEmail] = useState('')

  // Estados de erro
  const [nomeErro, setNomeErro] = useState<string | null>(null)
  const [telefoneErro, setTelefoneErro] = useState<string | null>(null)
  const [enderecoErro, setEnderecoErro] = useState<string | null>(null)
  const [emailErro, setEmailErro] = useState<string | null>(null)
  const [erroGeral, setErroGeral] = useState<string | null>(null)

  // Carrega lista de oficinas do backend
  const carregarOficinas = useCallback(async () => {
    try {
      setCarregando(true)
      const data = await oficinasService.listar()
      if (Array.isArray(data)) {
        const apiOficinas: Oficina[] = data.map((o) => ({
          id: String(o.id),
          nome: o.nome,
          endereco: o.endereco,
          telefone: o.telefone,
          email: o.email || '',
          dataCadastro: new Date().toISOString().slice(0, 10),
          status: o.ativo ? 'ativa' : 'suspensa',
        }))
        setOficinas(apiOficinas)
      }
    } catch {
      toast.error('Erro ao carregar oficinas da plataforma.')
      setOficinas([])
    } finally {
      setCarregando(false)
    }
  }, [])

  useEffect(() => {
    carregarOficinas()
  }, [carregarOficinas])

  const resetForm = () => {
    setNome('')
    setTelefone('')
    setEndereco('')
    setEmail('')
    setNomeErro(null)
    setTelefoneErro(null)
    setEnderecoErro(null)
    setEmailErro(null)
    setErroGeral(null)
  }

  const handleAbrirModal = () => {
    resetForm()
    setModalAberto(true)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setErroGeral(null)

    let temErro = false

    if (!nome.trim()) {
      setNomeErro('O nome da oficina é obrigatório.')
      temErro = true
    } else {
      setNomeErro(null)
    }

    if (!telefone.trim()) {
      setTelefoneErro('O telefone é obrigatório.')
      temErro = true
    } else if (!isValidTelefone(telefone)) {
      setTelefoneErro('Informe um telefone válido com DDD (ex: (11) 98765-4321).')
      temErro = true
    } else {
      setTelefoneErro(null)
    }

    if (!endereco.trim()) {
      setEnderecoErro('O endereço é obrigatório.')
      temErro = true
    } else {
      setEnderecoErro(null)
    }

    if (!email.trim()) {
      setEmailErro('O e-mail é obrigatório.')
      temErro = true
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      setEmailErro('Informe um e-mail válido.')
      temErro = true
    } else {
      setEmailErro(null)
    }

    if (temErro) return

    setSalvando(true)
    try {
      const res = await oficinasService.criar({
        nome: nome.trim(),
        endereco: endereco.trim(),
        telefone: telefone.trim(),
        email: email.trim(),
      })

      const novaOficina: Oficina = {
        id: String(res.id),
        nome: res.nome,
        endereco: res.endereco,
        telefone: res.telefone,
        email: res.email || email.trim(),
        dataCadastro: new Date().toISOString().slice(0, 10),
        status: res.ativo ? 'ativa' : 'suspensa',
      }

      setOficinas((prev) => [
        novaOficina,
        ...prev.filter((o) => o.id !== novaOficina.id),
      ])
      toast.success(
        'Oficina cadastrada com sucesso! Agora cadastre o primeiro administrador para liberar o acesso.',
      )
      setModalAberto(false)
      resetForm()
      router.push(`/superadmin/oficinas/${res.id}?novoAdmin=true`)
    } catch (err: unknown) {
      const normalized = normalizeError(err)

      const msgLower = (normalized.message || '').toLowerCase()
      if (msgLower.includes('nome')) {
        setNomeErro(normalized.message)
      } else if (msgLower.includes('telefone')) {
        setTelefoneErro(normalized.message)
      } else if (msgLower.includes('e-mail') || msgLower.includes('email')) {
        setEmailErro(normalized.message)
      } else {
        setErroGeral(normalized.message)
      }

      toast.error(normalized.title, {
        description: normalized.message,
      })
    } finally {
      setSalvando(false)
    }
  }

  const filtradas = useMemo(() => {
    const termo = busca.trim().toLowerCase()
    const digitosBusca = busca.replace(/\D/g, '')

    return oficinas.filter((o) => {
      const matchNome = o.nome.toLowerCase().includes(termo)
      const matchTelefone = o.telefone
        ? o.telefone.replace(/\D/g, '').includes(digitosBusca)
        : false
      const matchEmail = o.email ? o.email.toLowerCase().includes(termo) : false
      const matchBusca =
        !termo || matchNome || (digitosBusca && matchTelefone) || matchEmail
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
        actions={
          <Button onClick={handleAbrirModal}>
            <Plus data-icon="inline-start" />
            Cadastrar Oficina
          </Button>
        }
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
                placeholder="Buscar por nome ou telefone"
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
              title={
                busca || filtro !== 'todas'
                  ? 'Nenhuma oficina encontrada'
                  : 'Nenhuma oficina cadastrada'
              }
              description={
                busca || filtro !== 'todas'
                  ? 'Ajuste a busca ou o filtro de status para ver resultados.'
                  : 'Comece cadastrando a primeira oficina parceira na plataforma.'
              }
              action={
                !busca && filtro === 'todas' ? (
                  <Button onClick={handleAbrirModal}>
                    <Plus data-icon="inline-start" />
                    Cadastrar Oficina
                  </Button>
                ) : undefined
              }
            />
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Oficina</TableHead>
                    <TableHead className="hidden md:table-cell">Telefone</TableHead>
                    <TableHead className="hidden lg:table-cell">Endereço</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="hidden sm:table-cell">Cadastro</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filtradas.map((o) => (
                    <TableRow
                      key={o.id}
                      className="cursor-pointer"
                      onClick={() => router.push(`/superadmin/oficinas/${o.id}`)}
                    >
                      <TableCell>
                        <div className="flex flex-col">
                          <span className="font-medium">{o.nome}</span>
                          {o.email && (
                            <span className="text-xs text-muted-foreground md:hidden">
                              {o.email}
                            </span>
                          )}
                        </div>
                      </TableCell>
                      <TableCell className="hidden text-muted-foreground md:table-cell">
                        {o.telefone || o.email || '-'}
                      </TableCell>
                      <TableCell className="hidden max-w-xs truncate text-muted-foreground lg:table-cell">
                        {o.endereco}
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

      {/* Modal de Cadastrar Oficina */}
      <Dialog open={modalAberto} onOpenChange={setModalAberto}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Building2 className="size-5 text-primary" />
              Cadastrar Oficina
            </DialogTitle>
            <DialogDescription>
              Informe os dados cadastrais da nova oficina parceira da plataforma.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmit} className="space-y-4 pt-2">
            {erroGeral && (
              <Alert variant="destructive">
                <AlertCircle className="size-4" />
                <AlertDescription>{erroGeral}</AlertDescription>
              </Alert>
            )}

            <Field data-invalid={!!nomeErro || undefined}>
              <FieldLabel htmlFor="oficina-nome">Nome da Oficina *</FieldLabel>
              <Input
                id="oficina-nome"
                placeholder="Ex: Auto Mecânica Precision"
                value={nome}
                onChange={(e) => {
                  setNome(e.target.value)
                  if (nomeErro) setNomeErro(null)
                  if (erroGeral) setErroGeral(null)
                }}
                maxLength={250}
                required
              />
              {nomeErro && (
                <FieldDescription className="text-destructive">
                  {nomeErro}
                </FieldDescription>
              )}
            </Field>

            <Field data-invalid={!!telefoneErro || undefined}>
              <FieldLabel htmlFor="oficina-telefone">Telefone / WhatsApp *</FieldLabel>
              <Input
                id="oficina-telefone"
                placeholder="(11) 98765-4321"
                value={telefone}
                onChange={(e) => {
                  setTelefone(formatTelefone(e.target.value))
                  if (telefoneErro) setTelefoneErro(null)
                  if (erroGeral) setErroGeral(null)
                }}
                maxLength={15}
                required
              />
              {telefoneErro && (
                <FieldDescription className="text-destructive">
                  {telefoneErro}
                </FieldDescription>
              )}
            </Field>

            <Field data-invalid={!!enderecoErro || undefined}>
              <FieldLabel htmlFor="oficina-endereco">Endereço Completo *</FieldLabel>
              <Input
                id="oficina-endereco"
                placeholder="Ex: Av. das Indústrias, 1500 - São Paulo, SP"
                value={endereco}
                onChange={(e) => {
                  setEndereco(e.target.value)
                  if (enderecoErro) setEnderecoErro(null)
                  if (erroGeral) setErroGeral(null)
                }}
                maxLength={250}
                required
              />
              {enderecoErro && (
                <FieldDescription className="text-destructive">
                  {enderecoErro}
                </FieldDescription>
              )}
            </Field>

            <Field data-invalid={!!emailErro || undefined}>
              <FieldLabel htmlFor="oficina-email">E-mail de Contato *</FieldLabel>
              <Input
                id="oficina-email"
                type="email"
                placeholder="contato@oficina.com.br"
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value)
                  if (emailErro) setEmailErro(null)
                  if (erroGeral) setErroGeral(null)
                }}
                required
              />
              {emailErro && (
                <FieldDescription className="text-destructive">
                  {emailErro}
                </FieldDescription>
              )}
            </Field>

            <DialogFooter className="pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => setModalAberto(false)}
                disabled={salvando}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={salvando}>
                {salvando ? (
                  <>
                    <Spinner className="size-4" />
                    Cadastrando...
                  </>
                ) : (
                  <>
                    <Plus data-icon="inline-start" />
                    Cadastrar Oficina
                  </>
                )}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
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
