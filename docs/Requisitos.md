# Requisitos

Requisitos funcionais (RF) e não funcionais (RNF) do Blog-API. As regras detalhadas de cada requisito ficam em [Regras_de_Negócio.md](Regras_de_Negócio.md) (RN-xxx). O andamento de cada um fica nas Issues do GitHub.

**Status:** ✅ Implementado · 🟡 Parcial · ⬜ Planejado

**Como manter:** um requisito novo recebe o próximo número do grupo, e números nunca são reaproveitados. Ao abrir uma issue, cite os RF/RN que ela atende. Ao fechar a issue, atualize o status aqui.

---

## 1. Requisitos Funcionais

### 1.1 Identidade e acesso (`module:user`)

| ID | Requisito | Regras | Versão | Status |
|---|---|---|---|---|
| RF-001 | Cadastrar usuário com nome, e-mail e senha | RN-001 a RN-005 | v0.0 | ✅ |
| RF-002 | Cadastrar usuário com conta Google (ID Token) | RN-001, RN-003, RN-006, RN-007 | v0.0 | ✅ |
| RF-003 | Fazer login com e-mail ou nickname e senha | RN-008, RN-009 | v0.0 | ✅ |
| RF-004 | Fazer login com conta Google | RN-008, RN-010 | v0.0 | ✅ |
| RF-005 | Renovar o acesso com Refresh Token, com rotação | RN-011 a RN-013 | v0.1 | ⬜ |
| RF-006 | Fazer logout, encerrando a sessão atual | RN-011 | v0.1 | ⬜ |
| RF-007 | Listar e revogar as próprias sessões (dispositivos) | RN-014 | v0.1 | ⬜ |
| RF-008 | Ativar a conta local com código enviado por e-mail | RN-015 a RN-017 | v0.1 | ⬜ |
| RF-009 | Recuperar a senha esquecida por e-mail | RN-015, RN-018, RN-019 | v0.1 | ⬜ |
| RF-010 | Trocar a senha estando logado, com código por e-mail | RN-015, RN-020 | v0.1 | ⬜ |
| RF-011 | Ativar e usar 2FA com app autenticador (TOTP) | RN-021 | v0.4 | ⬜ |
| RF-012 | Vincular e desvincular conta Google | RN-022 | v0.4 | ⬜ |

### 1.2 Autoria e conteúdo (`module:article`)

| ID | Requisito | Regras | Versão | Status |
|---|---|---|---|---|
| RF-020 | Leitor solicita tornar-se autor | RN-030, RN-031 | v0.2 | ⬜ |
| RF-021 | Administrador aprova ou recusa pedidos de autoria | RN-032 | v0.2 | ⬜ |
| RF-022 | Administrador gerencia categorias hierárquicas | RN-033, RN-034 | v0.2 | ⬜ |
| RF-023 | Autor cria, edita, salva como rascunho, publica e arquiva artigos | RN-035 a RN-038 | v0.2 | ⬜ |
| RF-024 | Autor associa tags aos artigos, e o público filtra por tag | RN-039 | v0.2 | ⬜ |
| RF-025 | Upload de avatar e de capa de artigo | RN-040 | v0.2 | ⬜ |
| RF-026 | Buscar artigos por texto, categoria, tag e autor | RN-041 | v0.3 | ⬜ |

### 1.3 Social (`module:social`)

| ID | Requisito | Regras | Versão | Status |
|---|---|---|---|---|
| RF-030 | Comentar e responder comentários em artigos | RN-050, RN-051 | v0.3 | ⬜ |
| RF-031 | Reagir a artigos | RN-052 | v0.3 | ⬜ |
| RF-032 | Seguir e deixar de seguir autores | RN-053 | v0.3 | ⬜ |
| RF-033 | Ver o feed com os artigos dos autores seguidos | RN-054 | v0.3 | ⬜ |

### 1.4 Moderação (`module:moderation`)

| ID | Requisito | Regras | Versão | Status |
|---|---|---|---|---|
| RF-040 | Denunciar artigos e comentários | RN-060 | v0.4 | ⬜ |
| RF-041 | Analisar denúncias e aplicar ações (arquivar, ocultar, penalizar) | RN-061 | v0.4 | ⬜ |

---

## 2. Requisitos Não Funcionais

| ID | Categoria | Requisito | Status |
|---|---|---|---|
| RNF-001 | Segurança | Senhas armazenadas apenas com hash BCrypt | ✅ |
| RNF-002 | Segurança | Autenticação stateless com JWT assinado (HMAC256), entregue em cookie `HttpOnly` e `SameSite=Strict` | ✅ |
| RNF-003 | Segurança | Todo endpoint é protegido por padrão; rotas públicas são listadas explicitamente | ✅ |
| RNF-004 | Segurança | Cookies com `Secure=true` fora do ambiente local | ⬜ (hoje `false` fixo no código) |
| RNF-005 | Segurança | Segredos (chave JWT, banco, Google, AWS, SMTP) só em variáveis de ambiente, nunca versionados | ✅ |
| RNF-006 | Segurança | Tokens de refresh e códigos OTP guardados apenas como hash | ⬜ |
| RNF-007 | Segurança | Limite de tentativas e de frequência em login, códigos e reenvios | ⬜ |
| RNF-008 | Internacionalização | Mensagens de erro em pt-BR e en, escolhidas pelo `Accept-Language` | ✅ |
| RNF-009 | Padronização | Todo erro segue o formato `ErrorResponseDTO` (status, erro, mensagem, caminho, erros de validação) | ✅ |
| RNF-010 | Documentação | API documentada com OpenAPI/Swagger | ⬜ |
| RNF-011 | Manutenibilidade | Pragmatic Clean Architecture por funcionalidade (ver [Arquitetura.md](Arquitetura.md)) | ✅ |
| RNF-012 | Banco de dados | Esquema versionado só via Flyway; `ddl-auto` sem gerar tabelas | ✅ |
| RNF-013 | Testabilidade | Testes unitários para cada caso de uso e testes de controller (`@WebMvcTest`) para cada endpoint | 🟡 |
| RNF-014 | Desempenho | Listagens sempre paginadas | ⬜ |
| RNF-015 | Portabilidade | Executa localmente com Java 25 e PostgreSQL, sem Docker | ✅ |
| RNF-016 | Versionamento da API | Rotas prefixadas com `/api/v1` | ✅ |
