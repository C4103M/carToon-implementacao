import { toast } from 'sonner';

/**
 * Estrutura padronizada de erro retornada pelo backend (Spring Boot ErroResposta).
 */
export interface BackendErrorResponse {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  errosValidacao?: Record<string, string>;
}

/**
 * Estrutura normalizada de erro para consumo em qualquer parte do frontend.
 */
export interface NormalizedError {
  title: string;
  message: string;
  status?: number;
  validationErrors?: Record<string, string>;
  isNetworkError: boolean;
  isAuthError: boolean;
  raw: unknown;
}

export interface HandleErrorOptions {
  /**
   * Título customizado para sobrepor o título padrão inferido.
   */
  title?: string;
  /**
   * Mensagem customizada para sobrepor a mensagem padrão inferida.
   */
  fallbackMessage?: string;
  /**
   * Se true, não exibe o toast na tela (apenas normaliza e loga).
   */
  silent?: boolean;
  /**
   * Duração de exibição do toast em milissegundos (padrão: 5000).
   */
  duration?: number;
  /**
   * Ação opcional no toast (ex: botão de 'Tentar novamente').
   */
  action?: {
    label: string;
    onClick: () => void;
  };
}

/**
 * Classe de erro customizada para disparar erros estruturados na aplicação.
 */
export class AppError extends Error {
  public readonly status?: number;
  public readonly code?: string;
  public readonly validationErrors?: Record<string, string>;
  public readonly originalError?: unknown;

  constructor(
    message: string,
    options?: {
      status?: number;
      code?: string;
      validationErrors?: Record<string, string>;
      cause?: unknown;
    }
  ) {
    super(message);
    this.name = 'AppError';
    this.status = options?.status;
    this.code = options?.code;
    this.validationErrors = options?.validationErrors;
    this.originalError = options?.cause;
  }
}

/**
 * Analisa e normaliza qualquer tipo de erro (Response da fetch, Error, string, etc.)
 * para um formato único previsível.
 */
export function normalizeError(error: unknown, fallbackMessage?: string): NormalizedError {
  let title = 'Ocorreu um erro';
  let message = fallbackMessage || 'Não foi possível completar a operação.';
  let status: number | undefined;
  let validationErrors: Record<string, string> | undefined;
  let isNetworkError = false;
  let isAuthError = false;

  if (typeof error === 'string') {
    message = error;
  } else if (error instanceof AppError) {
    message = error.message;
    status = error.status;
    validationErrors = error.validationErrors;
  } else if (error instanceof Error) {
    const msgLower = error.message.toLowerCase();

    // Verificação de falhas de rede / servidor fora do ar
    if (
      msgLower.includes('failed to fetch') ||
      msgLower.includes('network') ||
      msgLower.includes('econnrefused') ||
      msgLower.includes('connection refused')
    ) {
      title = 'Servidor indisponível';
      message = 'Não foi possível conectar ao servidor. Verifique sua conexão com a internet ou se a API está online.';
      isNetworkError = true;
    } else {
      message = error.message;
    }

    // Se o erro tiver propriedades customizadas injetadas (ex: status ou reason)
    const errorAny = error as unknown as Record<string, unknown>;
    if (typeof errorAny.status === 'number') {
      status = errorAny.status;
    }
    if (errorAny.errosValidacao && typeof errorAny.errosValidacao === 'object') {
      validationErrors = errorAny.errosValidacao as Record<string, string>;
    }
  } else if (error && typeof error === 'object') {
    const errorObj = error as Record<string, unknown>;

    if (typeof errorObj.message === 'string') {
      message = errorObj.message;
    }
    if (typeof errorObj.status === 'number') {
      status = errorObj.status;
    }
    if (errorObj.errosValidacao && typeof errorObj.errosValidacao === 'object') {
      validationErrors = errorObj.errosValidacao as Record<string, string>;
    }
  }

  // Refina título e mensagem com base no status HTTP (Spring Boot)
  if (status) {
    switch (status) {
      case 400:
        title = 'Dados inválidos';
        if (!message || message.includes('Failed')) {
          message = 'Verifique as informações preenchidas e tente novamente.';
        }
        break;
      case 401:
        title = 'Falha na autenticação';
        isAuthError = true;
        if (!message || message.toLowerCase().includes('unauthorized')) {
          message = 'E-mail ou senha incorretos.';
        }
        break;
      case 403:
        title = 'Acesso negado';
        isAuthError = true;
        message = 'Você não possui permissão para executar esta ação.';
        break;
      case 404:
        title = 'Não encontrado';
        if (!message || message.toLowerCase().includes('not found')) {
          message = 'O recurso solicitado não foi encontrado no sistema.';
        }
        break;
      case 409:
        title = 'Conflito de dados';
        break;
      case 422:
        title = 'Regra de negócio';
        break;
      case 429:
        title = 'Muitas tentativas';
        isAuthError = true;
        message = 'Acesso temporariamente bloqueado por segurança. Aguarde alguns minutos e tente novamente.';
        break;
      case 500:
      case 502:
      case 503:
        title = 'Instabilidade no servidor';
        message = 'Ocorreu um erro interno no servidor. Tente novamente em instantes.';
        break;
      default:
        break;
    }
  }

  // Se houver erros específicos de validação de campo do Spring Boot
  if (validationErrors && Object.keys(validationErrors).length > 0) {
    const listaErros = Object.values(validationErrors).join(', ');
    message = `${message} (${listaErros})`;
  }

  return {
    title,
    message,
    status,
    validationErrors,
    isNetworkError,
    isAuthError,
    raw: error,
  };
}

/**
 * FUNÇÃO CENTRALIZADA DE TRATAMENTO DE ERROS DO FRONTEND.
 * 
 * Pode ser chamada em qualquer bloco catch, falha de API ou validação:
 * 
 * Exemplo de uso:
 * ```ts
 * try {
 *   await login(credentials);
 * } catch (err) {
 *   handleError(err);
 * }
 * ```
 * 
 * @param error O erro capturado (Error, AppError, Response ou desconhecido)
 * @param options Opções de personalização da mensagem ou do toast
 * @returns O erro normalizado (contendo title, message, status, campos de validação, etc.)
 */
export function handleError(
  error: unknown,
  options?: HandleErrorOptions
): NormalizedError {
  const normalized = normalizeError(error, options?.fallbackMessage);
  const displayTitle = options?.title || normalized.title;

  // Log detalhado no console para depuração
  console.error(`[ErrorHandler] ${displayTitle}:`, {
    message: normalized.message,
    status: normalized.status,
    validationErrors: normalized.validationErrors,
    error,
  });

  // Exibe o toast visual na tela (a menos que silent seja ativado)
  if (!options?.silent) {
    toast.error(displayTitle, {
      description: normalized.message,
      duration: options?.duration ?? 5000,
      action: options?.action
        ? {
            label: options.action.label,
            onClick: options.action.onClick,
          }
        : undefined,
    });
  }

  return normalized;
}

/**
 * Extrai diretamente apenas a mensagem amigável de um erro (útil para formulários ou componentes de Alert).
 */
export function getErrorMessage(error: unknown, fallback?: string): string {
  return normalizeError(error, fallback).message;
}

/**
 * Extrai os erros de validação por campo retornados pelo backend (Spring Boot errosValidacao).
 */
export function getFieldErrors(error: unknown): Record<string, string> | undefined {
  return normalizeError(error).validationErrors;
}
