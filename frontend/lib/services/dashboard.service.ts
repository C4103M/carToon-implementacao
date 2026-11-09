import { api } from '@/lib/api';
import type { DashboardAdminResponse, StatusServico } from '@/lib/types';

export const dashboardService = {
    admin: async (params?: {
        inicio?: string;
        fim?: string;
        status?: StatusServico;
    }): Promise<DashboardAdminResponse> => {
        return api.get<DashboardAdminResponse>('/dashboard/admin', { params });
    },
};
