import { api } from '@/lib/api';
import type { DashboardAdminResponse, StatusServico } from '@/lib/types';

/**
 * Métricas do dashboard do ADMIN (DashboardController - /dashboard/admin).
 * Faturamento considera apenas OS finalizadas; sem período, o backend usa o mês atual.
 */
export const dashboardService = {
    admin: async (params?: {
        inicio?: string;
        fim?: string;
        status?: StatusServico;
    }): Promise<DashboardAdminResponse> => {
        return api.get<DashboardAdminResponse>('/dashboard/admin', { params });
    },
};
