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

- [ ] **#1 Tratamento Global de Exceções**

  **Motivação:** Uma API RESTful profissional não cospe *stacktraces* do Java na cara do cliente. Precisamos centralizar o tratamento de erros e padronizar as respostas (baseado na RFC 7807 - *Problem Details for HTTP APIs*), para facilitar a integração, ocultar detalhes da infraestrutura (segurança) e exibir mensagens amigáveis.

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

  **Motivação:** O GRIFO é uma plataforma literária com potencial para alcance internacional. A API deve ser capaz de retornar mensagens adaptadas ao idioma do cliente com base no header `Accept-Language`.

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


- [ ] **#3 Configuração Base de Segurança com JWT**

  **Motivação:** O GRIFO precisa proteger os dados de seus leitores e autores. O uso de JSON Web Tokens (JWT) permite uma arquitetura *Stateless* (sem estado), que é altamente escalável, rápida e o padrão da indústria para APIs REST.

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

## Próximos Passos

Aqui ficam as funcionalidades mapeadas para o futuro. Quando a seção “Execução Imediata” esvaziar, deve ser puxado itens daqui, detalhado os critérios técnicos e movido para cima.

1. Módulo de Usuário: Cadastro de Usuário
2. Documentação Automatizada com OpenAPI/Swagger (`springdoc-openapi`).
3. Módulo de Autenticação: Endpoint de Login (gerando o JWT).
4. Entidades JPA: Modelagem e mapeamento das relações Livro x Autor x Resenha.
5. Integração AWS S3 (Upload de foto de perfil).