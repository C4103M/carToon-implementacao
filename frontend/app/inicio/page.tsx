import type { Metadata } from 'next';
import { LandingView } from '@/components/landing/landing-view';

export const metadata: Metadata = {
    title: 'CAR TOON — Gestão de Oficinas Mecânicas fora do Desenho Animado',
    description:
        'Orçamentos automáticos com referência da Tabela FIPE, histórico completo por placa, gestão de mecânicos e catálogo de peças com preço protegido para oficinas mecânicas.',
};

export default function InicioPage() {
    return <LandingView />;
}
