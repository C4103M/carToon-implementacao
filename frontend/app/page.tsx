'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Wrench, AlertCircle } from 'lucide-react';
import { useStore } from '@/lib/store';
import { loginAndRedirect } from '@/lib/auth';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
    Field,
    FieldGroup,
    FieldLabel,
    FieldDescription,
} from '@/components/ui/field';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Spinner } from '@/components/ui/spinner';
import { ThemeToggle } from '@/components/theme-toggle';

export default function LoginPage() {
    const router = useRouter();
    const { setUser } = useStore();
    const [email, setEmail] = useState('');
    const [senha, setSenha] = useState('');
    const [emailInvalido, setEmailInvalido] = useState(false);
    const [erro, setErro] = useState<null | 'invalid' | 'suspensa' | 'bloqueado'>(null);
    const [mensagemErro, setMensagemErro] = useState<string>('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        const emailOk = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
        setEmailInvalido(!emailOk);
        if (!emailOk || !senha) return;

        setErro(null);
        setMensagemErro('');
        setLoading(true);

        const res = await loginAndRedirect(
            { email: email.trim(), password: senha },
            router
        );

        if (res.success && res.user) {
            setUser({
                id: res.user.id,
                nome: res.user.nome,
                email: res.user.email,
                role: res.user.role,
                ativo: res.user.ativo,
                oficinaId: res.user.oficinaId ?? undefined,
            });
        } else {
            const reason = res.reason === 'bloqueado'
                ? 'bloqueado'
                : res.reason === 'suspensa'
                ? 'suspensa'
                : 'invalid';
            setErro(reason);
            setMensagemErro(res.error || 'Falha ao autenticar.');
            setLoading(false);
        }
    };

    return (
        <div className="flex min-h-svh flex-col md:flex-row">
            {/* Painel de marca */}
            <div className="relative flex flex-col justify-between bg-sidebar p-8 text-sidebar-foreground md:w-1/2 md:p-12">
                <div className="flex items-center justify-between w-full">
                    <div className="flex items-center gap-2">
                        <div className="flex size-10 items-center justify-center rounded-md bg-sidebar-primary text-sidebar-primary-foreground">
                            <Wrench className="size-5" />
                        </div>
                        <span className="text-lg font-semibold">
                            CarTOON
                        </span>
                    </div>
                    <ThemeToggle />
                </div>

                <div className="my-10 flex max-w-md flex-col gap-4">
                    <h1 className="text-balance text-3xl font-semibold leading-tight md:text-4xl">
                        Orçamentos precisos, do seu jeito.
                    </h1>
                    <p className="text-pretty text-sidebar-foreground/70">
                        Gerencie clientes, veículos e monte orçamentos automatizados para seu negócio
                    </p>
                </div>

                <p className="text-xs text-sidebar-foreground/50">
                    {'© '}
                    {new Date().getFullYear()}
                    {' CarTOON. Todos os direitos reservados.'}
                </p>
            </div>

            {/* Formulário */}
            <div className="flex flex-1 items-center justify-center p-6 md:p-12">
                <div className="w-full max-w-sm">
                    <div className="mb-8 flex flex-col gap-2">
                        <h2 className="text-2xl font-semibold tracking-tight">
                            Entrar
                        </h2>
                        <p className="text-sm text-muted-foreground">
                            Acesse com seu e-mail e senha.
                        </p>
                    </div>

                    {erro === 'invalid' && (
                        <Alert variant="destructive" className="mb-4">
                            <AlertCircle />
                            <AlertTitle>Não foi possível entrar</AlertTitle>
                            <AlertDescription>
                                {mensagemErro || 'E-mail ou senha inválidos. Verifique os dados e tente novamente.'}
                            </AlertDescription>
                        </Alert>
                    )}

                    {erro === 'bloqueado' && (
                        <Alert variant="destructive" className="mb-4">
                            <AlertCircle />
                            <AlertTitle>Tentativas excedidas</AlertTitle>
                            <AlertDescription>
                                {mensagemErro || 'Acesso temporariamente bloqueado por excesso de tentativas. Tente novamente mais tarde.'}
                            </AlertDescription>
                        </Alert>
                    )}

                    {erro === 'suspensa' && (
                        <Alert variant="destructive" className="mb-4">
                            <AlertCircle />
                            <AlertTitle>Conta indisponível</AlertTitle>
                            <AlertDescription>
                                {mensagemErro || 'Sua conta está inativa ou suspensa. Entre em contato com o suporte.'}
                            </AlertDescription>
                        </Alert>
                    )}

                    <form onSubmit={handleSubmit}>
                        <FieldGroup>
                            <Field data-invalid={emailInvalido || undefined}>
                                <FieldLabel htmlFor="email">E-mail</FieldLabel>
                                <Input
                                    id="email"
                                    type="email"
                                    placeholder="voce@oficina.com"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    aria-invalid={emailInvalido || undefined}
                                    autoComplete="email"
                                />
                                {emailInvalido && (
                                    <FieldDescription className="text-destructive">
                                        Informe um e-mail válido.
                                    </FieldDescription>
                                )}
                            </Field>

                            <Field>
                                <FieldLabel htmlFor="senha">Senha</FieldLabel>
                                <Input
                                    id="senha"
                                    type="password"
                                    placeholder="••••••••"
                                    value={senha}
                                    onChange={(e) => setSenha(e.target.value)}
                                    autoComplete="current-password"
                                />
                                <FieldDescription>
                                    <button
                                        type="button"
                                        className="text-muted-foreground underline-offset-4 hover:text-foreground hover:underline"
                                    >
                                        Esqueci minha senha
                                    </button>
                                </FieldDescription>
                            </Field>

                            <Button
                                type="submit"
                                disabled={loading}
                                className="w-full"
                            >
                                {loading && (
                                    <Spinner data-icon="inline-start" />
                                )}
                                Entrar
                            </Button>
                        </FieldGroup>
                    </form>
                </div>
            </div>
        </div>
    );
}
