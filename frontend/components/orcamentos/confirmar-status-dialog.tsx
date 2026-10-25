'use client'

import React from 'react'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'

export type AcaoStatus = 'aceitar' | 'iniciar' | 'finalizar' | 'rejeitar'

interface ConfirmarStatusDialogProps {
  acao: AcaoStatus | null
  executando: boolean
  onClose: () => void
  onConfirmar: () => void
}

export function ConfirmarStatusDialog({
  acao,
  executando,
  onClose,
  onConfirmar,
}: ConfirmarStatusDialogProps) {
  return (
    <AlertDialog open={acao !== null} onOpenChange={(aberto) => !aberto && onClose()}>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Confirmar alteração de status?</AlertDialogTitle>
          <AlertDialogDescription>
            {acao === 'aceitar' &&
              'O orçamento será marcado como ACEITO pelo cliente, habilitando o início da execução.'}
            {acao === 'iniciar' &&
              'A Ordem de Serviço mudará para EM ANDAMENTO e a data de início da manutenção será registrada.'}
            {acao === 'finalizar' &&
              'A Ordem de Serviço será marcada como FINALIZADA. Após finalizar, não será mais possível adicionar/remover itens ou alterar descontos.'}
            {acao === 'rejeitar' &&
              'O orçamento será marcado como REJEITADO e a data de rejeição será registrada.'}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={executando}>Cancelar</AlertDialogCancel>
          <AlertDialogAction
            onClick={onConfirmar}
            disabled={executando}
          >
            {executando ? 'Processando...' : 'Confirmar'}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
