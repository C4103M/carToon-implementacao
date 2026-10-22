"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { LogOut, ShieldCheck } from "lucide-react";
import { useStore } from "@/lib/store";
import { getCurrentUser, logout as authLogout } from "@/lib/auth";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { Badge } from "@/components/ui/badge";
import { ThemeToggle } from "./theme-toggle";

function isAdminRole(role?: string): boolean {
  if (!role) return false;
  const normalized = role.toUpperCase().replace(/^ROLE_/, "");
  return normalized === "SUPERADMIN" || normalized === "ADMIN";
}

export function AdminShell({ children }: { children: React.ReactNode }) {
  const { user, setUser, logout: storeLogout } = useStore();
  const router = useRouter();
  const [checkingAuth, setCheckingAuth] = useState(!user);

  useEffect(() => {
    let isMounted = true;

    if (user && isAdminRole(user.role)) {
      setCheckingAuth(false);
      return;
    }

    getCurrentUser()
      .then((authUser) => {
        if (!isMounted) return;

        if (authUser && isAdminRole(authUser.role)) {
          setUser({
            id: authUser.id,
            nome: authUser.nome,
            email: authUser.email,
            role: authUser.role,
            ativo: authUser.ativo,
            oficinaId: authUser.oficinaId ?? undefined,
          });
          setCheckingAuth(false);
        } else if (authUser) {
          // Usuário autenticado mas sem permissão de admin (ex: mecânico)
          router.replace("/app");
        } else {
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

  if (checkingAuth || !user || !isAdminRole(user.role)) {
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
    <div className="min-h-svh bg-background">
      <header className="sticky top-0 z-30 border-b border-border bg-sidebar text-sidebar-foreground">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3 md:px-8">
          <div className="flex items-center gap-2">
            <div className="flex size-9 items-center justify-center rounded-md bg-sidebar-primary text-sidebar-primary-foreground">
              <ShieldCheck className="size-5" />
            </div>
            <div className="flex flex-col leading-tight">
              <span className="font-semibold">OficinaPro</span>
              <span className="text-xs text-sidebar-foreground/60">
                Painel administrativo
              </span>
            </div>
            <Badge variant="secondary" className="ml-2 hidden sm:inline-flex">
              Admin
            </Badge>
          </div>
          <div className="flex items-center gap-3">
            <span className="hidden text-sm text-sidebar-foreground/70 sm:inline">
              {user.email}
            </span>
            <ThemeToggle />
            <Button
              variant="ghost"
              size="sm"
              onClick={handleLogout}
              className="text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
            >
              <LogOut data-icon="inline-start" />
              Sair
            </Button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl p-4 md:p-8">{children}</main>
    </div>
  );
}
