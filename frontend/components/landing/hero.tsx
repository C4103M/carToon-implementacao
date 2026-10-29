import React from 'react';
import { ArrowRight, Play } from 'lucide-react';
import { useReveal } from '@/lib/use-reveal';
import {
  HeroGarageIllustration,
  SquiggleUnderline,
} from './ink-illustrations';

interface HeroProps {
  onOpenSimulator: () => void;
  onOpenContact: () => void;
}

export const Hero: React.FC<HeroProps> = ({
  onOpenSimulator,
  onOpenContact,
}) => {
  const { ref, visible } = useReveal<HTMLDivElement>(0.1);

  return (
    <section
      id="inicio"
      className="relative flex min-h-[90dvh] items-center pt-28 pb-20 lg:pt-36 lg:pb-28"
    >
      <div
        ref={ref}
        className={`reveal-item mx-auto w-full max-w-7xl px-5 sm:px-8 ${
          visible ? 'is-visible' : ''
        }`}
      >
        <div className="grid grid-cols-1 items-center gap-14 lg:grid-cols-12 lg:gap-12">
          {/* Left Column: High-Impact Editorial Typography */}
          <div className="lg:col-span-6">
            <h1 className="font-display text-4xl font-extrabold leading-[1.04] tracking-tight text-foreground sm:text-6xl lg:text-[4.15rem]">
              Sua oficina fora do{' '}
              <span className="relative inline-block text-primary pb-2">
                desenho animado.
                <SquiggleUnderline className="absolute -bottom-0.5 left-0 w-full h-3.5 text-foreground" />
              </span>
            </h1>

            <p className="mt-7 max-w-[50ch] text-lg leading-relaxed text-muted-foreground sm:text-xl font-medium">
              Orçamento com Tabela FIPE, histórico por placa e a equipe organizada em um
              só lugar.
            </p>

            {/* Primary CTA (Simulator Pop-up) + Secondary CTA (Contact Pop-up) */}
            <div className="mt-10 flex flex-wrap items-center gap-7">
              <button
                type="button"
                onClick={onOpenSimulator}
                className="btn-ink px-7 py-4 text-base sm:text-lg cursor-pointer"
              >
                <Play className="h-4 w-4 fill-current" />
                <span>Testar o sistema</span>
              </button>

              <button
                type="button"
                onClick={onOpenContact}
                className="group inline-flex items-center gap-2 py-2 font-display text-base font-bold text-foreground transition-colors hover:text-primary cursor-pointer"
              >
                <span>Falar com a gente</span>
                <ArrowRight className="h-4 w-4 transition-transform duration-300 ease-spring group-hover:translate-x-1" />
              </button>
            </div>
          </div>

          {/* Right Column: Custom Hand-Drawn Cartoon Ink Illustration */}
          <div className="lg:col-span-6">
            <HeroGarageIllustration onOpenSimulator={onOpenSimulator} />
          </div>
        </div>
      </div>
    </section>
  );
};
