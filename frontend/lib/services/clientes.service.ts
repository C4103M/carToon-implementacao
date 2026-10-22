import { api } from '@/lib/api';
import type {
  ClienteResponse,
  ClienteRequest,
} from '@/lib/types';

/**
 * Serviço de Clientes (UC04 - Manter Clientes)
 * Utilizado nas telas /app/clientes, /app/clientes/novo e /app/clientes/[id]
 */
export const clientesService = {
  /**
   * Lista todos os clientes ativos da oficina.
   */
  listar: async (): Promise<ClienteResponse[]> => {
    return api.get<ClienteResponse[]>('/clientes');
  },

  /**
   * Busca um cliente pelo ID.
   */
  buscarPorId: async (id: number): Promise<ClienteResponse> => {
    return api.get<ClienteResponse>(`/clientes/${id}`);
  },

  /**
   * Cadastra um novo cliente na oficina.
   */
  criar: async (data: ClienteRequest): Promise<ClienteResponse> => {
    return api.post<ClienteResponse>('/clientes', data);
  },

  /**
   * Atualiza os dados de um cliente existente.
   */
  atualizar: async (id: number, data: ClienteRequest): Promise<ClienteResponse> => {
    return api.put<ClienteResponse>(`/clientes/${id}`, data);
  },

  /**
   * Inativa um cliente (soft-delete).
   */
  inativar: async (id: number): Promise<void> => {
    return api.delete<void>(`/clientes/${id}`);
  },

  /**
   * Reativa um cliente inativo.
   */
  ativar: async (id: number): Promise<void> => {
    return api.patch<void>(`/clientes/${id}/ativar`);
  },
};

export default clientesService;
