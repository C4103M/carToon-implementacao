'use client'

import {
  createContext,
  useCallback as useCb,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import type {
  Cliente,
  Oficina,
  OficinaStatus,
  Orcamento,
  OrcamentoItem,
  OrcamentoStatus,
  Peca,
  Servico,
  SessionUser,
  Veiculo,
  VeiculoResponse,
} from './types'
import { computeLambda, type FipeResult } from './fipe'
import { authService } from './services/auth.service'
import { clientesService } from './services/clientes.service'
import { veiculosService } from './services/veiculos.service'
import { api, type Page } from './api'

function uid(prefix: string): string {
  return `${prefix}-${Math.random().toString(36).slice(2, 9)}`
}

interface StoreValue {
  // sessão
  user: SessionUser | null
  setUser: (user: SessionUser | null) => void

  logout: () => void

  // oficinas (admin)
  oficinas: Oficina[]
  setOficinas: React.Dispatch<React.SetStateAction<Oficina[]>>
  addOficina: (data: {
    nome: string
    endereco: string
    telefone?: string
    email?: string
    status?: OficinaStatus
  }) => Oficina
  setOficinaStatus: (id: string, status: OficinaStatus) => void

  // clientes
  clientes: Cliente[]
  refreshClientesEVeiculos: () => Promise<void>
  addCliente: (data: Omit<Cliente, 'id'>) => Cliente
  updateCliente: (id: string, data: Partial<Omit<Cliente, 'id'>>) => void

  // veículos
  veiculos: Veiculo[]
  addVeiculo: (
    data: Omit<Veiculo, 'id' | 'ativo' | 'fipeValor' | 'lambda' | 'fipeConsultadoEm'>,
    fipe: FipeResult,
  ) => Veiculo
  trocarVeiculo: (
    veiculoAntigoId: string,
    data: Omit<Veiculo, 'id' | 'ativo' | 'fipeValor' | 'lambda' | 'fipeConsultadoEm'>,
    fipe: FipeResult,
  ) => Veiculo

  // catálogo
  servicos: Servico[]
  addServico: (data: Omit<Servico, 'id' | 'ativo'>) => Servico
  updateServico: (id: string, data: Partial<Omit<Servico, 'id'>>) => void
  pecas: Peca[]
  addPeca: (data: Omit<Peca, 'id' | 'ativo'>) => Peca
  updatePeca: (id: string, data: Partial<Omit<Peca, 'id'>>) => void

  // orçamentos
  orcamentos: Orcamento[]
  createOrcamento: (clienteId: string, veiculoId: string) => Orcamento
  addOrcamentoItem: (
    orcamentoId: string,
    servico: Servico,
    quantidade: number,
    lambda: number,
  ) => void
  removeOrcamentoItem: (orcamentoId: string, itemId: string) => void
  setOrcamentoStatus: (orcamentoId: string, status: OrcamentoStatus) => void
}

const StoreContext = createContext<StoreValue | null>(null)

export function StoreProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<SessionUser | null>(null)
  const [oficinas, setOficinas] = useState<Oficina[]>([])
  const [clientes, setClientes] = useState<Cliente[]>([])
  const [veiculos, setVeiculos] = useState<Veiculo[]>([])
  const [servicos, setServicos] = useState<Servico[]>([])
  const [pecas, setPecas] = useState<Peca[]>([])
  const [orcamentos, setOrcamentos] = useState<Orcamento[]>([])

  const refreshClientesEVeiculos = useCb(async () => {
    try {
      const [listaClientes, pageVeiculos] = await Promise.all([
        clientesService.listar(),
        api.get<Page<VeiculoResponse>>('/veiculos', { params: { size: 200 } }),
      ])

      if (Array.isArray(listaClientes)) {
        setClientes(
          listaClientes.map((c) => ({
            id: String(c.id),
            nome: c.nome,
            cpf: c.cpf,
            endereco: c.endereco || '',
          })),
        )
      }

      if (pageVeiculos?.content && Array.isArray(pageVeiculos.content)) {
        setVeiculos(
          pageVeiculos.content.map((v) => {
            const valorFipe = Number(v.valorFipe ?? 50000)
            return {
              id: String(v.id),
              clienteId: String(v.clienteId),
              placa: v.placa,
              modelo: v.modelo,
              montadora: v.marca,
              ano: v.anoModelo,
              codigoFipe: v.codigoFipe,
              fipeValor: valorFipe,
              lambda: computeLambda(valorFipe),
              fipeConsultadoEm: new Date().toISOString().slice(0, 10),
              ativo: v.ativo ?? true,
            }
          }),
        )
      }
    } catch {
      // Se não autenticado ou API indisponível, mantém estado atual
    }
  }, [])

  // Sincroniza sessão ativa a partir do cookie HttpOnly com o backend
  useEffect(() => {
    let isMounted = true
    authService
      .getCurrentUser()
      .then((authUser) => {
        if (!isMounted || !authUser) return
        setUser({
          id: authUser.id,
          nome: authUser.nome,
          email: authUser.email,
          role: authUser.role,
          ativo: authUser.ativo,
          oficinaId: authUser.oficinaId ?? undefined,
        })
        refreshClientesEVeiculos()
      })
      .catch(() => {
        // Sem sessão ativa no cookie, mantém user como null
      })

    return () => {
      isMounted = false
    }
  }, [refreshClientesEVeiculos])

  const logout = useCb(async () => {
    setUser(null)
    try {
      await authService.logout()
    } catch {
      // Silencia se API já estiver inacessível
    }
  }, [])

  const setOficinaStatus = useCb<StoreValue['setOficinaStatus']>((id, status) => {
    setOficinas((prev) => prev.map((o) => (o.id === id ? { ...o, status } : o)))
  }, [])

  const addOficina = useCb<StoreValue['addOficina']>((data) => {
    const oficina: Oficina = {
      id: uid('ofi'),
      nome: data.nome,
      endereco: data.endereco,
      telefone: data.telefone || '',
      email: data.email || '',
      dataCadastro: new Date().toISOString().slice(0, 10),
      status: data.status || 'ativa',
    }
    setOficinas((prev) => [oficina, ...prev])
    return oficina
  }, [])

  const addCliente = useCb<StoreValue['addCliente']>((data) => {
    const tempId = uid('cli')
    const cliente: Cliente = { id: tempId, ...data }
    setClientes((prev) => [cliente, ...prev])
    clientesService
      .criar({
        nome: data.nome,
        cpf: data.cpf.replace(/\D/g, ''),
        endereco: data.endereco,
      })
      .then((salvo) => {
        setClientes((prev) =>
          prev.map((c) => (c.id === tempId ? { ...c, id: String(salvo.id) } : c)),
        )
      })
      .catch(() => {})
    return cliente
  }, [])

  const updateCliente = useCb<StoreValue['updateCliente']>((id, data) => {
    setClientes((prev) => prev.map((c) => (c.id === id ? { ...c, ...data } : c)))
    const numId = Number(id)
    if (!Number.isNaN(numId) && data.nome && data.cpf && data.endereco) {
      clientesService
        .atualizar(numId, {
          nome: data.nome,
          cpf: data.cpf.replace(/\D/g, ''),
          endereco: data.endereco,
        })
        .catch(() => {})
    }
  }, [])

  const addVeiculo = useCb<StoreValue['addVeiculo']>((data, fipe) => {
    const tempId = uid('vei')
    const veiculo: Veiculo = {
      id: tempId,
      ...data,
      fipeValor: fipe.valor,
      lambda: fipe.lambda,
      fipeConsultadoEm: fipe.consultadoEm,
      ativo: true,
    }
    setVeiculos((prev) => [veiculo, ...prev])
    const numClienteId = Number(data.clienteId)
    if (!Number.isNaN(numClienteId)) {
      veiculosService
        .criar({
          clienteId: numClienteId,
          placa: data.placa,
          marca: data.montadora,
          modelo: data.modelo,
          anoModelo: data.ano,
          codigoFipe: data.codigoFipe,
          valorFipe: fipe.valor,
        })
        .then((salvo) => {
          setVeiculos((prev) =>
            prev.map((v) => (v.id === tempId ? { ...v, id: String(salvo.id) } : v)),
          )
        })
        .catch(() => {})
    }
    return veiculo
  }, [])

  const trocarVeiculo = useCb<StoreValue['trocarVeiculo']>((veiculoAntigoId, data, fipe) => {
    const tempId = uid('vei')
    const novoVeiculo: Veiculo = {
      id: tempId,
      ...data,
      fipeValor: fipe.valor,
      lambda: fipe.lambda,
      fipeConsultadoEm: fipe.consultadoEm,
      ativo: true,
    }
    setVeiculos((prev) =>
      prev
        .map((v) =>
          v.id === veiculoAntigoId
            ? { ...v, ativo: false, substituidoPorId: novoVeiculo.id }
            : v,
        )
        .concat(novoVeiculo),
    )
    const numAntigoId = Number(veiculoAntigoId)
    if (!Number.isNaN(numAntigoId)) {
      veiculosService.inativar(numAntigoId).catch(() => {})
    }
    const numClienteId = Number(data.clienteId)
    if (!Number.isNaN(numClienteId)) {
      veiculosService
        .criar({
          clienteId: numClienteId,
          placa: data.placa,
          marca: data.montadora,
          modelo: data.modelo,
          anoModelo: data.ano,
          codigoFipe: data.codigoFipe,
          valorFipe: fipe.valor,
        })
        .then((salvo) => {
          setVeiculos((prev) =>
            prev.map((v) => (v.id === tempId ? { ...v, id: String(salvo.id) } : v)),
          )
        })
        .catch(() => {})
    }
    return novoVeiculo
  }, [])

  const addServico = useCb<StoreValue['addServico']>((data) => {
    const servico: Servico = { id: uid('srv'), ativo: true, ...data }
    setServicos((prev) => [servico, ...prev])
    return servico
  }, [])

  const updateServico = useCb<StoreValue['updateServico']>((id, data) => {
    setServicos((prev) => prev.map((s) => (s.id === id ? { ...s, ...data } : s)))
  }, [])

  const addPeca = useCb<StoreValue['addPeca']>((data) => {
    const peca: Peca = { id: uid('pec'), ativo: true, ...data }
    setPecas((prev) => [peca, ...prev])
    return peca
  }, [])

  const updatePeca = useCb<StoreValue['updatePeca']>((id, data) => {
    setPecas((prev) => prev.map((p) => (p.id === id ? { ...p, ...data } : p)))
  }, [])

  const createOrcamento = useCb<StoreValue['createOrcamento']>((clienteId, veiculoId) => {
    const orcamento: Orcamento = {
      id: uid('orc'),
      clienteId,
      veiculoId,
      data: new Date().toISOString().slice(0, 10),
      status: 'pendente',
      itens: [],
    }
    setOrcamentos((prev) => [orcamento, ...prev])
    return orcamento
  }, [])

  const addOrcamentoItem = useCb<StoreValue['addOrcamentoItem']>(
    (orcamentoId, servico, quantidade, lambda) => {
      const valorUnitario = Math.round(servico.valorBase * lambda * 100) / 100
      const item: OrcamentoItem = {
        id: uid('it'),
        servicoId: servico.id,
        servicoNome: servico.nome,
        valorBaseServico: servico.valorBase,
        quantidade,
        valorUnitario,
      }
      setOrcamentos((prev) =>
        prev.map((o) =>
          o.id === orcamentoId && o.status === 'pendente'
            ? { ...o, itens: [...o.itens, item] }
            : o,
        ),
      )
    },
    [],
  )

  const removeOrcamentoItem = useCb<StoreValue['removeOrcamentoItem']>(
    (orcamentoId, itemId) => {
      setOrcamentos((prev) =>
        prev.map((o) =>
          o.id === orcamentoId && o.status === 'pendente'
            ? { ...o, itens: o.itens.filter((i) => i.id !== itemId) }
            : o,
        ),
      )
    },
    [],
  )

  const setOrcamentoStatus = useCb<StoreValue['setOrcamentoStatus']>((orcamentoId, status) => {
    setOrcamentos((prev) =>
      prev.map((o) => (o.id === orcamentoId ? { ...o, status } : o)),
    )
  }, [])

  const value = useMemo<StoreValue>(
    () => ({
      user,
      setUser,
      logout,
      oficinas,
      setOficinas,
      addOficina,
      setOficinaStatus,
      clientes,
      refreshClientesEVeiculos,
      addCliente,
      updateCliente,
      veiculos,
      addVeiculo,
      trocarVeiculo,
      servicos,
      addServico,
      updateServico,
      pecas,
      addPeca,
      updatePeca,
      orcamentos,
      createOrcamento,
      addOrcamentoItem,
      removeOrcamentoItem,
      setOrcamentoStatus,
    }),
    [
      user,
      setUser,
      logout,
      oficinas,
      setOficinas,
      addOficina,
      setOficinaStatus,
      clientes,
      refreshClientesEVeiculos,
      addCliente,
      updateCliente,
      veiculos,
      addVeiculo,
      trocarVeiculo,
      servicos,
      addServico,
      updateServico,
      pecas,
      addPeca,
      updatePeca,
      orcamentos,
      createOrcamento,
      addOrcamentoItem,
      removeOrcamentoItem,
      setOrcamentoStatus,
    ],
  )

  return <StoreContext.Provider value={value}>{children}</StoreContext.Provider>
}

export function useStore(): StoreValue {
  const ctx = useContext(StoreContext)
  if (!ctx) throw new Error('useStore deve ser usado dentro de StoreProvider')
  return ctx
}
