'use client'

import React, { useState, useEffect } from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Spinner } from '@/components/ui/spinner'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Pencil, Calculator } from 'lucide-react'
import { ordemServicoService } from '@/lib/services/ordem-servico.service'
import type { OrdemServicoResponse } from '@/lib/types'
import { PESO_SERVICO_OPCOES } from '@/lib/types'
import { toast } from 'sonner'
import { handleError } from '@/lib/error-handler'

export interface ItemEmEdicao {
  id: number
  tipo: 'servico' | 'peca'
  nome: string
  quantidade: number
  valorUnitarioAtual: number
  pesoServicoAtual?: number
}

interface EditarValorItemDialogProps {
  item: ItemEmEdicao | null
  ordemId: number
  onClose: () => void
  onSucesso: (osAtualizada: OrdemServicoResponse) => void
}

function formatCurrency(valor: number): string {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(valor || 0)
}

export function EditarValorItemDialog({
  item,
  ordemId,
  onClose,
  onSucesso,
}: EditarValorItemDialogProps) {
  const [novoValorInput, setNovoValorInput] = useState('')
  const [novoPeso, setNovoPeso] = useState<number>(0.5)
  const [modoAjuste, setModoAjuste] = useState<'peso' | 'valor'>('peso')
  const [salvando, setSalvando] = useState(false)

  useEffect(() => {
    if (item) {
      setNovoValorInput(item.valorUnitarioAtual ? item.valorUnitarioAtual.toFixed(2) : '0.00')
      setNovoPeso(item.pesoServicoAtual != null ? Number(item.pesoServicoAtual) : 0.5)
      setModoAjuste(item.tipo === 'servico' ? 'peso' : 'valor')
    } else {
      setNovoValorInput('')
      setNovoPeso(0.5)
      setModoAjuste('peso')
    }
  }, [item])

  const handleSalvar = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!item) return

    setSalvando(true)
    try {
      let osAtualizada: OrdemServicoResponse

      if (item.tipo === 'servico') {
        if (modoAjuste === 'peso') {
          // Recalcula no backend pela fórmula oficial usando o novo peso selecionado
          osAtualizada = await ordemServicoService.atualizarValorItemServico(ordemId, item.id, {
            pesoServico: novoPeso,
          })
          toast.success(`Peso do serviço "${item.nome}" alterado para ${novoPeso} e valor recalculado com sucesso!`)
        } else {
          // Valor manual fixo
          const parsed = parseFloat(novoValorInput.replace(',', '.'))
          if (isNaN(parsed) || parsed < 0) {
            toast.error('Informe um valor unitário válido e maior ou igual a zero.')
            setSalvando(false)
            return
          }
          osAtualizada = await ordemServicoService.atualizarValorItemServico(ordemId, item.id, {
            valorUnitario: parsed,
            pesoServico: novoPeso,
          })
          toast.success(`Valor de "${item.nome}" atualizado com sucesso!`)
        }
      } else {
        const parsed = parseFloat(novoValorInput.replace(',', '.'))
        if (isNaN(parsed) || parsed < 0) {
          toast.error('Informe um valor unitário válido e maior ou igual a zero.')
          setSalvando(false)
          return
        }
        osAtualizada = await ordemServicoService.atualizarValorItemPeca(ordemId, item.id, parsed)
        toast.success(`Valor de "${item.nome}" atualizado com sucesso!`)
      }

      onSucesso(osAtualizada)
      onClose()
    } catch (err) {
      console.error('Erro ao atualizar item:', err)
      toast.error(handleError(err, { fallbackMessage: 'Não foi possível alterar o item.' }).message)
    } finally {
      setSalvando(false)
    }
  }

  const parsedVal = parseFloat(novoValorInput.replace(',', '.')) || 0
  const novoSubtotal = item ? parsedVal * item.quantidade : 0

  return (
    <Dialog open={item !== null} onOpenChange={(aberto) => !aberto && onClose()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Pencil className="size-4 text-primary" />
            Ajustar {item?.tipo === 'servico' ? 'Serviço (Mão de Obra)' : 'Peça'}
          </DialogTitle>
          <DialogDescription>
            {item?.tipo === 'servico'
              ? 'Altere o nível de risco (peso) para recalcular automaticamente pela fórmula da oficina ou defina um valor manual.'
              : 'Altere o valor unitário cobrado no orçamento. O subtotal do item e o total serão recalculados automaticamente.'}
          </DialogDescription>
        </DialogHeader>

        {item && (
          <form onSubmit={handleSalvar}>
            <div className="flex flex-col gap-4 py-4 text-sm">
              <div className="p-3 rounded-lg bg-muted/50 border space-y-1">
                <span className="text-xs uppercase tracking-wide text-muted-foreground font-semibold">
                  Item Selecionado
                </span>
                <p className="font-medium text-foreground">{item.nome}</p>
                <div className="flex justify-between text-xs text-muted-foreground pt-1">
                  <span>Quantidade: {item.quantidade}x</span>
                  <span>Valor Atual: {formatCurrency(item.valorUnitarioAtual)}</span>
                </div>
              </div>

              {item.tipo === 'servico' && (
                <div className="flex flex-col gap-3">
                  <div className="flex flex-col gap-1.5">
                    <Label htmlFor="dialog-select-peso">Nível de Risco do Serviço (Peso)</Label>
                    <Select
                      value={String(novoPeso)}
                      onValueChange={(val) => setNovoPeso(Number(val))}
                    >
                      <SelectTrigger id="dialog-select-peso" className="w-full">
                        <SelectValue placeholder="Selecione o peso..." />
                      </SelectTrigger>
                      <SelectContent>
                        {PESO_SERVICO_OPCOES.map((opcao) => (
                          <SelectItem key={opcao.valor} value={String(opcao.valor)}>
                            <div className="flex flex-col text-left py-0.5">
                              <span className="font-medium text-foreground">{opcao.label}</span>
                              <span className="text-[11px] text-muted-foreground">{opcao.descricao}</span>
                            </div>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  {/* Alternância de Modo para Serviços */}
                  <div className="flex items-center gap-2 pt-1 border-t">
                    <Button
                      type="button"
                      size="sm"
                      variant={modoAjuste === 'peso' ? 'default' : 'outline'}
                      className="text-xs flex-1 gap-1"
                      onClick={() => setModoAjuste('peso')}
                    >
                      <Calculator className="size-3.5" />
                      Recalcular pelo Peso
                    </Button>
                    <Button
                      type="button"
                      size="sm"
                      variant={modoAjuste === 'valor' ? 'default' : 'outline'}
                      className="text-xs flex-1 gap-1"
                      onClick={() => setModoAjuste('valor')}
                    >
                      <Pencil className="size-3.5" />
                      Valor Manual (R$)
                    </Button>
                  </div>

                  {modoAjuste === 'peso' && (
                    <div className="p-3 rounded-lg bg-blue-500/10 border border-blue-500/20 text-xs text-blue-900 dark:text-blue-200">
                      O backend aplicará a fórmula oficial:
                      <p className="font-mono font-medium mt-1">
                        Horas × Valor_Hora × (1 + FIPE/100000 × Taxa_Risco × {novoPeso})
                      </p>
                    </div>
                  )}
                </div>
              )}

              {(item.tipo === 'peca' || modoAjuste === 'valor') && (
                <div className="flex flex-col gap-2">
                  <Label htmlFor="dialog-input-novo-valor">Novo Valor Unitário Cobrado (R$)</Label>
                  <Input
                    id="dialog-input-novo-valor"
                    type="number"
                    step="0.01"
                    min="0"
                    placeholder="0.00"
                    value={novoValorInput}
                    onChange={(e) => setNovoValorInput(e.target.value)}
                    autoFocus
                    required
                  />
                  <span className="text-[11px] text-muted-foreground">
                    Defina o valor unitário cobrado. O subtotal será atualizado proporcionalmente à quantidade.
                  </span>

                  {/* Prévia dinâmica do subtotal para valor manual */}
                  <div className="flex items-center justify-between p-3 rounded-md bg-primary/10 border border-primary/20 text-xs">
                    <span className="font-semibold text-primary">Novo Subtotal do Item:</span>
                    <span className="font-mono font-bold text-sm text-foreground">
                      {formatCurrency(novoSubtotal)}
                    </span>
                  </div>
                </div>
              )}
            </div>

            <DialogFooter className="gap-2 sm:gap-0">
              <Button
                type="button"
                variant="outline"
                onClick={onClose}
                disabled={salvando}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={salvando}>
                {salvando && <Spinner className="size-4 mr-2" />}
                {modoAjuste === 'peso' && item.tipo === 'servico'
                  ? 'Recalcular e Salvar'
                  : 'Salvar Novo Valor'}
              </Button>
            </DialogFooter>
          </form>
        )}
      </DialogContent>
    </Dialog>
  )
}
