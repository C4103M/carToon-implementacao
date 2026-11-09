import { api } from '@/lib/api';
import type { TipoVeiculoFipe, FipeItemDTO, FipeValorDTO } from '@/lib/types';

export type { TipoVeiculoFipe, FipeItemDTO, FipeValorDTO };

/**
 * Serviço de Integração com a Tabela FIPE (UC05 / UC08)
 * Conectado ao FipeController (/fipe/{tipo}/**)
 */
export const fipeService = {
    /**
     * Lista as marcas disponíveis na FIPE para o tipo informado (carros, motos, caminhoes).
     * GET /fipe/{tipo}/marcas
     */
    listarMarcas: async (
        tipo: TipoVeiculoFipe = 'carros',
    ): Promise<FipeItemDTO[]> => {
        return api.get<FipeItemDTO[]>(`/fipe/${tipo}/marcas`);
    },

    /**
     * Lista os modelos disponíveis na FIPE para a marca informada.
     * GET /fipe/{tipo}/marcas/{marcaId}/modelos
     */
    listarModelos: async (
        marcaId: string | number,
        tipo: TipoVeiculoFipe = 'carros',
    ): Promise<FipeItemDTO[]> => {
        return api.get<FipeItemDTO[]>(
            `/fipe/${tipo}/marcas/${marcaId}/modelos`,
        );
    },

    /**
     * Lista os anos/combustíveis disponíveis na FIPE para o modelo informado.
     * GET /fipe/{tipo}/marcas/{marcaId}/modelos/{modeloId}/anos
     */
    listarAnos: async (
        marcaId: string | number,
        modeloId: string | number,
        tipo: TipoVeiculoFipe = 'carros',
    ): Promise<FipeItemDTO[]> => {
        return api.get<FipeItemDTO[]>(
            `/fipe/${tipo}/marcas/${marcaId}/modelos/${modeloId}/anos`,
        );
    },

    /**
     * Consulta os dados detalhados e o valor de mercado segundo a tabela FIPE.
     * GET /fipe/{tipo}/marcas/{marcaId}/modelos/{modeloId}/anos/{anoId}
     */
    consultarValor: async (
        marcaId: string | number,
        modeloId: string | number,
        anoId: string,
        tipo: TipoVeiculoFipe = 'carros',
    ): Promise<FipeValorDTO> => {
        return api.get<FipeValorDTO>(
            `/fipe/${tipo}/marcas/${marcaId}/modelos/${modeloId}/anos/${anoId}`,
        );
    },
};

export default fipeService;
