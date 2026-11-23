"use client";

import { useEffect, useState } from "react";
import { useStore } from "@/lib/store";
import { usuariosService } from "@/lib/services";
import { toast } from "sonner";
import { AlertCircle, Check, UserCheck } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Field, FieldLabel, FieldDescription } from "@/components/ui/field";
import { Spinner } from "@/components/ui/spinner";
import { Alert, AlertDescription } from "@/components/ui/alert";
import type { AtualizarUsuarioRequest } from "@/lib/types";

interface MeusDadosModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function MeusDadosModal({ open, onOpenChange }: MeusDadosModalProps) {
  const { user, setUser } = useStore();

  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [confirmarSenha, setConfirmarSenha] = useState("");
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (open && user) {
      setNome(user.nome || "");
      setEmail(user.email || "");
      setSenha("");
      setConfirmarSenha("");
      setErro(null);
    }
  }, [open, user]);

  const senhasCoincidem = senha.length > 0 && confirmarSenha.length > 0 && senha === confirmarSenha;
  const senhasDivergem = senha.length > 0 && confirmarSenha.length > 0 && senha !== confirmarSenha;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro(null);

    const nomeFormatado = nome.trim();
    const emailFormatado = email.trim();

    if (!nomeFormatado) {
      setErro("O nome é obrigatório.");
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(emailFormatado)) {
      setErro("Informe um e-mail válido.");
      return;
    }

    // Verificação dupla de alteração de senha
    if (senha || confirmarSenha) {
      if (!senha) {
        setErro("Digite a nova senha desejada.");
        return;
      }
      if (senha.length < 8) {
        setErro("A nova senha deve ter no mínimo 8 caracteres.");
        return;
      }
      if (!confirmarSenha) {
        setErro("Por favor, digite a confirmação da nova senha.");
        return;
      }
      if (senha !== confirmarSenha) {
        setErro("A confirmação de senha não confere com a nova senha digitada.");
        return;
      }
    }

    setLoading(true);

    try {
      const payload: AtualizarUsuarioRequest = {
        nome: nomeFormatado,
        email: emailFormatado,
      };

      // Só envia senha se preenchida para não violar validação @Size(min = 8) no backend
      if (senha.trim()) {
        payload.senha = senha.trim();
        payload.confirmarSenha = confirmarSenha.trim();
      }

      const usuarioAtualizado = await usuariosService.atualizarMe(payload);

      if (user) {
        setUser({
          ...user,
          nome: usuarioAtualizado.nome,
          email: usuarioAtualizado.email,
        });
      }

      toast.success("Seus dados foram atualizados com sucesso!");
      onOpenChange(false);
    } catch (err: unknown) {
      const mensagem =
        err instanceof Error
          ? err.message
          : "Erro ao atualizar dados. Tente novamente.";
      setErro(mensagem);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <UserCheck className="size-5 text-primary" />
            Meus Dados
          </DialogTitle>
          <DialogDescription>
            Atualize suas informações pessoais de perfil e altere sua senha de acesso.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4 py-2">
          {erro && (
            <Alert variant="destructive">
              <AlertCircle className="size-4" />
              <AlertDescription>{erro}</AlertDescription>
            </Alert>
          )}

          <Field>
            <FieldLabel htmlFor="meus-dados-nome">Nome</FieldLabel>
            <Input
              id="meus-dados-nome"
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              placeholder="Seu nome completo"
              required
              disabled={loading}
            />
          </Field>

          <Field>
            <FieldLabel htmlFor="meus-dados-email">E-mail</FieldLabel>
            <Input
              id="meus-dados-email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="seu.email@exemplo.com"
              required
              disabled={loading}
            />
          </Field>

          {/* Seção com dupla verificação de nova senha */}
          <div className="rounded-lg border border-border bg-muted/30 p-3 flex flex-col gap-3">
            <span className="text-xs font-semibold text-foreground uppercase tracking-wider">
              Alterar Senha (Opcional)
            </span>

            <Field>
              <FieldLabel htmlFor="meus-dados-senha">Nova Senha</FieldLabel>
              <Input
                id="meus-dados-senha"
                type="password"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                placeholder="Mínimo de 8 caracteres"
                disabled={loading}
              />
              <FieldDescription>
                Deixe em branco caso não queira alterar sua senha atual.
              </FieldDescription>
            </Field>

            <Field>
              <FieldLabel htmlFor="meus-dados-confirmar-senha">
                Confirmar Nova Senha
              </FieldLabel>
              <Input
                id="meus-dados-confirmar-senha"
                type="password"
                value={confirmarSenha}
                onChange={(e) => setConfirmarSenha(e.target.value)}
                placeholder="Repita a nova senha exatamente igual"
                disabled={loading}
              />
              {senhasCoincidem && (
                <span className="mt-1 flex items-center gap-1 text-xs font-medium text-success">
                  <Check className="size-3.5" /> As senhas conferem
                </span>
              )}
              {senhasDivergem && (
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
              onClick={() => onOpenChange(false)}
              disabled={loading}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={loading}>
              {loading && <Spinner data-icon="inline-start" />}
              Salvar Alterações
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
