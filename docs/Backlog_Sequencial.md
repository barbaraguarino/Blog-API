# Backlog Sequencial

Este documento centraliza as tarefas de desenvolvimento da API REST do Blog. O fluxo de trabalho é iterativo e simplificado, priorizando a entrega de valor real, a base estrutural e a persistência de dados antes de expor os endpoints e aplicar a segurança complexa.

## Definição de Pronto (DoD)

Para que qualquer item abaixo seja marcado como concluído (`[x]`), ele deve obrigatoriamente cumprir o seguinte checklist:

- [ ]  **Compilação:** O código compila sem erros ou *warnings*.
- [ ]  **Testes:** Cobertura de testes unitários e de integração validando as regras de negócio centrais.
- [ ]  **Validação:** Endpoints testados com sucesso via HTTPClient (arquivos `.http`).
- [ ]  **Documentação:** README e Swagger (OpenAPI) atualizados com as novas rotas.
- [ ]  **Versionamento:** Pull Request aprovado e mergeado na branch principal.

---

## Pronto Para Execução

Tarefas planejadas e prontas para serem desenvolvidas. Devem ser executadas sequencialmente.

- [ ]  **[#08] Sistema de Autoria: Solicitação para Publicação**
    - **Objetivo:** Permitir que usuários comuns (Role `READER`) solicitem permissão para se tornarem autores (Role `AUTHOR`).
    - **Regra de Negócio:** O usuário só pode solicitar essa transição de perfil se a conta tiver sido criada há mais de 2 meses. Validar a data de criação (`createdAt`) no backend e retornar erro de negócio caso o tempo seja insuficiente.
- [ ]  **[#09] Sistema de Autoria: Aprovação da Solicitação**
    - **Objetivo:** Criar um endpoint restrito para moderação.
    - **Regra de Negócio:** Permitir que usuários com Role `MODERATOR` ou `ADMIN` visualizem solicitações pendentes e aprovem (alterando o perfil do usuário para `AUTHOR`) ou rejeitem a solicitação de autoria.

---

## Próximos Passos

Tarefas mapeadas para o futuro. Serão detalhadas e puxadas para a seção "Pronto Para Execução" conforme a necessidade.

- [ ]  Módulo de Artigos: CRUD de Publicações e Rascunhos.
- [ ]  Interações Sociais: Sistema de Comentários e Reações nos artigos.
- [ ]  Integração Cloud: Configuração do AWS S3 para Upload de Mídias (avatares e capas de artigos).
- [ ]  Módulo de Moderação: Sistema de Denúncias de conteúdo abusivo.
- [ ]  Módulo de Usuário Avançado: Fluxo de Recuperação de Senha e Verificação de E-mail via SMTP.
- [ ]  Módulo de Perfil: Vincular Conta Existente ao Google.
- [ ]  Módulo de Perfil: Desvincular Conta do Google.

---

## Completas

Histórico de tarefas já executadas, testadas e integradas ao projeto.

- [x]  **[#01] Tratamento Global de Exceções:** Criação de interceptador (`@RestControllerAdvice`) para capturar exceções e formatá-las em um DTO padrão de erro (ApiErrorResponse).
- [x]  **[#02] Internacionalização (i18n):** Configuração do `MessageSource` do Spring Boot para externalizar mensagens em arquivos `.properties`, permitindo tradução dinâmica via cabeçalho HTTP.
- [x]  **[#03] Configuração Base de Segurança com JWT:** Configuração do Spring Security (`STATELESS`), blindagem de endpoints e mecanismo de geração/validação de tokens JWT.
- [x]  **[#04] Módulo de Usuário: Cadastro de Novos Usuários:** Criação do domínio de User (JPA, Flyway, Repository, Service) e endpoint REST para registro local com senha criptografada (`BCrypt`).
- [x]  **[#05] Módulo de Autenticação: Login Local:** Implementação do fluxo via e-mail e senha, integrando `AuthenticationManager` e retornando JWT encapsulado em `HttpOnly Cookies`.
- [x]  **[#06] Módulo de Usuário: Cadastro via Google (OAuth2/OIDC):** Endpoint que delega a responsabilidade da senha, recebendo e validando a assinatura matemática de um Google ID Token.
- [x]  **[#07] Módulo de Autenticação: Login via Google:** Autenticação de usuário existente via Google ID Token, emitindo o JWT nativo do Blog para navegação unificada.