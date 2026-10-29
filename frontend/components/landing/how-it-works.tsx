import React from 'react';
import { useReveal } from '@/lib/use-reveal';
import { RoadCarSvg } from './ink-illustrations';

const STOPS = [
  {
    step: '01',
    verb: 'Recebe',
    text: 'Digite a placa na chegada e veja proprietário e valor de Tabela FIPE na hora.',
  },
  {
    step: '02',
    verb: 'Orça',
    text: 'Lance peças e mão de obra do catálogo com soma imediata e sem erro de conta.',
  },
  {
    step: '03',
    verb: 'Executa',
    text: 'Vincule o mecânico responsável e acompanhe o serviço avançar no elevador.',
  },
  {
    step: '04',
    verb: 'Entrega',
    text: 'Finalize a ordem para travar os valores e guardar tudo no histórico da placa.',
  },
];

export const HowItWorks: React.FC = () => {
  const { ref, visible } = useReveal<HTMLDivElement>(0.14);

  return (
    <section
      id="como-funciona"
      className="bg-secondary/40 py-28 lg:py-36 border-b-2 border-border/20"
    >
      <div
        ref={ref}
        className={`reveal-item mx-auto max-w-7xl px-5 sm:px-8 ${
          visible ? 'is-visible' : ''
        }`}
      >
        <div className="max-w-2xl">
          <p className="font-mono text-xs font-bold uppercase tracking-widest text-primary">
            Como funciona
          </p>
          <h2 className="mt-3 font-display text-3xl font-extrabold tracking-tight text-foreground sm:text-5xl">
            Do portão à entrega, em quatro paradas.
          </h2>
        </div>

        {/* Cartoon Ink Road + Car */}
        <div className="relative mt-16 pt-6">
          {/* Desktop Horizontal Dashed Ink Road */}
          <div
            aria-hidden="true"
            className="hidden lg:block absolute top-[3.25rem] left-0 right-0 border-t-[3px] border-dashed border-border/40"
          />

          {/* Cartoon Car Travelling on the Road */}
          <div className="mb-8 flex items-center gap-3 lg:mb-10">
            <RoadCarSvg className="h-12 w-auto animate-car-idle" />
            <span className="font-mono text-xs font-bold uppercase tracking-wider text-muted-foreground">
              Fluxo da Ordem de Serviço
            </span>
          </div>

          {/* 4 Road Stops (No Card Boxes) */}
          <div className="grid grid-cols-1 gap-10 sm:grid-cols-2 lg:grid-cols-4 lg:gap-8">
            {STOPS.map((stop) => (
              <div
                key={stop.step}
                className="relative border-l-2 border-dashed border-border/40 pl-6 lg:border-l-0 lg:border-t-2 lg:border-solid lg:border-border lg:pl-0 lg:pt-6"
              >
                <div className="flex items-baseline gap-2.5">
                  <span className="font-mono text-xs font-bold text-primary">
                    {stop.step}
                  </span>
                  <h3 className="font-display text-2xl font-bold text-foreground">
                    {stop.verb}
                  </h3>
                </div>
                <p className="mt-2.5 text-base leading-relaxed text-muted-foreground font-medium">
                  {stop.text}
                </p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
};

