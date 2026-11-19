import Link from "next/link";
import { ChevronLeft } from "lucide-react";
import { cn } from "@/lib/utils";

interface PageHeaderProps {
  title: string;
  description?: string;
  backHref?: string;
  backLabel?: string;
  actions?: React.ReactNode;
  className?: string;
}

export function PageHeader({
  title,
  description,
  backHref,
  backLabel = "Voltar",
  actions,
  className,
}: PageHeaderProps) {
  return (
    <div className={cn("mb-6 flex flex-col gap-4", className)}>
      {backHref && (
        <Link
          href={backHref}
          className="inline-flex w-fit items-center gap-1 text-sm font-bold text-foreground/70 transition-colors hover:text-primary"
        >
          <ChevronLeft className="size-4" />
          {backLabel}
        </Link>
      )}
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center min-w-0 w-full">
        <div className="flex flex-col gap-1 min-w-0">
          <h1 className="text-pretty text-3xl font-extrabold font-display tracking-tight text-foreground">
            {title}
          </h1>
          {description && (
            <p className="text-pretty text-sm font-medium text-muted-foreground">
              {description}
            </p>
          )}
        </div>
        {actions && (
          <div className="flex flex-wrap items-center gap-2 sm:shrink-0">{actions}</div>
        )}
      </div>
    </div>
  );
}
