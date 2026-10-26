import { api } from '@/lib/api';
import type {
    UsuarioResponse,
    AdminRequest,
    MecanicoRequest,
    AtualizarUsuarioRequest,
} from '@/lib/types';

/**
 * Serviço de Usuários e Mecânicos (UC01 e UC03)
 * Utilizado na tela /app/mecanicos, no modal Meus Dados e na gestão de administradores
 */
export const usuariosService = {
    /**
     * Consulta os dados do usuário autenticado atualmente.
     */
    getMe: async (): Promise<UsuarioResponse> => {
        return api.get<UsuarioResponse>('/usuario/me');
    },

    /**
     * Atualiza os próprios dados do usuário autenticado (nome, email, senha).
     */
    atualizarMe: async (
        data: AtualizarUsuarioRequest,
    ): Promise<UsuarioResponse> => {
        return api.put<UsuarioResponse>('/usuario/me', data);
    },

    /**
     * Lista usuários vinculados à oficina ou à plataforma.
     */
    listar: async (params?: {
        oficinaId?: number;
        role?: string;
        ativo?: boolean;
    }): Promise<UsuarioResponse[]> => {
        return api.get<UsuarioResponse[]>('/usuario', { params });
    },

    /**
     * Busca um usuário pelo ID.
     */
    buscarPorId: async (id: number): Promise<UsuarioResponse> => {
        return api.get<UsuarioResponse>(`/usuario/${id}`);
    },

    /**
     * Cadastra um novo mecânico na oficina (exclusivo para ADMIN).
     */
    cadastrarMecanico: async (
        data: MecanicoRequest,
    ): Promise<UsuarioResponse> => {
        return api.post<UsuarioResponse>('/usuario/mecanico', data);
    },

    /**
     * Cadastra um administrador para uma oficina (exclusivo para SUPERADMIN).
     */
    cadastrarAdmin: async (data: AdminRequest): Promise<UsuarioResponse> => {
        return api.post<UsuarioResponse>('/usuario/admin', data);
    },

    /**
     * Atualiza dados de um usuário pelo ID.
     */
    atualizar: async (
        id: number,
        data: AtualizarUsuarioRequest,
    ): Promise<UsuarioResponse> => {
        return api.put<UsuarioResponse>(`/usuario/${id}`, data);
    },

    /**
     * Desativa (soft-delete) um usuário.
     */
    desativar: async (id: number): Promise<void> => {
        return api.delete<void>(`/usuario/${id}`);
    },

    /**
     * Reativa um usuário inativo.
     */
    ativar: async (id: number): Promise<void> => {
        return api.patch<void>(`/usuario/${id}/ativar`);
    },
};

export default usuariosService;
