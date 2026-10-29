import type React from 'react';

let activeAnimationId: number | null = null;

function easeInOutCubic(t: number): number {
  return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
}

export function smoothScrollTo(targetY: number, customDuration?: number): Promise<void> {
  return new Promise((resolve) => {
    if (activeAnimationId !== null) {
      cancelAnimationFrame(activeAnimationId);
      activeAnimationId = null;
    }

    const prefersReducedMotion =
      typeof window.matchMedia === 'function' &&
      window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    const startY = window.scrollY || window.pageYOffset;
    const distance = targetY - startY;

    if (prefersReducedMotion || Math.abs(distance) < 4) {
      window.scrollTo(0, targetY);
      resolve();
      return;
    }

    const duration =
      customDuration ??
      Math.min(950, Math.max(500, Math.round(Math.abs(distance) * 0.12 + 380)));

    let startTime: number | null = null;

    const removeListeners = () => {
      window.removeEventListener('wheel', cancelScroll);
      window.removeEventListener('touchmove', cancelScroll);
    };

    const cancelScroll = () => {
      if (activeAnimationId !== null) {
        cancelAnimationFrame(activeAnimationId);
        activeAnimationId = null;
      }
      removeListeners();
      resolve();
    };

    window.addEventListener('wheel', cancelScroll, { passive: true });
    window.addEventListener('touchmove', cancelScroll, { passive: true });

    function animate(currentTime: number) {
      if (startTime === null) startTime = currentTime;
      const elapsed = currentTime - startTime;
      const progress = Math.min(elapsed / duration, 1);
      const ease = easeInOutCubic(progress);

      window.scrollTo(0, Math.round(startY + distance * ease));

      if (progress < 1) {
        activeAnimationId = requestAnimationFrame(animate);
      } else {
        activeAnimationId = null;
        removeListeners();
        resolve();
      }
    }

    activeAnimationId = requestAnimationFrame(animate);
  });
}

export function navigateToSection(targetHref: string, e?: React.MouseEvent) {
  if (e) {
    e.preventDefault();
  }

  const sectionId = targetHref.replace('#', '');

  if (sectionId === 'inicio' || sectionId === '') {
    smoothScrollTo(0, 650).then(() => {
      if (window.history.pushState) {
        window.history.pushState(null, '', '#inicio');
      }
    });
    return;
  }

  const targetElement = document.getElementById(sectionId);
  if (!targetElement) return;

  const navHeight = 76;
  const elementPosition =
    targetElement.getBoundingClientRect().top + (window.scrollY || window.pageYOffset);
  const offsetPosition = Math.max(0, elementPosition - navHeight);

  smoothScrollTo(offsetPosition).then(() => {
    if (window.history.pushState) {
      window.history.pushState(null, '', `#${sectionId}`);
    }
  });
}
