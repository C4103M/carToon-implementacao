'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { AlertCircle } from 'lucide-react';
import { useStore } from '@/lib/store';
import { authService } from '@/lib/services';
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
import { BrandLogo } from '@/components/brand-logo';

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

        const res = await authService.loginAndRedirect(
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

    const handlePreencherDemo = (demoEmail: string, demoSenha: string) => {
        setEmail(demoEmail);
        setSenha(demoSenha);
        setEmailInvalido(false);
        setErro(null);
        setMensagemErro('');
    };

    return (
        <div className="flex min-h-svh flex-col md:flex-row bg-background">
            {/* Painel de marca */}
            <div className="relative flex flex-col justify-between bg-sidebar p-8 text-sidebar-foreground md:w-1/2 md:p-12 border-b md:border-b-0 md:border-r border-sidebar-border">
                <div className="flex items-center justify-between w-full">
                    <BrandLogo variant="on-dark" size="lg" priority />
                    <ThemeToggle />
                </div>

                <div className="my-10 flex max-w-md flex-col gap-4">
                    <h1 className="text-balance text-4xl font-extrabold font-display leading-[1.08] md:text-5xl">
                        Orçamentos precisos, do seu jeito.
                    </h1>
                    <p className="text-pretty text-sidebar-foreground/80 font-medium leading-relaxed">
                        Gerencie clientes, veículos e monte orçamentos automatizados para seu negócio
                    </p>
                </div>

                <p className="text-xs text-sidebar-foreground/60 font-medium">
                    {'© '}
                    {new Date().getFullYear()}
                    {' CAR TOON. Todos os direitos reservados.'}
                </p>
            </div>

            {/* Formulário */}
            <div className="flex flex-1 items-center justify-center p-6 md:p-12">
                <div className="w-full max-w-sm">
                    <div className="mb-8 flex flex-col gap-2">
                        <h2 className="text-3xl font-extrabold font-display tracking-tight text-foreground">
                            Entrar
                        </h2>
                        <p className="text-sm font-medium text-muted-foreground">
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
                                <FieldLabel htmlFor="email" className="font-bold font-display">E-mail</FieldLabel>
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
                                    <FieldDescription className="text-destructive font-medium">
                                        Informe um e-mail válido.
                                    </FieldDescription>
                                )}
                            </Field>

                            <Field>
                                <FieldLabel htmlFor="senha" className="font-bold font-display">Senha</FieldLabel>
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
                                        className="text-muted-foreground font-medium underline-offset-4 hover:text-foreground hover:underline"
                                    >
                                        Esqueci minha senha
                                    </button>
                                </FieldDescription>
                            </Field>

                            <Button
                                type="submit"
                                disabled={loading}
                                className="w-full mt-2 h-10 text-base font-bold font-display shadow-ink"
                            >
                                {loading && (
                                    <Spinner data-icon="inline-start" />
                                )}
                                Entrar
                            </Button>
                        </FieldGroup>
                    </form>

                    <div className="mt-6 border-t pt-4">
                        <p className="mb-2 text-xs font-medium text-muted-foreground">
                            Contas de demonstração:
                        </p>
                        <div className="flex flex-wrap gap-2">
                            <Button
                                type="button"
                                variant="outline"
                                size="sm"
                                className="text-xs"
                                onClick={() => handlePreencherDemo('admin@teste.com', 'senha123')}
                            >
                                Admin
                            </Button>
                            <Button
                                type="button"
                                variant="outline"
                                size="sm"
                                className="text-xs"
                                onClick={() => handlePreencherDemo('mecanico@teste.com', 'senha123')}
                            >
                                Mecânico
                            </Button>
                            <Button
                                type="button"
                                variant="outline"
                                size="sm"
                                className="text-xs"
                                onClick={() => handlePreencherDemo('superadmin@teste.com', 'senha123')}
                            >
                                Superadmin
                            </Button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
