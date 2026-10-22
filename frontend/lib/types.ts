export type OficinaStatus = 'ativa' | 'suspensa' | 'pendente'

export interface Oficina {
  id: string
  nome: string
  email: string
  endereco: string
  dataCadastro: string
  status: OficinaStatus
}

export interface Cliente {
  id: string
  nome: string
  cpf: string
  endereco: string
}

export interface Veiculo {
  id: string
  clienteId: string
  placa: string
  modelo: string
  montadora: string
  ano: number
  /** Valor FIPE congelado no momento do cadastro */
  fipeValor: number
  /** Data em que a FIPE foi consultada e congelada */
  fipeConsultadoEm: string
  /** Multiplicador de mão de obra derivado da FIPE, congelado */
  lambda: number
  ativo: boolean
  /** Se foi substituído, id do veículo que o substituiu */
  substituidoPorId?: string
}

export interface Peca {
  id: string
  nome: string
  valor: number
  ativo: boolean
}

export interface Servico {
  id: string
  nome: string
  valorBase: number
  /** Peças que compõem a "receita" deste serviço */
  pecaIds: string[]
  ativo: boolean
}

export type OrcamentoStatus = 'pendente' | 'aceito' | 'rejeitado' | 'finalizado'

export interface OrcamentoItem {
  id: string
  servicoId: string
  /** Snapshot do nome do serviço no momento em que foi adicionado */
  servicoNome: string
  /** Snapshot do valor base do serviço no momento em que foi adicionado */
  valorBaseServico: number
  quantidade: number
  /** Valor unitário congelado (valorBase * lambda do veículo) */
  valorUnitario: number
}

export interface Orcamento {
  id: string
  clienteId: string
  veiculoId: string
  data: string
  status: OrcamentoStatus
  itens: OrcamentoItem[]
}

export type UserRole = 'admin' | 'oficina' | 'SUPERADMIN' | 'ADMIN' | 'MECANICO' | string

export interface SessionUser {
  id?: number
  role: UserRole
  /** Preenchido quando associado a uma oficina */
  oficinaId?: string | number | null
  nome: string
  email: string
  ativo?: boolean
}
