"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import {
  LayoutDashboard,
  Users,
  BookOpen,
  FileText,
  LogOut,
  User,
  UserCog,
  Building2,
} from "lucide-react";
import { useStore } from "@/lib/store";
import { authService } from "@/lib/services";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Spinner } from "@/components/ui/spinner";
import { ThemeToggle } from "./theme-toggle";
import { BrandLogo } from "./brand-logo";
import { MeusDadosModal } from "./meus-dados-modal";

function isOficinaRole(role?: string): boolean {
  if (!role) return false;
  const normalized = role.toUpperCase().replace(/^ROLE_/, "");
  return normalized === "OFICINA" || normalized === "MECANICO" || normalized === "ADMIN";
}

export function AppShell({ children }: { children: React.ReactNode }) {
  const { user, setUser, logout: storeLogout } = useStore();
  const router = useRouter();
  const pathname = usePathname();
  const [checkingAuth, setCheckingAuth] = useState(!user);
  const [meusDadosOpen, setMeusDadosOpen] = useState(false);

  useEffect(() => {
    let isMounted = true;

    // Se já estiver logado como Superadmin, redireciona imediatamente
    if (user && user.role?.toUpperCase().replace(/^ROLE_/, "") === "SUPERADMIN") {
      router.replace("/superadmin");
      return;
    }

    // Se já temos usuário válido na store com role de oficina, encerra checagem
    if (user && isOficinaRole(user.role)) {
      setCheckingAuth(false);
      return;
    }

    // Se não temos usuário (ex: após refresh F5), consulta o backend via cookie HttpOnly
    authService.getCurrentUser()
      .then((authUser) => {
        if (!isMounted) return;

        if (authUser && isOficinaRole(authUser.role)) {
          setUser({
            id: authUser.id,
            nome: authUser.nome,
            email: authUser.email,
            role: authUser.role,
            ativo: authUser.ativo,
            oficinaId: authUser.oficinaId ?? undefined,
          });
          setCheckingAuth(false);
        } else if (authUser && authUser.role?.toUpperCase().replace(/^ROLE_/, "") === "SUPERADMIN") {
          // Se for Superadmin tentando acessar a área da oficina, envia para o painel de admin
          router.replace("/superadmin");
        } else {
          // Não autenticado ou papel inválido
          router.replace("/login");
        }
      })
      .catch(() => {
        if (isMounted) {
          router.replace("/login");
        }
      });

    return () => {
      isMounted = false;
    };
  }, [user, setUser, router]);

  const isAdmin = user?.role?.toUpperCase().replace(/^ROLE_/, "") === "ADMIN";

  const navigationItems = useMemo(() => {
    const items = [
      { href: "/app", label: "Dashboard", icon: LayoutDashboard, exact: true },
      { href: "/app/clientes", label: "Clientes", icon: Users, exact: false },
      { href: "/app/catalogo", label: "Catálogo", icon: BookOpen, exact: false },
      { href: "/app/orcamentos", label: "Orçamentos", icon: FileText, exact: false },
    ];

    if (isAdmin) {
      items.push(
        { href: "/app/mecanicos", label: "Mecânicos", icon: UserCog, exact: false },
        { href: "/app/oficina", label: "Minha Oficina", icon: Building2, exact: false }
      );
    }

    return items;
  }, [isAdmin]);

  if (checkingAuth || !user || !isOficinaRole(user.role)) {
    return (
      <div className="flex min-h-svh items-center justify-center">
        <Spinner />
      </div>
    );
  }

  const handleLogout = async () => {
    await authService.logout();
    storeLogout();
    router.replace("/login");
  };

  return (
    <div className="flex min-h-svh w-full max-w-full overflow-x-hidden bg-background">
      <aside className="fixed inset-y-0 left-0 hidden w-64 flex-col bg-sidebar text-sidebar-foreground md:flex border-r border-sidebar-border">
        <div className="flex items-center gap-2.5 border-b border-sidebar-border px-5 py-4">
          <BrandLogo variant="on-dark" size="sm" priority />
          <span className="text-xs text-sidebar-foreground/60 font-medium">
            Gestão
          </span>
        </div>

        <nav className="flex flex-1 flex-col gap-1.5 p-3">
          {navigationItems.map((item) => {
            const active = item.exact
              ? pathname === item.href
              : pathname.startsWith(item.href);
            const Icon = item.icon;
            return (
              <Link
                key={item.href}
                href={item.href}
                className={cn(
                  "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-semibold transition-all",
                  active
                    ? "bg-sidebar-primary text-sidebar-primary-foreground shadow-ink-sm"
                    : "text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground font-medium",
                )}
              >
                <Icon className="size-4" />
                {item.label}
              </Link>
            );
          })}
        </nav>

        <div className="border-t border-sidebar-border p-3">
          <div className="flex items-center justify-between gap-3 rounded-lg border border-sidebar-border bg-sidebar-accent/50 p-2.5">
            <div className="flex items-center gap-2.5 min-w-0 flex-1">
              <Avatar className="size-9 shrink-0 border border-sidebar-foreground/30">
                <AvatarFallback className="bg-sidebar-primary text-sidebar-primary-foreground text-xs font-bold font-display">
                  {user.nome ? user.nome.slice(0, 2).toUpperCase() : "OF"}
                </AvatarFallback>
              </Avatar>
              <div className="flex min-w-0 flex-1 flex-col leading-tight">
                <span className="truncate text-sm font-semibold">{user.nome}</span>
                <span className="truncate text-xs text-sidebar-foreground/70">
                  {user.email}
                </span>
              </div>
            </div>
            <ThemeToggle />
          </div>
          <Button
            variant="ghost"
            className="mt-2 w-full justify-start text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground font-medium"
            onClick={() => setMeusDadosOpen(true)}
          >
            <User data-icon="inline-start" />
            Meus Dados
          </Button>
          <Button
            variant="ghost"
            className="mt-1 w-full justify-start text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground font-medium"
            onClick={handleLogout}
          >
            <LogOut data-icon="inline-start" />
            Sair
          </Button>
        </div>
      </aside>

      {/* Barra superior mobile */}
      <div className="flex flex-1 flex-col min-w-0 w-full max-w-full md:pl-64">
        <header className="sticky top-0 z-30 flex items-center justify-between border-b border-border bg-sidebar px-4 py-3 text-sidebar-foreground md:hidden">
          <div className="flex items-center gap-2">
            <BrandLogo variant="on-dark" size="xs" priority />
          </div>
          <div className="flex items-center gap-1">
            <ThemeToggle />
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setMeusDadosOpen(true)}
              className="text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
            >
              <User data-icon="inline-start" />
              <span className="sr-only">Meus Dados</span>
            </Button>
            <Button variant="ghost" size="sm" onClick={handleLogout}>
              <LogOut data-icon="inline-start" />
              Sair
            </Button>
          </div>
        </header>

        <nav className="flex gap-1.5 overflow-x-auto border-b border-border bg-card px-3 py-2 md:hidden">
          {navigationItems.map((item) => {
            const active = item.exact
              ? pathname === item.href
              : pathname.startsWith(item.href);
            const Icon = item.icon;
            return (
              <Link
                key={item.href}
                href={item.href}
                className={cn(
                  "flex items-center gap-2 whitespace-nowrap rounded-lg px-3 py-1.5 text-sm font-semibold transition-all",
                  active
                    ? "bg-primary text-primary-foreground shadow-ink-sm"
                    : "text-muted-foreground hover:bg-secondary font-medium",
                )}
              >
                <Icon className="size-4" />
                {item.label}
              </Link>
            );
          })}
        </nav>

        <main className="flex-1 p-4 md:p-8 min-w-0 w-full max-w-full">{children}</main>
      </div>

      <MeusDadosModal
        open={meusDadosOpen}
        onOpenChange={setMeusDadosOpen}
      />
    </div>
  );
}
