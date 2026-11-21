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

## Documentação do projeto

Toda a documentação fica em `docs/`, em Markdown. Mantenha-a atualizada no mesmo PR da mudança de código:

| Arquivo | Atualize quando… |
|---|---|
| `docs/DEFINICAO_DO_PROJETO.md` | mudar escopo, papéis ou entregas |
| `docs/REQUISITOS.md` | um requisito (RF/RNF) for criado ou mudar de status |
| `docs/REGRAS_DE_NEGOCIO.md` | uma regra (RN) for criada, alterada ou implementada, ou uma divergência for corrigida |
| `docs/ARQUITETURA.md` | mudar estrutura, fluxo, modelo de dados, segurança ou stack |
| `docs/testes/PLANO_DE_TESTES.md` | mudar a estratégia de testes ou a matriz de rastreabilidade |
| `docs/testes/relatorios/` | ao fechar um Milestone (novo relatório, sem editar os antigos) |
| `docs/DECISOES.md` | for tomada qualquer decisão de projeto |

Issues e PRs citam os IDs que atendem (ex. `RF-005`, `RN-012`).

## Fluxo de trabalho

- Tarefas vivem em **GitHub Issues**, agrupadas em Milestones (fases do `docs/ROADMAP.md`) e no quadro do GitHub Projects "Blog-API". Não recrie um backlog em `.md`.
- Decisões de projeto ficam registradas em `docs/DECISOES.md`.

### Git Flow

| Branch | Sai de | PR para | Uso |
|---|---|---|---|
| `main` | — | — | Código em produção/versões fechadas. Só recebe `release/*` e `hotfix/*`. |
| `develop` | `main` | — | Integração do que está pronto para a próxima versão. |
| `feature/<nome>` | `develop` | `develop` | Funcionalidade nova. Variações aceitas para o mesmo papel: `refactor/`, `docs/`, `test/`, `chore/`. |
| `bugfix/<nome>` | `develop` | `develop` | Correção de bug ainda não lançado. |
| `release/vX.Y.Z` | `develop` | `main` **e** `develop` | Fechamento de um Milestone: só ajustes finais, versão e documentação. |
| `hotfix/<nome>` | `main` | `main` **e** `develop` | Correção urgente do que já está na `main`. |

- Nome do branch: descrição curta em inglês, kebab-case (ex. `feature/refresh-token-rotation`).
- **Um PR por branch** e **um branch por issue**. Não reaproveite branch para outra tarefa nem misture issues no mesmo PR.
- O PR usa `Closes #N` e segue o checklist da Definição de Pronto em `.github/pull_request_template.md`.

### Regras para o Claude (obrigatórias)

- **Nunca mesclar nada.** Não rode `git merge` entre branches, não faça merge de PR (`gh pr merge`), não ative auto-merge e não faça push direto que leve mudanças para a `develop` ou a `main`. O merge é sempre feito pela autora.
- **Pode abrir PR**, seguindo a tabela do Git Flow, sempre a partir de um branch próprio.
- **Pode fazer commits**, mas **curtos e passo a passo**: um commit por mudança lógica (ex. migration, depois entidade, depois caso de uso, depois testes), nunca tudo de uma vez em um commit só.
- Mensagens de commit em português, curtas e descritivas (ex. "Adiciona migration da tabela de sessões").
- Nunca commitar direto na `develop` ou na `main`.
- **Só feche uma issue (ou marque como concluída) se ela cumprir todos os critérios de aceite e a Definição de Pronto.** Se algum critério falhar ou não puder ser verificado, deixe a issue aberta e explique o que falta em um comentário.
- **Nunca incluir links de sessão ou de chat do Claude** (claude.ai, sessões, conversas) em commits, PRs, issues, comentários ou qualquer arquivo. Pode indicar que foi feito com IA (ex. a linha `Co-Authored-By` do Claude ou "Gerado com Claude Code"), mas sem link para a sessão.
- **Nunca baixar nem instalar nada sem aprovação prévia da autora**: programas, CLIs, extensões, dependências novas no `pom.xml`, plugins, scripts ou arquivos. Antes, diga o que é, de onde vem e por quê, e espere o "sim".
- **Só fontes oficiais e seguras**: Maven Central, site ou repositório oficial do fabricante, `winget` com o ID oficial do publicador. Nada de mirrors, binários soltos, pacotes desconhecidos ou scripts copiados de sites de terceiros.
- **Registre decisões de projeto** em `docs/DECISOES.md` sempre que escolher uma tecnologia, um padrão, uma regra de negócio não óbvia ou mudar algo já decidido. Se a decisão for da autora, registre com as palavras dela.

## Definição de Pronto

Uma tarefa só está pronta quando: compila sem warnings, tem testes das regras centrais, os endpoints foram validados via `.http`, o README e o Swagger estão atualizados, todos os critérios de aceite da issue foram cumpridos, e o PR foi mergeado na `develop`.
