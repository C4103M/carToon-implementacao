'use client'

import React from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Printer, Wrench, Package } from 'lucide-react'
import type { OrdemServicoResponse } from '@/lib/types'

interface ImpressaoOrcamentoDialogProps {
  aberto: boolean
  onOpenChange: (aberto: boolean) => void
  os: OrdemServicoResponse
  valoresResumo: {
    totalServicos: number
    totalPecas: number
    valorBruto: number
    valorDesconto: number
    total: number
  }
}

function formatCurrency(valor: number): string {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(valor || 0)
}

function formatDate(dataStr?: string): string {
  if (!dataStr) return '—'
  const partes = dataStr.split('-')
  if (partes.length === 3) {
    return `${partes[2]}/${partes[1]}/${partes[0]}`
  }
  return dataStr
}

function formatCpf(cpf?: string): string {
  if (!cpf) return ''
  const limpo = cpf.replace(/\D/g, '')
  if (limpo.length !== 11) return cpf
  return limpo.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
}

function formatarTempoExibicao(tempo?: string, horasDecimais?: number): string {
  if (tempo) {
    const partes = tempo.split(':')
    if (partes.length >= 2) {
      const h = parseInt(partes[0], 10)
      const m = parseInt(partes[1], 10)
      if (h > 0 && m > 0) return `${h}h ${m}min`
      if (h > 0) return `${h}h`
      return `${m}min`
    }
  }
  if (horasDecimais !== undefined && horasDecimais !== null) {
    const h = Math.floor(horasDecimais)
    const m = Math.round((horasDecimais - h) * 60)
    if (h > 0 && m > 0) return `${h}h ${m}min`
    if (h > 0) return `${h}h`
    return `${m}min`
  }
  return '—'
}

export function ImpressaoOrcamentoDialog({
  aberto,
  onOpenChange,
  os,
  valoresResumo,
}: ImpressaoOrcamentoDialogProps) {
  return (
    <Dialog open={aberto} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-5xl w-[96vw] h-[92vh] max-h-[92vh] flex flex-col p-0 gap-0 overflow-hidden bg-background border shadow-2xl">
        <DialogHeader className="p-4 sm:p-5 border-b shrink-0 flex flex-col gap-1 pr-12 bg-card">
          <DialogTitle className="flex items-center gap-2 text-base font-semibold">
            <Printer className="size-5 text-primary" />
            Visualização de Impressão (Folha A4)
          </DialogTitle>
          <DialogDescription className="text-xs text-muted-foreground">
            Documento formatado em proporção padrão A4 para cliente, discriminando exclusivamente os valores finais unitários e totais.
          </DialogDescription>
        </DialogHeader>

        {/* Área de visualização estilo mesa de desenho/papel */}
        <div className="flex-1 overflow-y-auto bg-zinc-200/70 dark:bg-zinc-950 p-4 sm:p-6 md:p-8 flex justify-center items-start">
          {/* Folha Física A4 */}
          <div
            id="secao-impressao-orcamento"
            className="folha-a4-impressao w-full max-w-[210mm] min-h-[297mm] bg-white text-zinc-900 border border-zinc-300 shadow-xl rounded-sm p-8 sm:p-12 space-y-6 text-sm"
          >
            {/* Cabeçalho do Orçamento */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between border-b border-zinc-200 pb-4 gap-2">
              <div>
                <h2 className="text-xl font-bold tracking-tight text-zinc-900">
                  {os.oficinaNome || 'Cartoon Oficina Mecânica'}
                </h2>
                <p className="text-xs text-zinc-500 font-medium">
                  Orçamento de Serviços e Peças #{os.id}
                </p>
              </div>
              <div className="text-left sm:text-right text-xs space-y-1">
                <div>
                  <span className="text-zinc-500">Data de Emissão: </span>
                  <span className="font-semibold text-zinc-800">
                    {os.dataOrcamento ? formatDate(os.dataOrcamento) : '—'}
                  </span>
                </div>
                <div>
                  <span className="text-zinc-500">Status: </span>
                  <span className="font-semibold text-zinc-800">{os.status}</span>
                </div>
              </div>
            </div>

            {/* Informações do Cliente e Veículo */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 bg-zinc-50 border border-zinc-200 p-4 rounded-md">
              <div>
                <h3 className="font-semibold text-xs uppercase tracking-wider text-zinc-500 mb-2">
                  Dados do Cliente
                </h3>
                <p className="font-medium text-zinc-900">{os.clienteNome || 'Cliente não identificado'}</p>
                {os.clienteCpf && (
                  <p className="text-xs text-zinc-600">CPF: {formatCpf(os.clienteCpf)}</p>
                )}
                {os.mecanicoNome && (
                  <p className="text-xs text-zinc-600 mt-1">
                    Mecânico Responsável: <span className="text-zinc-900 font-medium">{os.mecanicoNome}</span>
                  </p>
                )}
              </div>
              <div>
                <h3 className="font-semibold text-xs uppercase tracking-wider text-zinc-500 mb-2">
                  Dados do Veículo
                </h3>
                <p className="font-medium text-zinc-900">
                  {os.veiculoMarca || ''} {os.veiculoModelo || ''} {os.veiculoAno ? `(${os.veiculoAno})` : ''}
                </p>
                <p className="text-xs font-mono font-semibold text-zinc-900">Placa: {os.placa}</p>
                {os.descricao && (
                  <p className="text-xs text-zinc-600 mt-1">
                    Motivo: <span className="text-zinc-800">{os.descricao}</span>
                  </p>
                )}
              </div>
            </div>

            {/* Serviços Lançados */}
            <div>
              <h3 className="font-semibold text-sm mb-2 flex items-center gap-1.5 text-zinc-900">
                <Wrench className="size-4 text-zinc-700" />
                Mão de Obra e Serviços
              </h3>
              <Table className="border-zinc-200">
                <TableHeader>
                  <TableRow className="border-zinc-200 hover:bg-transparent">
                    <TableHead className="text-zinc-700 font-semibold">Serviço</TableHead>
                    <TableHead className="text-zinc-700 font-semibold">Tempo Est.</TableHead>
                    <TableHead className="text-center w-16 text-zinc-700 font-semibold">Qtd</TableHead>
                    <TableHead className="text-right text-zinc-700 font-semibold">Valor Unitário</TableHead>
                    <TableHead className="text-right text-zinc-700 font-semibold">Subtotal</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {!os.itensServico || os.itensServico.length === 0 ? (
                    <TableRow className="border-zinc-200">
                      <TableCell colSpan={5} className="text-center text-zinc-500 py-4">
                        Nenhum serviço lançado neste orçamento.
                      </TableCell>
                    </TableRow>
                  ) : (
                    os.itensServico.map((s) => (
                      <TableRow key={s.id} className="border-zinc-200 hover:bg-zinc-50/50">
                        <TableCell className="font-medium text-zinc-900">{s.servicoNome}</TableCell>
                        <TableCell className="text-xs text-zinc-600">
                          {formatarTempoExibicao(s.tempo, s.quantidadeHoras)}
                        </TableCell>
                        <TableCell className="text-center text-zinc-800">{s.quantidade}</TableCell>
                        <TableCell className="text-right font-mono text-xs text-zinc-800">
                          {formatCurrency(Number(s.valorUnitario))}
                        </TableCell>
                        <TableCell className="text-right font-mono font-medium text-zinc-900">
                          {formatCurrency(Number(s.subtotal))}
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
              <div className="flex justify-end pt-2 text-xs">
                <span className="text-zinc-500 mr-2">Subtotal Mão de Obra:</span>
                <span className="font-bold font-mono text-zinc-900">
                  {formatCurrency(valoresResumo.totalServicos)}
                </span>
              </div>
            </div>

            {/* Peças Lançadas - EXCLUSIVAMENTE COM VALOR UNITÁRIO FINAL PARA O CLIENTE */}
            <div>
              <h3 className="font-semibold text-sm mb-2 flex items-center gap-1.5 text-zinc-900">
                <Package className="size-4 text-zinc-700" />
                Peças e Componentes (Preço Final ao Consumidor)
              </h3>
              <Table className="border-zinc-200">
                <TableHeader>
                  <TableRow className="border-zinc-200 hover:bg-transparent">
                    <TableHead className="text-zinc-700 font-semibold">Peça</TableHead>
                    <TableHead className="text-zinc-700 font-semibold">Fabricante</TableHead>
                    <TableHead className="text-center w-16 text-zinc-700 font-semibold">Qtd</TableHead>
                    <TableHead className="text-right text-zinc-700 font-semibold">Valor Unitário</TableHead>
                    <TableHead className="text-right text-zinc-700 font-semibold">Subtotal</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {!os.itensPeca || os.itensPeca.length === 0 ? (
                    <TableRow className="border-zinc-200">
                      <TableCell colSpan={5} className="text-center text-zinc-500 py-4">
                        Nenhuma peça lançada neste orçamento.
                      </TableCell>
                    </TableRow>
                  ) : (
                    os.itensPeca.map((p) => {
                      const valorFinal = Number(p.valorFinal ?? p.valorUnitario)
                      return (
                        <TableRow key={p.id} className="border-zinc-200 hover:bg-zinc-50/50">
                          <TableCell className="font-medium text-zinc-900">
                            {p.pecaNome || p.nomePeca || `Peça #${p.id}`}
                          </TableCell>
                          <TableCell className="text-xs text-zinc-600">
                            {p.fabricante || '—'}
                          </TableCell>
                          <TableCell className="text-center text-zinc-800">{p.quantidade}</TableCell>
                          <TableCell className="text-right font-mono text-xs font-medium text-zinc-800">
                            {formatCurrency(valorFinal)}
                          </TableCell>
                          <TableCell className="text-right font-mono font-medium text-zinc-900">
                            {formatCurrency(Number(p.subtotal))}
                          </TableCell>
                        </TableRow>
                      )
                    })
                  )}
                </TableBody>
              </Table>
              <div className="flex justify-end pt-2 text-xs">
                <span className="text-zinc-500 mr-2">Subtotal Peças:</span>
                <span className="font-bold font-mono text-zinc-900">
                  {formatCurrency(valoresResumo.totalPecas)}
                </span>
              </div>
            </div>

            {/* Resumo Final de Valores */}
            <div className="bloco-evitar-quebra border-t border-zinc-200 pt-4 flex flex-col items-end space-y-1.5 text-xs">
              <div className="flex justify-between w-64 text-zinc-700">
                <span className="text-zinc-500">Subtotal Serviços:</span>
                <span className="font-mono">{formatCurrency(valoresResumo.totalServicos)}</span>
              </div>
              <div className="flex justify-between w-64 text-zinc-700">
                <span className="text-zinc-500">Subtotal Peças:</span>
                <span className="font-mono">{formatCurrency(valoresResumo.totalPecas)}</span>
              </div>
              <div className="flex justify-between w-64 text-zinc-700">
                <span className="text-zinc-500">Valor Bruto:</span>
                <span className="font-mono">{formatCurrency(valoresResumo.valorBruto)}</span>
              </div>
              {valoresResumo.valorDesconto > 0 && (
                <div className="flex justify-between w-64 text-emerald-700 font-medium">
                  <span>Desconto Concedido:</span>
                  <span className="font-mono">-{formatCurrency(valoresResumo.valorDesconto)}</span>
                </div>
              )}
              <div className="flex justify-between w-64 border-t border-zinc-300 pt-2 text-base font-bold text-zinc-900">
                <span>TOTAL FINAL:</span>
                <span className="font-mono text-zinc-900">{formatCurrency(valoresResumo.total)}</span>
              </div>
            </div>

            {/* Termos e Assinaturas */}
            <div className="bloco-evitar-quebra border-t border-zinc-200 pt-6 mt-6 text-xs text-zinc-500 space-y-8">
              <p className="italic text-center">
                * Este orçamento tem validade legal de 10 (dez) dias corridos a partir da data de sua emissão.
              </p>
              <div className="grid grid-cols-2 gap-12 pt-6">
                <div className="text-center border-t border-zinc-400 pt-2">
                  <p className="font-medium text-zinc-900">{os.clienteNome || 'Cliente'}</p>
                  <p className="text-[11px] text-zinc-500">Assinatura de Aprovação</p>
                </div>
                <div className="text-center border-t border-zinc-400 pt-2">
                  <p className="font-medium text-zinc-900">{os.mecanicoNome || os.oficinaNome || 'Oficina'}</p>
                  <p className="text-[11px] text-zinc-500">Responsável Técnico</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <DialogFooter className="p-3 sm:p-4 border-t shrink-0 bg-card flex flex-row justify-between items-center sm:justify-between">
          <Button variant="ghost" onClick={() => onOpenChange(false)}>
            Fechar
          </Button>
          <Button onClick={() => window.print()} className="gap-2 shadow-sm">
            <Printer className="size-4" />
            Imprimir Documento
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
