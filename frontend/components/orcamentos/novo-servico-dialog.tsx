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
import { Textarea } from '@/components/ui/textarea'
import { Spinner } from '@/components/ui/spinner'
import { catalogoService } from '@/lib/services/catalogo.service'
import type { ServicoResponse } from '@/lib/types'
import { toast } from 'sonner'
import { handleError } from '@/lib/error-handler'

function normalizarTempoParaEnvio(input: string): string | null {
  const str = input.trim()
  if (!str) return null

  const colonMatch = str.match(/^(\d{1,2}):(\d{1,2})(?::\d{1,2})?$/)
  if (colonMatch) {
    const h = parseInt(colonMatch[1], 10)
    const m = parseInt(colonMatch[2], 10)
    if (h === 0 && m === 0) return null
    if (m > 59) return null
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
  }

  const horaMinMatch = str.match(/^(\d+)\s*[hH]\s*(\d+)?\s*(?:[mM])?$/)
  if (horaMinMatch) {
    const h = parseInt(horaMinMatch[1], 10)
    const m = horaMinMatch[2] ? parseInt(horaMinMatch[2], 10) : 0
    if (h === 0 && m === 0) return null
    if (m > 59) return null
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
  }

  const minMatch = str.match(/^(\d+)\s*(?:m|min|minutos)$/i)
  if (minMatch) {
    const totalMin = parseInt(minMatch[1], 10)
    if (totalMin <= 0) return null
    const h = Math.floor(totalMin / 60)
    const m = totalMin % 60
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
  }

  const num = parseFloat(str.replace(',', '.'))
  if (!isNaN(num) && num > 0) {
    const h = Math.floor(num)
    const m = Math.round((num - h) * 60)
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
  }

  return null
}

interface NovoServicoDialogProps {
  aberto: boolean
  onOpenChange: (aberto: boolean) => void
  onServicoCriado: (servicoCriado: ServicoResponse) => void
}

export function NovoServicoDialog({
  aberto,
  onOpenChange,
  onServicoCriado,
}: NovoServicoDialogProps) {
  const [nome, setNome] = useState('')
  const [tempo, setTempo] = useState('')
  const [descricao, setDescricao] = useState('')
  const [salvando, setSalvando] = useState(false)

  const handleCadastrar = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!nome.trim()) {
      toast.error('Informe o nome do serviço.')
      return
    }

    const tempoFormatado = normalizarTempoParaEnvio(tempo)
    if (!tempoFormatado) {
      toast.error('Informe um tempo válido (ex: 1h30, 1:30 ou 45min).')
      return
    }

    setSalvando(true)
    try {
      const criado = await catalogoService.criarServico({
        nome: nome.trim(),
        tempo: tempoFormatado,
        descricao: descricao.trim() || undefined,
      })

      toast.success(`Serviço "${criado.nome}" cadastrado no catálogo!`)
      setNome('')
      setTempo('')
      setDescricao('')
      onOpenChange(false)
      onServicoCriado(criado)
    } catch (err) {
      console.error('Erro ao cadastrar serviço no catálogo:', err)
      toast.error(handleError(err, { fallbackMessage: 'Não foi possível cadastrar o serviço.' }).message)
    } finally {
      setSalvando(false)
    }
  }

  return (
    <Dialog open={aberto} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Cadastrar Novo Serviço no Catálogo</DialogTitle>
          <DialogDescription>
            O serviço será salvo no catálogo da sua oficina e selecionado automaticamente para este orçamento.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleCadastrar}>
          <div className="flex flex-col gap-4 py-4">
            <div className="flex flex-col gap-2">
              <Label htmlFor="dialog-srv-nome">Nome do Serviço</Label>
              <Input
                id="dialog-srv-nome"
                placeholder="Ex: Troca da Correia Dentada"
                value={nome}
                onChange={(e) => setNome(e.target.value)}
                required
              />
            </div>
            <div className="flex flex-col gap-2">
              <Label htmlFor="dialog-srv-tempo">Tempo Estimado (ex: 1h30 ou 1:30)</Label>
              <Input
                id="dialog-srv-tempo"
                placeholder="Ex: 1h30 ou 01:30"
                value={tempo}
                onChange={(e) => setTempo(e.target.value)}
                required
              />
            </div>
            <div className="flex flex-col gap-2">
              <Label htmlFor="dialog-srv-desc">Descrição (Opcional)</Label>
              <Textarea
                id="dialog-srv-desc"
                rows={2}
                placeholder="Procedimentos inclusos..."
                value={descricao}
                onChange={(e) => setDescricao(e.target.value)}
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
