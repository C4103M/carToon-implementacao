import { api, type Page } from '@/lib/api';
import type {
  PecaResponse,
  PecaRequest,
  ServicoResponse,
  ServicoRequest,
} from '@/lib/types';

/**
 * Serviço de Catálogo (UC07 - Serviços e UC08 - Peças)
 * Utilizado na tela /app/catalogo
 */
export const catalogoService = {
  // --- PEÇAS ---

  /**
   * Lista peças cadastradas com paginação e ordenação.
   */
  listarPecas: async (params?: { page?: number; size?: number; sort?: string }): Promise<Page<PecaResponse>> => {
    return api.get<Page<PecaResponse>>('/pecas', { params });
  },

  /**
   * Busca detalhes de uma peça pelo ID.
   */
  buscarPecaPorId: async (id: number): Promise<PecaResponse> => {
    return api.get<PecaResponse>(`/pecas/${id}`);
  },

  /**
   * Cadastra uma nova peça no catálogo.
   */
  criarPeca: async (data: PecaRequest): Promise<PecaResponse> => {
    return api.post<PecaResponse>('/pecas', data);
  },

  /**
   * Atualiza uma peça existente.
   */
  atualizarPeca: async (id: number, data: PecaRequest): Promise<PecaResponse> => {
    return api.put<PecaResponse>(`/pecas/${id}`, data);
  },

  /**
   * Exclui uma peça do catálogo.
   */
  excluirPeca: async (id: number): Promise<void> => {
    return api.delete<void>(`/pecas/${id}`);
  },

  // --- SERVIÇOS ---

  /**
   * Lista serviços cadastrados com paginação e ordenação.
   */
  listarServicos: async (params?: { page?: number; size?: number; sort?: string }): Promise<Page<ServicoResponse>> => {
    return api.get<Page<ServicoResponse>>('/servicos', { params });
  },

  /**
   * Busca detalhes de um serviço pelo ID.
   */
  buscarServicoPorId: async (id: number): Promise<ServicoResponse> => {
    return api.get<ServicoResponse>(`/servicos/${id}`);
  },

  /**
   * Cadastra um novo serviço no catálogo.
   */
  criarServico: async (data: ServicoRequest): Promise<ServicoResponse> => {
    return api.post<ServicoResponse>('/servicos', data);
  },

  /**
   * Atualiza um serviço existente.
   */
  atualizarServico: async (id: number, data: ServicoRequest): Promise<ServicoResponse> => {
    return api.put<ServicoResponse>(`/servicos/${id}`, data);
  },

  /**
   * Exclui um serviço do catálogo.
   */
  excluirServico: async (id: number): Promise<void> => {
    return api.delete<void>(`/servicos/${id}`);
  },
};

export default catalogoService;
