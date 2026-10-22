import { AppError, handleError } from './error-handler';

/**
 * Módulo de Autenticação (Client / Frontend)
 * 
 * Gerencia todo o fluxo de autenticação com o backend:
 * 1. Envio de credenciais para autenticação (POST /api/auth/login)
 * 2. Recepção e manutenção de cookie HttpOnly ("jwt") via credentials: 'include'
 * 3. Consulta dos dados e perfil do usuário autenticado (GET /api/usuario/me)
 * 4. Validação do nível de acesso (Role: SUPERADMIN, ADMIN, MECANICO)
 * 5. Redirecionamento inteligente para a rota adequada conforme as permissões
 * 6. Encerramento de sessão e invalidação de cookie (POST /api/auth/logout)
 */

export type UserRole = 'SUPERADMIN' | 'ADMIN' | 'MECANICO' | string;

export interface AuthUser {
  id: number;
  nome: string;
  email: string;
  role: UserRole;
  ativo: boolean;
  oficinaId?: number | null;
}

export interface LoginCredentials {
  email: string;
  password: string;
}

export type LoginErrorReason = 'invalid' | 'suspensa' | 'bloqueado' | 'network' | 'desconhecido';

export interface LoginResult {
  success: boolean;
  user?: AuthUser;
  redirectUrl?: string;
  error?: string;
  reason?: LoginErrorReason;
}

/**
 * Resolve a URL completa para os endpoints da API.
 * No navegador, usa o caminho relativo '/api' atendido pelo rewrite do Next.js
 * para garantir mesma origem (Same-Origin), eliminando bloqueios de CORS e problemas de cookies.
 */
export function getApiUrl(endpoint: string): string {
  const isBrowser = typeof window !== 'undefined';
  const envUrl = process.env.NEXT_PUBLIC_API_URL;

  // No navegador, por padrão usa caminho relativo para usufruir do proxy do Next.js (mesma origem)
  if (isBrowser && (!envUrl || envUrl === '' || envUrl === '/api' || envUrl.includes('localhost:3000'))) {
    const cleanEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
    return cleanEndpoint.startsWith('/api') ? cleanEndpoint : `/api${cleanEndpoint}`;
  }

  const cleanBase = (envUrl || 'http://localhost:8080').replace(/\/+$/, '');
  const cleanEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;

  if (cleanBase.endsWith('/api') && cleanEndpoint.startsWith('/api')) {
    return `${cleanBase}${cleanEndpoint.slice(4)}`;
  }
  if (!cleanBase.endsWith('/api') && !cleanEndpoint.startsWith('/api')) {
    return `${cleanBase}/api${cleanEndpoint}`;
  }
  return `${cleanBase}${cleanEndpoint}`;
}

/**
 * Determina a URL de redirecionamento apropriada com base no nível de acesso (Role).
 * 
 * - SUPERADMIN: Administrador da plataforma -> /admin
 * - ADMIN: Administrador de oficina ou gestor -> /app (ou /admin se não possuir oficina vinculada)
 * - MECANICO: Operador da oficina -> /app
 */
export function getRedirectPathForUser(user: AuthUser): string {
  const roleUpper = String(user.role || '').toUpperCase().replace(/^ROLE_/, '');

  if (roleUpper === 'SUPERADMIN') {
    return '/admin';
  }

  if (roleUpper === 'ADMIN') {
    // Se for admin geral sem oficina vinculada, vai para o painel administrativo
    return user.oficinaId ? '/app' : '/admin';
  }

  // Padrão para MECANICO ou outros perfis operacionais da oficina
  return '/app';
}

/**
 * Busca os dados do usuário autenticado atualmente usando o cookie HttpOnly salvo.
 * 
 * @returns AuthUser se logado, ou null caso o cookie esteja ausente/expirado/inválido.
 */
export async function getCurrentUser(): Promise<AuthUser | null> {
  try {
    const url = getApiUrl('/usuario/me');
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
      },
      // credentials: 'include' garante o envio do cookie HttpOnly "jwt"
      credentials: 'include',
    });

    if (!response.ok) {
      return null;
    }

    const data = await response.json();
    return {
      id: data.id,
      nome: data.nome,
      email: data.email,
      role: data.role,
      ativo: Boolean(data.ativo),
      oficinaId: data.oficinaId ?? null,
    };
  } catch (error) {
    console.error('Erro ao buscar dados do usuário autenticado:', error);
    return null;
  }
}

/**
 * Realiza a chamada de login no backend.
 * 
 * O backend responde com Set-Cookie HttpOnly ("jwt") no navegador.
 * Em seguida, busca os dados do usuário autenticado via getCurrentUser() para checar seu nível de acesso.
 */
export async function login(credentials: LoginCredentials): Promise<AuthUser> {
  const url = getApiUrl('/auth/login');

  let response: Response;
  try {
    response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
      // credentials: 'include' é indispensável para receber e persistir o cookie HttpOnly
      credentials: 'include',
      body: JSON.stringify({
        email: credentials.email.trim(),
        password: credentials.password,
      }),
    });
  } catch (err: unknown) {
    if (err instanceof AppError) throw err;
    const errorMsg = err instanceof Error ? err.message : 'Falha na conexão com o servidor';
    throw new AppError(errorMsg, { code: 'network', cause: err });
  }

  if (!response.ok) {
    let errorDetail = 'Falha ao autenticar';
    let reason: LoginErrorReason = 'invalid';
    let errosValidacao: Record<string, string> | undefined;

    try {
      const errorData = await response.json();
      errorDetail = errorData.message || errorData.error || errorDetail;
      errosValidacao = errorData.errosValidacao;
    } catch {
      if (response.status === 401) {
        errorDetail = 'Email ou senha incorreto';
      } else if (response.status === 429) {
        errorDetail = 'Número excessivo de tentativas. Tente novamente mais tarde.';
        reason = 'bloqueado';
      }
    }

    if (response.status === 429 || errorDetail.toLowerCase().includes('bloqueio') || errorDetail.toLowerCase().includes('tentativas')) {
      reason = 'bloqueado';
    } else if (errorDetail.toLowerCase().includes('inativo') || errorDetail.toLowerCase().includes('suspensa')) {
      reason = 'suspensa';
    }

    throw new AppError(errorDetail, {
      status: response.status,
      code: reason,
      validationErrors: errosValidacao,
    });
  }

  // Cookie HttpOnly foi armazenado pelo navegador; agora recuperamos as permissões do usuário
  const user = await getCurrentUser();

  if (!user) {
    throw new AppError('Não foi possível carregar as informações do usuário logado.', {
      code: 'desconhecido',
    });
  }

  if (!user.ativo) {
    throw new AppError('Esta conta está inativa ou suspensa.', {
      code: 'suspensa',
    });
  }

  return user;
}

/**
 * Encerra a sessão do usuário, solicitando ao backend a remoção do cookie HttpOnly.
 */
export async function logout(): Promise<boolean> {
  try {
    const url = getApiUrl('/auth/logout');
    await fetch(url, {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
      },
      credentials: 'include',
    });
    return true;
  } catch (error) {
    console.error('Erro ao efetuar logout:', error);
    return false;
  }
}

/**
 * Executa o fluxo completo:
 * 1. Faz o login no backend
 * 2. Grava o cookie HttpOnly
 * 3. Valida os dados e nível de acesso do usuário
 * 4. Determina a rota correspondente
 * 5. Realiza o redirecionamento (via router do Next.js ou window.location)
 * 
 * @param credentials Email e senha
 * @param router Instância de useRouter() do next/navigation (opcional)
 */
export async function loginAndRedirect(
  credentials: LoginCredentials,
  router?: { push: (url: string) => void; replace?: (url: string) => void },
  options?: { notifyError?: boolean }
): Promise<LoginResult> {
  try {
    const user = await login(credentials);
    const redirectUrl = getRedirectPathForUser(user);

    if (router && typeof router.push === 'function') {
      router.push(redirectUrl);
    } else if (typeof window !== 'undefined') {
      window.location.href = redirectUrl;
    }

    return {
      success: true,
      user,
      redirectUrl,
    };
  } catch (err: unknown) {
    if (options?.notifyError !== false) {
      handleError(err, { title: 'Falha no login' });
    }
    const appErr = err instanceof AppError ? err : null;
    return {
      success: false,
      error: err instanceof Error ? err.message : 'Erro inesperado ao realizar login',
      reason: (appErr?.code as LoginErrorReason) || 'invalid',
    };
  }
}

/**
 * Verifica se o usuário atual possui uma sessão ativa válida.
 */
export async function isAuthenticated(): Promise<boolean> {
  const user = await getCurrentUser();
  return user !== null && user.ativo;
}

