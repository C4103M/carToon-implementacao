/**
 * ============================================================================
 * CONTRATOS REAIS DO BACKEND (Spring Boot / REST API)
 * Alinhados com os DTOs em com.cartoon.api.* e especificados no CONTEXT.md
 * ============================================================================
 */

// Roles de Usuário no sistema
export type Role = 'SUPERADMIN' | 'ADMIN' | 'MECANICO';
export type UserRole = Role | 'admin' | 'oficina' | string;

// Status de Ordem de Serviço no backend
export type StatusServico = 'PENDENTE' | 'ACEITO' | 'REJEITADO' | 'EM_ANDAMENTO' | 'FINALIZADO';

export type Desconto = 'SEM_DESCONTO' | 'CINCO_PORCENTO' | 'DEZ_PORCENTO' | 'QUINZE_PORCENTO';

/**
 * Usuário / Autenticação (GET /api/usuario/me, GET /api/usuario)
 */
export interface UsuarioResponse {
  id: number;
  nome: string;
  email: string;
  role: Role;
  ativo: boolean;
  oficinaId?: number | null;
}
export type UsuarioDTO = UsuarioResponse;

export interface AdminRequest {
  nome: string;
  email: string;
  senha: string;
  confirmarSenha: string;
  oficinaId: number;
}

export interface MecanicoRequest {
  nome: string;
  email: string;
  senha: string;
  confirmarSenha: string;
}

export interface AtualizarUsuarioRequest {
  nome: string;
  email: string;
  role?: Role;
  senha?: string;
  confirmarSenha?: string;
}

/**
 * Oficina (GET /api/oficina, POST /api/oficina)
 */
export interface OficinaResponse {
  id: number;
  nome: string;
  endereco: string;
  telefone: string;
  email: string;
  valorHora?: number;
  taxaRisco?: number;
  ativo: boolean;
}
export type OficinaDTO = OficinaResponse;

export interface OficinaRequest {
  nome: string;
  endereco: string;
  telefone: string;
  email: string;
  valorHora?: number;
  taxaRisco?: number;
  ativo?: boolean;
}

/**
 * Cliente (GET /api/clientes, POST /api/clientes)
 */
export interface ClienteResponse {
  id: number;
  nome: string;
  cpf: string;
  telefone?: string;
  endereco: string;
  oficinaId?: number;
  ativo: boolean;
}
export type ClienteDTO = ClienteResponse;

export interface ClienteRequest {
  nome: string;
  cpf: string;
  telefone?: string;
  endereco: string;
}

/**
 * Veículo (GET /api/veiculos, POST /api/veiculos)
 */
export interface VeiculoResponse {
  id: number;
  placa: string;
  marca: string;
  modelo: string;
  anoModelo: number;
  combustivel?: string;
  codigoFipe?: string;
  valorFipe?: number;
  mesReferencia?: string;
  ativo: boolean;
  clienteId: number;
  clienteNome?: string;
  clienteCpf?: string;
}
export type VeiculoDTO = VeiculoResponse;

export interface VeiculoResumo {
  id: number;
  placa: string;
  modelo: string;
  marca: string;
  anoModelo: number;
  ativo: boolean;
}

export interface VeiculoRequest {
  clienteId: number;
  placa: string;
  marca: string;
  modelo: string;
  anoModelo: number;
  combustivel?: string;
  codigoFipe?: string;
  valorFipe?: number;
  mesReferencia?: string;
}

export interface VeiculoAtualizacaoRequest {
  marca?: string;
  modelo?: string;
  anoModelo?: number;
  combustivel?: string;
  codigoFipe?: string;
  valorFipe?: number;
  mesReferencia?: string;
}

/**
 * FIPE - Tabela FIPE (GET /fipe/{tipo}/...)
 */
export type TipoVeiculoFipe = 'carros' | 'motos' | 'caminhoes' | string;

export interface FipeItemDTO {
  codigo: string;
  nome: string;
}

export interface FipeValorDTO {
  marca: string;
  modelo: string;
  anoModelo: number;
  combustivel: string;
  siglaCombustivel: string;
  codigoFipe: string;
  valorFipe: number;
  mesReferencia: string;
}

/**
 * Catálogo - Peças (GET /api/pecas, POST /api/pecas)
 */
export interface PecaResponse {
  id: number;
  nome: string;
  fabricante?: string;
  valorBase: number;
  oficinaId?: number;
}
export type PecaDTO = PecaResponse;

export interface PecaRequest {
  nome: string;
  fabricante: string;
  valorBase: number;
}

/**
 * Catálogo - Serviços (GET /api/servicos, POST /api/servicos)
 */
export interface ServicoResponse {
  id: number;
  nome: string;
  tempo?: string; // "01:30"
  quantidadeHoras?: number; // 1.5
  descricao?: string;
  oficinaId?: number;
}
export type ServicoDTO = ServicoResponse;

export interface ServicoRequest {
  nome: string;
  tempo: string; // "01:30" ou "1h30"
  quantidadeHoras?: number;
  descricao?: string;
}

/**
 * Ordem de Serviço (GET /api/ordem-servico, POST /api/ordem-servico)
 */
export interface ItemPecaResponse {
  id: number;
  pecaId: number;
  pecaNome?: string;
  nomePeca?: string;
  fabricante?: string;
  quantidade: number;
  valorUnitario: number;
  subtotal: number;
}

export interface ItemServicoResponse {
  id: number;
  servicoId: number;
  servicoNome: string;
  quantidade: number;
  valorUnitario: number;
  subtotal: number;
  tempo?: string;
  quantidadeHoras?: number;
  valorHoraBase?: number;
  multiplicadorFipe?: number;
}

export interface OrdemServicoResponse {
  id: number;
  status: StatusServico;
  dataOrcamento: string;
  dataInicio?: string | null;
  dataRejeicao?: string | null;
  dataFinalizacao?: string | null;
  descricao?: string | null;
  veiculoId: number;
  placa: string;
  veiculoMarca?: string | null;
  veiculoModelo?: string | null;
  veiculoAno?: number | null;
  veiculoValorFipe?: number | null;
  clienteId?: number | null;
  clienteNome?: string | null;
  clienteCpf?: string | null;
  oficinaId: number;
  oficinaNome?: string | null;
  oficinaValorHora?: number | null;
  oficinaTaxaRisco?: number | null;
  mecanicoId?: number | null;
  mecanicoNome?: string | null;
  itensPeca: ItemPecaResponse[];
  itensServico: ItemServicoResponse[];
  totalPecas?: number;
  totalServicos?: number;
  desconto?: Desconto;
  valorBruto?: number;
  valorDesconto?: number;
  total: number;
}

export interface OrdemServicoResumo {
  id: number;
  status?: StatusServico;
  statusServico?: StatusServico;
  dataOrcamento?: string;
  dataInicio?: string;
  dataFinalizacao?: string;
  dataRejeicao?: string;
  descricao?: string;
  veiculoId?: number;
  placa?: string;
  placaVeiculo?: string;
  veiculoMarca?: string;
  veiculoModelo?: string;
  clienteId?: number;
  clienteNome?: string;
  nomeCliente?: string;
  mecanicoNome?: string;
  oficinaNome?: string;
  totalPecas?: number;
  totalServicos?: number;
  valorBruto?: number;
  desconto?: Desconto;
  valorDesconto?: number;
  total?: number;
  valorTotal?: number;
}

export interface ItemPecaRequest {
  pecaId: number;
  quantidade: number;
}

export interface ItemServicoRequest {
  servicoId: number;
  quantidade: number;
}

export interface OrdemServicoRequest {
  veiculoId: number;
  mecanicoId: number;
  descricao?: string;
  desconto?: Desconto;
}

/**
 * ============================================================================
 * TIPOS DA SESSÃO E MOCKS LEGADOS
 * Mantidos para compatibilidade com as telas que serão migradas nas próximas issues
 * ============================================================================
 */

export interface SessionUser {
  id?: number;
  role: UserRole;
  oficinaId?: string | number | null;
  nome: string;
  email: string;
  ativo?: boolean;
}

export type OficinaStatus = 'ativa' | 'suspensa' | 'pendente';

export interface Oficina {
  id: string;
  nome: string;
  email?: string;
  telefone?: string;
  endereco: string;
  dataCadastro?: string;
  status: OficinaStatus;
}

export interface Cliente {
  id: string;
  nome: string;
  cpf: string;
  endereco: string;
}

export interface Veiculo {
  id: string;
  clienteId: string;
  placa: string;
  modelo: string;
  montadora: string;
  ano: number;
  codigoFipe?: string;
  /** Valor FIPE congelado no momento do cadastro */
  fipeValor: number;
  /** Data em que a FIPE foi consultada e congelada */
  fipeConsultadoEm: string;
  /** Multiplicador de mão de obra derivado da FIPE, congelado */
  lambda: number;
  ativo: boolean;
  /** Se foi substituído, id do veículo que o substituiu */
  substituidoPorId?: string;
}

export interface Peca {
  id: string;
  nome: string;
  valor: number;
  ativo: boolean;
}

export interface Servico {
  id: string;
  nome: string;
  valorBase: number;
  /** Peças que compõem a "receita" deste serviço */
  pecaIds: string[];
  ativo: boolean;
}

export type OrcamentoStatus = 'pendente' | 'aceito' | 'rejeitado' | 'finalizado';

export interface OrcamentoItem {
  id: string;
  servicoId: string;
  /** Snapshot do nome do serviço no momento em que foi adicionado */
  servicoNome: string;
  /** Snapshot do valor base do serviço no momento em que foi adicionado */
  valorBaseServico: number;
  quantidade: number;
  /** Valor unitário congelado (valorBase * lambda do veículo) */
  valorUnitario: number;
}

export interface Orcamento {
  id: string;
  clienteId: string;
  veiculoId: string;
  data: string;
  status: OrcamentoStatus;
  itens: OrcamentoItem[];
}
