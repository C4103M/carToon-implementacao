export type StatusServico = 'ACEITO' | 'EM_ANDAMENTO' | 'FINALIZADO' | 'REJEITADO';

export interface CatalogItem {
  id: string;
  codigo: string;
  nome: string;
  categoria: 'PECA' | 'SERVICO';
  precoCatalogo: number;
  detalhe: string;
}

export interface OrderLineItem {
  itemId: string;
  catalogId: string;
  nome: string;
  categoria: 'PECA' | 'SERVICO';
  quantidade: number;
  valorUnitarioCongelado: number;
}

export interface HistoricalRecord {
  osNumero: number;
  data: string;
  quilometragem: string;
  mecanicoResponsavel: string;
  resumoServicos: string;
  valorTotal: number;
  status: StatusServico;
}

export interface VehicleDemo {
  id: number;
  placa: string;
  modelo: string;
  montadora: string;
  ano: number;
  valorFipe: number;
  codigoFipe: string;
  mesReferenciaFipe: string;
  proprietario: string;
  mecanicoAlocado: {
    nome: string;
    especialidade: string;
    box: string;
    entregasMes: string;
  };
  osAtiva: {
    id: number;
    abertura: string;
    statusInicial: StatusServico;
    itensIniciais: OrderLineItem[];
  };
  historicoPlaca: HistoricalRecord[];
}

export interface FaqItem {
  question: string;
  answer: string;
}

export interface LeadFormPayload {
  nomeResponsavel: string;
  nomeOficina: string;
  whatsapp: string;
  mensagemOpcional: string;
}
