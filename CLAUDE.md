# CLAUDE.md

Regras para trabalhar neste repositório. Projeto de estudo de backend: API REST de um blog em Java 25 + Spring Boot 4 + PostgreSQL. Responda e escreva documentação, commits e issues em **português**.

## Comandos

- Testes: `./mvnw test` (no Windows/PowerShell: `.\mvnw.cmd test`)
- Rodar a API: `./mvnw spring-boot:run`
- Requisições manuais: arquivos `.http` em `requests/<modulo>/<funcionalidade>/`

Configuração sensível fica no `.env` da raiz (carregado por `spring.config.import`). Ele está no `.gitignore`: **nunca** leia valores dele para o chat nem commite segredos.

## Arquitetura

Pragmatic Clean Architecture, organizada por funcionalidade (package by feature). Detalhes no README.

- `core/`: configurações globais, exceções e segurança (JWT, `SecurityConfig`, `UserDetails`).
- `infrastructure/providers/<provedor>/`: integrações externas (Google, e futuramente e-mail e S3), sempre atrás de um gateway.
- `modules/<modulo>/`, com 4 camadas:
  - `domain/`: entidades ricas e enums. Regras de criação ficam na entidade (ex. `User.createLocalUser`).
  - `application/`: casos de uso em `usecases/<contexto>/`, DTOs em `dtos/` e mappers.
  - `infrastructure/persistence/`: repositórios Spring Data.
  - `presentation/controllers/<contexto>/`: controllers REST, sem regra de negócio.

## Convenções de código

- **Casos de uso:** uma classe por ação, nomeada `<Verbo><Objeto>Service` (ex. `RegisterLocalUserService`), com um único método público `execute(...)`, `@Service` + `@RequiredArgsConstructor`, e `@Transactional` quando escreve no banco.
- **DTOs:** `record` com sufixo `DTO`. Os que nunca saem da API ficam em `dtos/.../internal/`.
- **Erros:** lance as exceções de `core/exceptions/domain` (`BusinessRuleException`, `ResourceNotFoundException`, `ResourceAlreadyExistsException`) ou de `core/exceptions/infrastructure`, passando uma **chave de i18n**, nunca um texto fixo. O `GlobalExceptionHandler` formata a resposta.
- **i18n:** toda chave nova vai nos três arquivos: `messages.properties`, `messages_pt_BR.properties` e `messages_en.properties`. Padrão de chave: `error.<area>.<motivo>`.
- **Banco:** só via Flyway, com uma nova migration `V<n>__descricao.sql`. Nunca edite uma migration já aplicada.
- **Segurança:** JWT em cookie `HttpOnly`. Endpoints novos são protegidos por padrão; liberar rota pública exige ajuste explícito no `SecurityConfig`.

## Testes

- Ficam em `src/test/java/br/com/blog/modules/<modulo>/services/...` (casos de uso) e `.../controllers/...` (controllers).
- Casos de uso: testes unitários com JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`) e AssertJ.
- Controllers: `@WebMvcTest` + `MockMvc`, importando `SecurityConfig` e mockando dependências com `@MockitoBean`.
- Use `@DisplayName` em português descrevendo o comportamento, e `@Nested` para agrupar cenários.
- Cubra o caminho feliz e cada erro de regra de negócio.

## Fluxo de trabalho

- Tarefas vivem em **GitHub Issues**, agrupadas em Milestones (fases do `docs/ROADMAP.md`) e no quadro do GitHub Projects "Blog-API". Não recrie um backlog em `.md`.
- Branches saem da `develop`: `feature/`, `fix/`, `refactor/`, `docs/` + descrição curta em inglês e kebab-case.
- PRs vão para a `develop`, usam `Closes #N` e seguem o checklist da Definição de Pronto em `.github/pull_request_template.md`.
- A `main` só recebe a `develop` ao fechar um Milestone.

### Regras para o Claude (obrigatórias)

- **Nunca mesclar nada.** Não rode `git merge`, não faça merge de PR (`gh pr merge`), não ative auto-merge e não faça rebase/push que leve mudanças para a `develop` ou a `main`. O merge é sempre feito pela autora.
- **Pode abrir PR**, sempre para a `develop`, a partir de um branch próprio.
- **Pode fazer commits**, mas **curtos e passo a passo**: um commit por mudança lógica (ex. migration, depois entidade, depois caso de uso, depois testes), nunca tudo de uma vez em um commit só.
- Mensagens de commit em português, curtas e descritivas (ex. "Adiciona migration da tabela de sessões").
- Nunca commitar direto na `develop` ou na `main`.
- **Nunca incluir links de sessão ou de chat do Claude** (claude.ai, sessões, conversas) em commits, PRs, issues, comentários ou qualquer arquivo. Pode indicar que foi feito com IA (ex. a linha `Co-Authored-By` do Claude ou "Gerado com Claude Code"), mas sem link para a sessão.

## Definição de Pronto

Uma tarefa só está pronta quando: compila sem warnings, tem testes das regras centrais, os endpoints foram validados via `.http`, o README e o Swagger estão atualizados, e o PR foi mergeado na `develop`.
