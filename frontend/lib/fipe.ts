// Mock da consulta à tabela FIPE.
// Simula latência de rede e falhas ocasionais para exercitar os estados de
// carregamento e erro/retry exigidos pela especificação.
// TODO: substituir por API real (ex.: parallelum/brasilapi) numa fase futura.

export interface FipeResult {
  valor: number
  lambda: number
  consultadoEm: string
}

/**
 * Deriva o multiplicador de mão de obra (lambda) a partir do valor de mercado.
 * Veículos mais valiosos elevam o custo da hora trabalhada.
 * Faixa: 0.85x a 2.4x.
 */
export function computeLambda(fipeValor: number): number {
  const raw = 0.85 + fipeValor / 130000
  const clamped = Math.min(Math.max(raw, 0.85), 2.4)
  return Math.round(clamped * 100) / 100
}

// Base de valores fictícios por montadora para gerar números plausíveis.
const BASE_POR_MONTADORA: Record<string, number> = {
  Volkswagen: 62000,
  Chevrolet: 58000,
  Fiat: 49000,
  Ford: 66000,
  Toyota: 98000,
  Honda: 92000,
  Hyundai: 74000,
  Renault: 52000,
  Jeep: 128000,
  Nissan: 71000,
}

function pseudoValor(montadora: string, ano: number): number {
  const base = BASE_POR_MONTADORA[montadora] ?? 55000
  const idade = Math.max(0, new Date().getFullYear() - ano)
  const depreciado = base * Math.pow(0.92, idade)
  // pequena variação determinística para não ficar "redondo demais"
  const jitter = ((montadora.length * 137 + ano) % 40) * 90
  return Math.round((depreciado + jitter) / 10) * 10
}

export interface ConsultarFipeParams {
  montadora: string
  modelo: string
  ano: number
}

/**
 * Simula a consulta assíncrona à FIPE.
 * Rejeita ~25% das vezes para permitir demonstrar o fluxo de erro + retry.
 */
export function consultarFipe({
  montadora,
  ano,
}: ConsultarFipeParams): Promise<FipeResult> {
  return new Promise((resolve, reject) => {
    const delay = 1200 + Math.random() * 1400
    setTimeout(() => {
      if (Math.random() < 0.25) {
        reject(
          new Error(
            'Não foi possível consultar o valor de mercado agora. O serviço da FIPE pode estar indisponível.',
          ),
        )
        return
      }
      const valor = pseudoValor(montadora, ano)
      resolve({
        valor,
        lambda: computeLambda(valor),
        consultadoEm: new Date().toISOString(),
      })
    }, delay)
  })
}
