# Backlog Sequencial

## Definição de Ponto

Para que qualquer item abaixo seja marcado como `[x]`, ele deve cumprir:

- [ ]  O código compila sem erros e warnings.
- [ ]  Testes unitários cobrem as regras de negócio principais.
- [ ]  A funcionalidade foi testada via Postman/Swagger.
- [ ]  Documentação (Swagger/README) atualizada.
- [ ]  Pull Request aprovado e mergeado na branch `dev`.

## Guia de Classificação e Etiquetas

Para manter o fluxo de trabalho do **GRIFO** organizado e previsível, utilizamos um sistema tridimensional de classificação para cada Issue no GitHub. Isso nos permite filtrar o backlog rapidamente por urgência (Prioridade), esforço (Tamanho) e domínio técnico (Tag).

## Prioridade

Define a **ordem de execução**. Responde à pergunta: "O quão urgente é isso para o negócio?"

| Etiqueta | Significado | Quando Usar? |
| --- | --- | --- |
| **P0 (Critical)** | **Bloqueador / Emergência** | O sistema não roda, o build quebrou, há uma falha de segurança grave ou bloqueia todos os outros devs. **Ação:** Para tudo e resolve. |
| **P1 (High)** | **MVP Core** | Funcionalidades essenciais. Sem isso, o produto não cumpre seu propósito (ex: Login, Cadastro, Transação). Foco das primeiras Sprints. |
| **P2 (Medium)** | **Melhoria / Secundário** | Funcionalidades importantes, mas o produto "vive" sem elas por um tempo (ex: Dashboards, Relatórios, Filtros avançados). |
| **P3 (Low)** | **Cosmético / Futuro** | Ajustes visuais, refatorações não críticas ou ideias "nice-to-have" para versões 2.0. |

## Tamanho

*Define a **estimativa de esforço**. Responde à pergunta: "Quanto tempo/complexidade isso leva?"Nota: As horas são aproximadas e servem para evitar que peguemos mais trabalho do que conseguimos entregar.*

- **XS (Extra Small):** *< 8 horas (1 dia)*.
    - Ajustes de configuração (`application.properties`), correção de typo, bug simples, criar um DTO isolado.
- **S (Small):** *~ 16 horas (2 dias)*.
    - Tarefa de rotina. Criar um endpoint simples sem muita lógica de negócio, criar uma migração de banco simples.
- **M (Medium):** *~ 3 dias*.
    - Uma feature padrão. Envolve criar Controller, Service, Repository, Testes Unitários e tratar erros. (Ex: CRUD de Categorias).
- **L (Large):** *~ 4 a 5 dias (1 semana)*.
    - Funcionalidade complexa ou com integrações externas. (Ex: Configurar Security com JWT, Integração com Gateway de Pagamento).
- **XL (Extra Large):** *Bloqueante*.
    - **Regra:** Se uma tarefa for classificada como XL, ela é **grande demais**. Ela deve ser quebrada em 2 ou mais tarefas (ex: separar o Back-end do Front-end, ou separar a configuração da implementação).

## Tags de Domínio

Define a **natureza técnica** da tarefa. Responde à pergunta: "Onde no sistema eu vou mexer?"

- **`bug`:** Correção de falhas. Algo funcionava e parou, ou não funciona conforme a especificação.
- **`core`:** O coração do sistema. Lógica de domínio, regras de negócio financeiras, cálculos e arquitetura base.
- **`feat`:** (Feature) Nova funcionalidade perceptível para o usuário final. Gera valor direto.
- **`docs`:** Documentação. Atualizar o README, Swagger, diagramas ou Wiki.
- **`infra`:** DevOps. Docker, CI/CD (GitHub Actions), AWS, Banco de Dados, Logs.
- **`sec`:** Segurança. Autenticação, Autorização, Criptografia, Tokens, Sanitização de dados.
- **`question`:** Investigação (Spike). Quando não sabemos como fazer e precisamos de um tempo para estudar antes de codar.

## Pronto Para Execução

Estas tarefas já foram planejadas tecnicamente e devem ser executadas na ordem abaixo,

- [x] **#1 Tratamento Global de Exceções**

    1. **Objetivo**: Criar um interceptador global para capturar exceções lançadas em qualquer lugar da aplicação e formatá-las em um DTO padrão.
    2. **Prioridade**: P1
    3. **Tamanho**: S
    4. **Tag**: `core` + `sec`
    5. **Critérios de Aceitação**:
        - [x]  Criação de um `GlobalExceptionHandler` anotado com `@RestControllerAdvice`.
        - [x]  Criação de um objeto imutável `ApiErrorResponse` (DTO) contendo: `timestamp`, `status`, `error`, `message` e `path`.
        - [x]  Tratamento específico para `MethodArgumentNotValidException` (retorna HTTP 400 e a lista de campos inválidos).
        - [x]  Tratamento específico para `BusinessException` (exceção customizada nossa, retorna HTTP 400 ou 422).
        - [x]  Tratamento genérico para `Exception.class` (retorna HTTP 500 sem vazar a stacktrace real).
    6. **Testes de Aceitação**:
        - [x]  Simular um erro de validação (ex: e-mail inválido) e verificar se o JSON retorna a estrutura padronizada com HTTP 400.
        - [x]  Simular um erro de sistema e garantir que o retorno seja um genérico "Erro interno no servidor" com HTTP 500.

- [x] **#2 Internacionalização (i18n)**

    1. **Objetivo**: Configurar o `MessageSource` do Spring Boot para externalizar todas as mensagens da aplicação em ficheiros de propriedades, permitindo a tradução dinâmica das respostas da API baseada no idioma solicitado pelo cliente.
    2. **Prioridade**: P1
    3. **Tamanho**: M
    4. **Tag**: `core` + `docs`
    5. **Critérios de Aceitação**:
        - [x]  Configuração de `MessageSource` centralizada.
        - [x]  Arquivos criados:
            - `messages_pt_BR.properties`
            - `messages_en.properties`
        - [x]  Locale padrão definido como `pt-BR`.
        - [x]  Integrar com `GlobalExceptionHandler`.
    6. **Testes de Aceitação**:
        - [x]  Ao enviar **`Accept-Language: en`**, mensagens retornam em inglês.
        - [x]  Ao enviar **`Accept-Language: pt-BR`**, mensagens retornam em português.
        - [x]  Sem header, retorna idioma padrão.

- [x] **#3 Configuração Base de Segurança com JWT**

    1. **Objetivo**: Configurar o `Spring Security`, blindar todos os endpoints por padrão e implementar o mecanismo de geração e validação de tokens JWT.
    2. **Prioridade**: P0
    3. **Tamanho**: L
    4. **Tag**: `sec` + `core`
    5. **Critérios de Aceitação**:
        - [x]  Dependências `spring-boot-starter-security` e biblioteca JWT (ex: `java-jwt` da Auth0) instaladas.
        - [x]  Classe de configuração `SecurityConfig` criada, desativando CSRF e configurando a sessão como `STATELESS`.
        - [x]  Implementação do `JwtTokenProvider` (componente responsável por assinar e ler tokens).
        - [x]  Implementação do `JwtAuthenticationFilter` (estende `OncePerRequestFilter`) para interceptar requisições, ler o header `Authorization` e injetar o contexto de segurança.
        - [x]  Rotas públicas definidas explicitamente (ex: `/api/v1/users/register`, `/api/v1/auth/login`).
    6. **Testes de Aceitação**:
        - [x]  Acessar uma rota protegida sem token resulta em HTTP 401 (Unauthorized) ou 403 (Forbidden).
        - [x]  Acessar a mesma rota com um Bearer Token válido no header retorna sucesso.
        - [x]  Tentar usar um token expirado ou forjado retorna o erro tratado e não quebra a aplicação.

- [x] **#4 Módulo de Usuário: Cadastro de Novos Usuário**

    1. **Objetivo**: Criar o alicerce do primeiro domínio de negócio (User). Implementar a entidade JPA, a migração do banco de dados, o repositório, o serviço com regras de negócio e o endpoint REST para criação de contas locais com senhas criptografadas.
    2. **Prioridade**: P1
    3. **Tamanho**: M
    4. **Tag**: `feat` + `core`
    5. **Critérios de Aceitação**:
        - [x]  Criação do script Flyway (`V1__create_table_users.sql`) com campos essenciais.
        - [x]  Criação da entidade `User` (JPA).
        - [x]  Criação do `UserRegistrationDTO` com Bean Validation.
        - [x]  Implementação do `UserService` com a validação: se o e-mail já existir, lançar a `BusinessException` usando a chave `error.user.already_exists`.
        - [x]  Criptografia da senha usando `BCryptPasswordEncoder` antes de salvar no banco.
        - [x]  Endpoint `POST /api/v1/users/register` liberado no `SecurityConfig`.
    6. **Testes de Aceitação**:
        - [x]  Enviar um payload válido deve retornar HTTP 201 (Created) e o DTO do usuário sem a senha.
        - [x]  Tentar cadastrar um e-mail já existente deve retornar HTTP 409 (Conflict) formatado pelo *GlobalExceptionHandler*.
        - [x]  Enviar payload com e-mail inválido ou senha fraca deve retornar HTTP 400 (Bad Request) com lista de campos inválidos.

- [x] **#5 Módulo de Autenticação: Endpoint de Login Local**

    1. **Objetivo**: Implementar o fluxo de autenticação tradicional por e-mail e senha, integrando o `AuthenticationManager` do Spring Security com o nosso `JwtTokenProvider` para devolver um token válido ao front-end usando HttpOnly Cookies.
    2. **Prioridade**: P1
    3. **Tamanho**: M
    4. **Tag**: `feat` + `sec`
    5. **Critérios de Aceitação**:
        - [x]  Configuração do `AuthenticationManager` e do `PasswordEncoder` no `SecurityConfig` ou em nova classe de configuração de beans.
        - [x]  Criação do `CustomUserDetailsService` implementando a interface `UserDetailsService` para buscar o usuário no banco via e-mail.
        - [x]  Criação do `LoginRequestDTO` com dados necessários.
        - [x]  O endpoint não deve devolver o token no corpo do JSON. Ele deve injetar o token em um cookie HTTP com as flags `HttpOnly`, `Secure` e `SameSite=Strict`.
        - [x]  Criação do `AuthController` com o endpoint `POST /api/v1/auth/login`.
    6. **Testes de Aceitação**:
        - [x]  Enviar credenciais corretas deve retornar HTTP 200 (OK) e o JWT.
        - [x]  Enviar senha incorreta deve retornar HTTP 401 (Unauthorized) ou 403 (Forbidden) mapeado corretamente via exceção.
        - [x]  Enviar um e-mail não cadastrado deve retornar HTTP 401/403.
        - [x]  Validação de credenciais e verificação se o header `Set-Cookie` está presente na resposta HTTP 200.

- [x] **#6 Módulo de Usuário: Cadastro via Google (OAuth2/OIDC)**

    1. **Objetivo**: Permitir que usuários criem uma conta no GRIFO delegando a responsabilidade da senha para o Google. A API deve receber e validar um Google ID Token gerado no front-end.
    2. **Prioridade**: P2
    3. **Tamanho**: L
    4. **Tag**: `feat` + `sec`
    5. **Critérios de Aceitação**:
        - [x] Inclusão da biblioteca oficial do Google (google-api-client v2.9.0) no pom.xml para validação matemática de assinatura de tokens.
        - [x] Utilização da coluna google_id já existente na entidade/tabela User para identificar usuários delegados (se for nulo, é conta local; se preenchido, é conta Google).
        - [x] Configuração do GoogleIdTokenVerifier como um @Bean no Spring (GoogleAuthConfig) para garantir a Inversão de Controle e permitir testes unitários.
        - [x] Criação do endpoint POST /api/v1/register/google recebendo um GoogleTokenDTO validado.
        - [x] Implementação no UserRegistrationService para interceptar o token, validar a assinatura e extrair o payload (email, nome, subject/id).
        - [x] Salvar o usuário no banco gerando uma senha randômica (UUID), associando o google_id e retornando o UserResponseDTO.
        - [x] Mapeamento das exceções no i18n (messages_pt_BR.properties) para conflitos e tokens expirados.
    6. **Testes de Aceitação**:
        - [x] Enviar um Google ID Token válido cria um usuário e retorna HTTP 201 (Validado via Mockito).
        - [x] Tentar cadastrar via Google usando um e-mail que já existe como conta LOCAL (com google_id nulo) lança BusinessException mapeada como HTTP 409 (Conflict).
        - [x] Enviar um Google Token forjado ou expirado lança BusinessException mapeada como HTTP 401 (Unauthorized).

- [x] **#7 Módulo de Autenticação: Login via Google**

    1. **Objetivo**: Autenticar um usuário existente através de um Google ID Token. Uma vez validado pelo Google, a nossa API emite o token nativo do GRIFO para o cliente navegar de forma unificada.
    2. **Prioridade**: P2
    3. **Tamanho**: M
    4. **Tag**: `feat` + `sec`
    5. **Critérios de Aceitação**:
        - [x]  Criação do endpoint `POST /api/v1/auth/login/google`.
        - [x]  Validação do Google ID Token usando o validador criado na Tarefa #6.
        - [x]  Busca do usuário no banco pelo Google ID extraído do token do Google.
        - [x]  Geração do token JWT nativo do GRIFO pelo `JwtTokenProvider` se o usuário existir e o provedor for compatível.
    6. **Testes de Aceitação**:
        - [x]  Enviar token válido de um usuário Google existente retorna HTTP 200 (OK) e o JWT do GRIFO.
        - [x]  Enviar token válido, mas de um usuário não cadastrado no banco, retorna HTTP 404 (Not Found) - instruindo o Front-end a redirecionar para a tela de registro.

- [x] **#8 Catálogo Literário: Cadastro de Gênero (Padrão de Tradução)**

    1. **Objetivo**: Implementar o *endpoint* restrito de criação de gêneros literários. Para garantir um catálogo global, utilizaremos o padrão de Tabela de Tradução, separando o ID estrutural dos nomes em diferentes idiomas.
    2. **Prioridade**: P1
    3. **Tamanho**: S
    4. **Tag**: `feat` + `core` + `sec`
    5. **Critérios de Aceitação**:
        - [x]  Criação do script Flyway (`V2__create_table_genres.sql`) contendo apenas `id` (UUID) e os campos de auditoria (`created_at`, `updated_at`).
        - [x]  Criação do script Flyway (`V3__create_table_genre_translations.sql`) contendo `id`, `genre_id` (FK), `language_code` (Ex: pt-BR), `name` e `description`.
        - [x]  Criação das entidades JPA `Genre` e `GenreTranslation` com relacionamento `OneToMany`.
        - [x]  Criação do `GenreRequestDTO` que deve receber uma lista de traduções (exigindo pelo menos uma tradução padrão).
        - [x]  Implementação da regra de negócio: impedir a criação de traduções com nomes duplicados no mesmo idioma (ignorando *case sensitive*).
        - [x]  Criação do *endpoint* `POST /api/v1/genres` protegido com a *Role* `ADMIN`.
    6. **Testes de Aceitação**:
        - [x]  Enviar um *payload* válido (Admin) salva nas duas tabelas e retorna **HTTP 201**.
        - [x]  Tentar criar um gênero (Reader) retorna **HTTP 403**.
        - [x]  Tentar enviar um nome de gênero já existente no mesmo idioma retorna **HTTP 409**.

- [x] **#9 Catálogo Literário: Cadastro de Subgêneros**

    1. **Objetivo**: Implementar o padrão *Adjacency List* na entidade de Gêneros estruturais, permitindo hierarquia (ex: "Alta Fantasia" como filho de "Fantasia").
    2. **Prioridade**: P1
    3. **Tamanho**: S
    4. **Tag**: `feat` + `core`
    5. **Critérios de Aceitação**:
        - [x]  Criação do script Flyway (`V4__alter_table_genres_add_parent.sql`) adicionando a coluna `parent_id` referenciando `tb_genres`.
        - [x]  Atualização da entidade JPA `Genre` com o mapeamento bidirecional `@ManyToOne` (Pai) e `@OneToMany` (Filhos).
        - [x]  Criação do `SubgenreRequestDTO` contendo `@NotNull UUID parentId` e os dados de tradução.
        - [x]  Regra de negócio: Validar se o `parentId` existe (HTTP 404 se não).
        - [x]  Criação do *endpoint* `POST /api/v1/genres/{parentId}/subgenres` blindado para `ADMIN`.
    6. **Testes de Aceitação**:
        - [x]  Sucesso (HTTP 201) ao cadastrar subgênero em um pai válido.
        - [x]  Retorno HTTP 404 ao vincular a um `parentId` inexistente.

- [ ] **#10 Catálogo Literário: Cadastro de Autor (Padrão MARC 21 + Localização)**

    1. **Objetivo**: Criar o domínio de Autores isolando a forma como o nome é exibido da forma como ele é ordenado, e permitindo que a biografia seja cadastrada em múltiplos idiomas para suportar uma plataforma global.
    2. **Prioridade**: P1
    3. **Tamanho**: S
    4. **Tag**: `feat` + `core`
    5. **Critérios de Aceitação**:
        - [ ]  Criação do script Flyway (`V5__create_table_authors.sql`) contendo `id` (UUID), `display_name` (Ex: Stephen King), `sort_name` (Ex: King, Stephen), `birth_date` (DATE), `website` (VARCHAR) e campos de auditoria.
        - [ ]  Criação do script Flyway (`V6__create_table_author_localizations.sql`) contendo `id` (UUID), `author_id` (FK), `language_code` (Ex: pt-BR) e `biography` (TEXT).
        - [ ]  Criação das entidades JPA `Author` e `AuthorLocalization` com relacionamento `OneToMany`.
        - [ ]  Criação do `AuthorRequestDTO` que deve permitir o envio dos dados básicos e de uma lista de localizações para a biografia.
        - [ ]  Validação: O autor deve ter pelo menos uma biografia inicial (localização) no momento do cadastro.
        - [ ]  Criação do *endpoint* `POST /api/v1/authors` restrito a utilizadores com *Role* `ADMIN`.
    6. **Testes de Aceitação**:
        - [ ]  Enviar um *payload* com `display_name`, `sort_name` e ao menos uma biografia em um idioma válido deve retornar **HTTP 201 (Created)**.
        - [ ]  Tentar cadastrar um autor sem nenhuma localização de biografia deve retornar **HTTP 400 (Bad Request)**.
        - [ ]  Validar que os dados foram persistidos corretamente em ambas as tabelas (`tb_authors` e `tb_author_localizations`).

- [ ] **#11 Catálogo Literário: Cadastro de Série Literária (Localizada)**

    1. **Objetivo**: Criar a entidade de Séries/Coleções para agrupar obras. Assim como os livros, as séries terão títulos e descrições localizados para permitir que usuários de diferentes países encontrem a coleção pelo nome conhecido em sua região.
    2. **Prioridade**: P1
    3. **Tamanho**: XS
    4. **Tag**: `feat` + `core`
    5. **Critérios de Aceitação**:
        - [ ]  Criação do script Flyway (`V7__create_table_book_series.sql`) contendo `id` (UUID), `original_title` (O título original da obra, ex: *A Song of Ice and Fire*) e campos de auditoria.
        - [ ]  Criação do script Flyway (`V8__create_table_book_series_localizations.sql`) contendo `id` (UUID), `series_id` (FK), `language_code` (Ex: pt-BR), `localized_title` (Ex: *As Crônicas de Gelo e Fogo*) e `description` (TEXT).
        - [ ]  Criação das entidades JPA `BookSeries` e `BookSeriesLocalization`.
        - [ ]  Criação do `BookSeriesRequestDTO` que aceite os dados básicos e uma lista de localizações.
        - [ ]  Regra de Negócio: Impedir títulos duplicados dentro do mesmo idioma.
        - [ ]  Criação do *endpoint* `POST /api/v1/series` restrito a `ADMIN`.
    6. **Testes de Aceitação**:
        - [ ]  Cadastro de série com título original e tradução para PT-BR retorna **HTTP 201**.
        - [ ]  Tentar cadastrar uma série sem nenhuma localização (título/descrição) retorna **HTTP 400**.
        - [ ]  Tentar duplicar um título localizado já existente para o mesmo idioma retorna **HTTP 409**.

- [ ] **#12 Catálogo Literário: Cadastro de Livro Unificado (Letterboxd Style)**

    1. **Objetivo**: Criar a entidade central unificando todas as relações. O sistema tratará o Livro como uma entidade global e utilizará uma tabela de localização para títulos e sinopses em idiomas diferentes, reduzindo a fricção de cadastro.
    2. **Prioridade**: P1
    3. **Tamanho**: M
    4. **Tag**: `feat` + `core`
    5. **Critérios de Aceitação**:
        - [ ]  Criação do Flyway base (`V9__create_table_books.sql`) com `id`, `original_title`, `original_publication_date` (DATE), `cover_url` (VARCHAR), `series_id` (FK Nullable) e `series_volume` (INTEGER).
        - [ ]  Criação do Flyway de localização (`V10__create_table_book_localizations.sql`) com `id`, `book_id`, `language_code`, `localized_title` e `synopsis`.
        - [ ]  Criação dos *scripts* para tabelas N:M (`V9__create_table_books_authors.sql` e `V10__create_table_books_genres.sql`).
        - [ ]  Criação das entidades `Book` e `BookLocalization` (e seus mapeamentos `@ManyToMany`, `@ManyToOne` e `@OneToMany`).
        - [ ]  Criação do `BookRequestDTO` contendo os dados base, as listas de referências (`Set<UUID> authorIds`, `genreIds`, `UUID seriesId`) e a lista opcional de `BookLocalizationDTO`.
        - [ ]  Validações de Negócio: O livro deve ter no mínimo 1 autor e 1 gênero. Todos os IDs enviados devem existir no banco (HTTP 404 se falhar).
        - [ ]  Criação do *endpoint* `POST /api/v1/books` restrito a `ADMIN` ou `MODERATOR`.
    6. **Testes de Aceitação**:
        - [ ]  Cadastro completo (com IDs válidos e localizações) retorna HTTP 201 e salva em todas as tabelas envolvidas.
        - [ ]  Enviar `authorId` ou `genreId` inexistente retorna HTTP 404.
        - [ ]  Enviar lista de gêneros ou autores vazia retorna HTTP 400.

## Próximos Passos

Aqui ficam as funcionalidades mapeadas para o futuro. Quando a seção “Execução Imediata” esvaziar, deve ser puxado itens daqui, detalhado os critérios técnicos e movido para cima.

1. Sistema de Autoria: Solicitação e Aprovação
2. Módulo de Resenhas: Publicação e Rascunhos
3. Interações Sociais: Comentários e Reações
4. Catálogo Literário: Atualização de Livros, Gêneros, Autores, Séries Literárias.
5. Integração AWS S3: Upload de Mídias
6. Módulo de Moderação: Sistema de Denúncias
7. Documentação Automatizada com OpenAPI/Swagger
8. Módulo de Usuário Avançado: Recuperação de Senha e Verificação de E-mail
9. Módulo de Perfil: Vincular Conta ao Google
10. Módulo de Perfil: Desvincular Google