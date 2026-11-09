import type {
  Cliente,
  Oficina,
  Orcamento,
  Peca,
  Servico,
  Veiculo,
} from './types';

/**
 * Mocks iniciais vazios.
 * As telas exibem seus EmptyStates nativos de forma segura e elegante,
 * permitindo que cada desenvolvedor conecte sua respectiva tela diretamente à API
 * através dos serviços em `@/lib/services`.
 */
export const seedOficinas: Oficina[] = [];
export const seedClientes: Cliente[] = [];
export const seedVeiculos: Veiculo[] = [];
export const seedPecas: Peca[] = [];
export const seedServicos: Servico[] = [];
export const seedOrcamentos: Orcamento[] = [];
