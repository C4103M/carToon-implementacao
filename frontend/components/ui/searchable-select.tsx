'use client'

import React, { useState, useRef, useEffect, useMemo } from 'react'
import { Search, ChevronDown, Check, X, Loader2 } from 'lucide-react'
import { cn } from '@/lib/utils'

export interface SearchableOption {
  value: string
  label: string
  sublabel?: string
}

interface SearchableSelectProps {
  id?: string
  value: string
  onChange: (value: string) => void
  options: SearchableOption[]
  placeholder?: string
  searchPlaceholder?: string
  disabled?: boolean
  loading?: boolean
  error?: boolean
  className?: string
}

export function SearchableSelect({
  id,
  value,
  onChange,
  options,
  placeholder = 'Selecione uma opção',
  searchPlaceholder = 'Pesquisar...',
  disabled = false,
  loading = false,
  error = false,
  className,
}: SearchableSelectProps) {
  const [aberto, setAberto] = useState(false)
  const [termo, setTermo] = useState('')
  const containerRef = useRef<HTMLDivElement>(null)
  const inputBuscaRef = useRef<HTMLInputElement>(null)

  // Item selecionado
  const itemSelecionado = useMemo(() => {
    return options.find((opt) => opt.value === value)
  }, [options, value])

  // Filtragem das opções
  const opcoesFiltradas = useMemo(() => {
    const q = termo.trim().toLowerCase()
    if (!q) return options

    return options.filter(
      (opt) =>
        opt.label.toLowerCase().includes(q) ||
        (opt.sublabel && opt.sublabel.toLowerCase().includes(q)) ||
        opt.value.toLowerCase().includes(q),
    )
  }, [options, termo])

  // Fechar ao clicar fora
  useEffect(() => {
    function handleClickOutside(event: MouseEvent | TouchEvent) {
      if (
        containerRef.current &&
        !containerRef.current.contains(event.target as Node)
      ) {
        setAberto(false)
      }
    }

    if (aberto) {
      document.addEventListener('mousedown', handleClickOutside)
      document.addEventListener('touchstart', handleClickOutside)
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
      document.removeEventListener('touchstart', handleClickOutside)
    }
  }, [aberto])

  // Foco automático no input de busca ao abrir
  useEffect(() => {
    if (aberto) {
      setTermo('')
      // Timeout curto para garantir renderização do dropdown
      const timer = setTimeout(() => {
        inputBuscaRef.current?.focus()
      }, 50)
      return () => clearTimeout(timer)
    }
  }, [aberto])

  // Trata tecla Escape
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Escape') {
      setAberto(false)
    }
  }

  const handleSelect = (val: string) => {
    onChange(val)
    setAberto(false)
  }

  return (
    <div
      ref={containerRef}
      className={cn('relative w-full', className)}
      onKeyDown={handleKeyDown}
    >
      {/* Botão Gatilho (Trigger) */}
      <button
        id={id}
        type="button"
        disabled={disabled || loading}
        onClick={() => setAberto((prev) => !prev)}
        aria-haspopup="listbox"
        aria-expanded={aberto}
        className={cn(
          'flex h-9 w-full items-center justify-between rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-xs transition-colors',
          'placeholder:text-muted-foreground focus:outline-hidden focus:ring-1 focus:ring-ring',
          'disabled:cursor-not-allowed disabled:opacity-50',
          error && 'border-destructive focus:ring-destructive',
          aberto && 'ring-1 ring-ring border-ring',
        )}
      >
        <span
          className={cn(
            'truncate text-left',
            !itemSelecionado && 'text-muted-foreground',
          )}
        >
          {loading ? (
            <span className="flex items-center gap-2">
              <Loader2 className="size-3.5 animate-spin" />
              <span>Carregando opções...</span>
            </span>
          ) : itemSelecionado ? (
            itemSelecionado.label
          ) : (
            placeholder
          )}
        </span>
        <ChevronDown
          className={cn(
            'size-4 shrink-0 opacity-50 transition-transform duration-200',
            aberto && 'rotate-180',
          )}
        />
      </button>

      {/* Menu Suspenso com Campo de Pesquisa */}
      {aberto && (
        <div className="absolute left-0 top-full z-50 mt-1 w-full rounded-md border border-border bg-popover p-1 text-popover-foreground shadow-lg animate-in fade-in-0 zoom-in-95">
          {/* Barra de Pesquisa */}
          <div className="relative flex items-center border-b border-border px-2 pb-1.5 pt-1">
            <Search className="size-4 shrink-0 opacity-50 mr-2" />
            <input
              ref={inputBuscaRef}
              type="text"
              value={termo}
              onChange={(e) => setTermo(e.target.value)}
              placeholder={searchPlaceholder}
              className="w-full bg-transparent text-sm outline-hidden placeholder:text-muted-foreground"
            />
            {termo && (
              <button
                type="button"
                onClick={() => setTermo('')}
                className="size-4 shrink-0 opacity-50 hover:opacity-100"
              >
                <X className="size-3.5" />
              </button>
            )}
          </div>

          {/* Lista de Opções */}
          <div
            role="listbox"
            className="max-h-56 overflow-y-auto p-1 text-sm outline-hidden"
          >
            {loading ? (
              <div className="flex items-center justify-center py-6 gap-2 text-muted-foreground text-xs">
                <Loader2 className="size-4 animate-spin" />
                <span>Carregando dados da FIPE...</span>
              </div>
            ) : opcoesFiltradas.length === 0 ? (
              <div className="py-6 text-center text-xs text-muted-foreground">
                {options.length === 0
                  ? 'Nenhuma opção disponível'
                  : 'Nenhum resultado encontrado'}
              </div>
            ) : (
              opcoesFiltradas.map((opt) => {
                const selecionado = opt.value === value
                return (
                  <div
                    key={opt.value}
                    role="option"
                    aria-selected={selecionado}
                    onClick={() => handleSelect(opt.value)}
                    className={cn(
                      'relative flex cursor-pointer select-none items-center justify-between rounded-sm px-2.5 py-1.5 text-sm outline-hidden transition-colors',
                      'hover:bg-accent hover:text-accent-foreground',
                      selecionado && 'bg-accent/70 font-medium text-accent-foreground',
                    )}
                  >
                    <div className="flex flex-col min-w-0 pr-2">
                      <span className="truncate">{opt.label}</span>
                      {opt.sublabel && (
                        <span className="text-[11px] text-muted-foreground truncate">
                          {opt.sublabel}
                        </span>
                      )}
                    </div>
                    {selecionado && (
                      <Check className="size-4 shrink-0 text-primary ml-1" />
                    )}
                  </div>
                )
              })
            )}
          </div>
        </div>
      )}
    </div>
  )
}
