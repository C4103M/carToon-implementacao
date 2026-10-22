import { api, type Page } from '@/lib/api';
import type {
  VeiculoResponse,
  VeiculoRequest,
  VeiculoAtualizacaoRequest,
  OrdemServicoResumo,
} from '@/lib/types';

export interface HistoricoVeiculoResponse {
  veiculo: VeiculoResponse;
  historico: OrdemServicoResumo[];
}

/**
 * Serviço de Veículos (UC05 - Manter Veículos e UC06 - Histórico)
 * Utilizado nas telas /app/clientes/[id] e /app/veiculos/[id]/historico
 */
export const veiculosService = {
  /**
   * Lista os veículos vinculados a um cliente específico.
   */
  listarPorCliente: async (clienteId: number): Promise<VeiculoResponse[]> => {
    return api.get<VeiculoResponse[]>(`/veiculos/cliente/${clienteId}`);
  },

  /**
   * Busca um veículo pelo ID.
   */
  buscarPorId: async (id: number): Promise<VeiculoResponse> => {
    return api.get<VeiculoResponse>(`/veiculos/${id}`);
  },

  /**
   * Busca um veículo pela placa.
   */
  buscarPorPlaca: async (placa: string): Promise<VeiculoResponse> => {
    return api.get<VeiculoResponse>(`/veiculos/placa/${encodeURIComponent(placa)}`);
  },

  /**
   * Cadastra um novo veículo vinculado a um cliente.
   */
  criar: async (data: VeiculoRequest): Promise<VeiculoResponse> => {
    return api.post<VeiculoResponse>('/veiculos', data);
  },

  /**
   * Atualiza os dados de um veículo existente.
   */
  atualizar: async (id: number, data: VeiculoAtualizacaoRequest): Promise<VeiculoResponse> => {
    return api.put<VeiculoResponse>(`/veiculos/${id}`, data);
  },

  /**
   * Inativa um veículo da frota do cliente.
   */
  inativar: async (id: number): Promise<void> => {
    return api.patch<void>(`/veiculos/${id}/inativar`);
  },

  /**
   * Reativa um veículo inativo.
   */
  reativar: async (id: number): Promise<void> => {
    return api.patch<void>(`/veiculos/${id}/reativar`);
  },

  /**
   * Consulta o histórico completo de manutenções de um veículo pela placa.
   */
  consultarHistorico: async (placa: string): Promise<HistoricoVeiculoResponse> => {
    return api.get<HistoricoVeiculoResponse>(`/veiculos/placa/${encodeURIComponent(placa)}/historico`);
  },

  /**
   * Consulta o histórico paginado de manutenções de um veículo.
   */
  consultarHistoricoPaginado: async (
    placa: string,
    params?: { page?: number; size?: number }
  ): Promise<Page<OrdemServicoResumo>> => {
    return api.get<Page<OrdemServicoResumo>>(
      `/veiculos/placa/${encodeURIComponent(placa)}/historico/paginado`,
      { params }
    );
  },
};

export default veiculosService;
