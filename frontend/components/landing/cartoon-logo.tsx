import React from 'react';

interface CartoonLogoProps {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const SIZE_CLASSES: Record<NonNullable<CartoonLogoProps['size']>, string> = {
  sm: 'h-8 sm:h-9',
  md: 'h-10 sm:h-11',
  lg: 'h-12 sm:h-14',
};

export const CartoonLogo: React.FC<CartoonLogoProps> = ({
  size = 'md',
  className = '',
}) => {
  return (
    <span className={`inline-flex items-center select-none ${className}`.trim()}>
      <img
        src="/logo-cartoon.png?v=2"
        alt="CAR TOON — Sistema de Gestão para Oficinas Mecânicas"
        decoding="async"
        draggable={false}
        className={`${SIZE_CLASSES[size]} w-auto object-contain transition-transform duration-300 ease-spring group-hover:-rotate-2`}
      />
    </span>
  );
};
