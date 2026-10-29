import React from 'react';
import { buildWhatsAppUrl } from '@/lib/landing-contact';

interface FloatingWhatsAppProps {
  hidden?: boolean;
}

export const FloatingWhatsApp: React.FC<FloatingWhatsAppProps> = ({
  hidden = false,
}) => {
  if (hidden) return null;

  const url = buildWhatsAppUrl(
    'Olá! Vim pelo site do CAR TOON e gostaria de conversar sobre o sistema para minha oficina.'
  );

  return (
    <a
      href={url}
      target="_blank"
      rel="noopener noreferrer"
      aria-label="Falar pelo WhatsApp"
      className="fixed bottom-6 right-6 z-30 flex h-14 w-14 items-center justify-center rounded-2xl border-2 border-border bg-primary text-primary-foreground shadow-ink transition-transform duration-200 hover:-translate-y-1 hover:brightness-110 active:translate-y-0.5"
    >
      <svg
        viewBox="0 0 24 24"
        width="28"
        height="28"
        stroke="currentColor"
        strokeWidth="2"
        fill="none"
        strokeLinecap="round"
        strokeLinejoin="round"
        className="fill-current"
      >
        <path d="M3 21l1.65-3.8a9 9 0 1 1 3.4 2.9L3 21" />
        <path d="M9 10a.5.5 0 0 0 1 0V9a.5.5 0 0 0-1 0v1a5 5 0 0 0 5 5h1a.5.5 0 0 0 0-1h-1a.5.5 0 0 0 0 1" />
      </svg>
    </a>
  );
};

