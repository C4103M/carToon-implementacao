import type { LeadFormPayload } from './landing-types';

export const PHONE_NUMBER = '(11) 9999-9999';
export const PHONE_DISPLAY = '(11) 9999-9999';
export const COMMERCIAL_EMAIL = 'exemplo.email@cartoon.com';
export const APP_URL = '/';

export function buildWhatsAppUrl(message: string): string {
  return `https://wa.me/${PHONE_NUMBER}?text=${encodeURIComponent(message)}`;
}

export function buildLeadWhatsAppMessage(lead: LeadFormPayload): string {
  const lines = [
    'Olá! Vim pelo site do CAR TOON e quero conhecer o sistema para minha oficina.',
    '',
    `Nome: ${lead.nomeResponsavel.trim()}`,
    `Oficina: ${lead.nomeOficina.trim()}`,
    `WhatsApp: ${lead.whatsapp.trim()}`,
    lead.mensagemOpcional.trim() ? `Contexto: ${lead.mensagemOpcional.trim()}` : '',
  ].filter(Boolean);

  return lines.join('\n');
}

export function buildLeadMailtoUrl(lead: LeadFormPayload): string {
  const subject = `CAR TOON — Demonstração para ${lead.nomeOficina.trim()}`;
  const body = [
    'Olá equipe CAR TOON,',
    '',
    'Gostaria de agendar uma conversa sobre o sistema para nossa oficina.',
    '',
    `Nome: ${lead.nomeResponsavel.trim()}`,
    `Oficina: ${lead.nomeOficina.trim()}`,
    `WhatsApp: ${lead.whatsapp.trim()}`,
    lead.mensagemOpcional.trim() ? `Mensagem: ${lead.mensagemOpcional.trim()}` : '',
  ]
    .filter(Boolean)
    .join('\r\n');

  return `mailto:${COMMERCIAL_EMAIL}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
}

const currencyFormatter = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
});

export function formatCurrencyBRL(value: number): string {
  return currencyFormatter.format(value);
}

export function formatPhoneBR(raw: string): string {
  const digits = raw.replace(/\D/g, '').slice(0, 11);
  if (digits.length === 0) return '';
  if (digits.length <= 2) return `(${digits}`;
  if (digits.length <= 6) return `(${digits.slice(0, 2)}) ${digits.slice(2)}`;
  if (digits.length <= 10) {
    return `(${digits.slice(0, 2)}) ${digits.slice(2, 6)}-${digits.slice(6)}`;
  }
  return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7)}`;
}

