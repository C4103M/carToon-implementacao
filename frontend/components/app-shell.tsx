"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect } from "react";
import {
  LayoutDashboard,
  Users,
  BookOpen,
  FileText,
  LogOut,
  Wrench,
} from "lucide-react";
import { useStore } from "@/lib/store";
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

export function AppShell({ children }: { children: React.ReactNode }) {
  const { user, logout } = useStore();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (!user || user.role !== "oficina") {
      router.replace("/");
    }
  }, [user, router]);

  if (!user || user.role !== "oficina") {
    return (
      <div className="flex min-h-svh items-center justify-center">
        <Spinner />
      </div>
    );
  }

  const handleLogout = () => {
    logout();
    router.replace("/");
  };

  return (
    <div className="flex min-h-svh bg-background">
      <aside className="fixed inset-y-0 left-0 hidden w-64 flex-col bg-sidebar text-sidebar-foreground md:flex border-r-2 border-sidebar-border">
        <div className="flex items-center gap-2 border-b-2 border-sidebar-border px-5 py-4">
          <div className="flex size-9 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground border-2 border-sidebar-foreground shadow-ink-sm">
            <Wrench className="size-5" />
          </div>
          <div className="flex flex-col leading-tight">
            <span className="font-bold font-display text-base">OficinaPro</span>
            <span className="text-xs text-sidebar-foreground/70 font-medium">
              Gestão da oficina
            </span>
          </div>
        </div>

        <nav className="flex flex-1 flex-col gap-1.5 p-3">
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
                  "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-bold font-display transition-all",
                  active
                    ? "bg-sidebar-primary text-sidebar-primary-foreground border-2 border-sidebar-foreground shadow-ink-sm"
                    : "text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground border-2 border-transparent",
                )}
              >
                <Icon className="size-4" />
                {item.label}
              </Link>
            );
          })}
        </nav>

        <div className="border-t-2 border-sidebar-border p-3">
          <div className="flex items-center justify-between gap-3 rounded-lg border-2 border-sidebar-border bg-sidebar-accent/50 p-2.5">
            <div className="flex items-center gap-2.5 min-w-0 flex-1">
              <Avatar className="size-9 shrink-0 border-2 border-sidebar-foreground">
                <AvatarFallback className="bg-sidebar-primary text-sidebar-primary-foreground text-xs font-bold font-display">
                  {user.nome.slice(0, 2).toUpperCase()}
                </AvatarFallback>
              </Avatar>
              <div className="flex min-w-0 flex-1 flex-col leading-tight">
                <span className="truncate text-sm font-bold">{user.nome}</span>
                <span className="truncate text-xs text-sidebar-foreground/70">
                  {user.email}
                </span>
              </div>
            </div>
            <ThemeToggle />
          </div>
          <Button
            variant="ghost"
            className="mt-2 w-full justify-start text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground border-2 border-transparent hover:border-sidebar-border font-bold"
            onClick={handleLogout}
          >
            <LogOut data-icon="inline-start" />
            Sair
          </Button>
        </div>
      </aside>

      {/* Barra superior mobile */}
      <div className="flex flex-1 flex-col md:pl-64">
        <header className="sticky top-0 z-30 flex items-center justify-between border-b-2 border-border bg-sidebar px-4 py-3 text-sidebar-foreground md:hidden">
          <div className="flex items-center gap-2">
            <div className="flex size-8 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground border-2 border-sidebar-foreground shadow-ink-sm">
              <Wrench className="size-4" />
            </div>
            <span className="font-bold font-display">OficinaPro</span>
          </div>
          <div className="flex items-center gap-2">
            <ThemeToggle />
            <Button variant="ghost" size="sm" onClick={handleLogout} className="border-2 border-transparent hover:border-sidebar-border">
              <LogOut data-icon="inline-start" />
              Sair
            </Button>
          </div>
        </header>

        <nav className="flex gap-1.5 overflow-x-auto border-b-2 border-border bg-card px-3 py-2 md:hidden">
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
                  "flex items-center gap-2 whitespace-nowrap rounded-lg px-3 py-1.5 text-sm font-bold font-display transition-all",
                  active
                    ? "bg-primary text-primary-foreground border-2 border-border shadow-ink-sm"
                    : "text-muted-foreground hover:bg-secondary border-2 border-transparent",
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
