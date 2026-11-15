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

- [ ]  **[#09] Gestão de Sessões: Refresh Tokens e Dispositivos**
   - **Objetivo:** Mudar a estratégia de autenticação para permitir o controle de sessões ativas (Padrão Microsoft/Google).
   - **Regra de Negócio:** Criar entidade para guardar IP, User-Agent, Data de Expiração e RefreshToken. O JWT passa a durar apenas 15 minutos, enquanto o Refresh Token dura 7 dias. Criar endpoint para listar e revogar sessões ativas.

- [ ]  **[#10] Verificação de Duas Etapas (2FA): Infraestrutura de E-mail (SMTP)**
   - **Objetivo:** Integrar a API com um servidor SMTP e criar a tabela de Códigos Temporários (OTP) utilizando a AWS.
   - **Regra de Negócio:** Alterar o fluxo de "Cadastro de Novos Usuários". A conta nasce bloqueada (`enabled=false`). Enviar código de 6 dígitos para o e-mail. Criar endpoint para ativar a conta.

- [ ]  **[#11] Segurança: Troca de Senha com Verificação de Segurança**
   - **Objetivo:** Permitir que usuários logados alterem suas senhas.
   - **Regra de Negócio:** O usuário deve fornecer a senha antiga, a nova senha, e um código de verificação enviado para o e-mail no momento da solicitação.

- [ ]  **[#12] Verificação de Duas Etapas (2FA): Google Authenticator (TOTP)**
   - **Objetivo:** Permitir que o usuário vincule um aplicativo autenticador como método de segurança adicional ao login.

---

## Próximos Passos

Tarefas mapeadas para o futuro.

- [ ]  Sistema de Autoria: Solicitação para Publicação (Usuários com mais de 2 meses).
- [ ]  Sistema de Autoria: Aprovação da Solicitação (Moderação).
- [ ]  Módulo de Artigos: CRUD de Publicações e Rascunhos.
- [ ]  Interações Sociais: Sistema de Comentários e Reações nos artigos.
- [ ]  Integração Cloud: Configuração do AWS S3 para Upload de Mídias (avatares e capas de artigos).
- [ ]  Módulo de Moderação: Sistema de Denúncias de conteúdo abusivo.
- [ ]  Módulo de Perfil: Vincular/Desvincular Conta do Google.

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
- [x]  **[#08] Módulo de Autenticação: Login Dinâmico (E-mail ou Nickname)**: Permitir que o usuário insira tanto o e-mail quanto o nickname no campo de login.