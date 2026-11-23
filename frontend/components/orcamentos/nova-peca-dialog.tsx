'use client'

import React, { useState } from 'react'
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
import { catalogoService } from '@/lib/services/catalogo.service'
import type { PecaResponse } from '@/lib/types'
import { toast } from 'sonner'
import { handleError } from '@/lib/error-handler'

interface NovaPecaDialogProps {
  aberto: boolean
  onOpenChange: (aberto: boolean) => void
  onPecaCriada: (pecaCriada: PecaResponse) => void
}

export function NovaPecaDialog({
  aberto,
  onOpenChange,
  onPecaCriada,
}: NovaPecaDialogProps) {
  const [nome, setNome] = useState('')
  const [fabricante, setFabricante] = useState('')
  const [valorBase, setValorBase] = useState('')
  const [salvando, setSalvando] = useState(false)

  const handleCadastrar = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!nome.trim() || !fabricante.trim() || !valorBase) {
      toast.error('Preencha o nome, fabricante e o valor base da peça.')
      return
    }

    const parsed = Number.parseFloat(valorBase.replace(',', '.'))
    if (Number.isNaN(parsed) || parsed <= 0) {
      toast.error('Informe um valor base válido maior que zero.')
      return
    }

    setSalvando(true)
    try {
      const criada = await catalogoService.criarPeca({
        nome: nome.trim(),
        fabricante: fabricante.trim(),
        valorBase: parsed,
      })

      toast.success(`Peça "${criada.nome}" cadastrada no catálogo!`)
      setNome('')
      setFabricante('')
      setValorBase('')
      onOpenChange(false)
      onPecaCriada(criada)
    } catch (err) {
      console.error('Erro ao cadastrar peça no catálogo:', err)
      toast.error(handleError(err, { fallbackMessage: 'Não foi possível cadastrar a peça.' }).message)
    } finally {
      setSalvando(false)
    }
  }

  return (
    <Dialog open={aberto} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Cadastrar Nova Peça no Catálogo</DialogTitle>
          <DialogDescription>
            A peça será salva no catálogo da sua oficina e selecionada automaticamente para este orçamento.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleCadastrar}>
          <div className="flex flex-col gap-4 py-4">
            <div className="flex flex-col gap-2">
              <Label htmlFor="dialog-peca-nome">Nome da Peça</Label>
              <Input
                id="dialog-peca-nome"
                placeholder="Ex: Jogo de Pastilhas Dianteiras"
                value={nome}
                onChange={(e) => setNome(e.target.value)}
                required
              />
            </div>
            <div className="flex flex-col gap-2">
              <Label htmlFor="dialog-peca-fab">Fabricante</Label>
              <Input
                id="dialog-peca-fab"
                placeholder="Ex: Cobreq / Bosch"
                value={fabricante}
                onChange={(e) => setFabricante(e.target.value)}
                required
              />
            </div>
            <div className="flex flex-col gap-2">
              <Label htmlFor="dialog-peca-valor">Valor Base Unitário (R$)</Label>
              <Input
                id="dialog-peca-valor"
                type="number"
                step="0.01"
                min="0.01"
                placeholder="0.00"
                value={valorBase}
                onChange={(e) => setValorBase(e.target.value)}
                required
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={salvando}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={salvando}>
              {salvando && <Spinner className="size-4 mr-2" />}
              Cadastrar e Selecionar
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
