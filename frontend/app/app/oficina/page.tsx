"use client";

import { useState, useMemo, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import {
  Building2,
  Phone,
  MapPin,
  Mail,
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
} from "lucide-react";
import { useStore } from "@/lib/store";
import { oficinasService, usuariosService } from "@/lib/services";
import { formatTelefone, isValidTelefone } from "@/lib/format";
import { normalizeError } from "@/lib/error-handler";
import type { OficinaResponse, UsuarioResponse, AtualizarUsuarioRequest } from "@/lib/types";
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
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
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
import { toast } from "sonner";

interface UsuarioOficinaUI {
  id: number;
  nome: string;
  email: string;
  role: "ADMIN" | "MECANICO";
  ativo: boolean;
}

export default function MinhaOficinaPage() {
  const { user, setUser } = useStore();
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
  const [oficinaOriginal, setOficinaOriginal] = useState<OficinaResponse | null>(null);

  // Formulário de dados cadastrais
  const [nomeOficina, setNomeOficina] = useState("");
  const [emailOficina, setEmailOficina] = useState("");
  const [telefone, setTelefone] = useState("");
  const [endereco, setEndereco] = useState("");
  const [oficinaAtiva, setOficinaAtiva] = useState(true);

  // Parâmetros de Precificação da Oficina (Mão de Obra e Risco FIPE)
  const [valorHora, setValorHora] = useState(100.0);
  const [taxaRisco, setTaxaRisco] = useState(0.1);

  // Variáveis para o simulador interativo (Calculadora baseada em FIPE)
  const [simHoras, setSimHoras] = useState(2);
  const [simCustoPecas, setSimCustoPecas] = useState(150);
  const [simFipe, setSimFipe] = useState(80000);

  // Lista de usuários da oficina
  const [usuarios, setUsuarios] = useState<UsuarioOficinaUI[]>([]);
  const [carregandoUsuarios, setCarregandoUsuarios] = useState(false);
  const [buscaUsuarios, setBuscaUsuarios] = useState("");
  const [filtroPerfil, setFiltroPerfil] = useState<"todos" | "ADMIN" | "MECANICO">("todos");
  const [filtroStatus, setFiltroStatus] = useState<"todos" | "ativos" | "inativos">("todos");

  // Modal de edição / criação de usuário
  const [modalUsuarioAberto, setModalUsuarioAberto] = useState(false);
  const [usuarioEmEdicao, setUsuarioEmEdicao] = useState<UsuarioOficinaUI | null>(null);
  const [usuarioNome, setUsuarioNome] = useState("");
  const [usuarioEmail, setUsuarioEmail] = useState("");
  const [usuarioRole, setUsuarioRole] = useState<"ADMIN" | "MECANICO">("MECANICO");
  const [usuarioSenha, setUsuarioSenha] = useState("");
  const [usuarioConfirmarSenha, setUsuarioConfirmarSenha] = useState("");
  const [salvandoUsuario, setSalvandoUsuario] = useState(false);
  const [erroUsuario, setErroUsuario] = useState<string | null>(null);

  // Diálogo de confirmação de status
  const [usuarioAlteracao, setUsuarioAlteracao] = useState<UsuarioOficinaUI | null>(null);
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
      setValorHora(dadosOficina.valorHora != null ? Number(dadosOficina.valorHora) : 100.0);
      setTaxaRisco(dadosOficina.taxaRisco != null ? Number(dadosOficina.taxaRisco) : 0.1);
      setOficinaAtiva(dadosOficina.ativo !== false);

      const usuariosFormatados: UsuarioOficinaUI[] = (listaUsuarios || [])
        .filter((u: UsuarioResponse) => u.role !== "SUPERADMIN")
        .map((u: UsuarioResponse) => ({
          id: u.id,
          nome: u.nome,
          email: u.email,
          role: (u.role?.toUpperCase().replace(/^ROLE_/, "") as "ADMIN" | "MECANICO") || "MECANICO",
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

  // Cálculos do Modelo Matemático (Alinhado com a CalculadoraOrcamento do backend):
  // Fator Veículo = 1 + ((FIPE / 100.000) * taxaRisco)
  // Mão de Obra = Horas × Valor Hora × Fator Veículo
  // Total = Peças (Catálogo) + Mão de Obra
  const fatorVeiculoSimulado = useMemo(() => {
    const fator = 1 + (simFipe / 100000) * taxaRisco;
    return Math.max(1, fator);
  }, [simFipe, taxaRisco]);

  const totalPecasSimulado = useMemo(() => {
    return simCustoPecas;
  }, [simCustoPecas]);

  const totalMaoDeObraSimulado = useMemo(() => {
    return simHoras * valorHora * fatorVeiculoSimulado;
  }, [simHoras, valorHora, fatorVeiculoSimulado]);

  const valorFinalSimulado = useMemo(() => {
    return totalPecasSimulado + totalMaoDeObraSimulado;
  }, [totalPecasSimulado, totalMaoDeObraSimulado]);

  // Cenários Comparativos Fixos do Exemplo
  // Corsa Classic (R$ 20.000) com Peças R$ 100 e 2h
  const corsaFator = 1 + (20000 / 100000) * taxaRisco;
  const corsaPecas = 100;
  const corsaMO = 2 * valorHora * corsaFator;
  const corsaTotal = corsaPecas + corsaMO;

  // Sedan Premium (R$ 250.000) com Peças R$ 500 e 2h
  const sedanFator = 1 + (250000 / 100000) * taxaRisco;
  const sedanPecas = 500;
  const sedanMO = 2 * valorHora * sedanFator;
  const sedanTotal = sedanPecas + sedanMO;

  // Salvar dados da oficina via oficinasService.atualizar (PUT /oficina/{id})
  const handleSalvarOficina = async (e: React.FormEvent) => {
    e.preventDefault();
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

    try {
      setSalvandoOficina(true);
      const atualizada = await oficinasService.atualizar(oficinaIdNum, {
        nome: nomeFormatado,
        email: emailFormatado,
        telefone: telefoneFormatado,
        endereco: enderecoFormatado,
        valorHora,
        taxaRisco,
      });

      setOficinaOriginal(atualizada);
      setNomeOficina(atualizada.nome);
      setEmailOficina(atualizada.email);
      setTelefone(atualizada.telefone);
      setEndereco(atualizada.endereco);
      setValorHora(atualizada.valorHora != null ? Number(atualizada.valorHora) : valorHora);
      setTaxaRisco(atualizada.taxaRisco != null ? Number(atualizada.taxaRisco) : taxaRisco);
      setOficinaAtiva(atualizada.ativo !== false);

      toast.success("Configurações da oficina salvas com sucesso!");
    } catch (err: unknown) {
      const norm = normalizeError(err, "Falha ao salvar configurações da oficina.");
      toast.error(norm.message);
    } finally {
      setSalvandoOficina(false);
    }
  };

  const handleRestaurarPadroes = () => {
    if (oficinaOriginal) {
      setNomeOficina(oficinaOriginal.nome || "");
      setEmailOficina(oficinaOriginal.email || "");
      setTelefone(oficinaOriginal.telefone || "");
      setEndereco(oficinaOriginal.endereco || "");
      setValorHora(oficinaOriginal.valorHora != null ? Number(oficinaOriginal.valorHora) : 100.0);
      setTaxaRisco(oficinaOriginal.taxaRisco != null ? Number(oficinaOriginal.taxaRisco) : 0.1);
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

    // Validação com verificação dupla de senha (digitar 2 vezes)
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
        setErroUsuario("A confirmação de senha não confere com a senha digitada.");
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
          setErroUsuario("A confirmação de senha não confere com a nova senha digitada.");
          return;
        }
      }
    }

    try {
      setSalvandoUsuario(true);

      if (usuarioEmEdicao) {
        // Regra de segurança: não permite rebaixar seu próprio perfil de ADMIN
        if (
          user &&
          user.email === usuarioEmEdicao.email &&
          usuarioEmEdicao.role === "ADMIN" &&
          usuarioRole === "MECANICO"
        ) {
          setErroUsuario("Você não pode rebaixar o seu próprio perfil de administrador.");
          setSalvandoUsuario(false);
          return;
        }

        const payload: AtualizarUsuarioRequest = {
          nome: nomeFormatado,
          email: emailFormatado,
          role: usuarioRole,
          ...(usuarioSenha ? { senha: usuarioSenha, confirmarSenha: usuarioConfirmarSenha } : {}),
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

    // Regra: usuário logado não pode desativar a si próprio
    if (user && user.email === usuarioAlteracao.email && usuarioAlteracao.ativo) {
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
      const norm = normalizeError(err, "Falha ao alterar status do colaborador.");
      toast.error(norm.message);
    } finally {
      setAlterandoStatus(false);
    }
  };

  if (carregando) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-3">
        <Spinner className="size-8 text-primary" />
        <p className="text-sm text-muted-foreground">Carregando dados da oficina...</p>
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
            Nenhuma oficina vinculada à sua conta de usuário. Entre em contato com o suporte ou administrador do sistema.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-8 pb-10">
      <PageHeader
        title="Minha Oficina"
        description="Gerencie os dados cadastrais da oficina, parâmetros de precificação inteligente (Hora e Risco FIPE) e controle de usuários."
        actions={
          <div className="flex items-center gap-2">
            <Badge
              variant="outline"
              className={
                oficinaAtiva
                  ? "bg-success/15 text-success border-success/40"
                  : "bg-destructive/15 text-destructive border-destructive/40"
              }
            >
              {oficinaAtiva ? "Oficina Ativa" : "Oficina Inativa"}
            </Badge>
            <Button
              variant="outline"
              size="sm"
              onClick={carregarDadosOficina}
              disabled={carregando}
              title="Recarregar dados"
            >
              <RefreshCw className={`size-4 ${carregando ? "animate-spin" : ""}`} />
            </Button>
          </div>
        }
      />

      {/* SEÇÃO 1: Formulário de Configurações e Parâmetros */}
      <form onSubmit={handleSalvarOficina}>
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <Building2 className="size-5 text-primary" />
              <CardTitle>Dados Cadastrais da Oficina</CardTitle>
            </div>
            <CardDescription>
              Informações comerciais exibidas em ordens de serviço, relatórios e
              comprovantes de atendimento.
            </CardDescription>
          </CardHeader>

          <CardContent className="grid gap-6 md:grid-cols-2">
            <Field className="md:col-span-1">
              <FieldLabel htmlFor="oficina-nome">Nome da Oficina</FieldLabel>
              <Input
                id="oficina-nome"
                value={nomeOficina}
                onChange={(e) => setNomeOficina(e.target.value)}
                placeholder="Ex: Auto Precision Mecânica"
                required
              />
            </Field>

            <Field className="md:col-span-1">
              <FieldLabel htmlFor="oficina-email">E-mail Comercial</FieldLabel>
              <Input
                id="oficina-email"
                type="email"
                value={emailOficina}
                onChange={(e) => setEmailOficina(e.target.value)}
                placeholder="contato@oficina.com.br"
                required
              />
            </Field>

            <Field className="md:col-span-1">
              <FieldLabel htmlFor="oficina-telefone">Telefone / WhatsApp</FieldLabel>
              <Input
                id="oficina-telefone"
                value={telefone}
                onChange={(e) => setTelefone(formatTelefone(e.target.value))}
                placeholder="(11) 99999-9999"
                required
              />
            </Field>

            <Field className="md:col-span-1">
              <FieldLabel htmlFor="oficina-endereco">Endereço Completo</FieldLabel>
              <Input
                id="oficina-endereco"
                value={endereco}
                onChange={(e) => setEndereco(e.target.value)}
                placeholder="Rua, número, complemento, bairro, cidade - UF"
                required
              />
            </Field>

            {/* SEÇÃO: Parâmetros de Precificação da Oficina */}
            <div className="md:col-span-2 rounded-xl border border-border bg-sidebar/50 p-6 flex flex-col gap-6">
              <div className="flex flex-col gap-1">
                <div className="flex items-center gap-2">
                  <Coins className="size-5 text-primary" />
                  <h3 className="text-base font-semibold text-foreground">
                    Parâmetros de Precificação da Oficina
                  </h3>
                </div>
                <p className="text-xs text-muted-foreground">
                  A precificação separa o valor das peças do catálogo do risco sobre a{" "}
                  <strong>mão de obra</strong>. O risco do veículo incide apenas no tempo de
                  execução dos serviços conforme a tabela FIPE.
                </p>
              </div>

              {/* 2 Campos de Configuração Persistentes no Backend */}
              <div className="grid gap-4 sm:grid-cols-2">
                {/* 1. Valor da Hora Técnica */}
                <div className="rounded-lg border border-border bg-background p-4 flex flex-col justify-between">
                  <div className="flex flex-col gap-1.5">
                    <div className="flex items-center justify-between">
                      <span className="flex items-center gap-1.5 text-xs font-semibold text-foreground">
                        <Clock className="size-3.5 text-primary" />
                        Valor da Hora Técnica
                      </span>
                      <Badge variant="secondary" className="font-mono text-xs">
                        R$ {valorHora.toFixed(2)}/h
                      </Badge>
                    </div>
                    <p className="text-[11px] text-muted-foreground">
                      Valor base da hora de trabalho para a execução de serviços mecânicos.
                    </p>
                  </div>
                  <div className="mt-3">
                    <InputGroup>
                      <InputGroupAddon>R$</InputGroupAddon>
                      <InputGroupInput
                        type="number"
                        step="5"
                        min="20"
                        max="1000"
                        value={valorHora}
                        onChange={(e) => setValorHora(parseFloat(e.target.value) || 0)}
                        className="font-mono font-semibold"
                        required
                      />
                    </InputGroup>
                  </div>
                </div>

                {/* 2. Taxa de Risco FIPE */}
                <div className="rounded-lg border border-border bg-background p-4 flex flex-col justify-between">
                  <div className="flex flex-col gap-1.5">
                    <div className="flex items-center justify-between">
                      <span className="flex items-center gap-1.5 text-xs font-semibold text-foreground">
                        <TrendingUp className="size-3.5 text-primary" />
                        Taxa de Risco FIPE
                      </span>
                      <Badge variant="secondary" className="font-mono text-xs">
                        +{(taxaRisco * 100).toFixed(0)}% / 100k FIPE
                      </Badge>
                    </div>
                    <p className="text-[11px] text-muted-foreground">
                      Percentual de acréscimo da mão de obra a cada R$ 100.000,00 de valor FIPE do veículo.
                    </p>
                  </div>
                  <div className="mt-3">
                    <InputGroup>
                      <InputGroupInput
                        type="number"
                        step="0.01"
                        min="0"
                        max="1"
                        value={taxaRisco}
                        onChange={(e) => setTaxaRisco(parseFloat(e.target.value) || 0)}
                        className="font-mono font-semibold"
                        required
                      />
                      <InputGroupAddon>taxa</InputGroupAddon>
                    </InputGroup>
                  </div>
                </div>
              </div>

              {/* Explicação Didática da Fórmula */}
              <div className="rounded-lg border border-border bg-background p-4 flex flex-col gap-3 text-xs">
                <div className="flex items-center gap-2 font-semibold text-foreground">
                  <Info className="size-4 text-primary" />
                  Como a precificação inteligente funciona:
                </div>
                <div className="grid gap-2 sm:grid-cols-2 text-muted-foreground">
                  <div className="rounded-md bg-muted/40 p-2.5 flex flex-col gap-1">
                    <span className="font-semibold text-foreground">
                      1. Peças: Valor de Catálogo
                    </span>
                    <span>
                      As peças são cobradas pelo seu valor unitário de venda cadastrado diretamente
                      no catálogo da oficina.
                    </span>
                    <code className="text-foreground font-mono mt-0.5">
                      Total Peças = Σ (Quantidade × Valor Unitário)
                    </code>
                  </div>

                  <div className="rounded-md bg-muted/40 p-2.5 flex flex-col gap-1">
                    <span className="font-semibold text-foreground">
                      2. Mão de Obra: Risco Escalável por FIPE
                    </span>
                    <span>
                      O risco/responsabilidade de manusear um carro mais caro encarece as horas técnicas trabalhadas:
                    </span>
                    <code className="text-foreground font-mono mt-0.5">
                      Fator Veículo = 1 + ((FIPE / 100.000) × {taxaRisco.toFixed(2)})
                    </code>
                    <code className="text-foreground font-mono">
                      Mão de Obra = (Horas × R$ {valorHora.toFixed(2)}) × Fator Veículo
                    </code>
                  </div>
                </div>
              </div>

              {/* Comparador Prático em Tempo Real (Popular vs Premium) */}
              <div className="rounded-lg border border-primary/20 bg-primary/5 p-4 flex flex-col gap-3">
                <div className="flex items-center justify-between">
                  <span className="flex items-center gap-1.5 text-xs font-semibold text-primary uppercase tracking-wide">
                    <Car className="size-4" />
                    Comparativo Real com os Parâmetros Atuais (2h de mão de obra)
                  </span>
                  <Badge variant="outline" className="border-primary/30 text-primary text-xs">
                    Simulação Automática
                  </Badge>
                </div>

                <div className="grid gap-4 sm:grid-cols-2 text-xs">
                  {/* Cenário 1: Popular */}
                  <div className="rounded-lg border border-border bg-background p-3.5 flex flex-col gap-2">
                    <div className="flex items-center justify-between border-b border-border pb-1.5">
                      <span className="font-bold text-foreground flex items-center gap-1.5">
                        <Car className="size-3.5 text-muted-foreground" />
                        Carro Popular (FIPE: R$ 20.000)
                      </span>
                      <Badge variant="secondary" className="font-mono text-[10px]">
                        Fator: {corsaFator.toFixed(2)}x
                      </Badge>
                    </div>
                    <div className="flex flex-col gap-1 text-muted-foreground text-[11px]">
                      <div className="flex justify-between">
                        <span>Peças (Catálogo R$ 100):</span>
                        <span className="font-mono font-medium text-foreground">
                          R$ {corsaPecas.toFixed(2)}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span>
                          Mão de Obra (2h × R$ {valorHora.toFixed(2)} × {corsaFator.toFixed(2)}):
                        </span>
                        <span className="font-mono font-medium text-foreground">
                          R$ {corsaMO.toFixed(2)}
                        </span>
                      </div>
                      <div className="flex justify-between border-t border-border pt-1 font-semibold text-foreground text-xs">
                        <span>Orçamento Total:</span>
                        <span className="text-primary font-bold font-mono">
                          R$ {corsaTotal.toFixed(2)}
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Cenário 2: Premium */}
                  <div className="rounded-lg border border-border bg-background p-3.5 flex flex-col gap-2">
                    <div className="flex items-center justify-between border-b border-border pb-1.5">
                      <span className="font-bold text-foreground flex items-center gap-1.5">
                        <Car className="size-3.5 text-primary" />
                        Sedan Premium (FIPE: R$ 250.000)
                      </span>
                      <Badge variant="secondary" className="font-mono text-[10px]">
                        Fator: {sedanFator.toFixed(2)}x
                      </Badge>
                    </div>
                    <div className="flex flex-col gap-1 text-muted-foreground text-[11px]">
                      <div className="flex justify-between">
                        <span>Peças (Catálogo R$ 500):</span>
                        <span className="font-mono font-medium text-foreground">
                          R$ {sedanPecas.toFixed(2)}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span>
                          Mão de Obra (2h × R$ {valorHora.toFixed(2)} × {sedanFator.toFixed(2)}):
                        </span>
                        <span className="font-mono font-medium text-foreground">
                          R$ {sedanMO.toFixed(2)}
                        </span>
                      </div>
                      <div className="flex justify-between border-t border-border pt-1 font-semibold text-foreground text-xs">
                        <span>Orçamento Total:</span>
                        <span className="text-primary font-bold font-mono">
                          R$ {sedanTotal.toFixed(2)}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Simulador Interativo Livre */}
              <div className="rounded-lg border border-border bg-background p-4 flex flex-col gap-3">
                <div className="flex items-center gap-1.5 font-semibold text-foreground text-xs">
                  <Calculator className="size-3.5 text-primary" />
                  Calculadora Interativa de Orçamento:
                </div>

                <div className="grid gap-3 sm:grid-cols-3 text-xs">
                  <div>
                    <FieldLabel className="text-[11px]">Horas do Serviço</FieldLabel>
                    <Input
                      type="number"
                      step="0.5"
                      min="0.5"
                      max="40"
                      value={simHoras}
                      onChange={(e) => setSimHoras(parseFloat(e.target.value) || 1)}
                      className="h-8 font-mono text-xs"
                    />
                  </div>
                  <div>
                    <FieldLabel className="text-[11px]">Valor Peças (Catálogo)</FieldLabel>
                    <Input
                      type="number"
                      step="10"
                      min="0"
                      value={simCustoPecas}
                      onChange={(e) => setSimCustoPecas(parseFloat(e.target.value) || 0)}
                      className="h-8 font-mono text-xs"
                    />
                  </div>
                  <div>
                    <FieldLabel className="text-[11px]">Valor FIPE do Carro</FieldLabel>
                    <Input
                      type="number"
                      step="5000"
                      min="10000"
                      value={simFipe}
                      onChange={(e) => setSimFipe(parseFloat(e.target.value) || 10000)}
                      className="h-8 font-mono text-xs"
                    />
                  </div>
                </div>

                <div className="flex flex-wrap items-center justify-between border-t border-border pt-2 text-xs">
                  <div className="flex items-center gap-4 text-muted-foreground text-[11px]">
                    <span>
                      Fator Risco:{" "}
                      <strong className="text-foreground font-mono">
                        {fatorVeiculoSimulado.toFixed(2)}x
                      </strong>
                    </span>
                    <span>
                      Total Peças:{" "}
                      <strong className="text-foreground font-mono">
                        R$ {totalPecasSimulado.toFixed(2)}
                      </strong>
                    </span>
                    <span>
                      Total Mão de Obra:{" "}
                      <strong className="text-foreground font-mono">
                        R$ {totalMaoDeObraSimulado.toFixed(2)}
                      </strong>
                    </span>
                  </div>
                  <div className="text-right">
                    <span className="text-xs text-muted-foreground mr-2">Valor Estimado:</span>
                    <span className="text-base font-bold text-primary font-mono">
                      R$ {valorFinalSimulado.toFixed(2)}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </CardContent>

          <CardFooter className="flex items-center justify-between border-t border-border bg-muted/20 px-6 py-4">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleRestaurarPadroes}
              disabled={salvandoOficina}
            >
              <RotateCcw data-icon="inline-start" />
              Reverter Alterações
            </Button>
            <Button type="submit" size="sm" disabled={salvandoOficina}>
              <Save data-icon="inline-start" />
              {salvandoOficina ? "Salvando..." : "Salvar Configurações da Oficina"}
            </Button>
          </CardFooter>
        </Card>
      </form>

      {/* SEÇÃO 2: Tabela de Usuários com Acesso à Oficina (Conectada a usuariosService) */}
      <Card>
        <CardHeader>
          <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <div className="flex items-center gap-2">
                <Users className="size-5 text-primary" />
                <CardTitle>Usuários com Acesso à Oficina</CardTitle>
              </div>
              <CardDescription className="mt-1">
                Controle quem tem acesso à sua oficina, entre Administradores e Mecânicos autorizados.
              </CardDescription>
            </div>
            <div className="flex items-center gap-2">
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
                onValueChange={(val) => setFiltroPerfil(val as "todos" | "ADMIN" | "MECANICO")}
              >
                <SelectTrigger className="w-[140px]">
                  <SelectValue placeholder="Perfil" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="todos">Todos Perfis</SelectItem>
                  <SelectItem value="ADMIN">Administrador</SelectItem>
                  <SelectItem value="MECANICO">Mecânico</SelectItem>
                </SelectContent>
              </Select>

              <Select
                value={filtroStatus}
                onValueChange={(val) => setFiltroStatus(val as "todos" | "ativos" | "inativos")}
              >
                <SelectTrigger className="w-[130px]">
                  <SelectValue placeholder="Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="todos">Todos Status</SelectItem>
                  <SelectItem value="ativos">Ativos</SelectItem>
                  <SelectItem value="inativos">Inativos</SelectItem>
                </SelectContent>
              </Select>

              <Badge variant="secondary" className="px-2.5 py-1 text-xs">
                {usuariosFiltrados.length}{" "}
                {usuariosFiltrados.length === 1 ? "usuário" : "usuários"}
              </Badge>
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
            <div className="rounded-md border border-border">
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
                                    : "bg-sidebar-accent text-sidebar-accent-foreground text-xs font-semibold"
                                }
                              >
                                {u.nome.slice(0, 2).toUpperCase()}
                              </AvatarFallback>
                            </Avatar>
                            <span className="font-medium text-foreground">
                              {u.nome} {isCurrentUser && "(Você)"}
                            </span>
                          </div>
                        </TableCell>
                        <TableCell className="text-muted-foreground">
                          {u.email}
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center gap-1.5">
                            {isAdmin ? (
                              <Badge
                                variant="default"
                                className="gap-1 font-semibold text-xs"
                              >
                                <Shield className="size-3" />
                                ADMINISTRADOR
                              </Badge>
                            ) : (
                              <Badge
                                variant="outline"
                                className="gap-1 font-medium text-xs"
                              >
                                <Wrench className="size-3" />
                                MECÂNICO
                              </Badge>
                            )}
                          </div>
                        </TableCell>
                        <TableCell>
                          <Badge
                            variant="outline"
                            className={
                              u.ativo
                                ? "bg-success/15 text-success border-success/40"
                                : "bg-muted text-muted-foreground border-border"
                            }
                          >
                            {u.ativo ? "Ativo" : "Inativo"}
                          </Badge>
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
                              title={u.ativo ? "Desativar acesso" : "Reativar acesso"}
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

          <form onSubmit={handleSalvarUsuario} className="flex flex-col gap-4 py-2">
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
              <FieldLabel htmlFor="usuario-role">Nível de Acesso (Perfil)</FieldLabel>
              <Select
                value={usuarioRole}
                onValueChange={(val) => setUsuarioRole(val as "ADMIN" | "MECANICO")}
              >
                <SelectTrigger id="usuario-role">
                  <SelectValue placeholder="Selecione o perfil" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="MECANICO">Mecânico (Executa OS e orçamentos)</SelectItem>
                  <SelectItem value="ADMIN">
                    Administrador (Acesso total à oficina e relatórios)
                  </SelectItem>
                </SelectContent>
              </Select>
            </Field>

            {/* Seção de Senha com Verificação Dupla */}
            <div className="rounded-lg border border-border bg-muted/30 p-3 flex flex-col gap-3">
              <span className="text-xs font-semibold text-foreground uppercase tracking-wider">
                {usuarioEmEdicao ? "Alterar Senha (Opcional)" : "Definir Senha de Acesso"}
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
                      <AlertCircle className="size-3.5" /> As senhas não coincidem
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
                  Ao desativar <strong>{usuarioAlteracao.nome}</strong> ({usuarioAlteracao.role}), ele
                  não conseguirá mais realizar login na plataforma nem gerenciar dados
                  da oficina.
                </>
              ) : (
                <>
                  Deseja reativar o acesso de <strong>{usuarioAlteracao?.nome}</strong>{" "}
                  ({usuarioAlteracao?.role})? As permissões de acesso ao sistema
                  serão restabelecidas.
                </>
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={alterandoStatus}>Cancelar</AlertDialogCancel>
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
