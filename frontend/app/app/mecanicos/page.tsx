'use client';

import { useMemo, useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import {
    Users,
    Search,
    Plus,
    Pencil,
    UserCheck,
    UserX,
    AlertCircle,
    ShieldAlert,
    Wrench,
    Shield,
    CheckCircle2,
    Check,
    RefreshCw,
} from 'lucide-react';
import { useStore } from '@/lib/store';
import { usuariosService, ordemServicoService } from '@/lib/services';
import { normalizeError } from '@/lib/error-handler';
import type { AtualizarUsuarioRequest } from '@/lib/types';
import { PageHeader } from '@/components/page-header';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Spinner } from '@/components/ui/spinner';
import {
    InputGroup,
    InputGroupAddon,
    InputGroupInput,
} from '@/components/ui/input-group';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogDescription,
    DialogFooter,
} from '@/components/ui/dialog';
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Field, FieldLabel, FieldDescription } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { EmptyState } from '@/components/empty-state';
import { toast } from 'sonner';

interface MecanicoUI {
    id: number;
    nome: string;
    email: string;
    role: 'ADMIN' | 'MECANICO' | 'SUPERADMIN';
    ativo: boolean;
    ordensAtivasCount?: number;
}

export default function MecanicosPage() {
    const { user } = useStore();
    const router = useRouter();

    const userRoleNormalized =
        user?.role?.toUpperCase().replace(/^ROLE_/, '') || '';
    const isSuperAdmin = userRoleNormalized === 'SUPERADMIN';

    // Proteção: apenas ADMIN ou SUPERADMIN podem gerenciar a equipe de mecânicos
    useEffect(() => {
        if (user && userRoleNormalized === 'MECANICO') {
            router.replace('/app');
        }
    }, [user, userRoleNormalized, router]);

    const [mecanicos, setMecanicos] = useState<MecanicoUI[]>([]);
    const [carregando, setCarregando] = useState(true);
    const [salvando, setSalvando] = useState(false);
    const [alterandoStatus, setAlterandoStatus] = useState(false);

    const [busca, setBusca] = useState('');
    const [filtroRole, setFiltroRole] = useState<
        'todos' | 'ADMIN' | 'MECANICO'
    >('todos');
    const [filtroStatus, setFiltroStatus] = useState<
        'todos' | 'ativos' | 'inativos'
    >('todos');

    // Estados dos modais
    const [modalFormAberto, setModalFormAberto] = useState(false);
    const [mecanicoEmEdicao, setMecanicoEmEdicao] = useState<MecanicoUI | null>(
        null,
    );

    // Formulário
    const [nome, setNome] = useState('');
    const [email, setEmail] = useState('');
    const [role, setRole] = useState<'ADMIN' | 'MECANICO'>('MECANICO');
    const [senha, setSenha] = useState('');
    const [confirmarSenha, setConfirmarSenha] = useState('');
    const [erroForm, setErroForm] = useState<string | null>(null);

    // Confirmação de status (Ativar/Desativar)
    const [mecanicoAlteracao, setMecanicoAlteracao] =
        useState<MecanicoUI | null>(null);
    const [dialogStatusAberto, setDialogStatusAberto] = useState(false);

    // Carregamento dos colaboradores conectado a usuariosService e ordemServicoService
    const carregarDados = useCallback(async () => {
        try {
            setCarregando(true);
            const [usuarios, ordensResumo] = await Promise.all([
                usuariosService.listar(),
                ordemServicoService.listar({ size: 200 }).catch(() => null),
            ]);

            const ordensAtivas = ordensResumo?.content
                ? ordensResumo.content.filter((o) => {
                      const st = o.statusServico || o.status;
                      return (
                          st === 'PENDENTE' ||
                          st === 'ACEITO' ||
                          st === 'EM_ANDAMENTO'
                      );
                  })
                : [];

            const listaUI: MecanicoUI[] = (usuarios || []).map((u) => {
                const count = ordensAtivas.filter(
                    (o) => o.mecanicoNome === u.nome,
                ).length;
                return {
                    id: u.id,
                    nome: u.nome,
                    email: u.email,
                    role: u.role as 'ADMIN' | 'MECANICO' | 'SUPERADMIN',
                    ativo: u.ativo,
                    ordensAtivasCount: count,
                };
            });

            setMecanicos(listaUI);
        } catch (err: unknown) {
            const norm = normalizeError(
                err,
                'Erro ao carregar colaboradores da oficina',
            );
            toast.error(norm.message);
        } finally {
            setCarregando(false);
        }
    }, []);

    useEffect(() => {
        carregarDados();
    }, [carregarDados]);

    // Filtragem da lista
    const listaFiltrada = useMemo(() => {
        return mecanicos.filter((m) => {
            const matchBusca =
                m.nome.toLowerCase().includes(busca.trim().toLowerCase()) ||
                m.email.toLowerCase().includes(busca.trim().toLowerCase());

            const matchRole = filtroRole === 'todos' || m.role === filtroRole;

            const matchStatus =
                filtroStatus === 'todos' ||
                (filtroStatus === 'ativos' && m.ativo) ||
                (filtroStatus === 'inativos' && !m.ativo);

            return matchBusca && matchRole && matchStatus;
        });
    }, [mecanicos, busca, filtroRole, filtroStatus]);

    // Abertura do formulário
    const abrirNovoMecanico = () => {
        setMecanicoEmEdicao(null);
        setNome('');
        setEmail('');
        setRole('MECANICO');
        setSenha('');
        setConfirmarSenha('');
        setErroForm(null);
        setModalFormAberto(true);
    };

    const abrirEdicaoMecanico = (m: MecanicoUI) => {
        setMecanicoEmEdicao(m);
        setNome(m.nome);
        setEmail(m.email);
        setRole(m.role === 'ADMIN' ? 'ADMIN' : 'MECANICO');
        setSenha('');
        setConfirmarSenha('');
        setErroForm(null);
        setModalFormAberto(true);
    };

    const handleSalvar = async (e: React.FormEvent) => {
        e.preventDefault();
        setErroForm(null);

        const nomeFormatado = nome.trim();
        const emailFormatado = email.trim();

        if (!nomeFormatado) {
            setErroForm('O nome é obrigatório.');
            return;
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(emailFormatado)) {
            setErroForm('Informe um e-mail válido.');
            return;
        }

        // Validação com verificação dupla de senha (digitar 2 vezes)
        if (!mecanicoEmEdicao) {
            if (!senha) {
                setErroForm(
                    'A senha é obrigatória para o cadastro de novo colaborador.',
                );
                return;
            }
            if (senha.length < 8) {
                setErroForm('A senha deve ter no mínimo 8 caracteres.');
                return;
            }
            if (!confirmarSenha) {
                setErroForm('Por favor, digite a confirmação da senha.');
                return;
            }
            if (senha !== confirmarSenha) {
                setErroForm(
                    'A confirmação de senha não confere com a senha digitada.',
                );
                return;
            }
        } else {
            // Edição: se qualquer um dos campos de senha for preenchido, exige verificação dupla
            if (senha || confirmarSenha) {
                if (!senha) {
                    setErroForm('Digite a nova senha desejada.');
                    return;
                }
                if (senha.length < 8) {
                    setErroForm(
                        'A nova senha deve ter no mínimo 8 caracteres.',
                    );
                    return;
                }
                if (!confirmarSenha) {
                    setErroForm('Por favor, confirme a nova senha.');
                    return;
                }
                if (senha !== confirmarSenha) {
                    setErroForm(
                        'A confirmação de senha não confere com a nova senha digitada.',
                    );
                    return;
                }
            }
        }

        try {
            setSalvando(true);

            if (mecanicoEmEdicao) {
                // Regra de segurança: não permite que o usuário atual rebaixe o seu próprio perfil
                if (
                    user &&
                    user.email === mecanicoEmEdicao.email &&
                    mecanicoEmEdicao.role === 'ADMIN' &&
                    role === 'MECANICO'
                ) {
                    setErroForm(
                        'Você não pode rebaixar o seu próprio perfil de administrador.',
                    );
                    setSalvando(false);
                    return;
                }

                const payload: AtualizarUsuarioRequest = {
                    nome: nomeFormatado,
                    email: emailFormatado,
                    role: role,
                    ...(senha ? { senha, confirmarSenha } : {}),
                };

                await usuariosService.atualizar(mecanicoEmEdicao.id, payload);
                toast.success('Dados do colaborador atualizados com sucesso!');
            } else {
                // Criação de novo colaborador
                if (role === 'ADMIN') {
                    const oficinaId = (user as { oficinaId?: number })
                        ?.oficinaId;
                    if (!oficinaId) {
                        setErroForm(
                            'Não foi possível identificar a oficina para cadastro do administrador.',
                        );
                        setSalvando(false);
                        return;
                    }
                    await usuariosService.cadastrarAdmin({
                        nome: nomeFormatado,
                        email: emailFormatado,
                        senha,
                        confirmarSenha,
                        oficinaId,
                    });
                    toast.success('Novo administrador cadastrado com sucesso!');
                } else {
                    await usuariosService.cadastrarMecanico({
                        nome: nomeFormatado,
                        email: emailFormatado,
                        senha,
                        confirmarSenha,
                    });
                    toast.success('Mecânico cadastrado com sucesso!');
                }
            }

            setModalFormAberto(false);
            await carregarDados();
        } catch (err: unknown) {
            const norm = normalizeError(err, 'Falha ao salvar colaborador.');
            setErroForm(norm.message);
        } finally {
            setSalvando(false);
        }
    };

    const solicitarMudancaStatus = (m: MecanicoUI) => {
        setMecanicoAlteracao(m);
        setDialogStatusAberto(true);
    };

    const confirmarMudancaStatus = async () => {
        if (!mecanicoAlteracao) return;

        // Não permite desativar a si próprio
        if (
            user &&
            user.email === mecanicoAlteracao.email &&
            mecanicoAlteracao.ativo
        ) {
            toast.error('Você não pode desativar o seu próprio usuário.');
            setDialogStatusAberto(false);
            return;
        }

        // Regra de negócio (UC03): mecânico com OS aberta não pode ser desativado
        if (
            mecanicoAlteracao.ativo &&
            (mecanicoAlteracao.ordensAtivasCount ?? 0) > 0
        ) {
            toast.error(
                'Não é possível desativar: o mecânico possui ordens de serviço pendentes ou em andamento.',
            );
            setDialogStatusAberto(false);
            return;
        }

        try {
            setAlterandoStatus(true);
            if (mecanicoAlteracao.ativo) {
                await usuariosService.desativar(mecanicoAlteracao.id);
                toast.success(`${mecanicoAlteracao.nome} desativado.`);
            } else {
                await usuariosService.ativar(mecanicoAlteracao.id);
                toast.success(
                    `${mecanicoAlteracao.nome} reativado com sucesso!`,
                );
            }
            setDialogStatusAberto(false);
            await carregarDados();
        } catch (err: unknown) {
            const norm = normalizeError(
                err,
                'Erro ao alterar o status do colaborador.',
            );
            toast.error(norm.message);
        } finally {
            setAlterandoStatus(false);
        }
    };

    return (
        <div className="flex flex-col gap-6">
            <PageHeader
                title="Equipe da Oficina & Mecânicos"
                description="Cadastre profissionais, gerencie mecânicos e acompanhe o nível de acesso e ordens ativas da equipe."
                actions={
                    <div className="flex items-center gap-2">
                        <Button
                            variant="outline"
                            size="icon"
                            onClick={carregarDados}
                            disabled={carregando}
                            title="Recarregar lista"
                        >
                            <RefreshCw
                                className={`size-4 ${carregando ? 'animate-spin' : ''}`}
                            />
                        </Button>
                        <Button onClick={abrirNovoMecanico}>
                            <Plus data-icon="inline-start" />
                            Novo Colaborador
                        </Button>
                    </div>
                }
            />

            <Card>
                <CardContent className="flex flex-col gap-4 pt-6">
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                        <InputGroup className="sm:max-w-sm">
                            <InputGroupAddon>
                                <Search />
                            </InputGroupAddon>
                            <InputGroupInput
                                placeholder="Buscar por nome ou e-mail..."
                                value={busca}
                                onChange={(e) => setBusca(e.target.value)}
                            />
                        </InputGroup>

                        <div className="flex flex-wrap items-center gap-2">
                            <Select
                                value={filtroRole}
                                onValueChange={(val) =>
                                    setFiltroRole(
                                        val as 'todos' | 'ADMIN' | 'MECANICO',
                                    )
                                }
                            >
                                <SelectTrigger className="w-[140px]">
                                    <SelectValue placeholder="Perfil" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="todos">
                                        Todos Perfis
                                    </SelectItem>
                                    <SelectItem value="MECANICO">
                                        Mecânicos
                                    </SelectItem>
                                    <SelectItem value="ADMIN">
                                        Administradores
                                    </SelectItem>
                                </SelectContent>
                            </Select>

                            <Select
                                value={filtroStatus}
                                onValueChange={(val) =>
                                    setFiltroStatus(
                                        val as 'todos' | 'ativos' | 'inativos',
                                    )
                                }
                            >
                                <SelectTrigger className="w-[130px]">
                                    <SelectValue placeholder="Status" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="todos">
                                        Todos Status
                                    </SelectItem>
                                    <SelectItem value="ativos">
                                        Ativos
                                    </SelectItem>
                                    <SelectItem value="inativos">
                                        Inativos
                                    </SelectItem>
                                </SelectContent>
                            </Select>

                            <Badge
                                variant="secondary"
                                className="px-2.5 py-1 text-xs"
                            >
                                {listaFiltrada.length}{' '}
                                {listaFiltrada.length === 1
                                    ? 'conta'
                                    : 'contas'}
                            </Badge>
                        </div>
                    </div>

                    {carregando ? (
                        <div className="flex items-center justify-center py-12">
                            <Spinner className="size-6 text-primary" />
                        </div>
                    ) : listaFiltrada.length === 0 ? (
                        <EmptyState
                            icon={Users}
                            title="Nenhum colaborador encontrado"
                            description="Nenhum profissional corresponde aos filtros ou à busca realizada."
                            action={
                                <Button
                                    variant="outline"
                                    onClick={abrirNovoMecanico}
                                >
                                    <Plus data-icon="inline-start" />
                                    Cadastrar primeiro profissional
                                </Button>
                            }
                        />
                    ) : (
                        <div className="rounded-md border border-border">
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>Profissional</TableHead>
                                        <TableHead>E-mail</TableHead>
                                        <TableHead>
                                            Nível de Acesso (Role)
                                        </TableHead>
                                        <TableHead>OS Ativas</TableHead>
                                        <TableHead>Status</TableHead>
                                        <TableHead className="text-right">
                                            Ações
                                        </TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {listaFiltrada.map((m) => {
                                        const isAdmin =
                                            m.role === 'ADMIN' ||
                                            m.role === 'SUPERADMIN';
                                        const isSelf = user?.email === m.email;

                                        return (
                                            <TableRow key={m.id}>
                                                <TableCell>
                                                    <div className="flex items-center gap-3">
                                                        <Avatar className="size-8">
                                                            <AvatarFallback
                                                                className={
                                                                    isAdmin
                                                                        ? 'bg-primary text-primary-foreground text-xs font-semibold'
                                                                        : 'bg-sidebar-accent text-sidebar-accent-foreground text-xs font-semibold'
                                                                }
                                                            >
                                                                {m.nome
                                                                    .slice(0, 2)
                                                                    .toUpperCase()}
                                                            </AvatarFallback>
                                                        </Avatar>
                                                        <div className="flex flex-col">
                                                            <span className="font-medium text-foreground">
                                                                {m.nome}
                                                                {isSelf && (
                                                                    <span className="ml-1.5 text-xs text-muted-foreground font-normal">
                                                                        (Você)
                                                                    </span>
                                                                )}
                                                            </span>
                                                        </div>
                                                    </div>
                                                </TableCell>
                                                <TableCell className="text-muted-foreground">
                                                    {m.email}
                                                </TableCell>
                                                <TableCell>
                                                    {isAdmin ? (
                                                        <Badge
                                                            variant="default"
                                                            className="gap-1.5 font-medium text-xs"
                                                        >
                                                            <Shield className="size-3" />
                                                            {m.role ===
                                                            'SUPERADMIN'
                                                                ? 'Superadmin'
                                                                : 'Administrador'}
                                                        </Badge>
                                                    ) : (
                                                        <Badge
                                                            variant="outline"
                                                            className="gap-1.5 font-medium text-xs text-muted-foreground border-border"
                                                        >
                                                            <Wrench className="size-3 text-muted-foreground" />
                                                            Mecânico
                                                        </Badge>
                                                    )}
                                                </TableCell>
                                                <TableCell>
                                                    <span className="text-xs font-medium text-muted-foreground">
                                                        {m.ordensAtivasCount ??
                                                            0}{' '}
                                                        em andamento
                                                    </span>
                                                </TableCell>
                                                <TableCell>
                                                    <Badge
                                                        variant="outline"
                                                        className={
                                                            m.ativo
                                                                ? 'bg-success/15 text-success border-success/40'
                                                                : 'bg-muted text-muted-foreground border-border'
                                                        }
                                                    >
                                                        {m.ativo
                                                            ? 'Ativo'
                                                            : 'Inativo'}
                                                    </Badge>
                                                </TableCell>
                                                <TableCell className="text-right">
                                                    <div className="flex items-center justify-end gap-1">
                                                        <Button
                                                            variant="ghost"
                                                            size="sm"
                                                            onClick={() =>
                                                                abrirEdicaoMecanico(
                                                                    m,
                                                                )
                                                            }
                                                            title="Editar dados"
                                                        >
                                                            <Pencil className="size-4" />
                                                            <span className="sr-only">
                                                                Editar
                                                            </span>
                                                        </Button>
                                                        <Button
                                                            variant="ghost"
                                                            size="sm"
                                                            onClick={() =>
                                                                solicitarMudancaStatus(
                                                                    m,
                                                                )
                                                            }
                                                            disabled={
                                                                alterandoStatus
                                                            }
                                                            className={
                                                                m.ativo
                                                                    ? 'text-destructive hover:bg-destructive/10'
                                                                    : 'text-success hover:bg-success/10'
                                                            }
                                                            title={
                                                                m.ativo
                                                                    ? 'Desativar colaborador'
                                                                    : 'Reativar colaborador'
                                                            }
                                                        >
                                                            {m.ativo ? (
                                                                <UserX className="size-4" />
                                                            ) : (
                                                                <UserCheck className="size-4" />
                                                            )}
                                                            <span className="sr-only">
                                                                {m.ativo
                                                                    ? 'Desativar'
                                                                    : 'Ativar'}
                                                            </span>
                                                        </Button>
                                                    </div>
                                                </TableCell>
                                            </TableRow>
                                        );
                                    })}
                                </TableBody>
                            </Table>
                        </div>
                    )}
                </CardContent>
            </Card>

            {/* Modal de Cadastro / Edição com Seletor de Role */}
            <Dialog open={modalFormAberto} onOpenChange={setModalFormAberto}>
                <DialogContent className="sm:max-w-md">
                    <DialogHeader>
                        <DialogTitle className="flex items-center gap-2">
                            {role === 'ADMIN' ? (
                                <Shield className="size-5 text-primary" />
                            ) : (
                                <Wrench className="size-5 text-primary" />
                            )}
                            {mecanicoEmEdicao
                                ? 'Editar Colaborador'
                                : 'Novo Colaborador'}
                        </DialogTitle>
                        <DialogDescription>
                            {mecanicoEmEdicao
                                ? 'Atualize o nome, e-mail ou redefina a senha do colaborador.'
                                : 'Cadastre um novo profissional na equipe da sua oficina.'}
                        </DialogDescription>
                    </DialogHeader>

                    <form
                        onSubmit={handleSalvar}
                        className="flex flex-col gap-4 py-2"
                    >
                        {erroForm && (
                            <Alert variant="destructive">
                                <AlertCircle className="size-4" />
                                <AlertDescription>{erroForm}</AlertDescription>
                            </Alert>
                        )}

                        <Field>
                            <FieldLabel htmlFor="mecanico-nome">
                                Nome Completo
                            </FieldLabel>
                            <Input
                                id="mecanico-nome"
                                value={nome}
                                onChange={(e) => setNome(e.target.value)}
                                placeholder="Ex: Pedro Henrique"
                                required
                            />
                        </Field>

                        <Field>
                            <FieldLabel htmlFor="mecanico-email">
                                E-mail
                            </FieldLabel>
                            <Input
                                id="mecanico-email"
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="pedro@oficina.com"
                                required
                            />
                        </Field>

                        {/* Opção de Role / Nível de Acesso */}
                        <Field>
                            <FieldLabel htmlFor="mecanico-role">
                                Nível de Acesso (Role)
                            </FieldLabel>
                            <Select
                                value={role}
                                onValueChange={(val) =>
                                    setRole(val as 'ADMIN' | 'MECANICO')
                                }
                                disabled={
                                    mecanicoEmEdicao?.role === 'SUPERADMIN' ||
                                    (Boolean(mecanicoEmEdicao) && user?.email === mecanicoEmEdicao?.email && mecanicoEmEdicao?.role === 'ADMIN')
                                }
                            >
                                <SelectTrigger id="mecanico-role">
                                    <SelectValue placeholder="Selecione o nível de acesso" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="MECANICO">
                                        <div className="flex flex-col text-left py-0.5">
                                            <span className="font-medium">
                                                Mecânico
                                            </span>
                                            <span className="text-xs text-muted-foreground">
                                                Executa ordens de serviço,
                                                insere peças e serviços.
                                            </span>
                                        </div>
                                    </SelectItem>
                                    <SelectItem
                                        value="ADMIN"
                                    >
                                        <div className="flex flex-col text-left py-0.5">
                                            <span className="font-medium text-primary">
                                                Administrador
                                            </span>
                                            <span className="text-xs text-muted-foreground">
                                                Acesso total: gerencia equipe, oficina e clientes.
                                            </span>
                                        </div>
                                    </SelectItem>
                                </SelectContent>
                            </Select>
                            <FieldDescription>
                                {mecanicoEmEdicao
                                    ? 'Altere o nível de acesso entre Mecânico e Administrador conforme as permissões desejadas.'
                                    : isSuperAdmin
                                      ? 'Defina se o profissional atuará como Mecânico ou Administrador.'
                                      : 'Defina o nível de acesso do colaborador na oficina.'}
                            </FieldDescription>
                        </Field>

                        {/* Seção de Senha com Verificação Dupla */}
                        <div className="rounded-lg border border-border bg-muted/30 p-3 flex flex-col gap-3">
                            <span className="text-xs font-semibold text-foreground uppercase tracking-wider">
                                {mecanicoEmEdicao
                                    ? 'Alterar Senha (Opcional)'
                                    : 'Definir Senha de Acesso'}
                            </span>

                            <Field>
                                <FieldLabel htmlFor="mecanico-senha">
                                    {mecanicoEmEdicao ? 'Nova Senha' : 'Senha'}
                                </FieldLabel>
                                <Input
                                    id="mecanico-senha"
                                    type="password"
                                    value={senha}
                                    onChange={(e) => setSenha(e.target.value)}
                                    placeholder={
                                        mecanicoEmEdicao
                                            ? 'Deixe em branco para manter a atual'
                                            : 'Mínimo de 8 caracteres'
                                    }
                                    required={!mecanicoEmEdicao}
                                />
                                <FieldDescription>
                                    {mecanicoEmEdicao
                                        ? 'Preencha apenas se desejar redefinir o acesso.'
                                        : 'Mínimo de 8 caracteres alfanuméricos.'}
                                </FieldDescription>
                            </Field>

                            <Field>
                                <FieldLabel htmlFor="mecanico-confirmar-senha">
                                    {mecanicoEmEdicao
                                        ? 'Confirmar Nova Senha'
                                        : 'Confirmar Senha'}
                                </FieldLabel>
                                <Input
                                    id="mecanico-confirmar-senha"
                                    type="password"
                                    value={confirmarSenha}
                                    onChange={(e) =>
                                        setConfirmarSenha(e.target.value)
                                    }
                                    placeholder="Repita a senha exatamente igual"
                                    required={
                                        !mecanicoEmEdicao || senha.length > 0
                                    }
                                />
                                {senha.length > 0 &&
                                    confirmarSenha.length > 0 &&
                                    senha === confirmarSenha && (
                                        <span className="mt-1 flex items-center gap-1 text-xs font-medium text-success">
                                            <Check className="size-3.5" /> As
                                            senhas conferem
                                        </span>
                                    )}
                                {senha.length > 0 &&
                                    confirmarSenha.length > 0 &&
                                    senha !== confirmarSenha && (
                                        <span className="mt-1 flex items-center gap-1 text-xs font-medium text-destructive">
                                            <AlertCircle className="size-3.5" />{' '}
                                            As senhas não coincidem
                                        </span>
                                    )}
                            </Field>
                        </div>

                        <DialogFooter className="mt-4 pt-3">
                            <Button
                                type="button"
                                variant="outline"
                                onClick={() => setModalFormAberto(false)}
                                disabled={salvando}
                            >
                                Cancelar
                            </Button>
                            <Button type="submit" disabled={salvando}>
                                {salvando ? (
                                    <>
                                        <Spinner className="size-4 mr-2" />
                                        Salvando...
                                    </>
                                ) : mecanicoEmEdicao ? (
                                    'Salvar Alterações'
                                ) : (
                                    'Cadastrar Colaborador'
                                )}
                            </Button>
                        </DialogFooter>
                    </form>
                </DialogContent>
            </Dialog>

            {/* AlertDialog de Confirmação de Desativação / Ativação */}
            <AlertDialog
                open={dialogStatusAberto}
                onOpenChange={setDialogStatusAberto}
            >
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle className="flex items-center gap-2">
                            {mecanicoAlteracao?.ativo ? (
                                <ShieldAlert className="size-5 text-destructive" />
                            ) : (
                                <CheckCircle2 className="size-5 text-success" />
                            )}
                            {mecanicoAlteracao?.ativo
                                ? 'Desativar Colaborador?'
                                : 'Reativar Colaborador?'}
                        </AlertDialogTitle>
                        <AlertDialogDescription>
                            {mecanicoAlteracao?.ativo ? (
                                <>
                                    Ao desativar{' '}
                                    <strong>{mecanicoAlteracao.nome}</strong>,
                                    ele perderá o acesso ao sistema e não poderá
                                    receber ordens de serviço.
                                    {(mecanicoAlteracao.ordensAtivasCount ??
                                        0) > 0 && (
                                        <span className="mt-2 block text-destructive font-medium">
                                            Atenção: Este mecânico possui{' '}
                                            {
                                                mecanicoAlteracao.ordensAtivasCount
                                            }{' '}
                                            ordem(ns) de serviço em andamento.
                                            Conclua ou transfira as OS antes de
                                            efetuar a desativação.
                                        </span>
                                    )}
                                </>
                            ) : (
                                <>
                                    Deseja reativar o acesso de{' '}
                                    <strong>{mecanicoAlteracao?.nome}</strong>?
                                    Ele voltará a ter permissão de login na
                                    oficina.
                                </>
                            )}
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel disabled={alterandoStatus}>
                            Cancelar
                        </AlertDialogCancel>
                        <AlertDialogAction
                            onClick={confirmarMudancaStatus}
                            disabled={alterandoStatus}
                            className={
                                mecanicoAlteracao?.ativo
                                    ? 'bg-destructive text-destructive-foreground hover:bg-destructive/90'
                                    : ''
                            }
                        >
                            {alterandoStatus ? (
                                <>
                                    <Spinner className="size-4 mr-2" />
                                    Processando...
                                </>
                            ) : mecanicoAlteracao?.ativo ? (
                                'Confirmar Desativação'
                            ) : (
                                'Confirmar Reativação'
                            )}
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
}
