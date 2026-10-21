'use client'

import {
  createContext,
  useCallback as useCb,
  useContext,
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
} from './types'
import {
  ADMIN_EMAIL,
  seedClientes,
  seedOficinas,
  seedOrcamentos,
  seedPecas,
  seedServicos,
  seedVeiculos,
} from './mock-data'
import type { FipeResult } from './fipe'

function uid(prefix: string): string {
  return `${prefix}-${Math.random().toString(36).slice(2, 9)}`
}

interface StoreValue {
  // sessão
  user: SessionUser | null
  login: (email: string) => { ok: true } | { ok: false; reason: 'invalid' | 'suspensa' }
  logout: () => void

  // oficinas (admin)
  oficinas: Oficina[]
  setOficinaStatus: (id: string, status: OficinaStatus) => void

  // clientes
  clientes: Cliente[]
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
  const [oficinas, setOficinas] = useState<Oficina[]>(seedOficinas)
  const [clientes, setClientes] = useState<Cliente[]>(seedClientes)
  const [veiculos, setVeiculos] = useState<Veiculo[]>(seedVeiculos)
  const [servicos, setServicos] = useState<Servico[]>(seedServicos)
  const [pecas, setPecas] = useState<Peca[]>(seedPecas)
  const [orcamentos, setOrcamentos] = useState<Orcamento[]>(seedOrcamentos)

  const login = useCb<StoreValue['login']>(
    (email) => {
      const normalized = email.trim().toLowerCase()
      if (normalized === ADMIN_EMAIL) {
        setUser({
          role: 'admin',
          nome: 'Administrador',
          email: ADMIN_EMAIL,
        })
        return { ok: true }
      }
      const oficina = oficinas.find((o) => o.email.toLowerCase() === normalized)
      if (!oficina) return { ok: false, reason: 'invalid' }
      if (oficina.status === 'suspensa') return { ok: false, reason: 'suspensa' }
      if (oficina.status === 'pendente') return { ok: false, reason: 'suspensa' }
      setUser({
        role: 'oficina',
        oficinaId: oficina.id,
        nome: oficina.nome,
        email: oficina.email,
      })
      return { ok: true }
    },
    [oficinas],
  )

  const logout = useCb(() => setUser(null), [])

  const setOficinaStatus = useCb<StoreValue['setOficinaStatus']>((id, status) => {
    setOficinas((prev) => prev.map((o) => (o.id === id ? { ...o, status } : o)))
  }, [])

  const addCliente = useCb<StoreValue['addCliente']>((data) => {
    const cliente: Cliente = { id: uid('cli'), ...data }
    setClientes((prev) => [cliente, ...prev])
    return cliente
  }, [])

  const updateCliente = useCb<StoreValue['updateCliente']>((id, data) => {
    setClientes((prev) => prev.map((c) => (c.id === id ? { ...c, ...data } : c)))
  }, [])

  const addVeiculo = useCb<StoreValue['addVeiculo']>((data, fipe) => {
    const veiculo: Veiculo = {
      id: uid('vei'),
      ...data,
      fipeValor: fipe.valor,
      lambda: fipe.lambda,
      fipeConsultadoEm: fipe.consultadoEm,
      ativo: true,
    }
    setVeiculos((prev) => [veiculo, ...prev])
    return veiculo
  }, [])

  const trocarVeiculo = useCb<StoreValue['trocarVeiculo']>((antigoId, data, fipe) => {
    const novo: Veiculo = {
      id: uid('vei'),
      ...data,
      fipeValor: fipe.valor,
      lambda: fipe.lambda,
      fipeConsultadoEm: fipe.consultadoEm,
      ativo: true,
    }
    setVeiculos((prev) => [
      novo,
      ...prev.map((v) =>
        v.id === antigoId ? { ...v, ativo: false, substituidoPorId: novo.id } : v,
      ),
    ])
    return novo
  }, [])

  const addServico = useCb<StoreValue['addServico']>((data) => {
    const servico: Servico = { id: uid('srv'), ...data, ativo: true }
    setServicos((prev) => [servico, ...prev])
    return servico
  }, [])

  const updateServico = useCb<StoreValue['updateServico']>((id, data) => {
    setServicos((prev) => prev.map((s) => (s.id === id ? { ...s, ...data } : s)))
  }, [])

  const addPeca = useCb<StoreValue['addPeca']>((data) => {
    const peca: Peca = { id: uid('pec'), ...data, ativo: true }
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
      data: new Date().toISOString(),
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
      login,
      logout,
      oficinas,
      setOficinaStatus,
      clientes,
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
      login,
      logout,
      oficinas,
      setOficinaStatus,
      clientes,
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
