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

- [ ] **#4 Módulo de Usuário: Cadastro de Novos Usuário**

    1. **Objetivo**: Criar o alicerce do primeiro domínio de negócio (User). Implementar a entidade JPA, a migração do banco de dados, o repositório, o serviço com regras de negócio e o endpoint REST para criação de contas locais com senhas criptografadas.
    2. **Prioridade**: P1
    3. **Tamanho**: M
    4. **Tag**: `feat` + `core`
    5. **Critérios de Aceitação**:
        - [ ]  Criação do script Flyway (`V1__create_table_users.sql`) com campos essenciais.
        - [ ]  Criação da entidade `User` (JPA).
        - [ ]  Criação do `UserRegistrationDTO` com Bean Validation.
        - [ ]  Implementação do `UserService` com a validação: se o e-mail já existir, lançar a `BusinessException` usando a chave `error.user.already_exists`.
        - [ ]  Criptografia da senha usando `BCryptPasswordEncoder` antes de salvar no banco.
        - [ ]  Endpoint `POST /api/v1/users/register` liberado no `SecurityConfig`.
    6. **Testes de Aceitação**:
        - [ ]  Enviar um payload válido deve retornar HTTP 201 (Created) e o DTO do usuário sem a senha.
        - [ ]  Tentar cadastrar um e-mail já existente deve retornar HTTP 409 (Conflict) formatado pelo *GlobalExceptionHandler*.
        - [ ]  Enviar payload com e-mail inválido ou senha fraca deve retornar HTTP 400 (Bad Request) com lista de campos inválidos.

- [ ] **#5 Módulo de Autenticação: Endpoint de Login Local**

    1. **Objetivo**: Implementar o fluxo de autenticação tradicional por e-mail e senha, integrando o `AuthenticationManager` do Spring Security com o nosso `JwtTokenProvider` para devolver um token válido ao front-end usando HttpOnly Cookies.
    2. **Prioridade**: P1
    3. **Tamanho**: M
    4. **Tag**: `feat` + `sec`
    5. **Critérios de Aceitação**:
        - [ ]  Configuração do `AuthenticationManager` e do `PasswordEncoder` no `SecurityConfig` ou em nova classe de configuração de beans.
        - [ ]  Criação do `CustomUserDetailsService` implementando a interface `UserDetailsService` para buscar o usuário no banco via e-mail.
        - [ ]  Criação do `LoginRequestDTO` com dados necessários.
        - [ ]  O endpoint não deve devolver o token no corpo do JSON. Ele deve injetar o token em um cookie HTTP com as flags `HttpOnly`, `Secure` e `SameSite=Strict`.
        - [ ]  Criação do `AuthController` com o endpoint `POST /api/v1/auth/login`.
    6. **Testes de Aceitação**:
        - [ ]  Enviar credenciais corretas deve retornar HTTP 200 (OK) e o JWT.
        - [ ]  Enviar senha incorreta deve retornar HTTP 401 (Unauthorized) ou 403 (Forbidden) mapeado corretamente via exceção.
        - [ ]  Enviar um e-mail não cadastrado deve retornar HTTP 401/403.
        - [ ]  Validação de credenciais e verificação se o header `Set-Cookie` está presente na resposta HTTP 200.

- [ ] **#6 Módulo de Usuário: Cadastro via Google (OAuth2/OIDC)**

    1. **Objetivo**: Permitir que usuários criem uma conta no GRIFO delegando a responsabilidade da senha para o Google. A API deve receber e validar um Google ID Token gerado no front-end.
    2. **Prioridade**: P2
    3. **Tamanho**: L
    4. **Tag**: `feat` + `sec`
    5. **Critérios de Aceitação**:
        - [ ]  Inclusão da biblioteca oficial do Google (`google-api-client`) no `pom.xml` para validação de assinatura de tokens.
        - [ ]  Atualização da tabela e entidade `User` para suportar o tipo de provedor de login (`LOCAL`, `GOOGLE`).
        - [ ]  Criação do endpoint `POST /api/v1/users/register/google` recebendo um `GoogleTokenDTO`.
        - [ ]  Implementação de serviço que intercepta o token, valida a assinatura direto com os servidores do Google (JWKS) e extrai payload (email, nome, foto).
        - [ ]  Salvar o usuário no banco com uma senha nula/randômica (já que o login é delegado) e provedor `GOOGLE`.
    6. **Testes de Aceitação**:
        - [ ]  Enviar um Google ID Token válido cria um usuário e retorna HTTP 201.
        - [ ]  Tentar cadastrar via Google usando um e-mail que já existe como conta `LOCAL` deve lançar exceção de conflito de provedor.
        - [ ]  Enviar um Google Token forjado ou expirado deve retornar HTTP 401 (Unauthorized).

- [ ] **#7 Módulo de Autenticação: Login via Google**

    1. **Objetivo**: Autenticar um usuário existente através de um Google ID Token. Uma vez validado pelo Google, a nossa API emite o token nativo do GRIFO para o cliente navegar de forma unificada.
    2. **Prioridade**: P2
    3. **Tamanho**: M
    4. **Tag**: `feat` + `sec`
    5. **Critérios de Aceitação**:
        - [ ]  Criação do endpoint `POST /api/v1/auth/login/google`.
        - [ ]  Validação do Google ID Token usando o validador criado na Tarefa #6.
        - [ ]  Busca do usuário no banco pelo e-mail extraído do token do Google.
        - [ ]  Geração do token JWT nativo do GRIFO pelo `JwtTokenProvider` se o usuário existir e o provedor for compatível.
    6. **Testes de Aceitação**:
        - [ ]  Enviar token válido de um usuário Google existente retorna HTTP 200 (OK) e o JWT do GRIFO.
        - [ ]  Enviar token válido, mas de um usuário não cadastrado no banco, retorna HTTP 404 (Not Found) - instruindo o Front-end a redirecionar para a tela de registro.

## Próximos Passos

Aqui ficam as funcionalidades mapeadas para o futuro. Quando a seção “Execução Imediata” esvaziar, deve ser puxado itens daqui, detalhado os critérios técnicos e movido para cima.

1. Catálogo Literário: Gestão de Livros e Gêneros
2. Sistema de Autoria: Solicitação e Aprovação
3. Módulo de Resenhas: Publicação e Rascunhos
4. Interações Sociais: Comentários e Reações
5. Integração AWS S3: Upload de Mídias
6. Módulo de Moderação: Sistema de Denúncias
7. Documentação Automatizada com OpenAPI/Swagger
8. Módulo de Usuário Avançado: Recuperação de Senha e Verificação de E-mail
9. Módulo de Perfil: Vincular Conta ao Google
10. Módulo de Perfil: Desvincular Google