import { api, type Page } from '@/lib/api';
import type {
    OrdemServicoResponse,
    OrdemServicoResumo,
    OrdemServicoRequest,
    ItemPecaRequest,
    ItemServicoRequest,
    StatusServico,
    Desconto,
} from '@/lib/types';

/**
 * Serviço de Ordens de Serviço e Orçamentos (UC09)
 * Conectado diretamente ao OrdemServicoController (/ordem-servico)
 */
export const ordemServicoService = {
    /**
     * Lista as ordens de serviço da oficina com paginação e filtros.
     */
    listar: async (params?: {
        oficinaId?: number;
        veiculoId?: number;
        placa?: string;
        mecanicoId?: number;
        statusServico?: StatusServico | string;
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
     * Adiciona um item de peça à ordem de serviço.
     */
    adicionarItemPeca: async (
        id: number,
        data: ItemPecaRequest,
    ): Promise<OrdemServicoResponse> => {
        return api.post<OrdemServicoResponse>(
            `/ordem-servico/${id}/itens-peca`,
            data,
        );
    },

    /**
     * Remove um item de peça da ordem de serviço e retorna a OS recalculada.
     */
    removerItemPeca: async (
        id: number,
        itemPecaId: number,
    ): Promise<OrdemServicoResponse> => {
        return api.delete<OrdemServicoResponse>(
            `/ordem-servico/${id}/itens-peca/${itemPecaId}`,
        );
    },

    /**
     * Adiciona um item de serviço (mão de obra) à ordem de serviço.
     */
    adicionarItemServico: async (
        id: number,
        data: ItemServicoRequest,
    ): Promise<OrdemServicoResponse> => {
        return api.post<OrdemServicoResponse>(
            `/ordem-servico/${id}/itens-servico`,
            data,
        );
    },

    /**
     * Remove um item de serviço da ordem de serviço e retorna a OS recalculada.
     */
    removerItemServico: async (
        id: number,
        itemServicoId: number,
    ): Promise<OrdemServicoResponse> => {
        return api.delete<OrdemServicoResponse>(
            `/ordem-servico/${id}/itens-servico/${itemServicoId}`,
        );
    },

    /**
     * Atualiza o valor unitário e/ou o peso de risco de um item de serviço e recalcula a OS.
     */
    atualizarValorItemServico: async (
        id: number,
        itemServicoId: number,
        dados: number | { valorUnitario?: number; pesoServico?: number },
    ): Promise<OrdemServicoResponse> => {
        const body = typeof dados === 'number' ? { valorUnitario: dados } : dados;
        return api.patch<OrdemServicoResponse>(
            `/ordem-servico/${id}/itens-servico/${itemServicoId}`,
            body,
        );
    },

    /**
     * Atualiza manualmente o valor unitário de um item de peça e recalcula a OS.
     */
    atualizarValorItemPeca: async (
        id: number,
        itemPecaId: number,
        valorUnitario: number,
    ): Promise<OrdemServicoResponse> => {
        return api.patch<OrdemServicoResponse>(
            `/ordem-servico/${id}/itens-peca/${itemPecaId}`,
            { valorUnitario },
        );
    },

    /**
     * Aplica ou atualiza o percentual de desconto da ordem de serviço (0%, 5%, 10% ou 15%).
     */
    aplicarDesconto: async (
        id: number,
        desconto: Desconto,
    ): Promise<OrdemServicoResponse> => {
        return api.patch<OrdemServicoResponse>(
            `/ordem-servico/${id}/desconto`,
            { desconto },
        );
    },

    /**
     * Transição de estado: Aceitar orçamento (PENDENTE -> ACEITO).
     */
    aceitar: async (id: number): Promise<OrdemServicoResponse> => {
        return api.post<OrdemServicoResponse>(`/ordem-servico/${id}/aceitar`);
    },

    /**
     * Transição de estado: Rejeitar orçamento (PENDENTE/ACEITO -> REJEITADO).
     */
    rejeitar: async (id: number): Promise<OrdemServicoResponse> => {
        return api.post<OrdemServicoResponse>(`/ordem-servico/${id}/rejeitar`);
    },

    /**
     * Transição de estado: Iniciar serviço (ACEITO -> EM_ANDAMENTO).
     */
    iniciar: async (id: number): Promise<OrdemServicoResponse> => {
        return api.post<OrdemServicoResponse>(`/ordem-servico/${id}/iniciar`);
    },

    /**
     * Transição de estado: Finalizar serviço (EM_ANDAMENTO -> FINALIZADO).
     */
    finalizar: async (id: number): Promise<OrdemServicoResponse> => {
        return api.post<OrdemServicoResponse>(
            `/ordem-servico/${id}/finalizar`,
        );
    },
};

export default ordemServicoService;
