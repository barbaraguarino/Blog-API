# Plano de Testes

## 1. Objetivo

Garantir que cada regra de negócio ([REGRAS_DE_NEGOCIO.md](../REGRAS_DE_NEGOCIO.md)) e cada endpoint funcionem como especificado, e que mudanças futuras não quebrem o que já funciona. Este plano define **o que** testar, **como** testar e **quando** um teste é obrigatório.

## 2. Escopo

| Dentro | Fora (por enquanto) |
|---|---|
| Casos de uso (regras de negócio) | Testes de carga e desempenho |
| Controllers (contrato HTTP, status, validação, erros) | Testes de interface (não há front-end) |
| Segurança (rotas públicas e protegidas, cookie, papéis) | Testes contra os serviços reais do Google e da AWS |
| Entidades de domínio (fábricas, regras internas) | |
| Repositórios com consultas próprias | |
| Migrations Flyway (sobem num banco limpo) | |

## 3. Níveis de teste

| Nível | Ferramentas | O que cobre | Onde fica |
|---|---|---|---|
| **Unitário: domínio** | JUnit 5, AssertJ | Fábricas e regras das entidades (ex. geração de nickname) | `src/test/.../modules/<m>/domain/` |
| **Unitário: caso de uso** | JUnit 5, Mockito, AssertJ | Cada regra de negócio, com repositórios e gateways simulados | `src/test/.../modules/<m>/services/` |
| **Controller (fatia web)** | `@WebMvcTest`, `MockMvc`, `@MockitoBean`, `SecurityConfig` importado | Status HTTP, validação, formato de erro, cookies, rotas públicas e protegidas | `src/test/.../modules/<m>/controllers/` |
| **Integração** | `@SpringBootTest` + PostgreSQL de teste | Fluxos ponta a ponta, migrations, consultas JPA | `src/test/.../integration/` *(a criar)* |
| **Manual** | Arquivos `.http` (HTTP Client do IntelliJ) | Validação exploratória dos endpoints | `requests/<modulo>/` |

## 4. Critérios

### Quando um teste é obrigatório
- **Toda RN implementada** tem pelo menos um teste de caso de uso que a comprova. O nome do teste (`@DisplayName`) ou um comentário cita o ID, ex. `RN-001`.
- **Todo endpoint** tem testes de controller para: sucesso, validação (400), cada erro de negócio que pode retornar e acesso sem autenticação quando a rota é protegida (401/403).
- **Todo bug corrigido** ganha um teste que falhava antes da correção.

### Entrada (para começar a testar uma issue)
- Critérios de aceite da issue escritos.
- RN correspondentes registradas no REGRAS_DE_NEGOCIO.md.

### Saída (para considerar a issue pronta)
- Todos os testes passando com `./mvnw test`.
- Todos os critérios de aceite cobertos por teste automatizado ou validados via `.http`.
- Nenhum teste desativado (`@Disabled`) sem uma issue que explique o motivo.

## 5. Padrões de escrita

- `@DisplayName` em português, descrevendo o comportamento esperado ("Deve retornar 409 quando o e-mail já existir").
- `@Nested` para agrupar cenários do mesmo endpoint ou caso de uso.
- Estrutura **Arrange / Act / Assert**, com um comportamento por teste.
- Nada de dependência externa real nos testes unitários e de controller: Google, e-mail e S3 sempre simulados.

## 6. Matriz de rastreabilidade (regras implementadas)

| Regra | Coberta por | Situação |
|---|---|---|
| RN-001 e-mail único | `RegisterLocalUserServiceTest`, `RegisterGoogleUserServiceTest`, `UserRegistrationControllerTest` | ✅ |
| RN-002 nome/e-mail válidos | `UserRegistrationControllerTest` (DTO inválido → 400, genérico) | 🟡 não testa cada limite |
| RN-003 papel e estado inicial | — | ⬜ falta teste de domínio |
| RN-004 política de senha | `UserRegistrationControllerTest` (DTO inválido, genérico) | 🟡 não testa cada exigência |
| RN-005 geração de nickname | — | ⬜ falta teste de domínio (formato, limite de 40, sufixo) |
| RN-006 cadastro Google | `RegisterGoogleUserServiceTest`, `UserRegistrationControllerTest` | ✅ |
| RN-007 googleId único | `RegisterGoogleUserServiceTest`, `UserRegistrationControllerTest` | ✅ |
| RN-008 JWT em cookie HttpOnly | `AuthControllerTest` | ✅ |
| RN-009 login por e-mail ou nickname | `AuthenticateLocalUserServiceTest`, `AuthControllerTest` | 🟡 falta testar `CustomUserDetailsService` (ramo e-mail vs nickname) e conta inativa/bloqueada → 403 |
| RN-010 login Google | `AuthenticateGoogleUserServiceTest`, `AuthControllerTest` | ✅ |
| Segurança: `TokenService` e `SecurityFilter` | — | ⬜ falta teste de geração, validação, expiração e emissor |

## 7. Ambiente

- Java 25, Maven Wrapper (`./mvnw`).
- Os testes unitários e de controller não precisam de banco.
- O teste `BlogApiApplicationTests` (`contextLoads`) sobe o contexto completo e precisa do PostgreSQL e do `.env` configurados.
- *(Planejado)* banco de teste dedicado para os testes de integração.

## 8. Execução e relatórios

- Antes de abrir PR: `./mvnw test`.
- A cada fechamento de Milestone, ou quando houver mudança relevante, registre um relatório em [`relatorios/`](relatorios/) com o modelo do [README dos relatórios](relatorios/README.md).
- *(Planejado)* CI com GitHub Actions rodando `./mvnw test` em todo PR para a `develop`.
