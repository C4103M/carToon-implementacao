import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import type { OficinaStatus, OrcamentoStatus } from "@/lib/types";

const orcamentoConfig: Record<
  OrcamentoStatus,
  { label: string; className: string }
> = {
  pendente: {
    label: "Pendente",
    className: "bg-warning/15 text-warning-foreground border-warning/40",
  },
  aceito: {
    label: "Aceito",
    className: "bg-success/15 text-success border-success/40",
  },
  rejeitado: {
    label: "Rejeitado",
    className: "bg-destructive/15 text-destructive border-destructive/40",
  },
  finalizado: {
    label: "Finalizado",
    className: "bg-info/15 text-info border-info/40",
  },
};

export function OrcamentoStatusBadge({ status }: { status: OrcamentoStatus }) {
  const cfg = orcamentoConfig[status];
  return (
    <Badge variant="outline" className={cn("font-medium", cfg.className)}>
      {cfg.label}
    </Badge>
  );
}

const oficinaConfig: Record<
  OficinaStatus,
  { label: string; className: string }
> = {
  ativa: {
    label: "Ativa",
    className: "bg-success/15 text-success border-success/40",
  },
  suspensa: {
    label: "Suspensa",
    className: "bg-destructive/15 text-destructive border-destructive/40",
  },
  pendente: {
    label: "Pendente de aprovação",
    className: "bg-warning/15 text-warning-foreground border-warning/40",
  },
};

export function OficinaStatusBadge({ status }: { status: OficinaStatus }) {
  const cfg = oficinaConfig[status];
  return (
    <Badge variant="outline" className={cn("font-medium", cfg.className)}>
      {cfg.label}
    </Badge>
  );
}

export function VeiculoStatusBadge({ ativo }: { ativo: boolean }) {
  return (
    <Badge
      variant="outline"
      className={cn(
        "font-medium",
        ativo
          ? "bg-success/15 text-success border-success/40"
          : "bg-muted text-muted-foreground border-border",
      )}
    >
      {ativo ? "Ativo" : "Substituído"}
    </Badge>
  );
}
