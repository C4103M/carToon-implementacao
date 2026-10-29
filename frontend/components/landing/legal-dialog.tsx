import React from 'react';
import { LandingDialog } from './landing-dialog';

interface LegalDialogProps {
  mode: 'terms' | 'privacy' | null;
  onClose: () => void;
}

export const LegalDialog: React.FC<LegalDialogProps> = ({ mode, onClose }) => {
  if (!mode) return null;

  const isTerms = mode === 'terms';

  return (
    <LandingDialog
      open={Boolean(mode)}
      onClose={onClose}
      subtitle="Informações legais"
      title={
        isTerms
          ? 'Termos de Uso — CAR TOON'
          : 'Política de Privacidade e Proteção de Dados (LGPD)'
      }
      maxWidthClass="max-w-2xl"
      footer={
        <div className="flex justify-end">
          <button
            type="button"
            onClick={onClose}
            className="btn-ink px-5 py-2.5 text-sm cursor-pointer"
          >
            Fechar
          </button>
        </div>
      }
    >
      {isTerms ? (
        <div className="space-y-4 text-sm leading-relaxed text-foreground font-medium">
          <p>
            <strong>1. Finalidade do sistema:</strong> O CAR TOON é um sistema voltado à
            gestão operacional de oficinas mecânicas, reunindo ordens de serviço,
            catálogo de peças e mão de obra, cadastro de clientes e histórico de veículos
            por placa.
          </p>
          <p>
            <strong>2. Perfis de acesso:</strong> Cada oficina administra os acessos da
            sua equipe, separando a visão administrativa (clientes, preços e fechamento)
            da visão técnica dos mecânicos responsáveis pelos serviços no pátio.
          </p>
          <p>
            <strong>3. Referência da Tabela FIPE:</strong> Os valores de referência FIPE
            exibidos nas ordens de serviço têm caráter informativo para apoiar a
            apresentação de orçamentos no balcão, não substituindo laudos ou avaliações
            oficiais.
          </p>
          <p>
            <strong>4. Proteção de ordens finalizadas:</strong> Quando uma Ordem de
            Serviço recebe o status de finalizada, seus itens e valores unitários ficam
            bloqueados contra alterações retroativas, preservando o histórico contábil da
            oficina.
          </p>
        </div>
      ) : (
        <div className="space-y-4 text-sm leading-relaxed text-foreground font-medium">
          <p>
            Em conformidade com a Lei Geral de Proteção de Dados (Lei nº 13.709/2018 —
            LGPD), tratamos os dados da sua oficina e dos seus clientes com transparência
            e isolamento:
          </p>
          <p>
            <strong>1. Contato pelo site:</strong> Os dados preenchidos no formulário de
            contato (nome, oficina e WhatsApp) são utilizados exclusivamente para
            apresentação comercial do sistema.
          </p>
          <p>
            <strong>2. Isolamento dos dados da oficina:</strong> Os cadastros de clientes,
            placas, veículos e ordens de serviço pertencem unicamente à oficina
            contratante e não são compartilhados com terceiros.
          </p>
          <p>
            <strong>3. Preservação de histórico:</strong> A desativação de cadastros
            mantém o histórico de serviços íntegro para consulta futura da própria
            oficina, respeitando os direitos do titular.
          </p>
        </div>
      )}
    </LandingDialog>
  );
};
