"use client";

import { useState, useMemo, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import {
  Building2,
  Users,
  Shield,
  Wrench,
  Search,
  Plus,
  Pencil,
  UserCheck,
  UserX,
  Calculator,
  Save,
  RotateCcw,
  CheckCircle2,
  Check,
  AlertCircle,
  ShieldAlert,
  Coins,
  Clock,
  Car,
  TrendingUp,
  Info,
  RefreshCw,
  ExternalLink,
  Package,
} from "lucide-react";
import { useStore } from "@/lib/store";
import { oficinasService, usuariosService } from "@/lib/services";
import { formatCurrency, formatTelefone } from "@/lib/format";
import { normalizeError } from "@/lib/error-handler";
import type {
  OficinaResponse,
  UsuarioResponse,
  AtualizarUsuarioRequest,
} from "@/lib/types";
import { PageHeader } from "@/components/page-header";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  CardFooter,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Field, FieldLabel, FieldDescription } from "@/components/ui/field";
import {
  InputGroup,
  InputGroupAddon,
  InputGroupInput,
} from "@/components/ui/input-group";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { EmptyState } from "@/components/empty-state";
import { Spinner } from "@/components/ui/spinner";
import { cn } from "@/lib/utils";
import { toast } from "sonner";

interface UsuarioOficinaUI {
  id: number;
  nome: string;
  email: string;
  role: "ADMIN" | "MECANICO";
  ativo: boolean;
}

export default function MinhaOficinaPage() {
  const { user } = useStore();
  const router = useRouter();

  const userRoleNormalized =
    user?.role?.toUpperCase().replace(/^ROLE_/, "") || "";

  // Proteção: apenas ADMIN ou SUPERADMIN da oficina pode gerenciar os parâmetros da oficina
  useEffect(() => {
    if (user && userRoleNormalized === "MECANICO") {
      router.replace("/app");
    }
  }, [user, userRoleNormalized, router]);

  // Estados de Carregamento
  const [carregando, setCarregando] = useState(true);
  const [salvandoOficina, setSalvandoOficina] = useState(false);
  const [oficinaOriginal, setOficinaOriginal] =
    useState<OficinaResponse | null>(null);

  // Formulário de dados cadastrais
  const [nomeOficina, setNomeOficina] = useState("");
  const [emailOficina, setEmailOficina] = useState("");
  const [telefone, setTelefone] = useState("");
  const [endereco, setEndereco] = useState("");
  const [oficinaAtiva, setOficinaAtiva] = useState(true);

  // Parâmetros de Precificação da Oficina (Mão de Obra, Risco FIPE e Margem sobre Peças)
  const [valorHora, setValorHora] = useState(100.0);
  const [taxaRisco, setTaxaRisco] = useState(0.1);
  const [margemPeca, setMargemPeca] = useState(1.0);

  // Variáveis para o simulador interativo (Calculadora baseada em FIPE)
  const [simHoras, setSimHoras] = useState(2);
  const [simCustoPecas, setSimCustoPecas] = useState(150);
  const [simFipe, setSimFipe] = useState(80000);

  // Lista de usuários da oficina
  const [usuarios, setUsuarios] = useState<UsuarioOficinaUI[]>([]);
  const [buscaUsuarios, setBuscaUsuarios] = useState("");
  const [filtroPerfil, setFiltroPerfil] = useState<
    "todos" | "ADMIN" | "MECANICO"
  >("todos");
  const [filtroStatus, setFiltroStatus] = useState<
    "todos" | "ativos" | "inativos"
  >("todos");

  // Modal de edição / criação de usuário
  const [modalUsuarioAberto, setModalUsuarioAberto] = useState(false);
  const [usuarioEmEdicao, setUsuarioEmEdicao] =
    useState<UsuarioOficinaUI | null>(null);
  const [usuarioNome, setUsuarioNome] = useState("");
  const [usuarioEmail, setUsuarioEmail] = useState("");
  const [usuarioRole, setUsuarioRole] = useState<"ADMIN" | "MECANICO">(
    "MECANICO"
  );
  const [usuarioSenha, setUsuarioSenha] = useState("");
  const [usuarioConfirmarSenha, setUsuarioConfirmarSenha] = useState("");
  const [salvandoUsuario, setSalvandoUsuario] = useState(false);
  const [erroUsuario, setErroUsuario] = useState<string | null>(null);

  // Diálogo de confirmação de status
  const [usuarioAlteracao, setUsuarioAlteracao] =
    useState<UsuarioOficinaUI | null>(null);
  const [dialogStatusAberto, setDialogStatusAberto] = useState(false);
  const [alterandoStatus, setAlterandoStatus] = useState(false);

  // Carrega dados da oficina e lista de usuários
  const carregarDadosOficina = useCallback(async () => {
    const oficinaIdNum = user?.oficinaId ? Number(user.oficinaId) : null;
    if (!oficinaIdNum) return;

    try {
      setCarregando(true);
      const [dadosOficina, listaUsuarios] = await Promise.all([
        oficinasService.buscarPorId(oficinaIdNum),
        usuariosService.listar({ oficinaId: oficinaIdNum }).catch(() => []),
      ]);

      setOficinaOriginal(dadosOficina);
      setNomeOficina(dadosOficina.nome || "");
      setEmailOficina(dadosOficina.email || "");
      setTelefone(dadosOficina.telefone || "");
      setEndereco(dadosOficina.endereco || "");
      setValorHora(
        dadosOficina.valorHora != null ? Number(dadosOficina.valorHora) : 100.0
      );
      setTaxaRisco(
        dadosOficina.taxaRisco != null ? Number(dadosOficina.taxaRisco) : 0.1
      );
      setMargemPeca(
        dadosOficina.margemPeca != null ? Number(dadosOficina.margemPeca) : 1.0
      );
      setOficinaAtiva(dadosOficina.ativo !== false);

      const usuariosFormatados: UsuarioOficinaUI[] = (listaUsuarios || [])
        .filter((u: UsuarioResponse) => u.role !== "SUPERADMIN")
        .map((u: UsuarioResponse) => ({
          id: u.id,
          nome: u.nome,
          email: u.email,
          role:
            (u.role?.toUpperCase().replace(/^ROLE_/, "") as
              | "ADMIN"
              | "MECANICO") || "MECANICO",
          ativo: u.ativo,
        }));
      setUsuarios(usuariosFormatados);
    } catch (err: unknown) {
      const norm = normalizeError(err, "Falha ao carregar dados da oficina.");
      toast.error(norm.message);
    } finally {
      setCarregando(false);
    }
  }, [user?.oficinaId]);

  useEffect(() => {
    carregarDadosOficina();
  }, [carregarDadosOficina]);

  const temAlteracoesPendentes = useMemo(() => {
    if (!oficinaOriginal) return false;
    return (
      nomeOficina.trim() !== (oficinaOriginal.nome || "") ||
      emailOficina.trim() !== (oficinaOriginal.email || "") ||
      telefone.trim() !== (oficinaOriginal.telefone || "") ||
      endereco.trim() !== (oficinaOriginal.endereco || "") ||
      valorHora !== Number(oficinaOriginal.valorHora ?? 100) ||
      taxaRisco !== Number(oficinaOriginal.taxaRisco ?? 0.1) ||
      margemPeca !== Number(oficinaOriginal.margemPeca ?? 1.0)
    );
  }, [
    oficinaOriginal,
    nomeOficina,
    emailOficina,
    telefone,
    endereco,
    valorHora,
    taxaRisco,
    margemPeca,
  ]);

  // Filtragem da lista de usuários
  const usuariosFiltrados = useMemo(() => {
    return usuarios.filter((u) => {
      const matchBusca =
        u.nome.toLowerCase().includes(buscaUsuarios.trim().toLowerCase()) ||
        u.email.toLowerCase().includes(buscaUsuarios.trim().toLowerCase());

      const matchPerfil = filtroPerfil === "todos" || u.role === filtroPerfil;

      const matchStatus =
        filtroStatus === "todos" ||
        (filtroStatus === "ativos" && u.ativo) ||
        (filtroStatus === "inativos" && !u.ativo);

      return matchBusca && matchPerfil && matchStatus;
    });
  }, [usuarios, buscaUsuarios, filtroPerfil, filtroStatus]);

  // Cálculos do Modelo Matemático (Alinhado com a CalculadoraOrcamento do backend)
  const fatorVeiculoSimulado = useMemo(() => {
    const fator = 1 + (simFipe / 100000) * taxaRisco;
    return Math.max(1, fator);
  }, [simFipe, taxaRisco]);

  const totalPecasSimulado = useMemo(() => {
    return simCustoPecas * margemPeca;
  }, [simCustoPecas, margemPeca]);

  const totalMaoDeObraSimulado = useMemo(() => {
    return simHoras * valorHora * fatorVeiculoSimulado;
  }, [simHoras, valorHora, fatorVeiculoSimulado]);

  const valorFinalSimulado = useMemo(() => {
    return totalPecasSimulado + totalMaoDeObraSimulado;
  }, [totalPecasSimulado, totalMaoDeObraSimulado]);

  // Cenários Comparativos Fixos
  const corsaFator = 1 + (20000 / 100000) * taxaRisco;
  const corsaPecas = 100 * margemPeca;
  const corsaMO = 2 * valorHora * corsaFator;
  const corsaTotal = corsaPecas + corsaMO;

  const sedanFator = 1 + (250000 / 100000) * taxaRisco;
  const sedanPecas = 500 * margemPeca;
  const sedanMO = 2 * valorHora * sedanFator;
  const sedanTotal = sedanPecas + sedanMO;

  // Salvar dados da oficina via oficinasService.atualizar (PUT /oficina/{id})
  const salvarConfiguracoesOficina = async () => {
    const oficinaIdNum = user?.oficinaId ? Number(user.oficinaId) : null;
    if (!oficinaIdNum) {
      toast.error("Identificador da oficina não encontrado.");
      return;
    }

    const nomeFormatado = nomeOficina.trim();
    const emailFormatado = emailOficina.trim();
    const telefoneFormatado = telefone.trim();
    const enderecoFormatado = endereco.trim();

    if (!nomeFormatado) {
      toast.error("O nome da oficina é obrigatório.");
      return;
    }
    if (!emailFormatado) {
      toast.error("O e-mail da oficina é obrigatório.");
      return;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(emailFormatado)) {
      toast.error("Informe um e-mail válido para a oficina.");
      return;
    }
    if (!telefoneFormatado) {
      toast.error("O telefone da oficina é obrigatório.");
      return;
    }
    if (!enderecoFormatado) {
      toast.error("O endereço da oficina é obrigatório.");
      return;
    }
    if (valorHora <= 0) {
      toast.error("O valor da hora técnica deve ser positivo.");
      return;
    }
    if (taxaRisco < 0) {
      toast.error("A taxa de risco deve ser positiva ou zero.");
      return;
    }
    if (margemPeca < 1) {
      toast.error("A margem da peça deve ser maior ou igual a 1.");
      return;
    }

    try {
      setSalvandoOficina(true);
      const atualizada = await oficinasService.atualizar(oficinaIdNum, {
        nome: nomeFormatado,
        email: emailFormatado,
        telefone: telefoneFormatado,
        endereco: enderecoFormatado,
        valorHora,
        taxaRisco,
        margemPeca,
      });

      setOficinaOriginal(atualizada);
      setNomeOficina(atualizada.nome);
      setEmailOficina(atualizada.email);
      setTelefone(atualizada.telefone);
      setEndereco(atualizada.endereco);
      setValorHora(
        atualizada.valorHora != null ? Number(atualizada.valorHora) : valorHora
      );
      setTaxaRisco(
        atualizada.taxaRisco != null ? Number(atualizada.taxaRisco) : taxaRisco
      );
      setMargemPeca(
        atualizada.margemPeca != null
          ? Number(atualizada.margemPeca)
          : margemPeca
      );
      setOficinaAtiva(atualizada.ativo !== false);

      toast.success("Configurações da oficina salvas com sucesso!");
    } catch (err: unknown) {
      const norm = normalizeError(
        err,
        "Falha ao salvar configurações da oficina."
      );
      toast.error(norm.message);
    } finally {
      setSalvandoOficina(false);
    }
  };

  const handleSalvarOficina = async (e: React.FormEvent) => {
    e.preventDefault();
    await salvarConfiguracoesOficina();
  };

  const handleRestaurarPadroes = () => {
    if (oficinaOriginal) {
      setNomeOficina(oficinaOriginal.nome || "");
      setEmailOficina(oficinaOriginal.email || "");
      setTelefone(oficinaOriginal.telefone || "");
      setEndereco(oficinaOriginal.endereco || "");
      setValorHora(
        oficinaOriginal.valorHora != null
          ? Number(oficinaOriginal.valorHora)
          : 100.0
      );
      setTaxaRisco(
        oficinaOriginal.taxaRisco != null
          ? Number(oficinaOriginal.taxaRisco)
          : 0.1
      );
      setMargemPeca(
        oficinaOriginal.margemPeca != null
          ? Number(oficinaOriginal.margemPeca)
          : 1.0
      );
      toast.info("Valores revertidos para os dados salvos da oficina.");
    }
  };

  // Funções de Usuário
  const abrirNovoUsuario = () => {
    setUsuarioEmEdicao(null);
    setUsuarioNome("");
    setUsuarioEmail("");
    setUsuarioRole("MECANICO");
    setUsuarioSenha("");
    setUsuarioConfirmarSenha("");
    setErroUsuario(null);
    setModalUsuarioAberto(true);
  };

  const abrirEditarUsuario = (u: UsuarioOficinaUI) => {
    setUsuarioEmEdicao(u);
    setUsuarioNome(u.nome);
    setUsuarioEmail(u.email);
    setUsuarioRole(u.role);
    setUsuarioSenha("");
    setUsuarioConfirmarSenha("");
    setErroUsuario(null);
    setModalUsuarioAberto(true);
  };

  const handleSalvarUsuario = async (e: React.FormEvent) => {
    e.preventDefault();
    setErroUsuario(null);

    const nomeFormatado = usuarioNome.trim();
    const emailFormatado = usuarioEmail.trim();

    if (!nomeFormatado) {
      setErroUsuario("O nome é obrigatório.");
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(emailFormatado)) {
      setErroUsuario("Informe um e-mail válido.");
      return;
    }

    if (!usuarioEmEdicao) {
      if (!usuarioSenha) {
        setErroUsuario("A senha é obrigatória para o cadastro de novo usuário.");
        return;
      }
      if (usuarioSenha.length < 8) {
        setErroUsuario("A senha deve ter no mínimo 8 caracteres.");
        return;
      }
      if (!usuarioConfirmarSenha) {
        setErroUsuario("Por favor, digite a confirmação da senha.");
        return;
      }
      if (usuarioSenha !== usuarioConfirmarSenha) {
        setErroUsuario(
          "A confirmação de senha não confere com a senha digitada."
        );
        return;
      }
    } else {
      if (usuarioSenha || usuarioConfirmarSenha) {
        if (!usuarioSenha) {
          setErroUsuario("Digite a nova senha desejada.");
          return;
        }
        if (usuarioSenha.length < 8) {
          setErroUsuario("A nova senha deve ter no mínimo 8 caracteres.");
          return;
        }
        if (!usuarioConfirmarSenha) {
          setErroUsuario("Por favor, confirme a nova senha.");
          return;
        }
        if (usuarioSenha !== usuarioConfirmarSenha) {
          setErroUsuario(
            "A confirmação de senha não confere com a nova senha digitada."
          );
          return;
        }
      }
    }

    try {
      setSalvandoUsuario(true);

      if (usuarioEmEdicao) {
        if (
          user &&
          user.email === usuarioEmEdicao.email &&
          usuarioEmEdicao.role === "ADMIN" &&
          usuarioRole === "MECANICO"
        ) {
          setErroUsuario(
            "Você não pode rebaixar o seu próprio perfil de administrador."
          );
          setSalvandoUsuario(false);
          return;
        }

        const payload: AtualizarUsuarioRequest = {
          nome: nomeFormatado,
          email: emailFormatado,
          role: usuarioRole,
          ...(usuarioSenha
            ? { senha: usuarioSenha, confirmarSenha: usuarioConfirmarSenha }
            : {}),
        };

        await usuariosService.atualizar(usuarioEmEdicao.id, payload);
        toast.success("Usuário atualizado com sucesso!");
      } else {
        const oficinaIdNum = user?.oficinaId ? Number(user.oficinaId) : null;
        if (!oficinaIdNum) {
          setErroUsuario("Oficina não identificada para associar o usuário.");
          setSalvandoUsuario(false);
          return;
        }

        if (usuarioRole === "ADMIN") {
          await usuariosService.cadastrarAdmin({
            nome: nomeFormatado,
            email: emailFormatado,
            senha: usuarioSenha,
            confirmarSenha: usuarioConfirmarSenha,
            oficinaId: oficinaIdNum,
          });
          toast.success("Novo administrador cadastrado com sucesso!");
        } else {
          await usuariosService.cadastrarMecanico({
            nome: nomeFormatado,
            email: emailFormatado,
            senha: usuarioSenha,
            confirmarSenha: usuarioConfirmarSenha,
          });
          toast.success("Mecânico cadastrado com sucesso!");
        }
      }

      setModalUsuarioAberto(false);
      await carregarDadosOficina();
    } catch (err: unknown) {
      const norm = normalizeError(err, "Falha ao salvar colaborador.");
      setErroUsuario(norm.message);
    } finally {
      setSalvandoUsuario(false);
    }
  };

  const solicitarMudancaStatus = (u: UsuarioOficinaUI) => {
    setUsuarioAlteracao(u);
    setDialogStatusAberto(true);
  };

  const confirmarMudancaStatus = async () => {
    if (!usuarioAlteracao) return;

    if (
      user &&
      user.email === usuarioAlteracao.email &&
      usuarioAlteracao.ativo
    ) {
      toast.error("Você não pode desativar o seu próprio usuário.");
      setDialogStatusAberto(false);
      return;
    }

    try {
      setAlterandoStatus(true);
      if (usuarioAlteracao.ativo) {
        await usuariosService.desativar(usuarioAlteracao.id);
        toast.success(`Acesso de ${usuarioAlteracao.nome} desativado.`);
      } else {
        await usuariosService.ativar(usuarioAlteracao.id);
        toast.success(`Acesso de ${usuarioAlteracao.nome} reativado!`);
      }
      setDialogStatusAberto(false);
      await carregarDadosOficina();
    } catch (err: unknown) {
      const norm = normalizeError(
        err,
        "Falha ao alterar status do colaborador."
      );
      toast.error(norm.message);
    } finally {
      setAlterandoStatus(false);
    }
  };

  const totalUsuariosAtivos = useMemo(
    () => usuarios.filter((u) => u.ativo).length,
    [usuarios]
  );

  if (carregando) {
    return (
      <div className="flex min-h-[400px] flex-col items-center justify-center gap-3">
        <Spinner className="size-8 text-primary" />
        <p className="text-sm font-medium text-muted-foreground">
          Carregando configurações da oficina...
        </p>
      </div>
    );
  }

  if (!user?.oficinaId && userRoleNormalized !== "SUPERADMIN") {
    return (
      <div className="flex flex-col gap-6 py-6">
        <PageHeader
          title="Minha Oficina"
          description="Gerencie os dados cadastrais da oficina e parâmetros de precificação."
        />
        <Alert variant="destructive">
          <AlertCircle className="size-5" />
          <AlertDescription>
            Nenhuma oficina vinculada à sua conta de usuário. Entre em contato
            com o suporte ou administrador do sistema.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6 pb-10">
      <PageHeader
        title={nomeOficina || "Minha Oficina"}
        description="Configure os dados comerciais, regras de precificação inteligente (Hora e Risco FIPE) e permissões da equipe."
        actions={
          <div className="flex items-center gap-2.5">
            <span
              className={cn(
                "inline-flex items-center gap-1.5 rounded-md px-2.5 py-1 text-xs font-semibold ring-1 ring-inset",
                oficinaAtiva
                  ? "bg-success/12 text-success ring-success/25"
                  : "bg-destructive/12 text-destructive ring-destructive/25"
              )}
            >
              <span
                className={cn(
                  "size-1.5 rounded-full",
                  oficinaAtiva ? "bg-success" : "bg-destructive"
                )}
              />
              {oficinaAtiva ? "Oficina Ativa" : "Oficina Inativa"}
            </span>
            <Button
              variant="outline"
              size="sm"
              onClick={carregarDadosOficina}
              disabled={carregando}
              title="Recarregar dados"
            >
              <RefreshCw
                className={cn("size-4", carregando && "animate-spin")}
              />
              <span className="hidden sm:inline">Atualizar</span>
            </Button>
          </div>
        }
      />

      {/* Faixa de Resumo Rápido dos Parâmetros Atuais */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <Card>
          <CardContent className="flex flex-col gap-1.5 py-1">
            <div className="flex items-center justify-between text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              <span>Hora Técnica</span>
              <Clock className="size-4 text-primary" />
            </div>
            <span className="font-display text-2xl font-extrabold tracking-tight text-foreground tabular-nums">
              {formatCurrency(valorHora)}
              <span className="text-sm font-medium text-muted-foreground">
                /h
              </span>
            </span>
            <span className="text-xs text-muted-foreground">
              Base da mão de obra
            </span>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="flex flex-col gap-1.5 py-1">
            <div className="flex items-center justify-between text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              <span>Taxa Risco FIPE</span>
              <TrendingUp className="size-4 text-primary" />
            </div>
            <span className="font-display text-2xl font-extrabold tracking-tight text-foreground tabular-nums">
              +{(taxaRisco * 100).toFixed(0)}%
            </span>
            <span className="text-xs text-muted-foreground">
              Por R$ 100 mil de valor FIPE
            </span>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="flex flex-col gap-1.5 py-1">
            <div className="flex items-center justify-between text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              <span>Margem de Peças</span>
              <Package className="size-4 text-primary" />
            </div>
            <span className="font-display text-2xl font-extrabold tracking-tight text-foreground tabular-nums">
              {margemPeca.toFixed(2)}x
            </span>
            <span className="text-xs text-muted-foreground">
              {margemPeca >= 1
                ? `+${((margemPeca - 1) * 100).toFixed(0)}% sobre o catálogo`
                : `${((margemPeca - 1) * 100).toFixed(0)}% sobre o catálogo`}
            </span>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="flex flex-col gap-1.5 py-1">
            <div className="flex items-center justify-between text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              <span>Equipe com Acesso</span>
              <Users className="size-4 text-primary" />
            </div>
            <span className="font-display text-2xl font-extrabold tracking-tight text-foreground tabular-nums">
              {totalUsuariosAtivos}
              <span className="text-sm font-medium text-muted-foreground">
                /{usuarios.length}
              </span>
            </span>
            <span className="text-xs text-muted-foreground">
              Usuários ativos na oficina
            </span>
          </CardContent>
        </Card>
      </div>

      {/* Navegação por Abas para separar Dados/Precificação, Simulador e Equipe */}
      <Tabs defaultValue="configuracoes" className="gap-6">
        <div className="flex flex-col gap-3 border-b border-border pb-3 sm:flex-row sm:items-center sm:justify-between">
          <TabsList className="h-10 p-1">
            <TabsTrigger value="configuracoes" className="px-3.5 py-1.5">
              <Building2 className="size-4" />
              <span>Cadastro & Precificação</span>
            </TabsTrigger>
            <TabsTrigger value="simulador" className="px-3.5 py-1.5">
              <Calculator className="size-4" />
              <span>Simulador FIPE</span>
            </TabsTrigger>
            <TabsTrigger value="usuarios" className="px-3.5 py-1.5">
              <Users className="size-4" />
              <span>Equipe & Acessos</span>
              <span className="ml-1 rounded-md bg-secondary px-1.5 py-0.5 font-mono text-xs font-semibold text-foreground tabular-nums">
                {usuarios.length}
              </span>
            </TabsTrigger>
          </TabsList>

          {temAlteracoesPendentes && (
            <div className="flex items-center gap-2">
              <span className="text-xs font-semibold text-warning">
                Alterações não salvas
              </span>
              <Button
                size="sm"
                onClick={salvarConfiguracoesOficina}
                disabled={salvandoOficina}
              >
                <Save data-icon="inline-start" />
                {salvandoOficina ? "Salvando..." : "Salvar agora"}
              </Button>
            </div>
          )}
        </div>

        {/* ABA 1: CADASTRO & PRECIFICAÇÃO */}
        <TabsContent value="configuracoes">
          <form onSubmit={handleSalvarOficina} className="flex flex-col gap-6">
            {/* Card 1: Dados Cadastrais */}
            <Card>
              <CardHeader>
                <CardTitle>Dados Cadastrais</CardTitle>
                <CardDescription>
                  Informações comerciais exibidas no cabeçalho das ordens de
                  serviço e na impressão A4 de orçamentos.
                </CardDescription>
              </CardHeader>

              <CardContent className="grid gap-5 md:grid-cols-2">
                <Field>
                  <FieldLabel htmlFor="oficina-nome">
                    Nome da Oficina
                  </FieldLabel>
                  <Input
                    id="oficina-nome"
                    value={nomeOficina}
                    onChange={(e) => setNomeOficina(e.target.value)}
                    placeholder="Ex: Auto Precision Mecânica"
                    required
                  />
                </Field>

                <Field>
                  <FieldLabel htmlFor="oficina-email">
                    E-mail Comercial
                  </FieldLabel>
                  <Input
                    id="oficina-email"
                    type="email"
                    value={emailOficina}
                    onChange={(e) => setEmailOficina(e.target.value)}
                    placeholder="contato@oficina.com.br"
                    required
                  />
                </Field>

                <Field>
                  <FieldLabel htmlFor="oficina-telefone">
                    Telefone / WhatsApp
                  </FieldLabel>
                  <Input
                    id="oficina-telefone"
                    value={telefone}
                    onChange={(e) =>
                      setTelefone(formatTelefone(e.target.value))
                    }
                    placeholder="(11) 99999-9999"
                    required
                  />
                </Field>

                <Field>
                  <FieldLabel htmlFor="oficina-endereco">
                    Endereço Completo
                  </FieldLabel>
                  <Input
                    id="oficina-endereco"
                    value={endereco}
                    onChange={(e) => setEndereco(e.target.value)}
                    placeholder="Rua, número, bairro, cidade - UF"
                    required
                  />
                </Field>
              </CardContent>
            </Card>

            {/* Card 2: Parâmetros de Precificação (Sem cards aninhados) */}
            <Card>
              <CardHeader>
                <div className="flex items-center gap-2">
                  <Coins className="size-5 text-primary" />
                  <CardTitle>Regras de Precificação</CardTitle>
                </div>
                <CardDescription>
                  Defina os valores que calculam automaticamente a mão de obra
                  proporcional à Tabela FIPE e a margem das peças no orçamento.
                </CardDescription>
              </CardHeader>

              <CardContent className="p-0">
                <div className="grid divide-y divide-border/70 border-t border-border/70 md:grid-cols-3 md:divide-x md:divide-y-0">
                  {/* 1. Valor da Hora Técnica */}
                  <div className="flex flex-col justify-between gap-4 p-5">
                    <div className="flex flex-col gap-1.5">
                      <div className="flex items-center justify-between gap-2">
                        <label
                          htmlFor="input-valor-hora"
                          className="flex items-center gap-1.5 font-display text-sm font-bold text-foreground"
                        >
                          <Clock className="size-4 text-primary" />
                          Valor da Hora Técnica
                        </label>
                        <span className="rounded-md bg-secondary px-2 py-0.5 font-mono text-xs font-semibold text-foreground tabular-nums">
                          {formatCurrency(valorHora)}/h
                        </span>
                      </div>
                      <p className="text-xs leading-relaxed text-muted-foreground">
                        Valor base cobrado por hora de trabalho na execução de
                        serviços mecânicos.
                      </p>
                    </div>

                    <InputGroup>
                      <InputGroupAddon>R$</InputGroupAddon>
                      <InputGroupInput
                        id="input-valor-hora"
                        type="number"
                        step="5"
                        min="20"
                        max="1000"
                        value={valorHora}
                        onChange={(e) =>
                          setValorHora(parseFloat(e.target.value) || 0)
                        }
                        className="font-mono font-semibold tabular-nums"
                        required
                      />
                    </InputGroup>
                  </div>

                  {/* 2. Taxa de Risco FIPE */}
                  <div className="flex flex-col justify-between gap-4 p-5">
                    <div className="flex flex-col gap-1.5">
                      <div className="flex items-center justify-between gap-2">
                        <label
                          htmlFor="input-taxa-risco"
                          className="flex items-center gap-1.5 font-display text-sm font-bold text-foreground"
                        >
                          <TrendingUp className="size-4 text-primary" />
                          Taxa de Risco FIPE
                        </label>
                        <span className="rounded-md bg-secondary px-2 py-0.5 font-mono text-xs font-semibold text-foreground tabular-nums">
                          +{(taxaRisco * 100).toFixed(0)}% / 100k
                        </span>
                      </div>
                      <p className="text-xs leading-relaxed text-muted-foreground">
                        Acréscimo sobre a mão de obra a cada R$ 100.000,00 de
                        valor FIPE do veículo atendido.
                      </p>
                    </div>

                    <InputGroup>
                      <InputGroupInput
                        id="input-taxa-risco"
                        type="number"
                        step="0.01"
                        min="0"
                        max="1"
                        value={taxaRisco}
                        onChange={(e) =>
                          setTaxaRisco(parseFloat(e.target.value) || 0)
                        }
                        className="font-mono font-semibold tabular-nums"
                        required
                      />
                      <InputGroupAddon>taxa</InputGroupAddon>
                    </InputGroup>
                  </div>

                  {/* 3. Margem sobre Peças */}
                  <div className="flex flex-col justify-between gap-4 p-5">
                    <div className="flex flex-col gap-1.5">
                      <div className="flex items-center justify-between gap-2">
                        <label
                          htmlFor="input-margem-peca"
                          className="flex items-center gap-1.5 font-display text-sm font-bold text-foreground"
                        >
                          <Package className="size-4 text-primary" />
                          Margem sobre Peças
                        </label>
                        <span className="rounded-md bg-secondary px-2 py-0.5 font-mono text-xs font-semibold text-foreground tabular-nums">
                          {margemPeca.toFixed(2)}x (
                          {margemPeca >= 1
                            ? `+${((margemPeca - 1) * 100).toFixed(0)}%`
                            : `${((margemPeca - 1) * 100).toFixed(0)}%`}
                          )
                        </span>
                      </div>
                      <p className="text-xs leading-relaxed text-muted-foreground">
                        Multiplicador aplicado sobre o preço de custo das peças
                        do catálogo (ex.: 1.15 = +15%).
                      </p>
                    </div>

                    <InputGroup>
                      <InputGroupInput
                        id="input-margem-peca"
                        type="number"
                        step="0.01"
                        min="1"
                        value={margemPeca}
                        onChange={(e) =>
                          setMargemPeca(parseFloat(e.target.value) || 0)
                        }
                        className="font-mono font-semibold tabular-nums"
                        required
                      />
                      <InputGroupAddon>fator</InputGroupAddon>
                    </InputGroup>
                  </div>
                </div>
              </CardContent>

              <CardFooter className="flex items-center justify-between gap-3">
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleRestaurarPadroes}
                  disabled={salvandoOficina || !temAlteracoesPendentes}
                >
                  <RotateCcw data-icon="inline-start" />
                  Reverter Alterações
                </Button>
                <Button type="submit" disabled={salvandoOficina}>
                  <Save data-icon="inline-start" />
                  {salvandoOficina
                    ? "Salvando..."
                    : "Salvar Configurações da Oficina"}
                </Button>
              </CardFooter>
            </Card>
          </form>
        </TabsContent>

        {/* ABA 2: SIMULADOR FIPE & FÓRMULA */}
        <TabsContent value="simulador" className="flex flex-col gap-6">
          <div className="grid gap-6 xl:grid-cols-12">
            {/* Coluna Esquerda: Calculadora Interativa Livre */}
            <Card className="xl:col-span-6">
              <CardHeader>
                <div className="flex items-center gap-2">
                  <Calculator className="size-5 text-primary" />
                  <CardTitle>Calculadora Interativa de Orçamento</CardTitle>
                </div>
                <CardDescription>
                  Simule um atendimento qualquer para testar o impacto da Hora
                  Técnica ({formatCurrency(valorHora)}/h), Taxa FIPE (
                  {(taxaRisco * 100).toFixed(0)}%) e Margem (
                  {margemPeca.toFixed(2)}x).
                </CardDescription>
              </CardHeader>

              <CardContent className="flex flex-col gap-6">
                <div className="grid gap-4 sm:grid-cols-3">
                  <Field>
                    <FieldLabel htmlFor="sim-horas">
                      Horas de Serviço
                    </FieldLabel>
                    <InputGroup>
                      <InputGroupInput
                        id="sim-horas"
                        type="number"
                        step="0.5"
                        min="0.5"
                        max="40"
                        value={simHoras}
                        onChange={(e) =>
                          setSimHoras(parseFloat(e.target.value) || 1)
                        }
                        className="font-mono font-semibold tabular-nums"
                      />
                      <InputGroupAddon>h</InputGroupAddon>
                    </InputGroup>
                  </Field>

                  <Field>
                    <FieldLabel htmlFor="sim-pecas">
                      Peças (Catálogo)
                    </FieldLabel>
                    <InputGroup>
                      <InputGroupAddon>R$</InputGroupAddon>
                      <InputGroupInput
                        id="sim-pecas"
                        type="number"
                        step="10"
                        min="0"
                        value={simCustoPecas}
                        onChange={(e) =>
                          setSimCustoPecas(parseFloat(e.target.value) || 0)
                        }
                        className="font-mono font-semibold tabular-nums"
                      />
                    </InputGroup>
                  </Field>

                  <Field>
                    <FieldLabel htmlFor="sim-fipe">
                      Valor FIPE do Carro
                    </FieldLabel>
                    <InputGroup>
                      <InputGroupAddon>R$</InputGroupAddon>
                      <InputGroupInput
                        id="sim-fipe"
                        type="number"
                        step="5000"
                        min="10000"
                        value={simFipe}
                        onChange={(e) =>
                          setSimFipe(parseFloat(e.target.value) || 10000)
                        }
                        className="font-mono font-semibold tabular-nums"
                      />
                    </InputGroup>
                  </Field>
                </div>

                {/* Demonstrativo estilo recibo (sem caixa aninhada) */}
                <div className="flex flex-col divide-y divide-border/70 border-t border-border pt-3 text-sm">
                  <div className="flex items-center justify-between py-2.5">
                    <span className="text-muted-foreground">
                      Fator de risco aplicado ao veículo
                    </span>
                    <span className="font-mono font-semibold text-foreground tabular-nums">
                      {fatorVeiculoSimulado.toFixed(2)}x
                    </span>
                  </div>
                  <div className="flex items-center justify-between py-2.5">
                    <span className="text-muted-foreground">
                      Subtotal Peças ({formatCurrency(simCustoPecas)} ×{" "}
                      {margemPeca.toFixed(2)}x)
                    </span>
                    <span className="font-mono font-semibold text-foreground tabular-nums">
                      {formatCurrency(totalPecasSimulado)}
                    </span>
                  </div>
                  <div className="flex items-center justify-between py-2.5">
                    <span className="text-muted-foreground">
                      Subtotal Mão de Obra ({simHoras}h ×{" "}
                      {formatCurrency(valorHora)} ×{" "}
                      {fatorVeiculoSimulado.toFixed(2)}x)
                    </span>
                    <span className="font-mono font-semibold text-foreground tabular-nums">
                      {formatCurrency(totalMaoDeObraSimulado)}
                    </span>
                  </div>
                  <div className="flex items-baseline justify-between pt-4">
                    <span className="font-display text-base font-bold text-foreground">
                      Valor Estimado da Ordem
                    </span>
                    <span className="font-display text-2xl font-extrabold text-primary tabular-nums">
                      {formatCurrency(valorFinalSimulado)}
                    </span>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Coluna Direita: Comparativo Popular vs. Premium + Fórmula */}
            <div className="flex flex-col gap-6 xl:col-span-6">
              <Card>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <Car className="size-5 text-primary" />
                      <CardTitle>Comparativo Automático (2h de serviço)</CardTitle>
                    </div>
                    <span className="rounded-md bg-primary/12 px-2 py-0.5 font-mono text-xs font-semibold text-primary">
                      Tempo real
                    </span>
                  </div>
                  <CardDescription>
                    Veja como a mesma mão de obra de 2 horas se ajusta ao risco
                    de um carro popular versus um veículo premium.
                  </CardDescription>
                </CardHeader>

                <CardContent className="p-0">
                  <div className="grid divide-y divide-border/70 border-t border-border/70 sm:grid-cols-2 sm:divide-x sm:divide-y-0">
                    {/* Cenário 1: Popular */}
                    <div className="flex flex-col gap-3 p-5">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="font-display text-sm font-bold text-foreground">
                            Carro Popular
                          </p>
                          <p className="font-mono text-xs text-muted-foreground tabular-nums">
                            FIPE: R$ 20.000,00
                          </p>
                        </div>
                        <span className="rounded-md bg-secondary px-2 py-0.5 font-mono text-xs font-semibold text-foreground tabular-nums">
                          {corsaFator.toFixed(2)}x
                        </span>
                      </div>

                      <div className="flex flex-col gap-1.5 border-t border-border/60 pt-3 text-xs">
                        <div className="flex justify-between text-muted-foreground">
                          <span>Peças (Base R$ 100):</span>
                          <span className="font-mono font-semibold text-foreground tabular-nums">
                            {formatCurrency(corsaPecas)}
                          </span>
                        </div>
                        <div className="flex justify-between text-muted-foreground">
                          <span>Mão de obra (2h):</span>
                          <span className="font-mono font-semibold text-foreground tabular-nums">
                            {formatCurrency(corsaMO)}
                          </span>
                        </div>
                        <div className="mt-1 flex items-baseline justify-between border-t border-border/60 pt-2 font-semibold text-foreground">
                          <span>Total OS:</span>
                          <span className="font-mono text-base font-bold text-primary tabular-nums">
                            {formatCurrency(corsaTotal)}
                          </span>
                        </div>
                      </div>
                    </div>

                    {/* Cenário 2: Premium */}
                    <div className="flex flex-col gap-3 p-5">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="font-display text-sm font-bold text-foreground">
                            Sedan Premium
                          </p>
                          <p className="font-mono text-xs text-muted-foreground tabular-nums">
                            FIPE: R$ 250.000,00
                          </p>
                        </div>
                        <span className="rounded-md bg-primary/12 px-2 py-0.5 font-mono text-xs font-semibold text-primary tabular-nums">
                          {sedanFator.toFixed(2)}x
                        </span>
                      </div>

                      <div className="flex flex-col gap-1.5 border-t border-border/60 pt-3 text-xs">
                        <div className="flex justify-between text-muted-foreground">
                          <span>Peças (Base R$ 500):</span>
                          <span className="font-mono font-semibold text-foreground tabular-nums">
                            {formatCurrency(sedanPecas)}
                          </span>
                        </div>
                        <div className="flex justify-between text-muted-foreground">
                          <span>Mão de obra (2h):</span>
                          <span className="font-mono font-semibold text-foreground tabular-nums">
                            {formatCurrency(sedanMO)}
                          </span>
                        </div>
                        <div className="mt-1 flex items-baseline justify-between border-t border-border/60 pt-2 font-semibold text-foreground">
                          <span>Total OS:</span>
                          <span className="font-mono text-base font-bold text-primary tabular-nums">
                            {formatCurrency(sedanTotal)}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Explicação Didática da Fórmula */}
              <Card>
                <CardHeader className="pb-2">
                  <div className="flex items-center gap-2">
                    <Info className="size-4 text-primary" />
                    <CardTitle className="text-sm">
                      Memória de cálculo do sistema
                    </CardTitle>
                  </div>
                </CardHeader>
                <CardContent className="grid gap-4 text-xs text-muted-foreground sm:grid-cols-2">
                  <div className="flex flex-col gap-1">
                    <span className="font-semibold text-foreground">
                      1. Peças (Catálogo × Margem)
                    </span>
                    <span>
                      Preço base do item multiplicado pelo fator da oficina.
                    </span>
                    <code className="mt-1 rounded bg-secondary px-2 py-1 font-mono text-foreground tabular-nums">
                      Peças = Σ (Qtd × Base × {margemPeca.toFixed(2)})
                    </code>
                  </div>

                  <div className="flex flex-col gap-1">
                    <span className="font-semibold text-foreground">
                      2. Mão de Obra (Hora × Fator FIPE)
                    </span>
                    <span>
                      Fator = 1 + ((FIPE / 100.000) × {taxaRisco.toFixed(2)})
                    </span>
                    <code className="mt-1 rounded bg-secondary px-2 py-1 font-mono text-foreground tabular-nums">
                      M.O. = Horas × {formatCurrency(valorHora)} × Fator
                    </code>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </TabsContent>

        {/* ABA 3: EQUIPE & ACESSOS */}
        <TabsContent value="usuarios">
          <Card>
            <CardHeader>
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <CardTitle>Usuários com Acesso à Oficina</CardTitle>
                  <CardDescription className="mt-1">
                    Controle quem pode acessar o painel da oficina entre
                    Administradores e Mecânicos.
                  </CardDescription>
                </div>
                <div className="flex flex-wrap items-center gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => router.push("/app/mecanicos")}
                    title="Gestão avançada em Mecânicos"
                  >
                    <ExternalLink data-icon="inline-start" />
                    Gerenciar Equipe
                  </Button>
                  <Button onClick={abrirNovoUsuario} size="sm">
                    <Plus data-icon="inline-start" />
                    Adicionar Usuário
                  </Button>
                </div>
              </div>
            </CardHeader>

            <CardContent className="flex flex-col gap-4">
              {/* Barra de Filtros */}
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <InputGroup className="sm:max-w-xs">
                  <InputGroupAddon>
                    <Search />
                  </InputGroupAddon>
                  <InputGroupInput
                    placeholder="Buscar por nome ou e-mail..."
                    value={buscaUsuarios}
                    onChange={(e) => setBuscaUsuarios(e.target.value)}
                  />
                </InputGroup>

                <div className="flex flex-wrap items-center gap-2">
                  <Select
                    value={filtroPerfil}
                    onValueChange={(val) =>
                      setFiltroPerfil(val as "todos" | "ADMIN" | "MECANICO")
                    }
                  >
                    <SelectTrigger className="w-[145px]">
                      <SelectValue placeholder="Perfil" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="todos">Todos os perfis</SelectItem>
                      <SelectItem value="ADMIN">Administrador</SelectItem>
                      <SelectItem value="MECANICO">Mecânico</SelectItem>
                    </SelectContent>
                  </Select>

                  <Select
                    value={filtroStatus}
                    onValueChange={(val) =>
                      setFiltroStatus(val as "todos" | "ativos" | "inativos")
                    }
                  >
                    <SelectTrigger className="w-[135px]">
                      <SelectValue placeholder="Status" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="todos">Todos os status</SelectItem>
                      <SelectItem value="ativos">Ativos</SelectItem>
                      <SelectItem value="inativos">Inativos</SelectItem>
                    </SelectContent>
                  </Select>

                  <span className="rounded-md bg-secondary px-2.5 py-1 font-mono text-xs font-semibold text-foreground tabular-nums">
                    {usuariosFiltrados.length}{" "}
                    {usuariosFiltrados.length === 1 ? "usuário" : "usuários"}
                  </span>
                </div>
              </div>

              {usuariosFiltrados.length === 0 ? (
                <EmptyState
                  icon={Users}
                  title="Nenhum usuário encontrado"
                  description="Nenhum administrador ou mecânico atende aos critérios da busca."
                  action={
                    <Button variant="outline" onClick={abrirNovoUsuario}>
                      <Plus data-icon="inline-start" />
                      Adicionar usuário
                    </Button>
                  }
                />
              ) : (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Usuário</TableHead>
                        <TableHead>E-mail</TableHead>
                        <TableHead>Nível de Acesso</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead className="text-right">Ações</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {usuariosFiltrados.map((u) => {
                        const isAdmin = u.role === "ADMIN";
                        const isCurrentUser = user?.email === u.email;
                        return (
                          <TableRow key={u.id}>
                            <TableCell>
                              <div className="flex items-center gap-3">
                                <Avatar className="size-8">
                                  <AvatarFallback
                                    className={
                                      isAdmin
                                        ? "bg-primary text-primary-foreground text-xs font-semibold"
                                        : "bg-secondary text-foreground text-xs font-semibold"
                                    }
                                  >
                                    {u.nome.slice(0, 2).toUpperCase()}
                                  </AvatarFallback>
                                </Avatar>
                                <span className="font-semibold text-foreground">
                                  {u.nome}{" "}
                                  {isCurrentUser && (
                                    <span className="text-xs font-normal text-muted-foreground">
                                      (Você)
                                    </span>
                                  )}
                                </span>
                              </div>
                            </TableCell>
                            <TableCell className="text-muted-foreground">
                              {u.email}
                            </TableCell>
                            <TableCell>
                              {isAdmin ? (
                                <span className="inline-flex items-center gap-1.5 rounded-md bg-primary/12 px-2 py-0.5 text-xs font-semibold text-primary ring-1 ring-inset ring-primary/25">
                                  <Shield className="size-3" />
                                  Administrador
                                </span>
                              ) : (
                                <span className="inline-flex items-center gap-1.5 rounded-md bg-secondary px-2 py-0.5 text-xs font-medium text-foreground ring-1 ring-inset ring-border">
                                  <Wrench className="size-3" />
                                  Mecânico
                                </span>
                              )}
                            </TableCell>
                            <TableCell>
                              <span
                                className={cn(
                                  "inline-flex items-center gap-1.5 rounded-md px-2 py-0.5 text-xs font-semibold ring-1 ring-inset",
                                  u.ativo
                                    ? "bg-success/12 text-success ring-success/25"
                                    : "bg-muted text-muted-foreground ring-border"
                                )}
                              >
                                <span
                                  className={cn(
                                    "size-1.5 rounded-full",
                                    u.ativo
                                      ? "bg-success"
                                      : "bg-muted-foreground/60"
                                  )}
                                />
                                {u.ativo ? "Ativo" : "Inativo"}
                              </span>
                            </TableCell>
                            <TableCell className="text-right">
                              <div className="flex items-center justify-end gap-1">
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  onClick={() => abrirEditarUsuario(u)}
                                  title="Editar dados"
                                >
                                  <Pencil className="size-4" />
                                  <span className="sr-only">Editar</span>
                                </Button>
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  onClick={() => solicitarMudancaStatus(u)}
                                  className={
                                    u.ativo
                                      ? "text-destructive hover:bg-destructive/10"
                                      : "text-success hover:bg-success/10"
                                  }
                                  title={
                                    u.ativo
                                      ? "Desativar acesso"
                                      : "Reativar acesso"
                                  }
                                >
                                  {u.ativo ? (
                                    <UserX className="size-4" />
                                  ) : (
                                    <UserCheck className="size-4" />
                                  )}
                                  <span className="sr-only">
                                    {u.ativo ? "Desativar" : "Ativar"}
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
        </TabsContent>
      </Tabs>

      {/* Modal de Criação / Edição de Usuário da Oficina com Verificação Dupla */}
      <Dialog open={modalUsuarioAberto} onOpenChange={setModalUsuarioAberto}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Users className="size-5 text-primary" />
              {usuarioEmEdicao ? "Editar Usuário" : "Novo Usuário da Oficina"}
            </DialogTitle>
            <DialogDescription>
              {usuarioEmEdicao
                ? "Atualize as informações cadastrais e o nível de acesso do colaborador."
                : "Vincule um novo administrador ou mecânico à equipe da sua oficina."}
            </DialogDescription>
          </DialogHeader>

          <form
            onSubmit={handleSalvarUsuario}
            className="flex flex-col gap-4 py-2"
          >
            {erroUsuario && (
              <Alert variant="destructive">
                <AlertCircle className="size-4" />
                <AlertDescription>{erroUsuario}</AlertDescription>
              </Alert>
            )}

            <Field>
              <FieldLabel htmlFor="usuario-nome">Nome Completo</FieldLabel>
              <Input
                id="usuario-nome"
                value={usuarioNome}
                onChange={(e) => setUsuarioNome(e.target.value)}
                placeholder="Ex: Pedro Henrique"
                required
              />
            </Field>

            <Field>
              <FieldLabel htmlFor="usuario-email">E-mail</FieldLabel>
              <Input
                id="usuario-email"
                type="email"
                value={usuarioEmail}
                onChange={(e) => setUsuarioEmail(e.target.value)}
                placeholder="pedro@oficina.com"
                required
              />
            </Field>

            <Field>
              <FieldLabel htmlFor="usuario-role">
                Nível de Acesso (Perfil)
              </FieldLabel>
              <Select
                value={usuarioRole}
                onValueChange={(val) =>
                  setUsuarioRole(val as "ADMIN" | "MECANICO")
                }
              >
                <SelectTrigger id="usuario-role">
                  <SelectValue placeholder="Selecione o perfil" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="MECANICO">
                    Mecânico (Executa OS e orçamentos)
                  </SelectItem>
                  <SelectItem value="ADMIN">
                    Administrador (Acesso total à oficina e relatórios)
                  </SelectItem>
                </SelectContent>
              </Select>
            </Field>

            {/* Seção de Senha com Verificação Dupla */}
            <div className="flex flex-col gap-3 border-t border-border pt-4">
              <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                {usuarioEmEdicao
                  ? "Alterar Senha (Opcional)"
                  : "Definir Senha de Acesso"}
              </span>

              <Field>
                <FieldLabel htmlFor="usuario-senha">
                  {usuarioEmEdicao ? "Nova Senha" : "Senha Inicial"}
                </FieldLabel>
                <Input
                  id="usuario-senha"
                  type="password"
                  value={usuarioSenha}
                  onChange={(e) => setUsuarioSenha(e.target.value)}
                  placeholder={
                    usuarioEmEdicao
                      ? "Deixe em branco para manter a atual"
                      : "Mínimo de 8 caracteres"
                  }
                  required={!usuarioEmEdicao}
                />
                <FieldDescription>
                  {usuarioEmEdicao
                    ? "Preencha apenas se desejar redefinir a senha do usuário."
                    : "Mínimo de 8 caracteres alfanuméricos."}
                </FieldDescription>
              </Field>

              <Field>
                <FieldLabel htmlFor="usuario-confirmar-senha">
                  {usuarioEmEdicao ? "Confirmar Nova Senha" : "Confirmar Senha"}
                </FieldLabel>
                <Input
                  id="usuario-confirmar-senha"
                  type="password"
                  value={usuarioConfirmarSenha}
                  onChange={(e) => setUsuarioConfirmarSenha(e.target.value)}
                  placeholder="Repita a senha exatamente igual"
                  required={!usuarioEmEdicao || usuarioSenha.length > 0}
                />
                {usuarioSenha.length > 0 &&
                  usuarioConfirmarSenha.length > 0 &&
                  usuarioSenha === usuarioConfirmarSenha && (
                    <span className="mt-1 flex items-center gap-1 text-xs font-medium text-success">
                      <Check className="size-3.5" /> As senhas conferem
                    </span>
                  )}
                {usuarioSenha.length > 0 &&
                  usuarioConfirmarSenha.length > 0 &&
                  usuarioSenha !== usuarioConfirmarSenha && (
                    <span className="mt-1 flex items-center gap-1 text-xs font-medium text-destructive">
                      <AlertCircle className="size-3.5" /> As senhas não
                      coincidem
                    </span>
                  )}
              </Field>
            </div>

            <DialogFooter className="mt-4 pt-3">
              <Button
                type="button"
                variant="outline"
                onClick={() => setModalUsuarioAberto(false)}
                disabled={salvandoUsuario}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={salvandoUsuario}>
                {salvandoUsuario
                  ? "Salvando..."
                  : usuarioEmEdicao
                  ? "Salvar Alterações"
                  : "Adicionar Usuário"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Diálogo de Confirmação de Alteração de Status */}
      <AlertDialog
        open={dialogStatusAberto}
        onOpenChange={setDialogStatusAberto}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle className="flex items-center gap-2">
              {usuarioAlteracao?.ativo ? (
                <ShieldAlert className="size-5 text-destructive" />
              ) : (
                <CheckCircle2 className="size-5 text-success" />
              )}
              {usuarioAlteracao?.ativo
                ? "Desativar Acesso do Usuário?"
                : "Reativar Acesso do Usuário?"}
            </AlertDialogTitle>
            <AlertDialogDescription>
              {usuarioAlteracao?.ativo ? (
                <>
                  Ao desativar <strong>{usuarioAlteracao.nome}</strong> (
                  {usuarioAlteracao.role}), ele não conseguirá mais realizar
                  login na plataforma nem gerenciar dados da oficina.
                </>
              ) : (
                <>
                  Deseja reativar o acesso de{" "}
                  <strong>{usuarioAlteracao?.nome}</strong> (
                  {usuarioAlteracao?.role})? As permissões de acesso ao sistema
                  serão restabelecidas.
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
                usuarioAlteracao?.ativo
                  ? "bg-destructive text-destructive-foreground hover:bg-destructive/90"
                  : ""
              }
            >
              {alterandoStatus
                ? "Processando..."
                : usuarioAlteracao?.ativo
                ? "Confirmar Desativação"
                : "Confirmar Reativação"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
