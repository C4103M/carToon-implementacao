import { cn } from "@/lib/utils";
import type { OficinaStatus, OrcamentoStatus, StatusServico } from "@/lib/types";

interface StatusStyle {
  label: string;
  containerClass: string;
  dotClass: string;
}

const orcamentoConfig: Record<string, StatusStyle> = {
  PENDENTE: {
    label: "Pendente",
    containerClass: "bg-warning/12 text-warning ring-1 ring-inset ring-warning/25",
    dotClass: "bg-warning",
  },
  pendente: {
    label: "Pendente",
    containerClass: "bg-warning/12 text-warning ring-1 ring-inset ring-warning/25",
    dotClass: "bg-warning",
  },
  ACEITO: {
    label: "Aceito",
    containerClass: "bg-success/12 text-success ring-1 ring-inset ring-success/25",
    dotClass: "bg-success",
  },
  aceito: {
    label: "Aceito",
    containerClass: "bg-success/12 text-success ring-1 ring-inset ring-success/25",
    dotClass: "bg-success",
  },
  EM_ANDAMENTO: {
    label: "Em Andamento",
    containerClass: "bg-primary/12 text-primary ring-1 ring-inset ring-primary/25",
    dotClass: "bg-primary",
  },
  em_andamento: {
    label: "Em Andamento",
    containerClass: "bg-primary/12 text-primary ring-1 ring-inset ring-primary/25",
    dotClass: "bg-primary",
  },
  REJEITADO: {
    label: "Rejeitado",
    containerClass: "bg-destructive/12 text-destructive ring-1 ring-inset ring-destructive/25",
    dotClass: "bg-destructive",
  },
  rejeitado: {
    label: "Rejeitado",
    containerClass: "bg-destructive/12 text-destructive ring-1 ring-inset ring-destructive/25",
    dotClass: "bg-destructive",
  },
  FINALIZADO: {
    label: "Finalizado",
    containerClass: "bg-info/12 text-info ring-1 ring-inset ring-info/25",
    dotClass: "bg-info",
  },
  finalizado: {
    label: "Finalizado",
    containerClass: "bg-info/12 text-info ring-1 ring-inset ring-info/25",
    dotClass: "bg-info",
  },
};

export function OrcamentoStatusBadge({
  status,
  className,
}: {
  status: StatusServico | OrcamentoStatus | string | undefined;
  className?: string;
}) {
  const key = status || "PENDENTE";
  const cfg = orcamentoConfig[key] || orcamentoConfig.PENDENTE;
  return (
    <span
      data-slot="badge"
      className={cn(
        "inline-flex w-fit shrink-0 items-center gap-1.5 rounded-md px-2 py-0.5 text-xs font-semibold whitespace-nowrap",
        cfg.containerClass,
        className
      )}
    >
      <span
        className={cn("size-1.5 shrink-0 rounded-full", cfg.dotClass)}
        aria-hidden="true"
      />
      {cfg.label}
    </span>
  );
}

const oficinaConfig: Record<OficinaStatus, StatusStyle> = {
  ativa: {
    label: "Ativa",
    containerClass: "bg-success/12 text-success ring-1 ring-inset ring-success/25",
    dotClass: "bg-success",
  },
  suspensa: {
    label: "Suspensa",
    containerClass: "bg-destructive/12 text-destructive ring-1 ring-inset ring-destructive/25",
    dotClass: "bg-destructive",
  },
  pendente: {
    label: "Pendente de aprovação",
    containerClass: "bg-warning/12 text-warning ring-1 ring-inset ring-warning/25",
    dotClass: "bg-warning",
  },
};

export function OficinaStatusBadge({ status }: { status: OficinaStatus }) {
  const cfg = oficinaConfig[status];
  return (
    <span
      data-slot="badge"
      className={cn(
        "inline-flex w-fit shrink-0 items-center gap-1.5 rounded-md px-2 py-0.5 text-xs font-semibold whitespace-nowrap",
        cfg.containerClass
      )}
    >
      <span
        className={cn("size-1.5 shrink-0 rounded-full", cfg.dotClass)}
        aria-hidden="true"
      />
      {cfg.label}
    </span>
  );
}

export function VeiculoStatusBadge({ ativo }: { ativo: boolean }) {
  return (
    <span
      data-slot="badge"
      className={cn(
        "inline-flex w-fit shrink-0 items-center gap-1.5 rounded-md px-2 py-0.5 text-xs font-semibold whitespace-nowrap",
        ativo
          ? "bg-success/12 text-success ring-1 ring-inset ring-success/25"
          : "bg-muted text-muted-foreground ring-1 ring-inset ring-border"
      )}
    >
      <span
        className={cn(
          "size-1.5 shrink-0 rounded-full",
          ativo ? "bg-success" : "bg-muted-foreground/60"
        )}
        aria-hidden="true"
      />
      {ativo ? "Ativo" : "Substituído"}
    </span>
  );
}
