import { AppError } from './error-handler';
import { getApiUrl } from './services/auth.service';

/**
 * Estrutura padrão de paginação do Spring Data (Page<T>).
 */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  numberOfElements: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

/**
 * Type guard para identificar se uma resposta da API é paginada (Page<T>).
 */
export function isPage<T>(data: unknown): data is Page<T> {
  return (
    typeof data === 'object' &&
    data !== null &&
    'content' in data &&
    Array.isArray((data as { content?: unknown }).content)
  );
}

/**
 * Extrai os itens de uma resposta que pode ser paginada (Page<T>) ou uma lista simples (T[]).
 */
export function unwrapPage<T>(data: Page<T> | T[] | null | undefined): T[] {
  if (!data) return [];
  if (isPage<T>(data)) {
    return data.content;
  }
  return Array.isArray(data) ? data : [];
}

export interface RequestOptions extends Omit<RequestInit, 'body'> {
  params?: Record<string, string | number | boolean | undefined | null>;
  body?: unknown;
}

/**
 * Monta a URL completa adicionando query parameters quando fornecidos.
 */
export function buildUrl(endpoint: string, params?: Record<string, string | number | boolean | undefined | null>): string {
  const base = getApiUrl(endpoint);
  if (!params) return base;

  const origin = typeof window !== 'undefined' ? window.location.origin : 'http://localhost:3000';
  const url = new URL(base, origin);

  Object.entries(params).forEach(([key, val]) => {
    if (val !== undefined && val !== null && val !== '') {
      url.searchParams.append(key, String(val));
    }
  });

  if (base.startsWith('/')) {
    return `${url.pathname}${url.search}`;
  }
  return url.toString();
}

/**
 * Realiza uma requisição HTTP padronizada para a API com envio de credenciais (Cookie HttpOnly jwt).
 */
export async function request<T>(endpoint: string, options: RequestOptions = {}): Promise<T> {
  const { params, body, headers, ...customConfig } = options;
  const url = buildUrl(endpoint, params);

  const isJsonBody = body !== undefined && !(body instanceof FormData) && !(body instanceof Blob);

  const config: RequestInit = {
    ...customConfig,
    // credentials: 'include' garante o envio do cookie HttpOnly "jwt" em todas as chamadas
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      ...(isJsonBody ? { 'Content-Type': 'application/json' } : {}),
      ...headers,
    },
    body: isJsonBody ? JSON.stringify(body) : (body as BodyInit | undefined),
  };

  let response: Response;
  try {
    response = await fetch(url, config);
  } catch (error: unknown) {
    if (error instanceof AppError) throw error;
    const msg = error instanceof Error ? error.message : 'Falha na conexão com o servidor';
    throw new AppError(msg, { code: 'network', cause: error });
  }

  if (!response.ok) {
    let errorDetail = `Erro HTTP ${response.status}`;
    let validationErrors: Record<string, string> | undefined;

    try {
      const data = await response.json();
      errorDetail = data.message || data.error || errorDetail;
      validationErrors = data.errosValidacao;
    } catch {
      if (response.status === 401) {
        errorDetail = 'Sessão expirada ou credenciais inválidas.';
      } else if (response.status === 403) {
        errorDetail = 'Você não tem permissão para acessar este recurso.';
      } else if (response.status === 404) {
        errorDetail = 'Recurso não encontrado.';
      }
    }

    throw new AppError(errorDetail, {
      status: response.status,
      validationErrors,
    });
  }

  // Respostas 204 No Content
  if (response.status === 204) {
    return null as T;
  }

  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    return (await response.json()) as T;
  }

  return (await response.text()) as unknown as T;
}

/**
 * Cliente HTTP unificado para comunicação com a API.
 */
export const api = {
  get: <T>(endpoint: string, options?: RequestOptions) =>
    request<T>(endpoint, { ...options, method: 'GET' }),

  post: <T>(endpoint: string, body?: unknown, options?: RequestOptions) =>
    request<T>(endpoint, { ...options, method: 'POST', body }),

  put: <T>(endpoint: string, body?: unknown, options?: RequestOptions) =>
    request<T>(endpoint, { ...options, method: 'PUT', body }),

  patch: <T>(endpoint: string, body?: unknown, options?: RequestOptions) =>
    request<T>(endpoint, { ...options, method: 'PATCH', body }),

  delete: <T>(endpoint: string, options?: RequestOptions) =>
    request<T>(endpoint, { ...options, method: 'DELETE' }),

  buildUrl,
  unwrapPage,
  isPage,
  getApiUrl,
};

export default api;
