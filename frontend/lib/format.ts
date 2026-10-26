export function formatCurrency(value: number): string {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(value)
}

export function formatDate(value?: string | Date | null): string {
  if (!value) return '-'
  const date = typeof value === 'string' ? new Date(value) : value
  if (isNaN(date.getTime())) return '-'
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(date)
}

export function formatDateTime(value: string | Date): string {
  const date = typeof value === 'string' ? new Date(value) : value
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

// Máscara de Telefone: (XX) XXXXX-XXXX ou (XX) XXXX-XXXX
export function formatTelefone(raw: string): string {
  const digits = raw.replace(/\D/g, '').slice(0, 11)
  if (digits.length <= 2) return digits.length > 0 ? `(${digits}` : ''
  if (digits.length <= 6) return `(${digits.slice(0, 2)}) ${digits.slice(2)}`
  if (digits.length <= 10) {
    return `(${digits.slice(0, 2)}) ${digits.slice(2, 6)}-${digits.slice(6)}`
  }
  return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7, 11)}`
}

// Validação de telefone brasileiro (10 ou 11 dígitos com DDD)
export function isValidTelefone(raw: string): boolean {
  const digits = raw.replace(/\D/g, '')
  return digits.length >= 10 && digits.length <= 11
}

// Máscara de CPF: 000.000.000-00
export function formatCpf(raw: string): string {
  const digits = raw.replace(/\D/g, '').slice(0, 11)
  return digits
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d{1,2})$/, '$1-$2')
}

// Máscara de placa: ABC-1234 ou ABC1D23 (Mercosul) — mantém simples
export function formatPlaca(raw: string): string {
  return raw.toUpperCase().replace(/[^A-Z0-9-]/g, '').slice(0, 8)
}

// Validação de CPF com dígitos verificadores
export function isValidCpf(raw: string): boolean {
  const cpf = raw.replace(/\D/g, '')
  if (cpf.length !== 11) return false
  if (/^(\d)\1{10}$/.test(cpf)) return false

  const calcCheckDigit = (base: string, factor: number): number => {
    let total = 0
    for (const digit of base) {
      total += Number.parseInt(digit, 10) * factor--
    }
    const remainder = (total * 10) % 11
    return remainder === 10 ? 0 : remainder
  }

  const digit1 = calcCheckDigit(cpf.slice(0, 9), 10)
  if (digit1 !== Number.parseInt(cpf[9], 10)) return false

  const digit2 = calcCheckDigit(cpf.slice(0, 10), 11)
  if (digit2 !== Number.parseInt(cpf[10], 10)) return false

  return true
}
