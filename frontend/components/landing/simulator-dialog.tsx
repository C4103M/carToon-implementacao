import React from 'react';
import {
  ArrowRight,
  RefreshCw,
  History,
  Lock,
  Plus,
  Trash2,
  Wrench,
  X,
} from 'lucide-react';
import type { StatusServico } from '@/lib/landing-types';
import { formatCurrencyBRL } from '@/lib/landing-contact';
import { LandingDialog } from './landing-dialog';
import { useOrderSimulation } from './use-order-simulation';

interface SimulatorDialogProps {
  open: boolean;
  onClose: () => void;
  onGoToContact: () => void;
}

const STATUS_STEPS: { key: StatusServico; label: string }[] = [
  { key: 'ACEITO', label: 'Aceita' },
  { key: 'EM_ANDAMENTO', label: 'Em andamento' },
  { key: 'FINALIZADO', label: 'Finalizada' },
  { key: 'REJEITADO', label: 'Rejeitada' },
];

export const SimulatorDialog: React.FC<SimulatorDialogProps> = ({
  open,
  onClose,
  onGoToContact,
}) => {
  const sim = useOrderSimulation();

  return (
    <LandingDialog
      open={open}
      onClose={onClose}
      subtitle="Simulador interativo"
      title="Experimente uma Ordem de Serviço ao vivo"
      maxWidthClass="max-w-5xl"
      fullScreenMobile
      footer={
        <div className="flex flex-col items-start justify-between gap-4 sm:flex-row sm:items-center">
          <p className="text-sm font-semibold text-foreground">
            Quer usar esse fluxo com as placas e os mecânicos da sua oficina?
          </p>
          <button
            type="button"
            onClick={onGoToContact}
            className="btn-ink px-5 py-2.5 text-sm cursor-pointer"
          >
            <span>Quero na minha oficina</span>
            <ArrowRight className="h-4 w-4" />
          </button>
        </div>
      }
    >
      {/* Top Bar: Vehicle License Plate Selector */}
      <div className="flex flex-col justify-between gap-4 border-b-2 border-border/20 pb-5 sm:flex-row sm:items-center">
        <div>
          <p className="font-mono text-xs font-bold uppercase tracking-wider text-muted-foreground">
            Selecione a placa no pátio
          </p>
          <div className="mt-2 flex flex-wrap gap-2.5">
            {sim.vehicles.map((v, idx) => {
              const active = idx === sim.selectedVehicleIdx;
              return (
                <button
                  key={v.id}
                  type="button"
                  onClick={() => sim.selectVehicle(idx)}
                  className={`flex items-center gap-2.5 rounded-xl border-2 px-3.5 py-2 text-left transition-all cursor-pointer ${
                    active
                      ? 'border-border bg-foreground text-background shadow-ink-sm'
                      : 'border-border/30 bg-card text-foreground hover:border-border'
                  }`}
                >
                  <span
                    className={`font-mono text-xs font-bold tracking-wider ${
                      active ? 'text-primary' : 'text-primary'
                    }`}
                  >
                    {v.placa}
                  </span>
                  <span className="text-xs font-bold font-display">{v.modelo}</span>
                </button>
              );
            })}
          </div>
        </div>

        <button
          type="button"
          onClick={() => sim.setHistoryOpen((prev) => !prev)}
          className="btn-ink-outline self-start px-3.5 py-2 text-xs sm:self-auto cursor-pointer"
        >
          <History className="h-4 w-4 text-primary" />
          <span>
            {sim.historyOpen
              ? 'Voltar para a OS atual'
              : `Histórico da placa (${sim.currentVehicle.historicoPlaca.length})`}
          </span>
        </button>
      </div>

      {/* Inline Feedback Notice */}
      {sim.notice && (
        <div
          role="status"
          className="mt-4 flex items-center justify-between gap-3 border-l-4 border-primary bg-secondary/50 px-4 py-2.5 text-xs font-semibold text-foreground rounded-r-lg"
        >
          <span>{sim.notice}</span>
          <button
            type="button"
            onClick={() => sim.setNotice(null)}
            className="text-muted-foreground hover:text-foreground cursor-pointer"
            aria-label="Fechar aviso"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      )}

      {/* VIEW A: History Drawer View */}
      {sim.historyOpen ? (
        <div className="mt-6">
          <div className="flex items-baseline justify-between border-b-2 border-border/20 pb-3">
            <div>
              <h3 className="font-display text-xl font-bold text-foreground">
                Histórico da placa {sim.currentVehicle.placa}
              </h3>
              <p className="text-xs font-medium text-muted-foreground">
                {sim.currentVehicle.montadora} {sim.currentVehicle.modelo} · Proprietário:{' '}
                {sim.currentVehicle.proprietario}
              </p>
            </div>
            <button
              type="button"
              onClick={() => sim.setHistoryOpen(false)}
              className="font-display text-xs font-bold text-primary underline underline-offset-4 hover:text-foreground cursor-pointer"
            >
              Voltar para a Ordem de Serviço
            </button>
          </div>

          <div className="divide-y divide-border/20 border-b-2 border-border/20">
            {sim.currentVehicle.historicoPlaca.map((h) => (
              <div
                key={h.osNumero}
                className="flex flex-col justify-between gap-2 py-4 sm:flex-row sm:items-center"
              >
                <div>
                  <div className="flex items-center gap-3 font-mono text-xs font-bold text-foreground">
                    <span className="text-primary">OS #{h.osNumero}</span>
                    <span>·</span>
                    <span>{h.data}</span>
                    <span>·</span>
                    <span>{h.quilometragem}</span>
                  </div>
                  <p className="mt-1 font-display text-base font-bold text-foreground">
                    {h.resumoServicos}
                  </p>
                  <p className="text-xs font-medium text-muted-foreground">
                    Mecânico responsável: {h.mecanicoResponsavel}
                  </p>
                </div>

                <div className="font-mono text-base font-bold text-foreground tabular-nums">
                  {formatCurrencyBRL(h.valorTotal)}
                </div>
              </div>
            ))}
          </div>
        </div>
      ) : (
        /* VIEW B: Active Service Order + FIPE Summary */
        <div className="mt-6 grid grid-cols-1 gap-8 lg:grid-cols-12">
          {/* Left 7 Columns: Status Stepper, Items & Catalog */}
          <div className="lg:col-span-7">
            {/* Order Header & Status Stepper */}
            <div className="flex flex-col justify-between gap-3 border-b-2 border-border/20 pb-4 sm:flex-row sm:items-center">
              <div>
                <span className="font-mono text-xs font-bold text-primary">
                  Ordem de Serviço #{sim.currentVehicle.osAtiva.id} ·{' '}
                  {sim.currentVehicle.osAtiva.abertura}
                </span>
                <h3 className="font-display text-xl font-bold text-foreground">
                  {sim.currentVehicle.montadora} {sim.currentVehicle.modelo} (
                  {sim.currentVehicle.ano})
                </h3>
              </div>

              {/* Status Stepper */}
              <div className="flex flex-wrap items-center gap-1.5">
                {STATUS_STEPS.map((st) => {
                  const isCurrent = sim.currentStatus === st.key;
                  const activeColor =
                    st.key === 'FINALIZADO'
                      ? 'border-border bg-success text-success-foreground shadow-ink-sm'
                      : st.key === 'REJEITADO'
                      ? 'border-border bg-destructive text-destructive-foreground shadow-ink-sm'
                      : 'border-border bg-primary text-primary-foreground shadow-ink-sm';

                  return (
                    <button
                      key={st.key}
                      type="button"
                      onClick={() => sim.changeStatus(st.key)}
                      className={`rounded-lg border-2 px-2.5 py-1 font-mono text-xs font-bold transition-all cursor-pointer ${
                        isCurrent
                          ? activeColor
                          : 'border-border/30 bg-card text-foreground hover:border-border'
                      }`}
                    >
                      {st.label}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Locked State Notice */}
            {sim.isLocked && (
              <div className="mt-4 flex items-center gap-2 rounded-lg border-2 border-success bg-success/15 px-3.5 py-2.5 text-xs font-bold text-success">
                <Lock className="h-4 w-4 shrink-0" />
                <span>
                  OS finalizada. Os itens estão travados para proteger o fechamento do caixa.
                </span>
              </div>
            )}

            {/* Order Items List */}
            <div className="mt-4 divide-y divide-border/20 border-b-2 border-border/20">
              {sim.currentItems.length === 0 ? (
                <p className="py-8 text-center text-sm font-medium text-muted-foreground">
                  Nenhum item lançado nesta ordem. Adicione peças ou serviços abaixo.
                </p>
              ) : (
                sim.currentItems.map((item) => (
                  <div
                    key={item.itemId}
                    className="flex items-center justify-between gap-4 py-3.5 text-sm"
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-mono text-[11px] font-bold uppercase text-primary">
                          {item.categoria === 'PECA' ? 'Peça' : 'Serviço'}
                        </span>
                        <span className="font-bold text-foreground font-display">{item.nome}</span>
                      </div>
                      <p className="mt-0.5 font-mono text-xs text-muted-foreground tabular-nums font-medium">
                        {item.quantidade}x {formatCurrencyBRL(item.valorUnitarioCongelado)}{' '}
                        (valor congelado na OS)
                      </p>
                    </div>

                    <div className="flex items-center gap-3">
                      <span className="font-mono text-sm font-bold text-foreground tabular-nums">
                        {formatCurrencyBRL(
                          item.valorUnitarioCongelado * item.quantidade
                        )}
                      </span>
                      {!sim.isLocked && (
                        <button
                          type="button"
                          onClick={() => sim.removeOrderItem(item.itemId)}
                          className="rounded-lg p-1.5 text-muted-foreground transition-colors hover:bg-destructive/15 hover:text-destructive cursor-pointer"
                          aria-label={`Remover ${item.nome}`}
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* Catalog Picker Drawer */}
            <div className="mt-5">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <button
                  type="button"
                  onClick={() => sim.setCatalogOpen((prev) => !prev)}
                  disabled={sim.isLocked}
                  className={`inline-flex items-center gap-2 font-display text-sm font-bold cursor-pointer ${
                    sim.isLocked
                      ? 'cursor-not-allowed text-muted-foreground/50'
                      : 'text-primary hover:text-foreground'
                  }`}
                >
                  <Plus className="h-4 w-4" />
                  <span>
                    {sim.catalogOpen
                      ? 'Fechar catálogo de peças e serviços'
                      : 'Adicionar peça ou serviço do catálogo'}
                  </span>
                </button>

                <button
                  type="button"
                  onClick={sim.toggleCatalogInflation}
                  className="inline-flex items-center gap-1.5 font-mono text-xs font-bold text-muted-foreground underline decoration-border/40 underline-offset-4 hover:text-foreground cursor-pointer"
                >
                  <RefreshCw className="h-3.5 w-3.5" />
                  <span>
                    {sim.catalogMultiplier === 1
                      ? 'Simular reajuste +15% no catálogo'
                      : 'Restaurar preços do catálogo'}
                  </span>
                </button>
              </div>

              {sim.catalogOpen && !sim.isLocked && (
                <div className="mt-3 grid grid-cols-1 gap-2.5 border-t-2 border-border/20 pt-3 sm:grid-cols-2">
                  {sim.catalog.map((cat) => {
                    const price = Math.round(cat.precoCatalogo * sim.catalogMultiplier);
                    return (
                      <button
                        key={cat.id}
                        type="button"
                        onClick={() => sim.addCatalogItem(cat.id)}
                        className="flex items-center justify-between rounded-xl border-2 border-border/30 bg-card px-3.5 py-2.5 text-left text-xs transition-all hover:border-border hover:shadow-ink-sm cursor-pointer"
                      >
                        <div className="pr-2">
                          <p className="font-bold font-display text-foreground">{cat.nome}</p>
                          <p className="font-mono text-[11px] text-muted-foreground">
                            {cat.detalhe}
                          </p>
                        </div>
                        <span className="shrink-0 font-mono font-bold text-primary tabular-nums">
                          + {formatCurrencyBRL(price)}
                        </span>
                      </button>
                    );
                  })}
                </div>
              )}
            </div>
          </div>

          {/* Right 5 Columns: Total, FIPE Reference & Mechanic */}
          <div className="lg:col-span-5 flex flex-col justify-between border-t-2 border-border/20 pt-6 lg:border-t-0 lg:border-l-2 lg:pl-8 lg:pt-0">
            <div className="space-y-6">
              <div>
                <p className="font-mono text-xs font-bold uppercase tracking-wider text-muted-foreground">
                  Fechamento e referência FIPE
                </p>

                <div className="mt-4 space-y-2.5 text-sm">
                  <div className="flex justify-between text-muted-foreground font-medium">
                    <span>Peças</span>
                    <span className="font-mono font-bold text-foreground tabular-nums">
                      {formatCurrencyBRL(sim.totalPecas)}
                    </span>
                  </div>
                  <div className="flex justify-between text-muted-foreground font-medium">
                    <span>Mão de obra</span>
                    <span className="font-mono font-bold text-foreground tabular-nums">
                      {formatCurrencyBRL(sim.totalServicos)}
                    </span>
                  </div>
                  <div className="flex items-baseline justify-between border-t-2 border-border/30 pt-3">
                    <span className="font-display text-base font-bold text-foreground">
                      Total da Ordem
                    </span>
                    <span className="font-display text-2xl font-extrabold text-foreground tabular-nums">
                      {formatCurrencyBRL(sim.totalOS)}
                    </span>
                  </div>
                </div>
              </div>

              {/* FIPE Proportion Bar */}
              <div className="border-t-2 border-border/20 pt-5">
                <div className="flex items-baseline justify-between text-xs font-bold">
                  <span className="font-mono uppercase text-muted-foreground">
                    Tabela FIPE ({sim.currentVehicle.codigoFipe})
                  </span>
                  <span className="font-mono text-sm text-foreground tabular-nums">
                    {formatCurrencyBRL(sim.currentVehicle.valorFipe)}
                  </span>
                </div>

                <div className="mt-2.5 h-3 w-full overflow-hidden rounded-md bg-secondary border-2 border-border">
                  <div
                    className="h-full bg-primary transition-all duration-300"
                    style={{ width: `${sim.fipeBarWidth}%` }}
                  />
                </div>

                <p className="mt-2 text-xs font-semibold text-foreground">
                  Este orçamento equivale a{' '}
                  <strong className="font-mono text-primary tabular-nums">
                    {sim.fipePercentage}%
                  </strong>{' '}
                  do valor de tabela do veículo.
                </p>
              </div>
            </div>

            {/* Assigned Mechanic */}
            <div className="mt-6 border-t-2 border-border/20 pt-5">
              <div className="flex items-start gap-3">
                <div className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-primary text-primary-foreground border-2 border-border">
                  <Wrench className="h-5 w-5" />
                </div>
                <div>
                  <p className="font-mono text-[11px] font-bold uppercase tracking-wider text-muted-foreground">
                    Mecânico responsável · {sim.currentVehicle.mecanicoAlocado.box}
                  </p>
                  <p className="mt-0.5 font-display text-base font-bold text-foreground">
                    {sim.currentVehicle.mecanicoAlocado.nome}
                  </p>
                  <p className="text-xs font-medium text-muted-foreground">
                    {sim.currentVehicle.mecanicoAlocado.especialidade} ·{' '}
                    {sim.currentVehicle.mecanicoAlocado.entregasMes}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </LandingDialog>
  );
};

