"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import {
  LayoutDashboard,
  Users,
  BookOpen,
  FileText,
  LogOut,
  Wrench,
} from "lucide-react";
import { useStore } from "@/lib/store";
import { getCurrentUser, logout as authLogout } from "@/lib/auth";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Spinner } from "@/components/ui/spinner";
import { ThemeToggle } from "./theme-toggle";

const nav = [
  { href: "/app", label: "Dashboard", icon: LayoutDashboard, exact: true },
  { href: "/app/clientes", label: "Clientes", icon: Users },
  { href: "/app/catalogo", label: "Catálogo", icon: BookOpen },
  { href: "/app/orcamentos", label: "Orçamentos", icon: FileText },
];

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

  useEffect(() => {
    let isMounted = true;

    // Se já temos usuário válido na store com role de oficina, encerra checagem
    if (user && isOficinaRole(user.role)) {
      setCheckingAuth(false);
      return;
    }

    // Se não temos usuário (ex: após refresh F5), consulta o backend via cookie HttpOnly
    getCurrentUser()
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
          router.replace("/admin");
        } else {
          // Não autenticado ou papel inválido
          router.replace("/");
        }
      })
      .catch(() => {
        if (isMounted) {
          router.replace("/");
        }
      });

    return () => {
      isMounted = false;
    };
  }, [user, setUser, router]);

  if (checkingAuth || !user || !isOficinaRole(user.role)) {
    return (
      <div className="flex min-h-svh items-center justify-center">
        <Spinner />
      </div>
    );
  }

  const handleLogout = async () => {
    await authLogout();
    storeLogout();
    router.replace("/");
  };

  return (
    <div className="flex min-h-svh bg-background">
      <aside className="fixed inset-y-0 left-0 hidden w-64 flex-col bg-sidebar text-sidebar-foreground md:flex">
        <div className="flex items-center gap-2 border-b border-sidebar-border px-5 py-4">
          <div className="flex size-9 items-center justify-center rounded-md bg-sidebar-primary text-sidebar-primary-foreground">
            <Wrench className="size-5" />
          </div>
          <div className="flex flex-col leading-tight">
            <span className="font-semibold">OficinaPro</span>
            <span className="text-xs text-sidebar-foreground/60">
              Gestão da oficina
            </span>
          </div>
        </div>

        <nav className="flex flex-1 flex-col gap-1 p-3">
          {nav.map((item) => {
            const active = item.exact
              ? pathname === item.href
              : pathname.startsWith(item.href);
            const Icon = item.icon;
            return (
              <Link
                key={item.href}
                href={item.href}
                className={cn(
                  "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                  active
                    ? "bg-sidebar-primary text-sidebar-primary-foreground"
                    : "text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
                )}
              >
                <Icon className="size-4" />
                {item.label}
              </Link>
            );
          })}
        </nav>

        <div className="border-t border-sidebar-border p-3">
          <div className="flex items-center justify-between gap-3 rounded-md px-2 py-2">
            <div className="flex items-center gap-3 min-w-0 flex-1">
              <Avatar className="size-9 shrink-0">
                <AvatarFallback className="bg-sidebar-accent text-sidebar-accent-foreground text-xs font-semibold">
                  {user.nome ? user.nome.slice(0, 2).toUpperCase() : "OF"}
                </AvatarFallback>
              </Avatar>
              <div className="flex min-w-0 flex-1 flex-col leading-tight">
                <span className="truncate text-sm font-medium">{user.nome}</span>
                <span className="truncate text-xs text-sidebar-foreground/60">
                  {user.email}
                </span>
              </div>
            </div>
            <ThemeToggle />
          </div>
          <Button
            variant="ghost"
            className="mt-1 w-full justify-start text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
            onClick={handleLogout}
          >
            <LogOut data-icon="inline-start" />
            Sair
          </Button>
        </div>
      </aside>

      {/* Barra superior mobile */}
      <div className="flex flex-1 flex-col md:pl-64">
        <header className="sticky top-0 z-30 flex items-center justify-between border-b border-border bg-sidebar px-4 py-3 text-sidebar-foreground md:hidden">
          <div className="flex items-center gap-2">
            <div className="flex size-8 items-center justify-center rounded-md bg-sidebar-primary text-sidebar-primary-foreground">
              <Wrench className="size-4" />
            </div>
            <span className="font-semibold">OficinaPro</span>
          </div>
          <div className="flex items-center gap-2">
            <ThemeToggle />
            <Button variant="ghost" size="sm" onClick={handleLogout}>
              <LogOut data-icon="inline-start" />
              Sair
            </Button>
          </div>
        </header>

        <nav className="flex gap-1 overflow-x-auto border-b border-border bg-card px-2 py-2 md:hidden">
          {nav.map((item) => {
            const active = item.exact
              ? pathname === item.href
              : pathname.startsWith(item.href);
            const Icon = item.icon;
            return (
              <Link
                key={item.href}
                href={item.href}
                className={cn(
                  "flex items-center gap-2 whitespace-nowrap rounded-md px-3 py-1.5 text-sm font-medium",
                  active
                    ? "bg-primary text-primary-foreground"
                    : "text-muted-foreground hover:bg-accent",
                )}
              >
                <Icon className="size-4" />
                {item.label}
              </Link>
            );
          })}
        </nav>

        <main className="flex-1 p-4 md:p-8">{children}</main>
      </div>
    </div>
  );
}
