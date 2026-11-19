import { useEffect } from 'react';

let activeLocks = 0;

/**
 * Reference-counted body scroll lock so transitioning between
 * Mobile Menu -> Dialog or SimulatorDialog -> ContactDialog
 * never accidentally unlocks body scroll while another overlay is open.
 */
export function useBodyScrollLock(locked: boolean) {
  useEffect(() => {
    if (!locked) return;

    activeLocks += 1;
    document.body.style.overflow = 'hidden';

    return () => {
      activeLocks = Math.max(0, activeLocks - 1);
      if (activeLocks === 0) {
        document.body.style.overflow = '';
      }
    };
  }, [locked]);
}

