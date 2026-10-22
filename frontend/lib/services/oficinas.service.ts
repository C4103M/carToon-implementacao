import { api } from '@/lib/api';
import type {
  OficinaResponse,
  OficinaRequest,
} from '@/lib/types';

/**
 * Serviço de Oficinas (UC02 - Gestão de Oficinas)
 * Utilizado nas telas /admin e /admin/oficinas/[id] (perfil SUPERADMIN e ADMIN)
 */
export const oficinasService = {
  /**
   * Lista todas as oficinas cadastradas na plataforma.
   */
  listar: async (): Promise<OficinaResponse[]> => {
    return api.get<OficinaResponse[]>('/oficina');
  },

  /**
   * Busca os detalhes de uma oficina pelo ID.
   */
  buscarPorId: async (id: number): Promise<OficinaResponse> => {
    return api.get<OficinaResponse>(`/oficina/${id}`);
  },

  /**
   * Cadastra uma nova oficina na plataforma.
   */
  criar: async (data: OficinaRequest): Promise<OficinaResponse> => {
    return api.post<OficinaResponse>('/oficina', data);
  },

  /**
   * Atualiza os dados de uma oficina existente.
   */
  atualizar: async (id: number, data: OficinaRequest): Promise<OficinaResponse> => {
    return api.put<OficinaResponse>(`/oficina/${id}`, data);
  },

  /**
   * Ativa uma oficina na plataforma.
   */
  ativar: async (id: number): Promise<void> => {
    return api.patch<void>(`/oficina/${id}/ativar`);
  },

  /**
   * Desativa uma oficina na plataforma.
   */
  desativar: async (id: number): Promise<void> => {
    return api.patch<void>(`/oficina/${id}/desativar`);
  },
};

export default oficinasService;
