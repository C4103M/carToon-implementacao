'use client';

import { useCallback, useEffect, useState } from 'react';
import { Navbar } from './navbar';
import { Hero } from './hero';
import { Features } from './features';
import { HowItWorks } from './how-it-works';
import { Faq } from './faq';
import { Footer } from './footer';
import { FloatingWhatsApp } from './floating-whatsapp';
import { SimulatorDialog } from './simulator-dialog';
import { ContactDialog } from './contact-dialog';
import { LegalDialog } from './legal-dialog';

export function LandingView() {
  const [simulatorOpen, setSimulatorOpen] = useState(false);
  const [contactOpen, setContactOpen] = useState(false);
  const [legalMode, setLegalMode] = useState<'terms' | 'privacy' | null>(null);

  const openSimulator = useCallback(() => {
    setContactOpen(false);
    setSimulatorOpen(true);
    if (window.location.hash !== '#simulador') {
      window.history.replaceState(null, '', '#simulador');
    }
  }, []);

  const closeSimulator = useCallback(() => {
    setSimulatorOpen(false);
    if (window.location.hash === '#simulador') {
      window.history.replaceState(null, '', window.location.pathname + window.location.search);
    }
  }, []);

  const openContact = useCallback(() => {
    setSimulatorOpen(false);
    setContactOpen(true);
    if (window.location.hash !== '#contato') {
      window.history.replaceState(null, '', '#contato');
    }
  }, []);

  const closeContact = useCallback(() => {
    setContactOpen(false);
    if (window.location.hash === '#contato') {
      window.history.replaceState(null, '', window.location.pathname + window.location.search);
    }
  }, []);

  const openTerms = useCallback(() => setLegalMode('terms'), []);
  const openPrivacy = useCallback(() => setLegalMode('privacy'), []);
  const closeLegal = useCallback(() => setLegalMode(null), []);

  useEffect(() => {
    const syncHash = () => {
      if (window.location.hash === '#simulador') {
        setContactOpen(false);
        setSimulatorOpen(true);
      } else if (window.location.hash === '#contato') {
        setSimulatorOpen(false);
        setContactOpen(true);
      } else {
        setSimulatorOpen(false);
        setContactOpen(false);
      }
    };
    syncHash();
    window.addEventListener('hashchange', syncHash);
    return () => window.removeEventListener('hashchange', syncHash);
  }, []);

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col relative">
      <a
        href="#conteudo-principal"
        className="sr-only focus:not-sr-only focus:fixed focus:top-4 focus:left-4 focus:z-50 focus:rounded-lg focus:bg-primary focus:px-4 focus:py-2 focus:font-display focus:text-sm focus:font-bold focus:text-primary-foreground focus:shadow-ink"
      >
        Pular para o conteúdo principal
      </a>

      {/* Navigation Header */}
      <Navbar onOpenSimulator={openSimulator} onOpenContact={openContact} />

      {/* Main Content */}
      <main id="conteudo-principal" className="flex-1">
        {/* 1. Hero */}
        <Hero onOpenSimulator={openSimulator} onOpenContact={openContact} />

        {/* 2. Recursos */}
        <Features />

        {/* 3. Como Funciona */}
        <HowItWorks />

        {/* 4. Perguntas Frequentes */}
        <Faq />
      </main>

      {/* 5. Rodapé */}
      <Footer
        onOpenTerms={openTerms}
        onOpenPrivacy={openPrivacy}
      />

      {/* Floating WhatsApp Icon */}
      <FloatingWhatsApp
        hidden={simulatorOpen || contactOpen || Boolean(legalMode)}
      />

      {/* Pop-up Windows */}
      <SimulatorDialog
        open={simulatorOpen}
        onClose={closeSimulator}
        onGoToContact={openContact}
      />

      <ContactDialog open={contactOpen} onClose={closeContact} />

      <LegalDialog mode={legalMode} onClose={closeLegal} />
    </div>
  );
}

