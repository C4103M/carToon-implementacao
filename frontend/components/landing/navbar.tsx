'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { ArrowUpRight, Menu, X } from 'lucide-react';
import { useBodyScrollLock } from '@/lib/use-body-scroll-lock';
import { navigateToSection } from '@/lib/landing-navigation';
import { APP_URL } from '@/lib/landing-contact';
import { CartoonLogo } from './cartoon-logo';
import { ThemeToggle } from '@/components/theme-toggle';

interface NavbarProps {
  onOpenSimulator: () => void;
  onOpenContact: () => void;
}

const NAV_LINKS = [
  { label: 'Recursos', href: '#recursos' },
  { label: 'Como funciona', href: '#como-funciona' },
  { label: 'Perguntas', href: '#perguntas' },
];

export const Navbar: React.FC<NavbarProps> = ({
  onOpenSimulator,
  onOpenContact,
}) => {
  const [scrolled, setScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useBodyScrollLock(mobileMenuOpen);

  useEffect(() => {
    const onScroll = () => {
      setScrolled(window.scrollY > 12);
    };
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  useEffect(() => {
    if (!mobileMenuOpen) return;
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setMobileMenuOpen(false);
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [mobileMenuOpen]);

  const closeMenu = () => {
    setMobileMenuOpen(false);
  };

  const handleMobileNav = (href: string) => {
    closeMenu();
    window.setTimeout(() => {
      navigateToSection(href);
    }, 60);
  };

  return (
    <>
      <header
        className={`fixed top-0 left-0 right-0 z-40 transition-all duration-200 border-b border-border/80 ${
          scrolled ? 'bg-background/95 backdrop-blur-md py-3 shadow-xs' : 'bg-background/90 backdrop-blur-sm py-4'
        }`}
      >
        <div className="mx-auto flex max-w-7xl items-center justify-between px-5 sm:px-8">
          <a
            href="#inicio"
            onClick={(e) => {
              closeMenu();
              navigateToSection('#inicio', e);
            }}
            className="group inline-flex items-center focus:outline-none"
            aria-label="CAR TOON — Voltar ao início"
          >
            <CartoonLogo size="md" />
          </a>

          {/* Plain Editorial Navigation Links (Desktop) */}
          <nav className="hidden md:flex items-center gap-8" aria-label="Navegação principal">
            {NAV_LINKS.map((link) => (
              <a
                key={link.href}
                href={link.href}
                onClick={(e) => navigateToSection(link.href, e)}
                className="relative py-1 text-sm font-bold font-display text-foreground transition-colors hover:text-primary after:absolute after:bottom-0 after:left-0 after:h-[2.5px] after:w-0 after:bg-primary after:transition-all after:duration-300 hover:after:w-full"
              >
                {link.label}
              </a>
            ))}
            <button
              type="button"
              onClick={onOpenContact}
              className="relative py-1 text-sm font-bold font-display text-foreground transition-colors hover:text-primary after:absolute after:bottom-0 after:left-0 after:h-[2.5px] after:w-0 after:bg-primary after:transition-all after:duration-300 hover:after:w-full cursor-pointer"
            >
              Contato
            </button>
          </nav>

          {/* Right Actions: Theme Toggle + Login Link + Primary Simulator CTA (Desktop) */}
          <div className="hidden md:flex items-center gap-5">
            <ThemeToggle />

            <Link
              href={APP_URL}
              className="text-sm font-bold font-display text-foreground transition-colors hover:text-primary"
            >
              Entrar
            </Link>

            <button
              type="button"
              onClick={onOpenSimulator}
              className="btn-ink px-5 py-2.5 text-sm cursor-pointer"
            >
              <span>Testar o sistema</span>
              <ArrowUpRight className="h-4 w-4" />
            </button>
          </div>

          {/* Mobile Menu Trigger */}
          <div className="flex items-center gap-2 md:hidden">
            <ThemeToggle />
            <button
              type="button"
              onClick={() => setMobileMenuOpen((prev) => !prev)}
              className="btn-ink-outline h-10 px-3 text-sm cursor-pointer"
              aria-label={mobileMenuOpen ? 'Fechar menu de navegação' : 'Abrir menu de navegação'}
              aria-expanded={mobileMenuOpen}
              aria-controls="mobile-nav-drawer"
            >
              {mobileMenuOpen ? (
                <>
                  <X className="h-5 w-5" />
                  <span>Fechar</span>
                </>
              ) : (
                <>
                  <Menu className="h-5 w-5" />
                  <span>Menu</span>
                </>
              )}
            </button>
          </div>
        </div>
      </header>

      {/* Mobile Full-Screen Editorial Overlay */}
      {mobileMenuOpen && (
        <div
          id="mobile-nav-drawer"
          role="dialog"
          aria-modal="true"
          aria-label="Menu de navegação"
          className="fixed inset-0 z-50 flex min-h-[100dvh] flex-col justify-between overflow-y-auto px-5 pb-8 pt-4 sm:px-8 bg-background text-foreground md:hidden"
        >
          {/* Drawer Top Bar */}
          <div>
            <div className="flex items-center justify-between border-b-2 border-border pb-4">
              <a
                href="#inicio"
                onClick={(e) => {
                  closeMenu();
                  navigateToSection('#inicio', e);
                }}
                className="inline-flex items-center focus:outline-none"
                aria-label="CAR TOON — Voltar ao início"
              >
                <CartoonLogo size="md" />
              </a>

              <button
                type="button"
                onClick={closeMenu}
                className="btn-ink-outline h-10 px-3 text-sm cursor-pointer"
                aria-label="Fechar menu de navegação"
              >
                <X className="h-5 w-5" />
                <span>Fechar</span>
              </button>
            </div>

            {/* Navigation Items */}
            <nav
              className="mt-4 flex flex-col divide-y-2 divide-border/20 border-b-2 border-border/20"
              aria-label="Menu mobile"
            >
              {NAV_LINKS.map((link) => (
                <button
                  key={link.href}
                  type="button"
                  onClick={() => handleMobileNav(link.href)}
                  className="flex w-full items-center justify-between py-5 text-left font-display text-2xl font-bold text-foreground transition-colors hover:text-primary cursor-pointer"
                >
                  <span>{link.label}</span>
                  <ArrowUpRight className="h-6 w-6 text-primary" />
                </button>
              ))}
              <button
                type="button"
                onClick={() => {
                  closeMenu();
                  onOpenContact();
                }}
                className="flex w-full items-center justify-between py-5 text-left font-display text-2xl font-bold text-foreground transition-colors hover:text-primary cursor-pointer"
              >
                <span>Contato</span>
                <ArrowUpRight className="h-6 w-6 text-primary" />
              </button>
              <Link
                href={APP_URL}
                onClick={closeMenu}
                className="flex w-full items-center justify-between py-5 text-left font-display text-2xl font-bold text-primary transition-colors"
              >
                <span>Acessar o Sistema (Entrar)</span>
                <ArrowUpRight className="h-6 w-6" />
              </Link>
            </nav>
          </div>

          {/* Drawer Bottom CTAs */}
          <div className="mt-8 flex flex-col gap-3 pt-4">
            <button
              type="button"
              onClick={() => {
                closeMenu();
                onOpenSimulator();
              }}
              className="btn-ink w-full py-4 text-base cursor-pointer"
            >
              <span>Testar o sistema</span>
              <ArrowUpRight className="h-5 w-5" />
            </button>

            <button
              type="button"
              onClick={() => {
                closeMenu();
                onOpenContact();
              }}
              className="btn-ink-outline w-full py-3.5 text-base cursor-pointer"
            >
              <span>Falar com a gente</span>
              <ArrowUpRight className="h-5 w-5" />
            </button>
          </div>
        </div>
      )}
    </>
  );
};

