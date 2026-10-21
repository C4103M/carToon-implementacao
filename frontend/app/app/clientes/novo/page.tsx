'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { toast } from 'sonner'
import { useStore } from '@/lib/store'
import { formatCpf, isValidCpf } from '@/lib/format'
import { PageHeader } from '@/components/page-header'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent } from '@/components/ui/card'
import {
  Field,
  FieldGroup,
  FieldLabel,
  FieldDescription,
} from '@/components/ui/field'

export default function NovoClientePage() {
  const { addCliente } = useStore()
  const router = useRouter()

  const [nome, setNome] = useState('')
  const [cpf, setCpf] = useState('')
  const [endereco, setEndereco] = useState('')

  // Estados de erro
  const [nomeErro, setNomeErro] = useState(false)
  const [cpfErro, setCpfErro] = useState<string | null>(null)
  const [enderecoErro, setEnderecoErro] = useState(false)

  const handleCpfChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setCpf(formatCpf(e.target.value))
    if (cpfErro) setCpfErro(null)
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    let temErro = false

    if (!nome.trim()) {
      setNomeErro(true)
      temErro = true
    } else {
      setNomeErro(false)
    }

    if (!cpf.trim()) {
      setCpfErro('CPF é obrigatório.')
      temErro = true
    } else if (!isValidCpf(cpf)) {
      setCpfErro('CPF inválido. Verifique os dígitos.')
      temErro = true
    } else {
      setCpfErro(null)
    }

    if (!endereco.trim()) {
      setEnderecoErro(true)
      temErro = true
    } else {
      setEnderecoErro(false)
    }

    if (temErro) return

    addCliente({
      nome: nome.trim(),
      cpf: cpf.trim(),
      endereco: endereco.trim(),
    })

    toast.success('Cliente cadastrado com sucesso!')
    router.push('/app/clientes')
  }

  return (
    <div className="mx-auto max-w-2xl">
      <PageHeader
        title="Novo Cliente"
        description="Cadastre um novo cliente no sistema."
        backHref="/app/clientes"
        backLabel="Voltar para clientes"
      />

      <Card>
        <CardContent className="pt-6">
          <form onSubmit={handleSubmit}>
            <FieldGroup className="gap-6">
              <Field data-invalid={nomeErro || undefined}>
                <FieldLabel htmlFor="nome">Nome Completo</FieldLabel>
                <Input
                  id="nome"
                  placeholder="Ex: João da Silva"
                  value={nome}
                  onChange={(e) => {
                    setNome(e.target.value)
                    if (nomeErro) setNomeErro(false)
                  }}
                  required
                />
                {nomeErro && (
                  <FieldDescription className="text-destructive">
                    O nome é obrigatório.
                  </FieldDescription>
                )}
              </Field>

              <Field data-invalid={!!cpfErro || undefined}>
                <FieldLabel htmlFor="cpf">CPF</FieldLabel>
                <Input
                  id="cpf"
                  placeholder="000.000.000-00"
                  value={cpf}
                  onChange={handleCpfChange}
                  required
                />
                {cpfErro && (
                  <FieldDescription className="text-destructive">
                    {cpfErro}
                  </FieldDescription>
                )}
              </Field>

              <Field data-invalid={enderecoErro || undefined}>
                <FieldLabel htmlFor="endereco">Endereço Completo</FieldLabel>
                <Input
                  id="endereco"
                  placeholder="Ex: Rua das Flores, 123 - Centro, São Paulo/SP"
                  value={endereco}
                  onChange={(e) => {
                    setEndereco(e.target.value)
                    if (enderecoErro) setEnderecoErro(false)
                  }}
                  required
                />
                {enderecoErro && (
                  <FieldDescription className="text-destructive">
                    O endereço é obrigatório.
                  </FieldDescription>
                )}
              </Field>

              <div className="flex justify-end gap-3 pt-2">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => router.push('/app/clientes')}
                >
                  Cancelar
                </Button>
                <Button type="submit">Salvar Cliente</Button>
              </div>
            </FieldGroup>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
