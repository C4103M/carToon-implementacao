'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { AlertCircle, ShieldCheck, Store } from 'lucide-react';
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
import { BrandLogo } from '@/components/brand-logo';

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
                        Gerencie clientes, veículos e monte orçamentos com
                        valores de mão de obra ajustados automaticamente pelo
                        valor de mercado de cada veículo.
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

                    <div className="mt-8 rounded-xl border border-border/80 bg-card p-4 shadow-xs">
                        <p className="mb-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                            Contas de demonstração
                        </p>
                        <div className="flex flex-col gap-2.5">
                            <button
                                type="button"
                                onClick={() =>
                                    preencher(oficinaAtiva?.email ?? '')
                                }
                                className="flex items-center gap-3 rounded-lg border border-border/70 bg-secondary/40 p-3 text-left transition-all hover:bg-secondary hover:border-border active:scale-[0.99] cursor-pointer"
                            >
                                <div className="flex size-8 shrink-0 items-center justify-center rounded-md bg-primary text-primary-foreground shadow-xs">
                                    <Store className="size-4" />
                                </div>
                                <div className="flex flex-col min-w-0">
                                    <span className="text-sm font-semibold text-foreground">
                                        Oficina
                                    </span>
                                    <span className="text-xs text-muted-foreground font-medium truncate">
                                        {oficinaAtiva?.email}
                                    </span>
                                </div>
                            </button>
                            <button
                                type="button"
                                onClick={() => preencher(ADMIN_EMAIL)}
                                className="flex items-center gap-3 rounded-lg border border-border/70 bg-secondary/40 p-3 text-left transition-all hover:bg-secondary hover:border-border active:scale-[0.99] cursor-pointer"
                            >
                                <div className="flex size-8 shrink-0 items-center justify-center rounded-md bg-sidebar-primary text-sidebar-primary-foreground shadow-xs">
                                    <ShieldCheck className="size-4" />
                                </div>
                                <div className="flex flex-col min-w-0">
                                    <span className="text-sm font-semibold text-foreground">
                                        Administrador
                                    </span>
                                    <span className="text-xs text-muted-foreground font-medium truncate">
                                        {ADMIN_EMAIL}
                                    </span>
                                </div>
                            </button>
                        </div>
                        <p className="mt-3 text-xs text-muted-foreground font-medium">
                            Qualquer senha funciona neste protótipo.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}
