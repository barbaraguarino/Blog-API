# Requisitos

Este documento descreve **o que** o Blog-API deve fazer (requisitos funcionais, RF) e **com que qualidade** (requisitos não funcionais, RNF). Cada requisito funcional é apresentado também como **história de usuário**, no formato usado no Scrum, e tem critérios de aceite verificáveis. As regras detalhadas que cada requisito obedece estão em [Regras_de_Negócio.md](Regras_de_Negócio.md).

---

## 1. Como ler este documento

### 1.1 Identificadores

| Prefixo | Significado | Exemplo |
|---|---|---|
| `RF-0xx` | Requisito funcional: uma capacidade do sistema. | RF-001 Cadastrar usuário |
| `RNF-0xx` | Requisito não funcional: uma qualidade ou restrição. | RNF-001 Senhas com BCrypt |
| `RN-0xx` | Regra de negócio, no documento próprio. | RN-004 Política de senha |

Os números são agrupados por módulo (RF-001 a 019: identidade; 020 a 029: autoria e conteúdo; 030 a 039: social; 040 a 049: moderação) e **nunca são reaproveitados**. Um requisito descartado continua listado, com o status *Descartado*.

### 1.2 Status

| Símbolo | Significado |
|---|---|
| ✅ | Implementado e testado. |
| 🟡 | Parcial: implementado com lacunas conhecidas (ver a nota do requisito). |
| ⬜ | Planejado. |

### 1.3 Prioridade (MoSCoW)

| Prioridade | Significado |
|---|---|
| **Must** | Essencial: sem ele, a versão não tem valor. |
| **Should** | Importante, mas a versão funciona sem ele. |
| **Could** | Desejável, se houver tempo. |
| **Won't (agora)** | Fora desta versão; reavaliado depois. |

### 1.4 Como manter

- Um requisito novo recebe o próximo número livre do grupo do seu módulo.
- Toda issue cita os RF e RN que atende. Ao fechar a issue, atualize o status aqui.
- Se uma regra de negócio mudar, altere o [Regras_de_Negócio.md](Regras_de_Negócio.md) e, se for uma decisão de projeto, registre no [Decisões.md](Decisões.md).
- Os endpoints marcados como *(proposto)* ainda não existem e podem mudar na implementação.

---

## 2. Visão geral

| ID | Requisito | Prioridade | Versão | Status |
|---|---|---|---|---|
| RF-001 | Cadastrar usuário com e-mail e senha | Must | v0.0 | ✅ |
| RF-002 | Cadastrar usuário com conta Google | Must | v0.0 | ✅ |
| RF-003 | Login com e-mail ou nickname e senha | Must | v0.0 | ✅ |
| RF-004 | Login com conta Google | Must | v0.0 | 🟡 |
| RF-005 | Renovar o acesso com Refresh Token | Must | v0.1 | ⬜ |
| RF-006 | Logout | Must | v0.1 | ⬜ |
| RF-007 | Listar e revogar sessões | Should | v0.1 | ⬜ |
| RF-008 | Ativar a conta por e-mail | Must | v0.1 | ⬜ |
| RF-009 | Recuperar a senha esquecida | Must | v0.1 | ⬜ |
| RF-010 | Trocar a senha estando logado | Should | v0.1 | ⬜ |
| RF-011 | 2FA com app autenticador | Could | v0.4 | ⬜ |
| RF-012 | Vincular e desvincular conta Google | Could | v0.4 | ⬜ |
| RF-020 | Solicitar autoria | Must | v0.2 | ⬜ |
| RF-021 | Aprovar ou recusar autoria | Must | v0.2 | ⬜ |
| RF-022 | Gerenciar categorias | Must | v0.2 | ⬜ |
| RF-023 | Gerenciar artigos e rascunhos | Must | v0.2 | ⬜ |
| RF-024 | Tags em artigos | Should | v0.2 | ⬜ |
| RF-025 | Upload de imagens | Should | v0.2 | ⬜ |
| RF-026 | Buscar artigos | Should | v0.3 | ⬜ |
| RF-030 | Comentar e responder | Must | v0.3 | ⬜ |
| RF-031 | Reagir a artigos | Should | v0.3 | ⬜ |
| RF-032 | Seguir autores | Should | v0.3 | ⬜ |
| RF-033 | Feed de autores seguidos | Should | v0.3 | ⬜ |
| RF-040 | Denunciar conteúdo | Must | v0.4 | ⬜ |
| RF-041 | Moderar denúncias | Must | v0.4 | ⬜ |

---

## 3. Requisitos funcionais

### 3.1 Identidade e acesso (`module:user`)

#### RF-001 – Cadastrar usuário com e-mail e senha ✅

> **Como** visitante, **quero** criar uma conta informando nome, e-mail e senha, **para** participar da plataforma.

- **Ator:** visitante.
- **Endpoint:** `POST /api/v1/register` (público).
- **Entrada:** `RegisterUserRequestDTO`, com `name`, `email` e `password`.
- **Saída:** `201 Created` com o perfil do usuário (`UserProfileResponseDTO`: id, nome, e-mail, nickname, papel, vínculo com Google, ativo, bloqueado, data de criação). A senha nunca é devolvida.
- **Regras:** RN-001, RN-002, RN-003, RN-004, RN-005.
- **Critérios de aceite:**
  - [x] Dados válidos criam o usuário com o papel `READER` e devolvem `201`.
  - [x] E-mail já cadastrado devolve `409` com mensagem traduzida.
  - [x] Dados inválidos devolvem `400`, com um erro por campo em `validationErrors`.
  - [x] A senha é armazenada só como hash BCrypt.
  - [x] O nickname é gerado automaticamente.
- **Nota:** a partir da v0.1, a conta local nasce **desativada** até a confirmação do e-mail (RF-008, RN-016).

#### RF-002 – Cadastrar usuário com conta Google ✅

> **Como** visitante, **quero** criar uma conta usando minha conta Google, **para** não precisar criar outra senha.

- **Ator:** visitante.
- **Endpoint:** `POST /api/v1/register/google` (público).
- **Entrada:** `GoogleAuthRequestDTO` com `token` (o *ID Token* obtido pelo cliente no login do Google).
- **Saída:** `201 Created` com o perfil do usuário.
- **Regras:** RN-001, RN-003, RN-005, RN-006, RN-007.
- **Critérios de aceite:**
  - [x] Um token válido cria a conta com nome, e-mail e `googleId` vindos do Google, sem senha local.
  - [x] Um token inválido, expirado ou forjado devolve `401`.
  - [x] Um e-mail ou `googleId` já cadastrado devolve `409`.
  - [x] Um token vazio devolve `400`.
- **Nota:** há uma divergência nas mensagens de conflito (D2) e falta conferir `email_verified` (D5). Ver [Regras_de_Negócio.md](Regras_de_Negócio.md#5-divergências-conhecidas).

#### RF-003 – Login com e-mail ou nickname e senha ✅

> **Como** usuário cadastrado, **quero** entrar informando meu e-mail **ou** meu nickname e minha senha, **para** acessar as funcionalidades da minha conta.

- **Ator:** leitor, autor, moderador ou administrador.
- **Endpoint:** `POST /api/v1/auth/login` (público).
- **Entrada:** `LoginRequestDTO` com `login` (e-mail ou nickname) e `password`.
- **Saída:** `200 OK` com o perfil do usuário e o cabeçalho `Set-Cookie` com o JWT (`HttpOnly`, `SameSite=Strict`).
- **Regras:** RN-008, RN-009.
- **Critérios de aceite:**
  - [x] Login correto por e-mail ou por nickname devolve `200` e o cookie.
  - [x] Credenciais incorretas devolvem `401` com mensagem genérica, sem revelar se o usuário existe.
  - [x] Conta desativada ou bloqueada devolve `403`.
  - [x] Corpo vazio ou campos em branco devolvem `400`.

#### RF-004 – Login com conta Google 🟡

> **Como** usuário cadastrado com Google, **quero** entrar com minha conta Google, **para** acessar a plataforma sem senha.

- **Endpoint:** `POST /api/v1/auth/login/google` (público).
- **Entrada:** `GoogleAuthRequestDTO` com `token`.
- **Saída:** `200 OK` com o perfil e o cookie do JWT.
- **Regras:** RN-008, RN-010.
- **Critérios de aceite:**
  - [x] Um token válido de conta cadastrada devolve `200` e o cookie.
  - [x] Uma conta Google não cadastrada devolve `404`, orientando a fazer o cadastro.
  - [x] Um token inválido devolve `401`.
  - [ ] Uma conta desativada ou bloqueada devolve `403` (**falta**, divergência D3).

#### RF-005 – Renovar o acesso com Refresh Token ⬜

> **Como** usuário logado, **quero** que meu acesso seja renovado automaticamente, **para** não precisar fazer login a cada 15 minutos, sem abrir mão da segurança.

- **Endpoint:** `POST /api/v1/auth/refresh` *(proposto)*.
- **Regras:** RN-011, RN-012, RN-013.
- **Critérios de aceite:**
  - [ ] O login passa a emitir um token de acesso (15 min) e um Refresh Token (7 dias), ambos em cookies `HttpOnly`.
  - [ ] O refresh devolve um novo par de tokens e invalida o Refresh Token anterior.
  - [ ] Um Refresh Token reutilizado revoga todas as sessões do usuário e devolve `401`.
  - [ ] Um Refresh Token expirado ou revogado devolve `401`.

#### RF-006 – Logout ⬜

> **Como** usuário logado, **quero** sair da minha conta, **para** que o dispositivo atual não continue autenticado.

- **Endpoint:** `POST /api/v1/auth/logout` *(proposto)*.
- **Regras:** RN-011.
- **Critérios de aceite:**
  - [ ] A sessão atual é revogada e os cookies são apagados.
  - [ ] Depois do logout, o Refresh Token daquela sessão não funciona mais.

#### RF-007 – Listar e revogar sessões ⬜

> **Como** usuário logado, **quero** ver em quais dispositivos estou conectado e encerrar sessões, **para** retomar o controle da conta caso perca um aparelho.

- **Endpoints:** `GET /api/v1/sessions`, `DELETE /api/v1/sessions/{id}` e `DELETE /api/v1/sessions` (todas, exceto a atual) *(propostos)*.
- **Regras:** RN-014.
- **Critérios de aceite:**
  - [ ] A lista mostra só as sessões ativas do próprio usuário (IP, dispositivo, último uso) e indica a atual.
  - [ ] Revogar a sessão de outro usuário devolve `404`.
  - [ ] Revogar todas mantém a sessão atual.

#### RF-008 – Ativar a conta por e-mail ⬜

> **Como** novo usuário, **quero** confirmar meu e-mail com um código, **para** provar que o endereço é meu e ativar a conta.

- **Endpoints:** `POST /api/v1/register/activate` e `POST /api/v1/register/activate/resend` *(propostos)*.
- **Regras:** RN-015, RN-016, RN-017.
- **Critérios de aceite:**
  - [ ] O cadastro local envia um código de 6 dígitos e a conta fica desativada.
  - [ ] Um código correto e dentro da validade ativa a conta.
  - [ ] Um código errado, expirado ou já usado devolve um erro traduzido.
  - [ ] O reenvio respeita o limite de frequência.
  - [ ] Contas criadas com Google já nascem ativas.

#### RF-009 – Recuperar a senha esquecida ⬜

> **Como** usuário que esqueceu a senha, **quero** redefini-la por um código enviado ao meu e-mail, **para** voltar a acessar a conta.

- **Endpoints:** `POST /api/v1/auth/password/forgot` e `POST /api/v1/auth/password/reset` *(propostos)*.
- **Regras:** RN-015, RN-018, RN-019.
- **Critérios de aceite:**
  - [ ] O pedido responde sempre igual, exista ou não o e-mail.
  - [ ] Um código válido mais uma nova senha (RN-004) redefine a senha.
  - [ ] Todas as sessões antigas deixam de funcionar.
  - [ ] Uma conta só com Google recebe orientação para entrar com o Google.

#### RF-010 – Trocar a senha estando logado ⬜

> **Como** usuário logado, **quero** trocar minha senha confirmando a atual e um código por e-mail, **para** manter a conta segura.

- **Endpoints:** `POST /api/v1/account/password/code` e `PUT /api/v1/account/password` *(propostos)*.
- **Regras:** RN-015, RN-020.
- **Critérios de aceite:**
  - [ ] Uma senha atual errada devolve erro.
  - [ ] Um código inválido ou expirado devolve erro.
  - [ ] Uma nova senha igual à atual, ou fora da política, devolve `400`.
  - [ ] As outras sessões são revogadas; a atual continua.

#### RF-011 – 2FA com app autenticador ⬜

> **Como** usuário preocupado com segurança, **quero** exigir um código do meu app autenticador no login, **para** que só a senha não baste para entrar.

- **Regras:** RN-021.
- **Critérios de aceite:**
  - [ ] O usuário obtém um QR Code e ativa o 2FA após confirmar um código válido.
  - [ ] Com o 2FA ativo, o login só emite tokens depois do código TOTP.
  - [ ] Os códigos de recuperação funcionam uma única vez cada.
  - [ ] Desativar o 2FA exige a senha e um código.

#### RF-012 – Vincular e desvincular conta Google ⬜

> **Como** usuário, **quero** ligar minha conta Google à conta local, ou desligar, **para** escolher como prefiro entrar.

- **Regras:** RN-022.
- **Critérios de aceite:**
  - [ ] Vincular uma conta Google já usada por outro usuário devolve `409`.
  - [ ] Desvincular sem ter senha local devolve um erro traduzido.

### 3.2 Autoria e conteúdo (`module:article`)

#### RF-020 – Solicitar autoria ⬜

> **Como** leitor, **quero** pedir para me tornar autor, **para** poder publicar artigos.

- **Regras:** RN-030, RN-031.
- **Critérios de aceite:**
  - [ ] Uma conta com mais de 2 meses consegue enviar o pedido com uma justificativa.
  - [ ] Uma conta mais nova recebe um erro explicando o prazo.
  - [ ] Um segundo pedido pendente é bloqueado.

#### RF-021 – Aprovar ou recusar autoria ⬜

> **Como** administrador, **quero** analisar os pedidos de autoria, **para** controlar quem publica na plataforma.

- **Regras:** RN-032.
- **Critérios de aceite:**
  - [ ] Só o `ADMIN` lista, aprova e recusa pedidos; os demais recebem `403`.
  - [ ] A aprovação muda o papel para `AUTHOR`.
  - [ ] A recusa exige um motivo, visível para quem pediu.

#### RF-022 – Gerenciar categorias ⬜

> **Como** administrador, **quero** criar categorias e subcategorias, **para** organizar os artigos por tema.

- **Regras:** RN-033, RN-034.
- **Critérios de aceite:**
  - [ ] A listagem pública devolve a árvore de categorias.
  - [ ] Um slug duplicado devolve `409`.
  - [ ] Remover uma categoria com artigos ou subcategorias é bloqueado.

#### RF-023 – Gerenciar artigos e rascunhos ⬜

> **Como** autor, **quero** escrever artigos, salvá-los como rascunho e publicá-los quando estiverem prontos, **para** compartilhar conteúdo com a comunidade.

- **Regras:** RN-035, RN-036, RN-037, RN-038.
- **Critérios de aceite:**
  - [ ] Um leitor que tenta criar um artigo recebe `403`.
  - [ ] Um autor edita só os próprios artigos.
  - [ ] O rascunho de outro autor devolve `404`.
  - [ ] A listagem pública é paginada e mostra só os artigos publicados, do mais recente para o mais antigo.

#### RF-024 – Tags em artigos ⬜

> **Como** autor, **quero** marcar artigos com tags, **para** que leitores encontrem conteúdos relacionados.

- **Regras:** RN-039.
- **Critérios de aceite:**
  - [ ] "Java" e "java " resultam na mesma tag.
  - [ ] O filtro por tag devolve só artigos publicados.

#### RF-025 – Upload de imagens ⬜

> **Como** usuário, **quero** enviar uma foto de perfil, e como autor uma capa para meus artigos, **para** deixar o conteúdo mais atrativo.

- **Regras:** RN-040.
- **Critérios de aceite:**
  - [ ] O upload de avatar e de capa funciona.
  - [ ] Um arquivo que não é imagem, ou grande demais, devolve um erro traduzido.
  - [ ] Ao trocar a imagem, a anterior é removida do armazenamento.

#### RF-026 – Buscar artigos ⬜

> **Como** visitante, **quero** buscar artigos por texto e filtrar por categoria, tag e autor, **para** encontrar o que me interessa.

- **Regras:** RN-041.
- **Critérios de aceite:**
  - [ ] A busca ignora maiúsculas e acentos.
  - [ ] Os filtros podem ser combinados.
  - [ ] O resultado é paginado.

### 3.3 Social (`module:social`)

#### RF-030 – Comentar e responder ⬜

> **Como** leitor, **quero** comentar artigos e responder comentários, **para** participar da discussão.

- **Regras:** RN-050, RN-051.
- **Critérios de aceite:**
  - [ ] Comentar um rascunho devolve `404`.
  - [ ] Uma resposta a uma resposta é bloqueada (há um único nível).
  - [ ] O autor do comentário edita e remove o próprio comentário; ADMIN e MODERATOR removem qualquer um.

#### RF-031 – Reagir a artigos ⬜

> **Como** leitor, **quero** reagir aos artigos, **para** demonstrar o que achei sem precisar comentar.

- **Regras:** RN-052.
- **Critérios de aceite:**
  - [ ] Uma segunda reação substitui a anterior.
  - [ ] A reação pode ser removida.
  - [ ] Os contadores aparecem na listagem.

#### RF-032 – Seguir autores ⬜

> **Como** leitor, **quero** seguir autores, **para** acompanhar o que eles publicam.

- **Regras:** RN-053.
- **Critérios de aceite:**
  - [ ] Seguir duas vezes não duplica.
  - [ ] Seguir a si mesmo devolve um erro.
  - [ ] As listas de seguidores e seguidos são paginadas.

#### RF-033 – Feed de autores seguidos ⬜

> **Como** leitor, **quero** um feed com os artigos recentes de quem eu sigo, **para** não precisar procurar autor por autor.

- **Regras:** RN-054.
- **Critérios de aceite:**
  - [ ] O feed fica vazio quando o usuário não segue ninguém.
  - [ ] Os artigos novos dos autores seguidos aparecem no topo.

### 3.4 Moderação (`module:moderation`)

#### RF-040 – Denunciar conteúdo ⬜

> **Como** leitor, **quero** denunciar artigos ou comentários abusivos, **para** ajudar a manter a comunidade saudável.

- **Regras:** RN-060.
- **Critérios de aceite:**
  - [ ] Uma denúncia duplicada do mesmo usuário devolve `409`.
  - [ ] O motivo vem de uma lista fixa, com descrição opcional.

#### RF-041 – Moderar denúncias ⬜

> **Como** moderador ou administrador, **quero** analisar denúncias e aplicar ações, **para** remover abusos.

- **Regras:** RN-061.
- **Critérios de aceite:**
  - [ ] As denúncias pendentes são listadas por ordem de chegada.
  - [ ] O conteúdo ocultado some das listagens públicas.
  - [ ] Toda ação fica registrada com quem a tomou e quando.

---

## 4. Requisitos não funcionais

| ID | Categoria | Requisito | Como verificar | Status |
|---|---|---|---|---|
| RNF-001 | Segurança | As senhas são armazenadas só como hash **BCrypt**, nunca em texto. | Teste do caso de uso de cadastro e inspeção da coluna `password`. | ✅ |
| RNF-002 | Segurança | A autenticação é stateless, com JWT assinado (HMAC256), emissor = nome da aplicação, entregue em cookie `HttpOnly` com `SameSite=Strict`. | Testes de controller verificam o cabeçalho `Set-Cookie`. | ✅ |
| RNF-003 | Segurança | Todo endpoint é protegido por padrão; as rotas públicas são listadas explicitamente no `SecurityConfig`. | Teste de acesso sem login às rotas protegidas (`401`/`403`). | ✅ |
| RNF-004 | Segurança | Os cookies usam `Secure=true` fora do ambiente local, e a validade do cookie acompanha a do token. | Configuração por perfil e teste do cookie. | ⬜ (hoje `Secure=false` e validade fixa: D4) |
| RNF-005 | Segurança | Os segredos (chave JWT, banco, Google, AWS, SMTP) ficam só em variáveis de ambiente (`.env`), nunca versionados. | `.gitignore`, secret scanning e revisão de PR. | ✅ |
| RNF-006 | Segurança | Refresh Tokens e códigos OTP são guardados só como hash. | Inspeção do banco e testes. | ⬜ |
| RNF-007 | Segurança | Login, verificação de códigos e reenvios têm limite de tentativas e de frequência. | Testes de limite. | ⬜ |
| RNF-008 | Internacionalização | As mensagens ao cliente estão em pt-BR (padrão) e en, escolhidas pelo cabeçalho `Accept-Language`. | Testes com os dois idiomas e conferência das chaves nos três arquivos. | ✅ |
| RNF-009 | Padronização | Todo erro segue o formato `ErrorResponseDTO` (`timestamp`, `status`, `error`, `message`, `path` e, em validação, `validationErrors`). | Testes de controller e do handler. | ✅ |
| RNF-010 | Documentação | A API é documentada com OpenAPI/Swagger, com exemplos de requisição e de erro. | Acessar `/swagger-ui.html`. | ⬜ |
| RNF-011 | Manutenibilidade | Pragmatic Clean Architecture por funcionalidade, com casos de uso de ação única e DTOs obrigatórios (ver [Arquitetura.md](Arquitetura.md)). | Revisão de PR. | ✅ |
| RNF-012 | Banco de dados | O esquema é versionado só pelo Flyway, com `ddl-auto=validate` (o Hibernate valida, mas não altera tabelas). | A aplicação não sobe se a entidade divergir da tabela. | ✅ |
| RNF-013 | Testabilidade | Toda classe de produção tem teste unitário, e cada tipo de classe tem também os testes do seu tipo. | Inventário do [Plano_de_Testes.md](Testes/Plano_de_Testes.md). | 🟡 (6 de 35 classes) |
| RNF-014 | Desempenho | Toda listagem é paginada, com tamanho máximo de página. | Testes das listagens. | ⬜ |
| RNF-015 | Portabilidade | Executa localmente com Java 25 e PostgreSQL, sem Docker. | Execução local. | ✅ |
| RNF-016 | Versionamento da API | Todas as rotas usam o prefixo `/api/v1`. Uma mudança incompatível cria `/api/v2`. | Revisão de PR. | ✅ |
| RNF-017 | Privacidade | As respostas nunca expõem senha, hash, tokens, `googleId` ou dados de outros usuários além do perfil público. | Revisão dos DTOs de resposta e testes. | ✅ |
| RNF-018 | Observabilidade | Erros de infraestrutura são logados com a causa original; o cliente recebe uma mensagem genérica, sem stack trace. | Testes do handler e revisão. | ✅ |
| RNF-019 | Qualidade do processo | Toda mudança passa por issue → branch → PR → revisão, seguindo o Git Flow, e nenhum link de sessão do Claude entra no repositório. | Workflows do GitHub e template de PR. | ✅ |

---

## 5. Matriz de rastreabilidade

| Requisito | Regras | Issue / versão | Testes |
|---|---|---|---|
| RF-001 | RN-001 a RN-005 | v0.0 | `RegisterLocalUserServiceTest`, `UserRegistrationControllerTest` |
| RF-002 | RN-001, RN-003, RN-005 a RN-007 | v0.0 | `RegisterGoogleUserServiceTest`, `UserRegistrationControllerTest` |
| RF-003 | RN-008, RN-009 | v0.0 | `AuthenticateLocalUserServiceTest`, `AuthControllerTest` |
| RF-004 | RN-008, RN-010 | v0.0 (+ bug D3 na v0.1) | `AuthenticateGoogleUserServiceTest`, `AuthControllerTest` |
| RF-005 a RF-010 | RN-011 a RN-020 | v0.1 | — |
| RF-011, RF-012 | RN-021, RN-022 | v0.4 | — |
| RF-020 a RF-026 | RN-030 a RN-041 | v0.2–v0.3 | — |
| RF-030 a RF-033 | RN-050 a RN-054 | v0.3 | — |
| RF-040, RF-041 | RN-060, RN-061 | v0.4 | — |

A coluna *Issue* passa a mostrar o número da issue (`#N`) assim que as issues forem criadas no GitHub.
