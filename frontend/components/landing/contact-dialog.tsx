import React, { useEffect, useRef, useState } from 'react';
import { ArrowUpRight, Check, Copy } from 'lucide-react';
import type { LeadFormPayload } from '@/lib/landing-types';
import {
  COMMERCIAL_EMAIL,
  PHONE_DISPLAY,
  buildLeadMailtoUrl,
  buildLeadWhatsAppMessage,
  buildWhatsAppUrl,
  formatPhoneBR,
} from '@/lib/landing-contact';
import { LandingDialog } from './landing-dialog';

interface ContactDialogProps {
  open: boolean;
  onClose: () => void;
}

const DEFAULT_WHATSAPP_GREETING =
  'Olá! Vim pelo site do CAR TOON e gostaria de conversar sobre o sistema para minha oficina.';

export const ContactDialog: React.FC<ContactDialogProps> = ({ open, onClose }) => {
  const [formData, setFormData] = useState<LeadFormPayload>({
    nomeResponsavel: '',
    nomeOficina: '',
    whatsapp: '',
    mensagemOpcional: '',
  });

  const [errors, setErrors] = useState<{
    nomeResponsavel?: string;
    nomeOficina?: string;
    whatsapp?: string;
  }>({});

  const [copiedEmail, setCopiedEmail] = useState(false);
  const copyTimerRef = useRef<number | null>(null);

  useEffect(() => {
    return () => {
      if (copyTimerRef.current !== null) {
        window.clearTimeout(copyTimerRef.current);
      }
    };
  }, []);

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    const formattedValue = name === 'whatsapp' ? formatPhoneBR(value) : value;
    setFormData((prev) => ({ ...prev, [name]: formattedValue }));
    if (errors[name as keyof typeof errors]) {
      setErrors((prev) => ({ ...prev, [name]: undefined }));
    }
  };

  const validate = (): boolean => {
    const nextErrors: typeof errors = {};
    if (!formData.nomeResponsavel.trim()) {
      nextErrors.nomeResponsavel = 'Informe seu nome.';
    }
    if (!formData.nomeOficina.trim()) {
      nextErrors.nomeOficina = 'Informe o nome da oficina.';
    }
    const digits = formData.whatsapp.replace(/\D/g, '');
    if (digits.length < 10) {
      nextErrors.whatsapp = 'Informe um WhatsApp com DDD.';
    }
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmitWhatsApp = (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    const msg = buildLeadWhatsAppMessage(formData);
    window.open(buildWhatsAppUrl(msg), '_blank', 'noopener,noreferrer');
    onClose();
  };

  const handleSubmitEmail = () => {
    if (!validate()) return;
    window.location.href = buildLeadMailtoUrl(formData);
    onClose();
  };

  const handleCopyEmail = async () => {
    try {
      await navigator.clipboard.writeText(COMMERCIAL_EMAIL);
      setCopiedEmail(true);
      if (copyTimerRef.current !== null) {
        window.clearTimeout(copyTimerRef.current);
      }
      copyTimerRef.current = window.setTimeout(() => setCopiedEmail(false), 2500);
    } catch {
      window.location.href = `mailto:${COMMERCIAL_EMAIL}`;
    }
  };

  const directWhatsAppHref = buildWhatsAppUrl(DEFAULT_WHATSAPP_GREETING);

  return (
    <LandingDialog
      open={open}
      onClose={onClose}
      subtitle="Contato direto"
      title="Vamos colocar sua oficina para rodar."
      maxWidthClass="max-w-4xl"
    >
      <div className="grid grid-cols-1 gap-10 lg:grid-cols-12 lg:gap-12">
        {/* Left Column: Context & Direct Channels */}
        <div className="lg:col-span-5 flex flex-col justify-between">
          <div>
            <p className="text-base leading-relaxed text-muted-foreground font-medium">
              Uma conversa de 20 minutos e você vê o sistema funcionando com a rotina da
              sua oficina.
            </p>
          </div>

          <div className="mt-8 divide-y divide-border/20 border-y-2 border-border/20">
            <div className="flex items-center justify-between py-4">
              <div>
                <p className="font-mono text-xs font-bold uppercase tracking-wider text-muted-foreground">
                  WhatsApp
                </p>
                <a
                  href={directWhatsAppHref}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="mt-1 inline-block font-mono text-sm font-bold text-foreground hover:text-primary tabular-nums"
                >
                  {PHONE_DISPLAY}
                </a>
              </div>
              <a
                href={directWhatsAppHref}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-1.5 font-display text-sm font-bold text-primary hover:text-foreground"
              >
                <span>Abrir conversa</span>
                <ArrowUpRight className="h-4 w-4" />
              </a>
            </div>

            <div className="flex items-center justify-between py-4">
              <div>
                <p className="font-mono text-xs font-bold uppercase tracking-wider text-muted-foreground">
                  E-mail
                </p>
                <p className="mt-1 font-mono text-sm font-bold text-foreground">
                  {COMMERCIAL_EMAIL}
                </p>
              </div>
              <button
                type="button"
                onClick={handleCopyEmail}
                className="inline-flex items-center gap-1.5 font-display text-sm font-bold text-foreground hover:text-primary cursor-pointer"
              >
                {copiedEmail ? (
                  <>
                    <Check className="h-4 w-4 text-success" />
                    <span>Copiado</span>
                  </>
                ) : (
                  <>
                    <Copy className="h-4 w-4" />
                    <span>Copiar</span>
                  </>
                )}
              </button>
            </div>
          </div>
        </div>

        {/* Right Column: Editorial Underline Form */}
        <div className="lg:col-span-7">
          <form onSubmit={handleSubmitWhatsApp} noValidate className="space-y-6">
            <div>
              <label
                htmlFor="popup-nomeResponsavel"
                className="block font-mono text-xs font-bold uppercase tracking-wider text-foreground"
              >
                Seu nome
              </label>
              <input
                id="popup-nomeResponsavel"
                name="nomeResponsavel"
                type="text"
                autoComplete="name"
                value={formData.nomeResponsavel}
                onChange={handleInputChange}
                placeholder="Ex.: Roberto Almeida"
                aria-invalid={Boolean(errors.nomeResponsavel)}
                className="input-editorial w-full bg-transparent border-b-2 border-border/40 py-2.5 text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary font-semibold"
              />
              {errors.nomeResponsavel && (
                <p className="mt-1.5 font-mono text-xs font-bold text-destructive">
                  {errors.nomeResponsavel}
                </p>
              )}
            </div>

            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
              <div>
                <label
                  htmlFor="popup-nomeOficina"
                  className="block font-mono text-xs font-bold uppercase tracking-wider text-foreground"
                >
                  Nome da oficina
                </label>
                <input
                  id="popup-nomeOficina"
                  name="nomeOficina"
                  type="text"
                  autoComplete="organization"
                  value={formData.nomeOficina}
                  onChange={handleInputChange}
                  placeholder="Ex.: Oficina Almeida"
                  aria-invalid={Boolean(errors.nomeOficina)}
                  className="input-editorial w-full bg-transparent border-b-2 border-border/40 py-2.5 text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary font-semibold"
                />
                {errors.nomeOficina && (
                  <p className="mt-1.5 font-mono text-xs font-bold text-destructive">
                    {errors.nomeOficina}
                  </p>
                )}
              </div>

              <div>
                <label
                  htmlFor="popup-whatsapp"
                  className="block font-mono text-xs font-bold uppercase tracking-wider text-foreground"
                >
                  WhatsApp com DDD
                </label>
                <input
                  id="popup-whatsapp"
                  name="whatsapp"
                  type="tel"
                  inputMode="numeric"
                  autoComplete="tel"
                  value={formData.whatsapp}
                  onChange={handleInputChange}
                  placeholder="(11) 99999-0000"
                  aria-invalid={Boolean(errors.whatsapp)}
                  className="input-editorial w-full bg-transparent border-b-2 border-border/40 py-2.5 text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary font-semibold tabular-nums"
                />
                {errors.whatsapp && (
                  <p className="mt-1.5 font-mono text-xs font-bold text-destructive">
                    {errors.whatsapp}
                  </p>
                )}
              </div>
            </div>

            <div>
              <label
                htmlFor="popup-mensagemOpcional"
                className="block font-mono text-xs font-bold uppercase tracking-wider text-foreground"
              >
                Mensagem (opcional)
              </label>
              <textarea
                id="popup-mensagemOpcional"
                name="mensagemOpcional"
                rows={2}
                value={formData.mensagemOpcional}
                onChange={handleInputChange}
                placeholder="Quantos mecânicos trabalham hoje ou o que você quer organizar primeiro"
                className="input-editorial w-full bg-transparent border-b-2 border-border/40 py-2.5 text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary font-semibold resize-none"
              />
            </div>

            <div className="flex flex-wrap items-center gap-6 pt-2">
              <button type="submit" className="btn-ink px-7 py-3.5 text-base cursor-pointer">
                <span>Enviar pelo WhatsApp</span>
                <ArrowUpRight className="h-5 w-5" />
              </button>

              <button
                type="button"
                onClick={handleSubmitEmail}
                className="font-display text-sm font-bold text-foreground underline decoration-border/40 underline-offset-4 transition-colors hover:text-primary cursor-pointer"
              >
                ou enviar por e-mail
              </button>
            </div>
          </form>
        </div>
      </div>
    </LandingDialog>
  );
};
