import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import type { OficinaStatus, OrcamentoStatus, StatusServico } from "@/lib/types";

const orcamentoConfig: Record<
  string,
  { label: string; className: string }
> = {
  PENDENTE: {
    label: "Pendente",
    className: "bg-warning/15 text-warning-foreground border-warning/40",
  },
  pendente: {
    label: "Pendente",
    className: "bg-warning/15 text-warning-foreground border-warning/40",
  },
  ACEITO: {
    label: "Aceito",
    className: "bg-success/15 text-success border-success/40",
  },
  aceito: {
    label: "Aceito",
    className: "bg-success/15 text-success border-success/40",
  },
  EM_ANDAMENTO: {
    label: "Em Andamento",
    className: "bg-primary/15 text-primary border-primary/40",
  },
  em_andamento: {
    label: "Em Andamento",
    className: "bg-primary/15 text-primary border-primary/40",
  },
  REJEITADO: {
    label: "Rejeitado",
    className: "bg-destructive/15 text-destructive border-destructive/40",
  },
  rejeitado: {
    label: "Rejeitado",
    className: "bg-destructive/15 text-destructive border-destructive/40",
  },
  FINALIZADO: {
    label: "Finalizado",
    className: "bg-info/15 text-info border-info/40",
  },
  finalizado: {
    label: "Finalizado",
    className: "bg-info/15 text-info border-info/40",
  },
};

export function OrcamentoStatusBadge({
  status,
}: {
  status: StatusServico | OrcamentoStatus | string | undefined;
}) {
  const key = status || "PENDENTE";
  const cfg = orcamentoConfig[key] || orcamentoConfig.PENDENTE;
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
