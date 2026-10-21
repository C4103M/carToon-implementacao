'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Wrench, AlertCircle, ShieldCheck, Store } from 'lucide-react';
import { useStore } from '@/lib/store';
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
import { ADMIN_EMAIL, seedOficinas } from '@/lib/mock-data';
import { ThemeToggle } from '@/components/theme-toggle';

export default function LoginPage() {
    const router = useRouter();
    const { login, oficinas } = useStore();
    const [email, setEmail] = useState('');
    const [senha, setSenha] = useState('');
    const [emailInvalido, setEmailInvalido] = useState(false);
    const [erro, setErro] = useState<null | 'invalid' | 'suspensa'>(null);
    const [loading, setLoading] = useState(false);

    const oficinaAtiva =
        oficinas.find((o) => o.status === 'ativa') ?? seedOficinas[0];

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        const emailOk = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
        setEmailInvalido(!emailOk);
        if (!emailOk || !senha) return;

        setErro(null);
        setLoading(true);

        const res = login(email);
        if (res.ok) {
            if (email.trim().toLowerCase() === ADMIN_EMAIL.toLowerCase()) {
                router.push('/admin');
            } else {
                router.push('/app');
            }
        } else {
            setErro(res.reason);
            setLoading(false);
        }
    };

    const preencher = (value: string) => {
        setEmail(value);
        setSenha('123456');
        setErro(null);
        setEmailInvalido(false);
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
                            OficinaPro
                        </span>
                    </div>
                    <ThemeToggle />
                </div>

                <div className="my-10 flex max-w-md flex-col gap-4">
                    <h1 className="text-balance text-3xl font-semibold leading-tight md:text-4xl">
                        Orçamentos precisos, do seu jeito.
                    </h1>
                    <p className="text-pretty text-sidebar-foreground/70">
                        Gerencie clientes, veículos e monte orçamentos com
                        valores de mão de obra ajustados automaticamente pelo
                        valor de mercado de cada veículo.
                    </p>
                </div>

                <p className="text-xs text-sidebar-foreground/50">
                    {'© '}
                    {new Date().getFullYear()}
                    {' OficinaPro. Todos os direitos reservados.'}
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
                            Acesse com seu e-mail e senha. O sistema identifica
                            automaticamente o tipo de conta.
                        </p>
                    </div>

                    {erro === 'invalid' && (
                        <Alert variant="destructive" className="mb-4">
                            <AlertCircle />
                            <AlertTitle>Não foi possível entrar</AlertTitle>
                            <AlertDescription>
                                E-mail ou senha inválidos. Verifique os dados e
                                tente novamente.
                            </AlertDescription>
                        </Alert>
                    )}

                    {erro === 'suspensa' && (
                        <Alert variant="destructive" className="mb-4">
                            <AlertCircle />
                            <AlertTitle>Conta indisponível</AlertTitle>
                            <AlertDescription>
                                Sua conta está temporariamente indisponível.
                                Entre em contato com o suporte.
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

                    <div className="mt-8 rounded-lg border border-dashed border-border bg-muted/40 p-4">
                        <p className="mb-3 text-xs font-medium uppercase tracking-wide text-muted-foreground">
                            Contas de demonstração
                        </p>
                        <div className="flex flex-col gap-2">
                            <button
                                type="button"
                                onClick={() =>
                                    preencher(oficinaAtiva?.email ?? '')
                                }
                                className="flex items-center gap-3 rounded-md border border-border bg-card p-3 text-left transition-colors hover:border-primary/50"
                            >
                                <Store className="size-4 text-primary" />
                                <div className="flex flex-col">
                                    <span className="text-sm font-medium">
                                        Oficina
                                    </span>
                                    <span className="text-xs text-muted-foreground">
                                        {oficinaAtiva?.email}
                                    </span>
                                </div>
                            </button>
                            <button
                                type="button"
                                onClick={() => preencher(ADMIN_EMAIL)}
                                className="flex items-center gap-3 rounded-md border border-border bg-card p-3 text-left transition-colors hover:border-primary/50"
                            >
                                <ShieldCheck className="size-4 text-primary" />
                                <div className="flex flex-col">
                                    <span className="text-sm font-medium">
                                        Administrador
                                    </span>
                                    <span className="text-xs text-muted-foreground">
                                        {ADMIN_EMAIL}
                                    </span>
                                </div>
                            </button>
                        </div>
                        <p className="mt-3 text-xs text-muted-foreground">
                            Qualquer senha funciona neste protótipo.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}
