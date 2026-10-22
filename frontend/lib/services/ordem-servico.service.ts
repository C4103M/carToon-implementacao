import { api, type Page } from '@/lib/api';
import type {
  OrdemServicoResponse,
  OrdemServicoResumo,
  OrdemServicoRequest,
  ItemPecaRequest,
  ItemServicoRequest,
  StatusServico,
} from '@/lib/types';

/**
 * Serviço de Ordens de Serviço e Orçamentos (UC09)
 * Utilizado nas telas /app (Dashboard), /app/orcamentos, /app/orcamentos/novo e /app/orcamentos/[id]
 */
export const ordemServicoService = {
  /**
   * Lista as ordens de serviço da oficina com paginação e filtro por status.
   */
  listar: async (params?: {
    status?: StatusServico | string;
    page?: number;
    size?: number;
    sort?: string;
  }): Promise<Page<OrdemServicoResumo>> => {
    return api.get<Page<OrdemServicoResumo>>('/ordem-servico', { params });
  },

  /**
   * Busca os detalhes completos de uma ordem de serviço pelo ID.
   */
  buscarPorId: async (id: number): Promise<OrdemServicoResponse> => {
    return api.get<OrdemServicoResponse>(`/ordem-servico/${id}`);
  },

  /**
   * Cria/abre uma nova ordem de serviço (estado inicial PENDENTE).
   */
  criar: async (data: OrdemServicoRequest): Promise<OrdemServicoResponse> => {
    return api.post<OrdemServicoResponse>('/ordem-servico', data);
  },

  /**
   * Atualiza a descrição de uma ordem de serviço.
   */
  atualizarDescricao: async (id: number, descricao: string): Promise<OrdemServicoResponse> => {
    return api.put<OrdemServicoResponse>(`/ordem-servico/${id}`, { descricao });
  },

  /**
   * Adiciona um item de peça à ordem de serviço.
   */
  adicionarItemPeca: async (id: number, data: ItemPecaRequest): Promise<OrdemServicoResponse> => {
    return api.post<OrdemServicoResponse>(`/ordem-servico/${id}/itens-peca`, data);
  },

  /**
   * Remove um item de peça da ordem de serviço.
   */
  removerItemPeca: async (id: number, itemPecaId: number): Promise<void> => {
    return api.delete<void>(`/ordem-servico/${id}/itens-peca/${itemPecaId}`);
  },

  /**
   * Adiciona um item de serviço à ordem de serviço.
   */
  adicionarItemServico: async (id: number, data: ItemServicoRequest): Promise<OrdemServicoResponse> => {
    return api.post<OrdemServicoResponse>(`/ordem-servico/${id}/itens-servico`, data);
  },

  /**
   * Remove um item de serviço da ordem de serviço.
   */
  removerItemServico: async (id: number, itemServicoId: number): Promise<void> => {
    return api.delete<void>(`/ordem-servico/${id}/itens-servico/${itemServicoId}`);
  },

  /**
   * Transição de estado: Aceitar orçamento (PENDENTE -> ACEITO).
   */
  aceitar: async (id: number): Promise<OrdemServicoResponse> => {
    return api.patch<OrdemServicoResponse>(`/ordem-servico/${id}/aceitar`);
  },

  /**
   * Transição de estado: Rejeitar orçamento (PENDENTE -> REJEITADO).
   */
  rejeitar: async (id: number): Promise<OrdemServicoResponse> => {
    return api.patch<OrdemServicoResponse>(`/ordem-servico/${id}/rejeitar`);
  },

  /**
   * Transição de estado: Iniciar serviço (ACEITO -> EM_ANDAMENTO).
   */
  iniciar: async (id: number): Promise<OrdemServicoResponse> => {
    return api.patch<OrdemServicoResponse>(`/ordem-servico/${id}/iniciar`);
  },

  /**
   * Transição de estado: Finalizar serviço (EM_ANDAMENTO -> FINALIZADO).
   */
  finalizar: async (id: number): Promise<OrdemServicoResponse> => {
    return api.patch<OrdemServicoResponse>(`/ordem-servico/${id}/finalizar`);
  },
};

export default ordemServicoService;
