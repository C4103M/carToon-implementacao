# CONTEXT.md — Especificação e Alinhamento Frontend <-> Backend

Este documento estabelece o contrato completo, as regras de negócio, a segurança e os fluxos de dados entre a **API Spring Boot (`api`)** e a aplicação **Next.js (`frontend`)** do sistema **CarToon (OficinaPro)**.

Ele serve como a fonte única da verdade para a implementação de todas as telas, eliminando resquícios do protótipo estático/mockado e alinhando com precisão os 3 níveis de usuário (`SUPERADMIN`, `ADMIN`, `MECANICO`) e os Casos de Uso (UC01 a UC09).

---

## 1. Visão Geral de Arquitetura, Autenticação e Sessão

### 1.1 Fluxo de Autenticação e Sessão

A autenticação é baseada em **JWT (JSON Web Token)** transmitido por **Cookie HTTP-Only**, eliminando o armazenamento de tokens em `localStorage` ou `sessionStorage`:

1. **Login (`POST /auth/login`)**:
   - O frontend envia `email` e `password` no corpo da requisição.
   - O backend valida credenciais via BCrypt e verifica tentativas consecutivas via `LoginAttemptService`.
   - Se autenticado com sucesso, a API responde `200 OK` com o cabeçalho:
     ```http
     Set-Cookie: jwt=<token>; Path=/; Max-Age=3600; HttpOnly; SameSite=Lax
     ```
   - O token JWT tem duração de 1 hora e carrega nos claims: `id` (Long), `sub` (email) e `role` (`ROLE_SUPERADMIN`, `ROLE_ADMIN`, `ROLE_MECANICO`).
   - O filtro `JwtCookieFilter` no Spring intercepta requisições subsequentes extraindo o token do cookie `jwt` (ou do cabeçalho `Authorization: Bearer <token>`).

2. **Identificação do Usuário Logado (`GET /usuario/me`)**:
   - Após o login bem-sucedido, o cliente frontend chama `GET /usuario/me` (que é autenticado por qualquer role válida).
   - O backend retorna o DTO `UsuarioResponse` contendo `{ id, nome, email, role, ativo, oficinaId }`.
   - O frontend armazena esse usuário no estado global de sessão (ex: React Context / Zustand) para controlar a renderização condicional de menus, rotas protegidas e preenchimento de `oficinaId`.

3. **Logout (`POST /auth/logout`)**:
   - Remove o cookie invalidando-o imediatamente com `Max-Age=0`:
     ```http
     Set-Cookie: jwt=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax
     ```
   - O frontend limpa o estado de sessão em memória e redireciona o usuário para a tela `/`.

4. **Proteção Contra Força Bruta (Bloqueio de Tentativas)**:
   - O backend bloqueia temporariamente um e-mail após 5 tentativas consecutivas falhadas durante 15 minutos (`BloqueioTentativasException`, HTTP `400 Bad Request` com a mensagem `"Número de tentativas excedido. Conta temporariamente bloqueada por 15 minutos."`).
   - Credenciais inválidas geram erro com mensagem `"Email ou senha incorreto"`.

---

### 1.2 Matriz de Perfis (`Role`), Permissões e Casos de Uso

| Caso de Uso | Descrição | SUPERADMIN | ADMIN (Oficina) | MECANICO | Endpoints Principais |
| :--- | :--- | :---: | :---: | :---: | :--- |
| **UC01** | Autenticação, Sessão e Perfil | :white_check_mark: | :white_check_mark: | :white_check_mark: | `POST /auth/login`, `POST /auth/logout`, `GET /usuario/me`, `PUT /usuario/me` |
| **UC02** | Manter Oficinas e Administradores | :white_check_mark: | :no_entry_sign: *(apenas edita dados da própria oficina)* | :no_entry_sign: | `GET/POST /oficina`, `PUT /oficina/{id}`, `PATCH /oficina/{id}/ativar`, `PATCH /oficina/{id}/desativar`, `POST /usuario/admin`, `GET /usuario?oficinaId={id}` |
| **UC03** | Manter Mecânicos da Oficina | :no_entry_sign: | :white_check_mark: | :no_entry_sign: | `POST /usuario/mecanico`, `GET /usuario`, `GET /usuario/{id}`, `PUT /usuario/{id}`, `DELETE /usuario/{id}` *(desativa)*, `PATCH /usuario/{id}/ativar` |
| **UC04** | Manter Clientes | :no_entry_sign: | :white_check_mark: *(ver nota)* | :white_check_mark: | `POST /clientes`, `GET /clientes`, `GET /clientes/{id}`, `PUT /clientes/{id}`, `DELETE /clientes/{id}` *(inativa)*, `PATCH /clientes/{id}/ativar` |
| **UC05** | Manter Veículos do Cliente | :no_entry_sign: | :white_check_mark: | :white_check_mark: | `POST /veiculos`, `GET /veiculos`, `GET /veiculos/{id}`, `GET /veiculos/placa/{placa}`, `GET /veiculos/cliente/{clienteId}`, `PUT /veiculos/{id}`, `PATCH /veiculos/{id}/inativar`, `PATCH /veiculos/{id}/reativar` |
| **UC06** | Consultar Histórico do Veículo | :white_check_mark: | :white_check_mark: | :white_check_mark: | `GET /veiculos/placa/{placa}/historico`, `GET /veiculos/placa/{placa}/historico/paginado` |
| **UC07** | Manter Serviços (Catálogo) | :white_check_mark: | :white_check_mark: | :white_check_mark: | `POST /servicos`, `GET /servicos`, `GET /servicos/{id}`, `PUT /servicos/{id}`, `DELETE /servicos/{id}` |
| **UC08** | Manter Peças (Catálogo) | :white_check_mark: | :white_check_mark: | :white_check_mark: | `POST /pecas`, `GET /pecas`, `GET /pecas/{id}`, `PUT /pecas/{id}`, `DELETE /pecas/{id}` |
| **UC09** | Gerenciar Ordens de Serviço | :white_check_mark: *(consulta)* | :white_check_mark: | :white_check_mark: | `POST /ordem-servico`, `GET /ordem-servico`, `GET /ordem-servico/{id}`, `POST /ordem-servico/{id}/aceitar`, `POST /ordem-servico/{id}/iniciar`, `POST /ordem-servico/{id}/finalizar`, `POST /ordem-servico/{id}/rejeitar`, `POST/DELETE /ordem-servico/{id}/itens-peca`, `POST/DELETE /ordem-servico/{id}/itens-servico` |

> [!IMPORTANT]
> **Nota de Alinhamento do Backend (`SecurityConfig.java`)**:
> Na linha 40 de `SecurityConfig.java`, a regra atual está configurada como `.requestMatchers("/clientes/**").hasAnyRole("MECANICO")`. Para o correto funcionamento do UC04 por parte do perfil `ADMIN` da oficina, a configuração de segurança do Spring deve autorizar `.hasAnyRole("ADMIN", "MECANICO")`.
> Da mesma forma, as rotas `/veiculos/**` não possuem restrição explícita de role e caem em `.anyRequest().authenticated()`, permitindo o acesso tanto de `ADMIN` quanto de `MECANICO`.

---

## 2. Padrões Globais de Integração Frontend <-> Backend

### 2.1 Tratamento e Formato de Erros (`GlobalExceptionHandler`)

O backend padroniza as respostas de erro HTTP através da classe `GlobalExceptionHandler`. O frontend deve tratar os seguintes formatos:

1. **Erro de Validação de Campos (`400 Bad Request` com `MethodArgumentNotValidException`)**:
   ```json
   {
     "timestamp": "2026-10-21T15:30:00.000",
     "status": 400,
     "error": "Bad Request",
     "message": "Erro de validação nos campos",
     "errosValidacao": {
       "email": "E-mail inválido",
       "senha": "A senha deve ter no mínimo 8 caracteres"
     }
   }
   ```
   *Comportamento de UI*: Os erros mapeados em `errosValidacao` devem ser destacados diretamente sob os respectivos campos (`FieldDescription className="text-destructive"`).

2. **Violações de Regra de Negócio (`400 Bad Request` com `RegraDeNegocioException`)**:
   ```json
   {
     "timestamp": "2026-10-21T15:30:00.000",
     "status": 400,
     "error": "Bad Request",
     "message": "Veículo sem valor FIPE cadastrado: não é possível calcular o serviço."
   }
   ```

3. **Conflito de Dados / Bloqueio de Operação (`409 Conflict` com `ConflitoException` ou `DataIntegrityViolationException`)**:
   ```json
   {
     "timestamp": "2026-10-21T15:30:00.000",
     "status": 409,
     "error": "Conflict",
     "message": "Não é possível desativar: a oficina possui ordens de serviço em aberto."
   }
   ```
   *Exemplos de violações de chave única mapeadas*:
   - `"Já existe uma oficina com esse nome."`
   - `"Já existe uma oficina com esse telefone."`
   - `"Já existe um usuário com esse e-mail."`
   - `"Já existe um veículo cadastrado com esta placa no sistema."`
   - `"Cliente já cadastrado com este CPF."`

4. **Recurso Não Encontrado (`404 Not Found`)**:
   ```json
   {
     "timestamp": "2026-10-21T15:30:00.000",
     "status": 404,
     "error": "Not Found",
     "message": "Oficina não encontrada com ID: 99"
   }
   ```

5. **Acesso Negado (`403 Forbidden` com `AccessDeniedException`)**:
   ```json
   {
     "timestamp": "2026-10-21T15:30:00.000",
     "status": 403,
     "error": "Forbidden",
     "message": "Você só pode ver usuários da sua oficina."
   }
   ```

---

### 2.2 Paginação Spring Data (`Pageable` & `Page<T>`)

Os endpoints de listagem paginada (`/ordem-servico`, `/pecas`, `/servicos`, `/veiculos`) utilizam a paginação padrão do Spring:

- **Query Parameters**:
  - `page`: Número da página baseado em índice zero (`0` para a 1ª página).
  - `size`: Quantidade de itens por página (ex: `10`, `20`).
  - `sort`: Campo e direção de ordenação (ex: `sort=id,desc` ou `sort=nome,asc`).
- **Formato da Resposta (`Page<T>`)**:
  ```json
  {
    "content": [ ... ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "offset": 0
    },
    "totalElements": 45,
    "totalPages": 5,
    "last": false,
    "first": true,
    "size": 10,
    "number": 0,
    "empty": false
  }
  ```

---

### 2.3 Descarte do Protótipo Legado (O que NÃO existe mais)

Ao refatorar e construir as páginas, os seguintes itens do código protótipo antigo em `@/lib/...` devem ser **completamente eliminados**:

1. **`lambda` no Veículo e `fipeConsultadoEm`**: O `lambda` (ou `taxaRisco`) **não pertence ao veículo** (e sim à `Oficina`, configurado pelo `ADMIN` ou `SUPERADMIN`, com valor padrão `0.05` ou 5%). O valor unitário de serviço na Ordem de Serviço é calculado dinamicamente pelo backend como:
   $$\text{valorUnitarioServico} = \text{Horas} \times \text{ValorHora} \times \left(1 + \frac{\text{FIPE}}{100.000} \times \text{TaxaRisco} \times \text{PesoServico}\right)$$
2. **Peças embutidas em Serviços (`pecaIds`)**: O catálogo de Serviços e o catálogo de Peças são totalmente independentes. Não existe lista de peças pré-fixadas dentro de um serviço.
3. **Substituição de Veículo ("Swap")**: Não existe fluxo de "substituir veículo com preservação de histórico". Veículos são entidades independentes vinculadas ao cliente (`clienteId`) com status ativo/inativo.
4. **Status de Oficina com 3 estados (`ativa`, `pendente`, `suspensa`)**: A oficina no backend possui apenas o campo booleano `ativo` (`true`/`false`). A aprovação/ativação e desativação usam `PATCH /oficina/{id}/ativar` e `PATCH /oficina/{id}/desativar`.
5. **Ciclo de status de Orçamento simplificado**: O modelo do backend possui 5 estados explícitos: `PENDENTE`, `ACEITO`, `REJEITADO`, `EM_ANDAMENTO`, `FINALIZADO`.

---

## 3. Especificação Página por Página do Frontend

---

### 3.1 Rota `/` — Login (UC01)

- **Caso de Uso**: UC01 - Autenticação
- **Roles Permitidas**: Público (qualquer usuário não autenticado). Se já autenticado, redirecionar para `/superadmin` (se `SUPERADMIN`) ou `/app` (se `ADMIN`/`MECANICO`).
- **Endpoints Consumidos**:
  | Método | Endpoint | Auth | Descrição |
  | :--- | :--- | :---: | :--- |
  | `POST` | `/auth/login` | Não | Realiza autenticação com e-mail e senha, gravando cookie `jwt`. |
  | `GET` | `/usuario/me` | Sim (Cookie) | Obtém os dados do usuário logado para direcionar a rota e salvar na sessão. |

- **Contratos de Dados**:
  - **Request Body (`POST /auth/login`)**:
    ```json
    {
      "email": "admin@oficinapro.com",
      "password": "senhaSegura123"
    }
    ```
    *Validações*:
    - `email`: NotBlank, formato válido de e-mail.
    - `password`: NotBlank. *(Atenção: o campo no DTO se chama `password`, e não `senha`)*.
  - **Response (`POST /auth/login`)**: Status `200 OK` (sem corpo, cookie no header `Set-Cookie`).
  - **Response (`GET /usuario/me`)**:
    ```json
    {
      "id": 1,
      "nome": "Carlos Administrador",
      "email": "carlos@oficina.com",
      "role": "ADMIN",
      "ativo": true,
      "oficinaId": 10
    }
    ```

- **Regras de Negócio e Comportamento de UI**:
  - Exibir formulário de e-mail e senha.
  - O botão de envio exibe indicador de carregamento (`Spinner`) enquanto aguarda a resposta.
  - Ao obter sucesso em `/auth/login`, chamar imediatamente `/usuario/me`:
    - Se `role === "SUPERADMIN"` -> `router.push('/superadmin')`.
    - Se `role === "ADMIN"` ou `role === "MECANICO"` -> `router.push('/app')`.
  - Remover os botões fixos de demonstração do protótipo com senhas falsas ou substituí-los por preenchimento de teste funcional conectado ao banco de dados com senha real.
  - Tratamento de Erros:
    - Status `400` com mensagem de bloqueio (`BloqueioTentativasException`): Exibir alerta crítico informando o bloqueio por 15 minutos.
    - Status `400`/`401`/`500` com `"Email ou senha incorreto"`: Exibir mensagem de credenciais inválidas.

---

### 3.2 Rota `/superadmin` (antiga `/admin`) — Listagem e Cadastro de Oficinas (UC02)

- **Caso de Uso**: UC02 - Manter Oficinas
- **Roles Permitidas**: `SUPERADMIN`
- **Shell**: `SuperAdminShell` (antigo `AdminShell`)
- **Endpoints Consumidos**:
  | Método | Endpoint | Auth | Descrição |
  | :--- | :--- | :---: | :--- |
  | `GET` | `/oficina` | `SUPERADMIN` | Lista todas as oficinas ativas. |
  | `POST` | `/oficina` | `SUPERADMIN` | Cadastra uma nova oficina. |

- **Contratos de Dados**:
  - **Request Body (`POST /oficina`)**:
    ```json
    {
      "nome": "Auto Mecânica Precision",
      "endereco": "Av. das Indúustrías, 1500, São Paulo - SP",
      "telefone": "(11) 98765-4321",
      "lambda": 0.01
    }
    ```
    *Validações*:
    - `nome`: NotBlank, max 250 caracteres.
    - `endereco`: NotBlank, max 250 caracteres.
    - `telefone`: NotBlank, regex `^(\(\d{2}\)|\d{2})[\s-]?\d{4,5}[\s-]?\d{4}$`.
    - `lambda`: Opcional na criação (default `0.01`, equivalente a 1% sobre a FIPE; deve ser `>= 0`).
  - **Response (`OficinaResponse`)**:
    ```json
    {
      "id": 10,
      "nome": "Auto Mecânica Precision",
      "endereco": "Av. das Indústrias, 1500, São Paulo - SP",
      "telefone": "(11) 98765-4321",
      "lambda": 0.01,
      "ativo": true
    }
    ```

- **Regras de Negócio e Comportamento de UI**:
  - Exibir botão "+ Nova Oficina" que abre modal com campos: Nome, Endereço e Telefone (com máscara de telefone brasileiro).
  - Tabela listando as oficinas retornadas por `GET /oficina`:
    - Colunas: Nome, Telefone, Endereço, Status (`Ativa`/`Inativa`) e Botão de Ação "Ver Detalhes".
    - Ao clicar em uma oficina, navega para `/superadmin/oficinas/[id]`.
  - Filtro de busca local por nome ou telefone.
  - Tratamento de Erros:
    - `409 Conflict`: Tratar duplicidade de nome (`"Já existe uma oficina com esse nome."`) ou telefone (`"Já existe uma oficina com esse telefone."`).

---

### 3.3 Rota `/superadmin/oficinas/[id]` (antiga `/admin/oficinas/[id]`) — Detalhes, Edição de Oficina e Gestão de Administradores (UC02)

- **Caso de Uso**: UC02 - Manter Oficinas e Usuários Administradores
- **Roles Permitidas**: `SUPERADMIN`
- **Shell**: `SuperAdminShell` (antigo `AdminShell`)
- **Endpoints Consumidos**:
  | Método | Endpoint | Auth | Descrição |
  | :--- | :--- | :---: | :--- |
  | `GET` | `/oficina/{id}` | `SUPERADMIN` | Obtém os dados cadastrais da oficina. |
  | `PUT` | `/oficina/{id}` | `SUPERADMIN` | Atualiza dados (nome, endereço, telefone) da oficina. |
  | `PATCH` | `/oficina/{id}/desativar` | `SUPERADMIN` | Desativa a oficina (se não houver OS em aberto). |
  | `PATCH` | `/oficina/{id}/ativar` | `SUPERADMIN` | Reativa a oficina desativada. |
  | `GET` | `/usuario?oficinaId={id}` | `SUPERADMIN` | Lista os usuários vinculados à oficina informada. |
  | `POST` | `/usuario/admin` | `SUPERADMIN` | Cria um usuário com perfil `ADMIN` para a oficina. |
  | `PUT` | `/usuario/{usuarioId}` | `SUPERADMIN` | Atualiza dados do administrador da oficina. |
  | `DELETE` | `/usuario/{usuarioId}` | `SUPERADMIN` | Desativa o usuário administrador da oficina. |
  | `PATCH` | `/usuario/{usuarioId}/ativar` | `SUPERADMIN` | Reativa o usuário administrador da oficina. |

- **Contratos de Dados**:
  - **Request Body (`PUT /oficina/{id}`)**: Idêntico ao `POST /oficina` (`OficinaRequest`).
  - **Request Body (`POST /usuario/admin`)**:
    ```json
    {
      "nome": "Marcos Silva",
      "email": "marcos@autoprecision.com",
      "senha": "senhaForteAdmin123",
      "oficinaId": 10
    }
    ```
    *Validações*:
    - `nome`: NotBlank.
    - `email`: NotBlank, formato de e-mail válido.
    - `senha`: NotBlank, mínimo de 8 caracteres.
    - `oficinaId`: NotNull.
  - **Response (`UsuarioResponse`)**:
    ```json
    {
      "id": 5,
      "nome": "Marcos Silva",
      "email": "marcos@autoprecision.com",
      "role": "ADMIN",
      "ativo": true,
      "oficinaId": 10
    }
    ```

- **Regras de Negócio e Comportamento de UI**:
  - **Card de Dados da Oficina**:
    - Exibe Nome, Endereço, Telefone e Status (Ativa/Inativa).
    - Botão "Editar Oficina": Permite alterar nome, endereço e telefone via `PUT /oficina/{id}`.
    - Botão "Desativar Oficina" (se ativa) / "Ativar Oficina" (se inativa):
      - Se o usuário tentar desativar e a oficina tiver ordens de serviço pendentes/em andamento, o backend retorna `409 Conflict` com `"Não é possível desativar: a oficina possui ordens de serviço em aberto."`. A UI deve capturar essa mensagem e exibi-la via toast ou alert.
  - **Card / Seção de Administradores da Oficina**:
    - Tabela listando os administradores da oficina (filtrando por `role === 'ADMIN'`).
    - Botão "+ Novo Administrador": Abre modal com campos Nome, E-mail e Senha. Ao submeter, envia `POST /usuario/admin` com o `oficinaId` da página.
    - Ações em cada administrador: Editar (Nome, E-mail, Senha opcional) e Desativar/Ativar.
  - *Remover do protótipo*: Badge e ações de aprovação "pendente/suspensa" (a oficina agora é ativa ou inativa).

---

### 3.4 Rota `/app` — Dashboard da Oficina (UC09)

- **Caso de Uso**: UC09 - Gerenciamento e Visão Operacional de Ordens de Serviço
- **Roles Permitidas**: `ADMIN`, `MECANICO`
- **Shell**: `AppShell`
- **Endpoints Consumidos**:
  | Método | Endpoint | Auth | Descrição |
  | :--- | :--- | :---: | :--- |
  | `GET` | `/ordem-servico?oficinaId={id}&statusServico=PENDENTE` | `ADMIN`, `MECANICO` | Lista ordens de serviço com status `PENDENTE`. |
  | `GET` | `/ordem-servico?oficinaId={id}&statusServico=EM_ANDAMENTO` | `ADMIN`, `MECANICO` | Lista ordens em execução. |
  | `GET` | `/oficina/{id}` | `ADMIN`, `MECANICO` | Obtém os dados da oficina logada (nome, telefone). |

- **Regras de Negócio e Comportamento de UI**:
  - **Cards de Métricas**:
    - Quantidade de Orçamentos Pendentes (aguardando aprovação).
    - Quantidade de Ordens em Andamento.
    - Acesso rápido para "Novo Orçamento" (`/app/orcamentos/novo`) e "Novo Cliente" (`/app/clientes/novo`).
  - **Tabela de Orçamentos Recentes / Pendentes**:
    - Exibe as ordens mais recentes com placa, mecânico responsável, data de abertura, valor total e status.
    - Clicar na linha direciona para `/app/orcamentos/[id]`.

---

### 3.5 Rota `/app/mecanicos` — Gestão de Mecânicos da Oficina (UC03) **[NOVA PÁGINA]**

- **Caso de Uso**: UC03 - Manter Mecânicos da Oficina
- **Roles Permitidas**: `ADMIN` *(Usuários com role `MECANICO` não devem ter acesso; o link deve ser ocultado no sidebar e redirecionar para `/app` se tentarem acessar a URL direta)*.
- **Shell**: `AppShell`
- **Endpoints Consumidos**:
  | Método | Endpoint | Auth | Descrição |
  | :--- | :--- | :---: | :--- |
  | `GET` | `/usuario` | `ADMIN` | Lista os usuários da oficina do Admin logado. |
  | `POST` | `/usuario/mecanico` | `ADMIN` | Cadastra um novo mecânico na oficina do Admin. |
  | `PUT` | `/usuario/{id}` | `ADMIN` | Atualiza nome, e-mail e opcionalmente a senha do mecânico. |
  | `DELETE` | `/usuario/{id}` | `ADMIN` | Desativa o mecânico (soft delete). |
  | `PATCH` | `/usuario/{id}/ativar` | `ADMIN` | Reativa um mecânico desativado. |

- **Contratos de Dados**:
  - **Request Body (`POST /usuario/mecanico`)**:
    ```json
    {
      "nome": "Pedro Mecânico",
      "email": "pedro@oficina.com",
      "senha": "senhaSegura123"
    }
    ```
    *Validações*:
    - `nome`: NotBlank.
    - `email`: NotBlank, formato de e-mail válido.
    - `senha`: NotBlank, mínimo de 8 caracteres.
    *(Nota: `oficinaId` não é enviado no body; o backend vincula automaticamente à oficina do ADMIN logado)*.
  - **Request Body (`PUT /usuario/{id}`)**:
    ```json
    {
      "nome": "Pedro Mecânico Atualizado",
      "email": "pedro.novo@oficina.com",
      "senha": "novaSenhaOpcional123"
    }
    ```
    *Validações*:
    - `nome`: NotBlank.
    - `email`: NotBlank, e-mail válido.
    - `senha`: Opcional; se informada, deve ter no mínimo 8 caracteres.
  - **Response (`UsuarioResponse`)**:
    ```json
    {
      "id": 12,
      "nome": "Pedro Mecânico",
      "email": "pedro@oficina.com",
      "role": "MECANICO",
      "ativo": true,
      "oficinaId": 10
    }
    ```

- **Regras de Negócio e Comportamento de UI**:
  - Tabela com lista de mecânicos (`role === 'MECANICO'`).
  - Colunas: Nome, E-mail, Status (Ativo / Inativo), Ações (Editar, Desativar / Reativar).
  - Botão "+ Novo Mecânico" abre modal com campos: Nome, E-mail e Senha.
  - Ação de Desativação (`DELETE /usuario/{id}`):
    - Se o mecânico possuir ordens de serviço pendentes, aceitas ou em andamento, o backend lança `409 Conflict` com:
      `"Não é possível desativar: o mecânico possui ordens de serviço pendentes ou em andamento."`
    - A UI deve capturar esse erro e alertar o usuário para que transfira ou conclua as OS antes de desativar o profissional.
  - O Admin não pode desativar a si próprio através desta tela (o backend lança `409 Conflict`).

---

### 3.6 Rota `/app/clientes` — Listagem de Clientes (UC04)

- **Caso de Uso**: UC04 - Manter Clientes
- **Roles Permitidas**: `ADMIN`, `MECANICO`
- **Shell**: `AppShell`
- **Endpoints Consumidos**:
  | Método | Endpoint | Auth | Descrição |
  | :--- | :--- | :---: | :--- |
  | `GET` | `/clientes` | `ADMIN`, `MECANICO` | Retorna a lista de todos os clientes ativos. |

- **Contratos de Dados**:
  - **Response (`List<ClienteResponse>`)**:
    ```json
    [
      {
        "id": 1,
        "nome": "Ana Souza",
        "cpf": "12345678901",
        "telefone": "(11) 98765-4321",
        "endereco": "Rua das Flores, 123",
        "oficinaId": 10,
        "ativo": true
      }
    ]
    ```

- **Regras de Negócio e Comportamento de UI**:
  - Exibe campo de busca por Nome ou CPF (com normalização de dígitos).
  - Tabela listando: Nome, CPF formatado (`000.000.000-00`), Telefone e Endereço.
  - Botão "+ Novo Cliente" que direciona para `/app/clientes/novo`.
  - Clicar na linha do cliente direciona para `/app/clientes/[id]`.

---

### 3.7 Rota `/app/clientes/novo` — Cadastro de Cliente (UC04)

- **Caso de Uso**: UC04 - Manter Clientes
- **Roles Permitidas**: `ADMIN`, `MECANICO`
- **Shell**: `AppShell`
- **Endpoints Consumidos**:
  | Método | Endpoint | Auth | Descrição |
  | :--- | :--- | :---: | :--- |
  | `POST` | `/clientes` | `ADMIN`, `MECANICO` | Cria um novo cliente associado à oficina. |

- **Contratos de Dados**:
  - **Request Body (`ClienteRequest`)**:
    ```json
    {
      "nome": "Ana Souza",
      "cpf": "12345678901",
      "telefone": "(11) 98765-4321",
      "endereco": "Rua das Flores, 123",
      "oficinaId": 10
    }
    ```
    *Validações*:
    - `nome`: NotBlank.
    - `cpf`: NotBlank, regex `\d{11}` (apenas os 11 números, sem máscara).
    - `telefone`: NotBlank. *(Atenção: campo obrigatório que estava ausente no protótipo legado)*.
    - `endereco`: NotBlank.
    - `oficinaId`: NotNull (injetado automaticamente a partir de `user.oficinaId`).
  - **Response**: `201 Created` com `ClienteResponse`.

- **Regras de Negócio e Comportamento de UI**:
  - Formulário com campos: Nome Completo, CPF (com máscara de UI, mas enviando limpo para a API), Telefone e Endereço.
  - Ao salvar com sucesso, exibe toast e redireciona para `/app/clientes/[id]` ou `/app/clientes`.
  - Tratamento de Erros:
    - Se o CPF já estiver cadastrado, a API retorna `409 Conflict` com `"Cliente já cadastrado com este CPF."`. Exibir erro destacado no campo de CPF.

---

### 3.8 Rota `/app/clientes/[id]` — Edição de Cliente e Gestão de Veículos (UC04 e UC05)

- **Caso de Uso**: UC04 (Edição/Inativação de Cliente) e UC05 (Manter Veículos do Cliente)
- **Roles Permitidas**: `ADMIN`, `MECANICO`
- **Shell**: `AppShell`
- **Endpoints Consumidos**:
  | Método | Endpoint | Auth | Descrição |
  | :--- | :--- | :---: | :--- |
  | `GET` | `/clientes/{id}` | `ADMIN`, `MECANICO` | Obtém detalhes do cliente. |
  | `PUT` | `/clientes/{id}` | `ADMIN`, `MECANICO` | Atualiza dados do cliente. |
  | `DELETE` | `/clientes/{id}` | `ADMIN`, `MECANICO` | Inativa o cliente. |
  | `PATCH` | `/clientes/{id}/ativar` | `ADMIN`, `MECANICO` | Reativa o cliente inativo. |
  | `GET` | `/veiculos/cliente/{clienteId}` | `ADMIN`, `MECANICO` | Lista todos os veículos vinculados ao cliente. |
  | `POST` | `/veiculos` | `ADMIN`, `MECANICO` | Cadastra um novo veículo para o cliente. |
  | `PUT` | `/veiculos/{veiculoId}` | `ADMIN`, `MECANICO` | Atualiza os dados de um veículo. |
  | `PATCH` | `/veiculos/{veiculoId}/inativar` | `ADMIN`, `MECANICO` | Inativa o veículo (soft delete preservando OS). |
  | `PATCH` | `/veiculos/{veiculoId}/reativar` | `ADMIN`, `MECANICO` | Reativa o veículo. |

- **Contratos de Dados**:
  - **Request Body Veículo (`POST /veiculos`)**:
    ```json
    {
      "clienteId": 1,
      "placa": "ABC1D23",
      "montadora": "Volkswagen",
      "modelo": "Gol 1.6 MSI TotalFlex",
      "ano": 2021
    }
    ```
    *Validações*:
    - `clienteId`: NotNull.
    - `placa`: NotBlank, regex `^([A-Za-z]{3}-?[0-9]{4}|[A-Za-z]{3}-?[0-9][A-Za-z][0-9]{2})$` (padrão antigo ou Mercosul).
    - `montadora`: NotBlank.
    - `modelo`: NotBlank.
    - `ano`: NotNull, mínimo 1900.
  - **Response Veículo (`VeiculoResponse`)**:
    ```json
    {
      "id": 8,
      "placa": "ABC1D23",
      "modelo": "Gol 1.6 MSI TotalFlex",
      "ano": 2021,
      "montadora": "Volkswagen",
      "valorFipe": 58400.0,
      "ativo": true,
      "clienteId": 1,
      "clienteNome": "Ana Souza",
      "clienteCpf": "12345678901"
    }
    ```

- **Regras de Negócio e Comportamento de UI**:
  - **Painel do Cliente**:
    - Formulário para editar Nome, CPF, Telefone e Endereço.
    - Botão "Inativar Cliente" (`DELETE /clientes/{id}`) ou "Reativar Cliente" (`PATCH /clientes/{id}/ativar`). Se o cliente estiver inativo, não permitir cadastrar veículos para ele (a API retorna `400 Bad Request` com `ClienteInativoException`).
  - **Seção de Veículos do Cliente**:
    - Tabela listando os veículos do cliente com: Placa, Montadora, Modelo, Ano, Valor FIPE (formatado em R$), Status (Ativo / Inativo) e Ações.
    - Botão "+ Adicionar Veículo": Abre modal para informar Placa, Montadora, Modelo e Ano.
    - Ações do Veículo:
      - "Ver Histórico": Redireciona para `/app/veiculos/[placa]/historico`.
      - "Editar Veículo": Modal com `PUT /veiculos/{id}`.
      - "Inativar" / "Reativar": Alterna status do veículo via `PATCH`.
  - *Remover do protótipo*: Ação "Trocar Veículo" / "Swap" com vinculação de veículo substituto e cálculo artificial de `lambda`.

---

### 3.9 Rota `/app/veiculos/[placa]/historico` — Histórico de Manutenção do Veículo (UC06)

*(Nota: aceita rota por placa `/app/veiculos/[placa]/historico` ou por id `/app/veiculos/[id]/historico` mapeando para a placa)*

- **Caso de Uso**: UC06 - Consultar Histórico de Manutenção do Veículo
- **Roles Permitidas**: `ADMIN`, `MECANICO`
- **Shell**: `AppShell`
- **Endpoints Consumidos**:
  | Método | Endpoint | Auth | Descrição |
  | :--- | :--- | :---: | :--- |
  | `GET` | `/veiculos/placa/{placa}/historico` | `ADMIN`, `MECANICO` | Retorna o resumo do veículo e a lista cronológica de ordens de serviço. |
  | `GET` | `/veiculos/placa/{placa}/historico/paginado` | `ADMIN`, `MECANICO` | Versão paginada para veículos com grande volume de ordens. |

- **Contratos de Dados**:
  - **Response (`HistoricoVeiculoResponse`)**:
    ```json
    {
      "veiculo": {
        "id": 8,
        "placa": "ABC1D23",
        "modelo": "Gol 1.6 MSI TotalFlex",
        "montadora": "Volkswagen",
        "ano": 2021,
        "valorFipe": 58400.0,
        "ativo": true,
        "clienteNome": "Ana Souza"
      },
      "ordensServico": [
        {
          "id": 101,
          "dataOrcamento": "2026-10-15",
          "dataInicio": "2026-10-16",
          "dataFinalizacao": "2026-10-17",
          "dataRejeicao": null,
          "statusServico": "FINALIZADO",
          "descricao": "Revisão geral de freios e troca de óleo",
          "placa": "ABC1D23",
          "mecanicoNome": "Pedro Mecânico",
          "oficinaNome": "Auto Precision",
          "valorTotal": 850.00
        }
      ],
      "totalOrdens": 1
    }
    ```

- **Regras de Negócio e Comportamento de UI**:
  - **Ficha do Veículo**: Placa, Montadora, Modelo, Ano, Proprietário (`clienteNome`), Status e Valor FIPE de referência.
  - **Linha do Tempo / Lista de Ordens de Serviço**:
    - Ordenadas da mais recente para a mais antiga.
    - Cada item exibe: Código da OS, Data de Abertura, Mecânico Responsável, Status (`PENDENTE`, `ACEITO`, `EM_ANDAMENTO`, `FINALIZADO`, `REJEITADO`), Descrição e Valor Total.
    - Clicar na OS abre os detalhes em `/app/orcamentos/[id]`.
  - *Remover do protótipo*: Campo `lambda` e data de consulta manual de FIPE.

---

### 3.10 Rota `/app/catalogo` — Gestão de Serviços (UC07) e Peças (UC08)

- **Caso de Uso**: UC07 (Manter Serviços) e UC08 (Manter Peças)
- **Roles Permitidas**: `ADMIN`, `MECANICO`
- **Shell**: `AppShell`
- **Endpoints Consumidos**:
  | Método | Endpoint | Auth | Descrição |
  | :--- | :--- | :---: | :--- |
  | `GET` | `/servicos?nome={termo}&page=0&size=10` | `ADMIN`, `MECANICO` | Lista serviços cadastrados com busca opcional por nome. |
  | `POST` | `/servicos` | `ADMIN`, `MECANICO` | Cadastra um novo serviço. |
  | `PUT` | `/servicos/{id}` | `ADMIN`, `MECANICO` | Atualiza um serviço existente. |
  | `DELETE` | `/servicos/{id}` | `ADMIN`, `MECANICO` | Exclui um serviço. |
  | `GET` | `/pecas?busca={termo}&page=0&size=10` | `ADMIN`, `MECANICO` | Lista peças cadastradas com filtro opcional. |
  | `POST` | `/pecas` | `ADMIN`, `MECANICO` | Cadastra uma nova peça. |
  | `PUT` | `/pecas/{id}` | `ADMIN`, `MECANICO` | Atualiza uma peça existente. |
  | `DELETE` | `/pecas/{id}` | `ADMIN`, `MECANICO` | Exclui uma peça. |

- **Contratos de Dados**:
  - **Request Body Serviço (`POST /servicos` e `PUT /servicos/{id}`)**:
    ```json
    {
      "nome": "Troca de Pastilha de Freio",
      "valorBase": 150.00,
      "descricao": "Substituição completa do jogo de pastilhas dianteiras",
      "tempoEstimado": "01:30:00"
    }
    ```
    *Validações*:
    - `nome`: NotBlank.
    - `valorBase`: NotNull, positivo (`> 0`).
    - `descricao`: Texto opcional.
    - `tempoEstimado`: Formato `HH:mm:ss` (opcional).
  - **Response Serviço (`ServicoResponse`)**:
    ```json
    {
      "id": 1,
      "nome": "Troca de Pastilha de Freio",
      "valorBase": 150.00,
      "descricao": "Substituição completa do jogo de pastilhas dianteiras",
      "tempoEstimado": "01:30:00"
    }
    ```
  - **Request Body Peça (`POST /pecas` e `PUT /pecas/{id}`)**:
    ```json
    {
      "nome": "Pastilha de Freio Cerâmica Dianteira",
      "fabricante": "Bosch",
      "valorBase": 180.50
    }
    ```
    *Validações*:
    - `nome`: NotBlank.
    - `fabricante`: NotBlank.
    - `valorBase`: NotNull, positivo (`> 0`).
  - **Response Peça (`PecaResponse`)**:
    ```json
    {
      "id": 2,
      "nome": "Pastilha de Freio Cerâmica Dianteira",
      "fabricante": "Bosch",
      "valorBase": 180.50
    }
    ```

- **Regras de Negócio e Comportamento de UI**:
  - A tela é dividida em duas Abas: **Serviços** e **Peças**.
  - **Aba Serviços**:
    - Tabela com: Nome, Descrição, Tempo Estimado, Valor Base (R$) e Botões de Editar/Excluir.
    - Botão "+ Novo Serviço" abre modal para criar serviço.
  - **Aba Peças**:
    - Tabela com: Nome, Fabricante, Valor Base (R$) e Botões de Editar/Excluir.
    - Botão "+ Nova Peça" abre modal para criar peça.
  - *Remover do protótipo*: A vinculação fixa de "peças que compõem o serviço" no modal de serviço (`servicoPecaIds`). Serviços e peças são independentes no catálogo.

---

### 3.11 Rota `/app/orcamentos` — Listagem de Ordens de Serviço (UC09)

- **Caso de Uso**: UC09 - Gerenciamento de Ordens de Serviço
- **Roles Permitidas**: `ADMIN`, `MECANICO`
- **Shell**: `AppShell`
- **Endpoints Consumidos**:
  | Método | Endpoint | Auth | Descrição |
  | :--- | :--- | :---: | :--- |
  | `GET` | `/ordem-servico?oficinaId={id}&placa={placa}&statusServico={status}&page=0&size=10` | `ADMIN`, `MECANICO` | Lista paginada de ordens de serviço com filtros. |

- **Contratos de Dados**:
  - **Query Params aceitos (`OrdemServicoFiltro`)**:
    - `oficinaId`: Integer (injetado a partir de `user.oficinaId`).
    - `veiculoId`: Integer (opcional).
    - `placa`: String (opcional).
    - `mecanicoId`: Integer (opcional).
    - `statusServico`: `PENDENTE`, `ACEITO`, `REJEITADO`, `EM_ANDAMENTO`, `FINALIZADO` (opcional).
  - **Response (`Page<OrdemServicoResumo>`)**:
    ```json
    {
      "content": [
        {
          "id": 101,
          "dataOrcamento": "2026-10-15",
          "dataInicio": "2026-10-16",
          "dataFinalizacao": null,
          "dataRejeicao": null,
          "statusServico": "EM_ANDAMENTO",
          "descricao": "Revisão e troca de correia",
          "placa": "ABC1D23",
          "mecanicoNome": "Pedro Mecânico",
          "oficinaNome": "Auto Precision",
          "valorTotal": 1250.00
        }
      ],
      "totalElements": 25,
      "totalPages": 3,
      "number": 0,
      "size": 10
    }
    ```

- **Regras de Negócio e Comportamento de UI**:
  - Filtros no topo:
    - Busca por Placa.
    - Dropdown de Status: Todos, Pendente, Aceito, Em Andamento, Finalizado, Rejeitado.
  - Tabela listando: ID da OS, Placa do Veículo, Mecânico, Data de Orçamento, Status (com badges de cores semânticas) e Valor Total (R$).
  - Paginação na base da tabela (`Anterior`, `Próxima`, números de página).
  - Botão "+ Novo Orçamento" redireciona para `/app/orcamentos/novo`.
  - Clicar na linha redireciona para `/app/orcamentos/[id]`.

---

### 3.12 Rota `/app/orcamentos/novo` — Abertura de Ordem de Serviço (UC09)

- **Caso de Uso**: UC09 - Abertura de Ordem de Serviço
- **Roles Permitidas**: `ADMIN`, `MECANICO`
- **Shell**: `AppShell`
- **Endpoints Consumidos**:
  | Método | Endpoint | Auth | Descrição |
  | :--- | :--- | :---: | :--- |
  | `GET` | `/clientes` | `ADMIN`, `MECANICO` | Lista clientes ativos da oficina. |
  | `GET` | `/veiculos/cliente/{clienteId}` | `ADMIN`, `MECANICO` | Lista veículos ativos do cliente selecionado. |
  | `GET` | `/usuario` | `ADMIN`, `MECANICO` | Lista mecânicos da oficina para seleção de responsável. |
  | `POST` | `/ordem-servico` | `ADMIN`, `MECANICO` | Cria a Ordem de Serviço em estado `PENDENTE`. |

- **Contratos de Dados**:
  - **Request Body (`POST /ordem-servico`)**:
    ```json
    {
      "veiculoId": 8,
      "oficinaId": 10,
      "mecanicoId": 12,
      "descricao": "Cliente relata ruído agudo ao frear e vibração no pedal."
    }
    ```
    *Validações*:
    - `veiculoId`: NotNull.
    - `oficinaId`: NotNull (injetado a partir de `user.oficinaId`).
    - `mecanicoId`: NotNull (se o usuário logado for `MECANICO`, pré-seleciona seu próprio ID; se for `ADMIN`, seleciona o mecânico responsável).
    - `descricao`: String com relato inicial do problema.
  - **Response (`OrdemServicoResponse`)**: Retorna a OS criada com `status = "PENDENTE"`, `itensPeca = []`, `itensServico = []`, `total = 0.0`.

- **Regras de Negócio e Comportamento de UI**:
  - **Passo 1: Selecionar Cliente**: Select/Combobox com busca.
  - **Passo 2: Selecionar Veículo Ativo**: Dropdown carregado após selecionar o cliente. Se o cliente não possuir veículos ativos, exibir aviso sugerindo cadastrar um veículo antes.
  - **Passo 3: Selecionar Mecânico Responsável**: Dropdown com os profissionais da oficina.
  - **Passo 4: Descrição do Problema / Diagnóstico**: Campo textarea para observações do cliente/oficina.
  - Ao submeter, a API responde `201 Created` e a UI redireciona imediatamente para `/app/orcamentos/[id]` para montagem dos itens de peças e serviços.

---

### 3.13 Rota `/app/orcamentos/[id]` — Detalhe da OS, Ciclo de Vida e Composição de Itens (UC09)

- **Caso de Uso**: UC09 - Gerenciamento de Itens e Ciclo de Vida da Ordem de Serviço
- **Roles Permitidas**: `ADMIN`, `MECANICO`
- **Shell**: `AppShell`
- **Endpoints Consumidos**:
  | Método | Endpoint | Auth | Descrição |
  | :--- | :--- | :---: | :--- |
  | `GET` | `/ordem-servico/{id}` | `ADMIN`, `MECANICO` | Obtém os detalhes completos da OS, seus itens e total. |
  | `POST` | `/ordem-servico/{id}/aceitar` | `ADMIN`, `MECANICO` | Transiciona o status para `ACEITO`. |
  | `POST` | `/ordem-servico/{id}/iniciar` | `ADMIN`, `MECANICO` | Transiciona o status para `EM_ANDAMENTO` (grava `dataInicio`). |
  | `POST` | `/ordem-servico/{id}/finalizar` | `ADMIN`, `MECANICO` | Transiciona o status para `FINALIZADO` (grava `dataFinalizacao`). |
  | `POST` | `/ordem-servico/{id}/rejeitar` | `ADMIN`, `MECANICO` | Transiciona o status para `REJEITADO` (grava `dataRejeicao`). |
  | `POST` | `/ordem-servico/{id}/itens-peca` | `ADMIN`, `MECANICO` | Adiciona um item de peça à OS. |
  | `DELETE` | `/ordem-servico/{id}/itens-peca/{itemId}` | `ADMIN`, `MECANICO` | Remove um item de peça da OS. |
  | `POST` | `/ordem-servico/{id}/itens-servico` | `ADMIN`, `MECANICO` | Adiciona um item de serviço à OS. |
  | `DELETE` | `/ordem-servico/{id}/itens-servico/{itemId}` | `ADMIN`, `MECANICO` | Remove um item de serviço da OS. |
  | `GET` | `/pecas` | `ADMIN`, `MECANICO` | Lista peças do catálogo para seleção. |
  | `GET` | `/servicos` | `ADMIN`, `MECANICO` | Lista serviços do catálogo para seleção. |

- **Contratos de Dados**:
  - **Request Body Adicionar Peça (`POST /ordem-servico/{id}/itens-peca`)**:
    ```json
    {
      "pecaId": 2,
      "quantidade": 2
    }
    ```
    *Validações*: `pecaId`: NotNull; `quantidade`: NotNull, positivo (`> 0`).
  - **Request Body Adicionar Serviço (`POST /ordem-servico/{id}/itens-servico`)**:
    ```json
    {
      "servicoId": 1,
      "quantidade": 1
    }
    ```
    *Validações*: `servicoId`: NotNull; `quantidade`: NotNull, positivo (`> 0`).
  - **Response Completo (`OrdemServicoResponse`)**:
    ```json
    {
      "id": 101,
      "status": "EM_ANDAMENTO",
      "dataOrcamento": "2026-10-15",
      "dataInicio": "2026-10-16",
      "dataRejeicao": null,
      "dataFinalizacao": null,
      "descricao": "Revisão geral de freios",
      "veiculoId": 8,
      "placa": "ABC1D23",
      "oficinaId": 10,
      "mecanicoId": 12,
      "mecanicoNome": "Pedro Mecânico",
      "itensPeca": [
        {
          "id": 50,
          "pecaId": 2,
          "pecaNome": "Pastilha de Freio Cerâmica Dianteira",
          "quantidade": 2,
          "valorUnitario": 180.50,
          "subtotal": 361.00
        }
      ],
      "itensServico": [
        {
          "id": 70,
          "servicoId": 1,
          "servicoNome": "Troca de Pastilha de Freio",
          "quantidade": 1,
          "valorUnitario": 734.00,
          "subtotal": 734.00,
          "tempo": "01:30:00"
        }
      ],
      "total": 1095.00
    }
    ```

- **Regras de Negócio e Comportamento de UI**:
  1. **Regra de Cálculo de Serviço (`CalculadoraOrcamento` com `taxaRisco` da Oficina e `pesoServico`)**:
     - No backend, o preço unitário do serviço é calculado automaticamente pela fórmula:
       $$\text{valorUnitario} = \text{Horas} \times \text{ValorHora} \times \left(1 + \frac{\text{FIPE}}{100.000} \times \text{TaxaRisco} \times \text{PesoServico}\right)$$
     - **Níveis de Peso do Serviço (`pesoServico`)**:
       - `0.0`: Sem risco extra (óleo, alinhamento, bateria, lâmpada) — não incide sobrepreço da FIPE.
       - `0.5`: Risco moderado (freios, suspensão) — padrão para novos serviços.
       - `1.0`: Risco alto (embreagem, correia dentada, motor, câmbio, elétrica complexa).
     - **Flexibilidade**: O peso padrão vem cadastrado no catálogo de serviços (`Servico.pesoServico`), mas pode ser customizado por item na própria Ordem de Serviço (`ItemServico.pesoServico`).
     - Exemplo: 1.5 horas com Valor/Hora R\$ 100,00, FIPE R\$ 100.000,00 e Taxa de Risco da oficina 0.05 (5%):
       - Com peso `0.0`: $1{,}5 \times 100 \times (1 + 1 \times 0{,}05 \times 0) = \text{R\$}~150{,}00$.
       - Com peso `0.5`: $1{,}5 \times 100 \times (1 + 1 \times 0{,}05 \times 0{,}5) = \text{R\$}~153{,}75$.
       - Com peso `1.0`: $1{,}5 \times 100 \times (1 + 1 \times 0{,}05 \times 1{,}0) = \text{R\$}~157{,}50$.
     - O `ADMIN` da oficina pode alterar a `taxaRisco` da sua oficina em `PUT /oficina/{id}`. Orçamentos antigos mantêm o `valorUnitario` e o `pesoServico` congelados no item.
     - Se o veículo não possuir FIPE (`valorFipe == null`), a API lança `400 Bad Request` com:
       `"Veículo sem valor FIPE cadastrado: não é possível calcular o serviço."`
       A UI deve exibir alerta informando a necessidade de atualizar o valor FIPE no cadastro do veículo.
  2. **Ciclo de Estados e Botões de Ação**:
     - `PENDENTE`:
       - Pode adicionar/remover peças e serviços.
       - Ações: **"Aceitar Orçamento"** (chama `/aceitar`) ou **"Rejeitar Orçamento"** (chama `/rejeitar`).
     - `ACEITO`:
       - Orçamento aprovado pelo cliente.
       - Ações: **"Iniciar Serviço"** (chama `/iniciar`, transiciona para `EM_ANDAMENTO`) ou **"Rejeitar"**.
     - `EM_ANDAMENTO`:
       - Serviço em execução na oficina.
       - Ação: **"Finalizar Serviço"** (chama `/finalizar`, transiciona para `FINALIZADO`).
     - `FINALIZADO`:
       - Serviço concluído.
       - **Bloqueio Total**: Não permite adicionar ou remover peças e serviços (se tentado via API, retorna `409 Conflict` com `"Não é possível alterar uma ordem finalizada"`). Os formulários de inserção e botões de exclusão de itens devem ser desabilitados na UI.
     - `REJEITADO`:
       - Orçamento cancelado/rejeitado. Apenas leitura.
  3. **Composição em Duas Tabelas**:
     - **Tabela 1: Peças**: Nome da peça, Quantidade, Valor Unitário (R$), Subtotal (R$) e Botão de Remover Item.
     - **Tabela 2: Serviços**: Nome do serviço, Tempo estimado, Quantidade, Valor Unitário Calculado (R$), Subtotal (R$) e Botão de Remover Item.
     - **Card de Resumo Geral**:
       - Subtotal Peças + Subtotal Serviços = **Valor Total da Ordem**.

---

### 3.14 Modal / Seção Global — "Meus Dados" (UC01)

- **Caso de Uso**: UC01 - Manter Dados Cadastrais do Usuário
- **Roles Permitidas**: Autenticado (`SUPERADMIN`, `ADMIN`, `MECANICO`)
- **Acesso**: Disponível no menu do usuário (Avatar no canto inferior do `AppShell` ou header do `AdminShell`).
- **Endpoints Consumidos**:
  | Método | Endpoint | Auth | Descrição |
  | :--- | :--- | :---: | :--- |
  | `GET` | `/usuario/me` | Sim | Consulta dados do usuário autenticado. |
  | `PUT` | `/usuario/me` | Sim | Atualiza nome, e-mail e opcionalmente a senha do próprio usuário. |

- **Contratos de Dados**:
  - **Request Body (`PUT /usuario/me`)**:
    ```json
    {
      "nome": "Carlos Administrador Atualizado",
      "email": "carlos.novo@oficina.com",
      "senha": "novaSenhaOpcional123"
    }
    ```
    *Validações*:
    - `nome`: NotBlank.
    - `email`: NotBlank, formato válido de e-mail.
    - `senha`: Opcional; se preenchida, deve conter no mínimo 8 caracteres. Se vazia/nula, mantém a senha atual.
  - **Response (`UsuarioResponse`)**: Retorna o objeto `UsuarioResponse` atualizado.

- **Regras de Negócio e Comportamento de UI**:
  - Modal ou página dedicada acessível por qualquer perfil.
  - Campos: Nome, E-mail, Nova Senha (opcional, com confirmação de senha no frontend) e exibição do Perfil (somente leitura: `SUPERADMIN`, `ADMIN` ou `MECANICO`).
  - Ao salvar com sucesso, exibe toast e atualiza o estado da sessão em memória sem deslogar o usuário.

---

## 4. Estrutura de Navegação & Layouts (Shells)

### 4.1 `SuperAdminShell` (antigo `AdminShell` — Exclusivo `SUPERADMIN`)
- **Rota Base**: `/superadmin`
- **Cabeçalho/Sidebar**:
  - Logo OficinaPro com Badge `SUPERADMIN`.
  - Links: Oficinas (`/superadmin`).
  - Menu de perfil: "Meus Dados" (`PUT /usuario/me`) e "Sair" (`POST /auth/logout`).
- **Guarda de Rota**: Se `user.role !== 'SUPERADMIN'`, redireciona para `/app` ou `/`.

### 4.2 `AppShell` (`ADMIN` e `MECANICO`)
- **Rota Base**: `/app`
- **Menu Lateral (Sidebar)**:
  - **Dashboard**: `/app` (`ADMIN`, `MECANICO`)
  - **Mecânicos**: `/app/mecanicos` (**Visível apenas para `ADMIN`**, oculto para `MECANICO`)
  - **Minha Oficina**: `/app/oficina` (**Visível apenas para `ADMIN`**, oculto para `MECANICO` — edita dados da oficina e `lambda`)
  - **Clientes**: `/app/clientes` (`ADMIN`, `MECANICO`)
  - **Catálogo**: `/app/catalogo` (`ADMIN`, `MECANICO`)
  - **Orçamentos**: `/app/orcamentos` (`ADMIN`, `MECANICO`)
- **Rodapé do Menu**:
  - Avatar com iniciais do usuário, nome e role (`Admin` ou `Mecânico`).
  - Botão de Configurações / "Meus Dados" (abre modal de edição de perfil).
  - Botão "Sair" (chama `POST /auth/logout` e redireciona para `/`).
- **Guarda de Rota**: Se `user.role === 'SUPERADMIN'`, redireciona para `/superadmin`. Se não autenticado, redireciona para `/`.

---

## 5. Resumo das Mudanças em Relação ao Protótipo Legado

| Recurso / Conceito | Protótipo Legado (Mock) | Backend Real (Spring Boot API) | Ação no Frontend |
| :--- | :--- | :--- | :--- |
| **Autenticação** | Login sem senha por e-mail | `POST /auth/login` (email + password) com Cookie HTTP-Only `jwt` | Substituir mock por chamada real e ler cookie/sessão |
| **Níveis de Acesso** | `admin` e `oficina` | `SUPERADMIN`, `ADMIN`, `MECANICO` | Ajustar tipos, guards de rota e permissões |
| **Gestão de Mecânicos** | Inexistente no frontend | UC03 completo (`/usuario/mecanico`, etc.) | **Criar a nova página `/app/mecanicos`** |
| **Cálculo de Orçamento** | Multiplicador $\lambda$ fixo por veículo | $\text{valorBase} + (\lambda_{\text{oficina}} \times \text{valorFipe})$ | Mover $\lambda$ do Veículo para a Oficina (`PUT /oficina/{id}`) e usá-lo na prévia de orçamento |
| **Catálogo de Serviços** | Vinculado a peças (`pecaIds`) | Serviços e Peças independentes | Remover seleção de peças dentro de serviços |
| **Ordem de Serviço** | Itens apenas de serviço | Listas separadas: `itensPeca` e `itensServico` | Duas tabelas e endpoints dedicados na tela `/app/orcamentos/[id]` |
| **Status da OS** | 4 status minúsculos | 5 status enum: `PENDENTE`, `ACEITO`, `REJEITADO`, `EM_ANDAMENTO`, `FINALIZADO` | Atualizar badges, filtros e fluxo de botões |
| **Status da Oficina** | `ativa`, `pendente`, `suspensa` | Booleano `ativo` (`true`/`false`) | Simplificar filtros e trocar por ativar/desativar |
| **Substituição de Veículo** | Fluxo "Swap" com dados antigos | Inativação e cadastro independente | Remover modal de swap e manter CRUD padrão de veículos |
