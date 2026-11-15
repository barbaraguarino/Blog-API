# Documento de Arquitetura de Software

## 1. Introdução

Este documento descreve a arquitetura do Blog-API: como o sistema se organiza, quais tecnologias usa, como as requisições fluem e quais decisões guiam a evolução. As decisões individuais e seus motivos ficam no [Decisões.md](Decisões.md).

## 2. Visão geral

```text
┌──────────────┐   HTTPS/JSON    ┌──────────────────────────────────────┐   JDBC   ┌──────────────┐
│ Cliente      │ ──────────────▶ │            Blog-API                  │ ───────▶ │ PostgreSQL   │
│ (web/mobile) │ ◀────────────── │  Spring Boot 4 · Java 25             │          │ (Flyway)     │
└──────────────┘  cookie JWT     └──────────────────────────────────────┘          └──────────────┘
                                        │                    │
                          valida ID     │                    │  e-mail / mídias (planejado)
                          Token         ▼                    ▼
                                 ┌──────────────┐    ┌──────────────────┐
                                 │ Google OIDC  │    │ SMTP/SES · AWS S3│
                                 └──────────────┘    └──────────────────┘
```

- **Estilo:** API REST monolítica e modular (monólito modular), stateless.
- **Clientes:** qualquer front-end. O projeto não tem interface própria.
- **Integrações:** Google (validação de ID Token) hoje. Serviço de e-mail e AWS S3 estão planejados.

## 3. Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 25 |
| Framework | Spring Boot 4 (Web MVC, Data JPA, Validation, Security) |
| Banco | PostgreSQL + Flyway (`ddl-auto=validate`) |
| Autenticação | Spring Security + `java-jwt` (HMAC256) + `google-api-client` |
| Utilitários | Lombok |
| Testes | JUnit 5, Mockito, AssertJ, Spring Test (`@WebMvcTest`, `MockMvc`) |
| Build | Maven (wrapper `mvnw`) |
| Planejado | springdoc-openapi (Swagger), AWS SDK (S3), envio de e-mail |

## 4. Organização do código

Pragmatic Clean Architecture com **pacotes por funcionalidade** (D-001).

```text
br.com.blog
├── core/                 # Global: config, exceções, segurança (JWT, UserDetails)
├── infrastructure/
│   └── providers/        # Gateways para serviços externos (google/, futuramente email/, storage/)
└── modules/
    └── <modulo>/         # user, article, social, moderation
        ├── domain/           # Entidades ricas e enums
        ├── application/      # Casos de uso, DTOs, mappers
        ├── infrastructure/   # Repositórios Spring Data
        └── presentation/     # Controllers REST
```

### Regras de dependência

```text
presentation ──▶ application ──▶ domain
                      │
                      ▼
               infrastructure (repositórios, gateways)
```

- **domain** não depende de nenhuma outra camada do módulo. Regras de criação ficam na própria entidade (ex.: `User.createLocalUser`).
- **application** orquestra: um caso de uso por classe (`<Verbo><Objeto>Service`) com um único método `execute`.
- **presentation** só converte HTTP para DTO e DTO para HTTP, sem regra de negócio.
- Um módulo não acessa o repositório de outro módulo diretamente. Ele deve chamar um caso de uso desse módulo.
- Serviços externos ficam atrás de um gateway em `infrastructure/providers/`, para poderem ser trocados e simulados nos testes.

## 5. Fluxos principais

### 5.1 Cadastro local (`POST /api/v1/register`)

1. `UserRegistrationController` recebe o `RegisterUserRequestDTO`, validado com Bean Validation (RN-002, RN-004).
2. `RegisterLocalUserService` verifica se o e-mail é único (RN-001) e gera o hash BCrypt da senha.
3. `User.createLocalUser` cria a entidade com papel READER e nickname gerado (RN-003, RN-005).
4. A resposta é **201** com o `UserProfileResponseDTO`.

### 5.2 Login local (`POST /api/v1/auth/login`)

1. `AuthenticateLocalUserService` chama o `AuthenticationManager`.
2. `CustomUserDetailsService` busca o usuário por e-mail ou por nickname (RN-009).
3. O Spring Security confere a senha e se a conta está ativa e desbloqueada.
4. `TokenService` gera o JWT, e o controller o coloca no cookie `HttpOnly` (RN-008).

### 5.3 Google (`/register/google` e `/auth/login/google`)

`GoogleAuthGateway` valida o ID Token com o client-id configurado e devolve `GoogleUserInfoDTO`. O cadastro cria a conta (RN-006, RN-007). O login busca a conta pelo `googleId` e emite o JWT próprio do Blog (RN-010).

### 5.4 Requisição autenticada

`SecurityFilter` lê o JWT, valida assinatura e emissor em `TokenService.validateToken` e carrega o usuário no `SecurityContext`. Rotas fora de `PUBLIC_ROUTES` exigem autenticação. `@EnableMethodSecurity` permite usar `@PreAuthorize` por papel.

## 6. Modelo de dados

### Atual

**`tb_users`** (`V1__create_table_users.sql`)

| Coluna | Tipo | Restrições |
|---|---|---|
| id | UUID | PK |
| name | VARCHAR(150) | NOT NULL |
| email | VARCHAR(150) | NOT NULL, UNIQUE |
| nickname | VARCHAR(50) | NOT NULL, UNIQUE |
| password | VARCHAR(255) | nulo para contas só Google |
| google_id | VARCHAR(255) | UNIQUE |
| role | VARCHAR(50) | NOT NULL, padrão `READER` |
| is_enabled / is_locked | BOOLEAN | NOT NULL |
| created_at / updated_at | TIMESTAMP | NOT NULL |

### Planejado

```text
users 1──* sessions                  (v0.1, refresh tokens)
users 1──* verification_codes        (v0.1, OTP por finalidade)
users 1──* author_requests           (v0.2)
categories 1──* categories (pai)     (v0.2)
users 1──* articles *──1 categories  (v0.2)
articles *──* tags                   (v0.2)
articles 1──* comments 1──* comments (resposta, v0.3)
users *──* articles  via reactions   (v0.3)
users *──* users     via follows     (v0.3)
users 1──* reports   (alvo: artigo ou comentário, v0.4)
```

Toda tabela nova entra por uma nova migration `V<n>__descricao.sql`.

## 7. Segurança

| Aspecto | Como é tratado |
|---|---|
| Senhas | BCrypt (`PasswordEncoder`) |
| Sessão | Stateless. JWT HMAC256 com emissor = `spring.application.name` e assunto = e-mail |
| Transporte do token | Cookie `HttpOnly`, `SameSite=Strict`, `path=/`. `Secure=false` hoje (RNF-004) |
| CSRF | Proteção do Spring desativada. A mitigação vem do `SameSite=Strict`. Reavaliar ao adotar Refresh Token |
| Autorização | Rotas públicas explícitas, o resto exige login. Papéis `ROLE_*` para `@PreAuthorize` |
| Segredos | Lidos do `.env` (`spring.config.import`), que está no `.gitignore` |
| Erros | Mensagens genéricas no 500. Detalhes só no log |

## 8. Tratamento de erros e i18n

- Exceções de **domínio** (`BusinessRuleException` → 422, `ResourceAlreadyExistsException` → 409, `ResourceNotFoundException` → 404) e de **infraestrutura** (`ExternalProviderAuthException` → 401, `InfrastructureException` → 500) carregam uma chave de mensagem.
- O `GlobalExceptionHandler` traduz a chave pelo `MessageSource`, conforme o `Accept-Language`, e responde no formato `ErrorResponseDTO`.
- Erros de validação retornam **400** com um mapa `campo → mensagem`.

## 9. Configuração e ambientes

| Variável (no `.env`) | Uso |
|---|---|
| Datasource (URL, usuário, senha) | Conexão PostgreSQL |
| `JWT_EXPIRATION` e segredo do token | Assinatura e validade do JWT |
| Client-id do Google | Validação do ID Token |
| *(planejado)* SMTP/SES, AWS S3 | E-mail e mídias |

Hoje só existe o ambiente local. Um perfil de produção (`Secure=true`, `show-sql=false`) ainda precisa ser criado.

## 10. Riscos e débitos técnicos

| Item | Referência |
|---|---|
| Colisão de nickname gera erro 500 | RN-005 / D1 |
| Login Google não checa conta bloqueada ou desativada | RN-010 / D3 |
| `Secure=false` e `maxAge` fixos no cookie | RNF-004 / D4 |
| Sem Swagger, embora a Definição de Pronto o exija | RNF-010 |
| Sem CI: os testes não rodam automaticamente nos PRs | ver [Plano_de_Testes.md](testes/Plano_de_Testes.md) |
