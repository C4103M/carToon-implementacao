"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { LogOut } from "lucide-react";
import { useStore } from "@/lib/store";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { Badge } from "@/components/ui/badge";
import { ThemeToggle } from "./theme-toggle";
import { BrandLogo } from "./brand-logo";

export function AdminShell({ children }: { children: React.ReactNode }) {
  const { user, logout } = useStore();
  const router = useRouter();

  useEffect(() => {
    if (!user || user.role !== "admin") {
      router.replace("/");
    }
  }, [user, router]);

  if (!user || user.role !== "admin") {
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
    <div className="min-h-svh bg-background">
      <header className="sticky top-0 z-30 border-b border-sidebar-border bg-sidebar text-sidebar-foreground">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3 md:px-8">
          <div className="flex items-center gap-3">
            <BrandLogo variant="on-dark" size="sm" priority />
            <Badge variant="secondary" className="ml-1 hidden sm:inline-flex border border-sidebar-border/60">
              Admin
            </Badge>
          </div>
          <div className="flex items-center gap-3">
            <span className="hidden text-sm text-sidebar-foreground/70 sm:inline font-medium">
              {user.email}
            </span>
            <ThemeToggle />
            <Button
              variant="ghost"
              size="sm"
              onClick={handleLogout}
              className="text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground font-medium"
            >
              <LogOut data-icon="inline-start" />
              Sair
            </Button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8 md:px-8">{children}</main>
    </div>
  );
}
