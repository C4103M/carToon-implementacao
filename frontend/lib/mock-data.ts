import type {
  Cliente,
  Oficina,
  Orcamento,
  Peca,
  Servico,
  Veiculo,
} from './types'

export const ADMIN_EMAIL = 'admin@oficinapro.com'

export const seedOficinas: Oficina[] = [
  {
    id: 'of-1',
    nome: 'Auto Center Silva',
    email: 'contato@autocentersilva.com',
    endereco: 'Rua das Oficinas, 120 — Vila Industrial, São Paulo/SP',
    dataCadastro: '2025-03-14T10:00:00.000Z',
    status: 'ativa',
  },
  {
    id: 'of-2',
    nome: 'Mecânica do Zé',
    email: 'ze@mecanicadoze.com',
    endereco: 'Av. Brasil, 998 — Centro, Campinas/SP',
    dataCadastro: '2025-06-02T14:30:00.000Z',
    status: 'pendente',
  },
  {
    id: 'of-3',
    nome: 'Turbo Diesel Peças e Serviços',
    email: 'financeiro@turbodiesel.com',
    endereco: 'Rod. Anhanguera, km 42 — Jundiaí/SP',
    dataCadastro: '2024-11-20T09:15:00.000Z',
    status: 'suspensa',
  },
  {
    id: 'of-4',
    nome: 'Oficina Rota 66',
    email: 'atendimento@rota66.com',
    endereco: 'Rua dos Motores, 66 — Santo André/SP',
    dataCadastro: '2025-07-19T16:45:00.000Z',
    status: 'ativa',
  },
]

// Oficina que "loga" no ambiente da oficina neste protótipo.
export const CURRENT_OFICINA_ID = 'of-1'

export const seedClientes: Cliente[] = [
  {
    id: 'cli-1',
    nome: 'Marcos Antônio Pereira',
    cpf: '529.982.247-25',
    endereco: 'Rua Ipê Amarelo, 45 — Jardim Europa, São Paulo/SP',
  },
  {
    id: 'cli-2',
    nome: 'Juliana Ramos de Souza',
    cpf: '111.444.777-35',
    endereco: 'Av. Paulista, 1500, ap. 82 — Bela Vista, São Paulo/SP',
  },
  {
    id: 'cli-3',
    nome: 'Carlos Eduardo Nunes',
    cpf: '390.533.447-05',
    endereco: 'Rua das Acácias, 210 — Osasco/SP',
  },
]

export const seedVeiculos: Veiculo[] = [
  {
    id: 'vei-1',
    clienteId: 'cli-1',
    placa: 'FGH-1A23',
    modelo: 'Gol 1.6 Comfortline',
    montadora: 'Volkswagen',
    ano: 2019,
    fipeValor: 54200,
    fipeConsultadoEm: '2025-07-10T11:00:00.000Z',
    lambda: 1.27,
    ativo: true,
  },
  {
    id: 'vei-2',
    clienteId: 'cli-1',
    placa: 'ABC-4567',
    modelo: 'Palio Fire 1.0',
    montadora: 'Fiat',
    ano: 2012,
    fipeValor: 28900,
    fipeConsultadoEm: '2024-09-01T09:00:00.000Z',
    lambda: 1.07,
    ativo: false,
    substituidoPorId: 'vei-1',
  },
  {
    id: 'vei-3',
    clienteId: 'cli-2',
    placa: 'JKL-2B34',
    modelo: 'Corolla XEi 2.0',
    montadora: 'Toyota',
    ano: 2022,
    fipeValor: 132500,
    fipeConsultadoEm: '2025-07-22T15:30:00.000Z',
    lambda: 1.87,
    ativo: true,
  },
  {
    id: 'vei-4',
    clienteId: 'cli-3',
    placa: 'MNP-9C87',
    modelo: 'Onix 1.0 LT',
    montadora: 'Chevrolet',
    ano: 2020,
    fipeValor: 61300,
    fipeConsultadoEm: '2025-06-30T13:20:00.000Z',
    lambda: 1.32,
    ativo: true,
  },
]

export const seedPecas: Peca[] = [
  { id: 'pec-1', nome: 'Óleo sintético 5W30 (litro)', valor: 42.9, ativo: true },
  { id: 'pec-2', nome: 'Filtro de óleo', valor: 28.5, ativo: true },
  { id: 'pec-3', nome: 'Filtro de ar', valor: 39.9, ativo: true },
  { id: 'pec-4', nome: 'Pastilha de freio (par)', valor: 189.0, ativo: true },
  { id: 'pec-5', nome: 'Disco de freio', valor: 245.0, ativo: true },
  { id: 'pec-6', nome: 'Vela de ignição (unidade)', valor: 34.9, ativo: true },
  { id: 'pec-7', nome: 'Correia dentada', valor: 156.0, ativo: true },
]

export const seedServicos: Servico[] = [
  {
    id: 'srv-1',
    nome: 'Troca de óleo e filtros',
    valorBase: 90,
    pecaIds: ['pec-1', 'pec-2', 'pec-3'],
    ativo: true,
  },
  {
    id: 'srv-2',
    nome: 'Troca de pastilhas de freio',
    valorBase: 140,
    pecaIds: ['pec-4'],
    ativo: true,
  },
  {
    id: 'srv-3',
    nome: 'Revisão do sistema de freios',
    valorBase: 220,
    pecaIds: ['pec-4', 'pec-5'],
    ativo: true,
  },
  {
    id: 'srv-4',
    nome: 'Troca de correia dentada',
    valorBase: 380,
    pecaIds: ['pec-7'],
    ativo: true,
  },
  {
    id: 'srv-5',
    nome: 'Diagnóstico eletrônico',
    valorBase: 120,
    pecaIds: [],
    ativo: true,
  },
  // "Peça avulsa" cadastrada como serviço de valor zero (regra 5)
  {
    id: 'srv-6',
    nome: 'Jogo de velas (somente peça)',
    valorBase: 0,
    pecaIds: ['pec-6'],
    ativo: true,
  },
]

export const seedOrcamentos: Orcamento[] = [
  {
    id: 'orc-1',
    clienteId: 'cli-1',
    veiculoId: 'vei-1',
    data: '2025-07-28T10:30:00.000Z',
    status: 'pendente',
    itens: [
      {
        id: 'it-1',
        servicoId: 'srv-1',
        servicoNome: 'Troca de óleo e filtros',
        valorBaseServico: 90,
        quantidade: 1,
        valorUnitario: 114.3,
      },
      {
        id: 'it-2',
        servicoId: 'srv-2',
        servicoNome: 'Troca de pastilhas de freio',
        valorBaseServico: 140,
        quantidade: 1,
        valorUnitario: 177.8,
      },
    ],
  },
  {
    id: 'orc-2',
    clienteId: 'cli-2',
    veiculoId: 'vei-3',
    data: '2025-07-25T09:00:00.000Z',
    status: 'aceito',
    itens: [
      {
        id: 'it-3',
        servicoId: 'srv-3',
        servicoNome: 'Revisão do sistema de freios',
        valorBaseServico: 220,
        quantidade: 1,
        valorUnitario: 411.4,
      },
    ],
  },
  {
    id: 'orc-3',
    clienteId: 'cli-3',
    veiculoId: 'vei-4',
    data: '2025-07-20T14:15:00.000Z',
    status: 'finalizado',
    itens: [
      {
        id: 'it-4',
        servicoId: 'srv-1',
        servicoNome: 'Troca de óleo e filtros',
        valorBaseServico: 90,
        quantidade: 1,
        valorUnitario: 118.8,
      },
      {
        id: 'it-5',
        servicoId: 'srv-5',
        servicoNome: 'Diagnóstico eletrônico',
        valorBaseServico: 120,
        quantidade: 1,
        valorUnitario: 158.4,
      },
    ],
  },
  {
    id: 'orc-4',
    clienteId: 'cli-1',
    veiculoId: 'vei-2',
    data: '2024-09-05T11:00:00.000Z',
    status: 'rejeitado',
    itens: [
      {
        id: 'it-6',
        servicoId: 'srv-4',
        servicoNome: 'Troca de correia dentada',
        valorBaseServico: 380,
        quantidade: 1,
        valorUnitario: 406.6,
      },
    ],
  },
]
