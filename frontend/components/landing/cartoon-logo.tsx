import React from 'react';
import Image from 'next/image';

interface CartoonLogoProps {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const SIZE_CLASSES: Record<
  NonNullable<CartoonLogoProps['size']>,
  { width: number; height: number; className: string }
> = {
  sm: { width: 85, height: 36, className: 'h-8 sm:h-9' },
  md: { width: 104, height: 44, className: 'h-10 sm:h-11' },
  lg: { width: 132, height: 56, className: 'h-12 sm:h-14' },
};

export const CartoonLogo: React.FC<CartoonLogoProps> = ({
  size = 'md',
  className = '',
}) => {
  const cfg = SIZE_CLASSES[size];

  return (
    <span className={`inline-flex items-center select-none ${className}`.trim()}>
      <Image
        src="/logo-cartoon.png"
        alt="CAR TOON — Sistema de Gestão para Oficinas Mecânicas"
        width={cfg.width}
        height={cfg.height}
        draggable={false}
        className={`${cfg.className} w-auto object-contain transition-transform duration-300 ease-spring group-hover:-rotate-2 dark:hidden`}
      />
      <Image
        src="/logo-cartoon-dark.png"
        alt="CAR TOON — Sistema de Gestão para Oficinas Mecânicas"
        width={cfg.width}
        height={cfg.height}
        draggable={false}
        className={`${cfg.className} w-auto object-contain transition-transform duration-300 ease-spring group-hover:-rotate-2 hidden dark:inline-block`}
      />
    </span>
  );
};
