# Registro de Decisões

Decisões de arquitetura, tecnologia e processo do Blog-API, no formato de ADR (Architecture Decision Record) simplificado. A entrada mais recente fica no fim da lista.

**Como registrar:** copie o modelo, use o próximo número e não apague decisões antigas. Se uma decisão mudar, crie uma nova entrada e marque a anterior como `Substituída por D-XXX`.

```markdown
## D-XXX: Título curto

- **Data:** AAAA-MM-DD
- **Status:** Aceita | Substituída por D-YYY | Descartada
- **Contexto:** que problema ou pergunta levou à decisão.
- **Decisão:** o que foi escolhido.
- **Alternativas consideradas:** o que foi descartado e por quê.
- **Consequências:** o que muda, custos e cuidados daqui para frente.
```

---

## D-001: Pragmatic Clean Architecture organizada por funcionalidade

- **Data:** anterior a 2026-11 (registrada retroativamente)
- **Status:** Aceita
- **Contexto:** o projeto precisa crescer em módulos independentes (usuário, artigos, social, moderação) sem virar um amontoado de camadas globais.
- **Decisão:** pacotes por funcionalidade em `modules/<modulo>/`, cada um com as camadas `domain`, `application`, `infrastructure` e `presentation`. O que é global fica em `core/` e as integrações externas em `infrastructure/providers/`.
- **Alternativas consideradas:** pacotes por camada (`controllers/`, `services/`, `repositories/` globais), que espalham um mesmo assunto por todo o projeto.
- **Consequências:** cada módulo novo replica as 4 camadas. Casos de uso são classes de ação única (`<Verbo><Objeto>Service.execute`).

## D-002: JWT stateless entregue em cookie HttpOnly

- **Data:** anterior a 2026-11 (registrada retroativamente)
- **Status:** Aceita (será complementada pela issue de Refresh Token)
- **Contexto:** a API precisa autenticar sem guardar sessão em memória no servidor.
- **Decisão:** Spring Security em modo `STATELESS` com JWT, enviado ao cliente em cookie `HttpOnly`.
- **Alternativas consideradas:** token no corpo da resposta com o header `Authorization: Bearer`, que expõe o token ao JavaScript do cliente (risco de XSS).
- **Consequências:** é preciso cuidar de CSRF/SameSite nos cookies. A revogação de sessão exige o Refresh Token persistido (planejado para a v0.1).

## D-003: Login Google via validação do ID Token no backend

- **Data:** anterior a 2026-11 (registrada retroativamente)
- **Status:** Aceita
- **Contexto:** permitir cadastro e login com Google sem a API guardar senha desses usuários.
- **Decisão:** o cliente obtém o Google ID Token e a API valida a assinatura (`google-api-client`), emitindo depois o JWT próprio do Blog.
- **Alternativas consideradas:** fluxo OAuth2 completo com redirect no backend (`spring-boot-starter-oauth2-client`), mais complexo para uma API sem front-end.
- **Consequências:** login unificado: depois da validação, o usuário Google navega com o mesmo JWT que o usuário local.

## D-004: Login por e-mail ou nickname

- **Data:** anterior a 2026-11 (registrada retroativamente)
- **Status:** Aceita
- **Contexto:** facilitar o login para quem lembra só do nickname.
- **Decisão:** o campo de login aceita e-mail ou nickname, e a busca do usuário tenta os dois.
- **Consequências:** e-mail e nickname precisam ser únicos e não podem colidir entre si.

## D-005: Mensagens de erro externalizadas com i18n

- **Data:** anterior a 2026-11 (registrada retroativamente)
- **Status:** Aceita
- **Contexto:** a API responde em mais de um idioma.
- **Decisão:** exceções carregam uma chave (`error.<area>.<motivo>`), traduzida pelo `MessageSource` conforme o `Accept-Language`, com arquivos `messages`, `messages_pt_BR` e `messages_en`.
- **Consequências:** toda mensagem nova exige a chave nos três arquivos.

## D-006: Tarefas em GitHub Issues em vez de backlog em .md

- **Data:** 2026-11-15
- **Status:** Aceita
- **Contexto:** o `Backlog_Sequencial.md` exigia commits e PRs só para mover tarefas, e sua numeração (#01, #02…) colidia com a numeração de issues e PRs do GitHub.
- **Decisão:** cada tarefa vira uma Issue com template padrão, agrupada em Milestones (fases v0.1 a v0.4) e acompanhada em um quadro do GitHub Projects. O backlog virou o `docs/ROADMAP.md`, só com a visão geral.
- **Alternativas consideradas:** manter o `.md`, que funciona offline mas não tem rastreabilidade nem fechamento automático das tarefas.
- **Consequências:** PRs usam `Closes #N`. Uma issue só é fechada quando cumpre todos os critérios de aceite.

## D-007: Git Flow, com um PR por branch

- **Data:** 2026-11-15
- **Status:** Aceita
- **Contexto:** padronizar branches e merges e manter a `main` estável.
- **Decisão:** Git Flow com `main`, `develop`, `feature/*` (e variações `refactor/`, `docs/`, `test/`, `chore/`), `bugfix/*`, `release/*` e `hotfix/*`. Um branch por issue e um PR por branch. Detalhes no `CLAUDE.md`.
- **Alternativas consideradas:** GitHub Flow (só `main` + branches curtos), mais simples, mas sem a separação entre integração e versão fechada.
- **Consequências:** o fechamento de cada Milestone passa por um `release/vX.Y.Z`. O merge é sempre feito pela autora.

## D-008: Repriorização da 2FA com app autenticador

- **Data:** 2026-11-15
- **Status:** Aceita (proposta, pode ser revista pela autora)
- **Contexto:** o backlog antigo tinha quatro tarefas seguidas de autenticação antes de qualquer funcionalidade de blog.
- **Decisão:** a v0.1 fica com o essencial de identidade (sessões, e-mail, ativação, recuperação e troca de senha). A 2FA com TOTP foi para a v0.4, depois de Artigos e Social.
- **Consequências:** o núcleo do blog (artigos) é entregue antes. Para voltar à ordem original, basta mover a issue de milestone.

## D-009: Documentação do projeto em Markdown dentro do repositório

- **Data:** 2026-11-15
- **Status:** Aceita
- **Contexto:** o projeto precisava de definição, requisitos, regras de negócio, arquitetura e plano de testes formalizados, versionados junto com o código.
- **Decisão:** documentos `.md` em `docs/`, com IDs rastreáveis (RF-xxx, RNF-xxx, RN-xxx) citados nas issues e nos PRs. Relatórios de teste são datados e nunca editados depois de publicados.
- **Alternativas consideradas:** Wiki do GitHub ou Notion, que ficam fora do versionamento e do review dos PRs.
- **Consequências:** cada PR que muda comportamento também atualiza os documentos afetados.

## D-010: Licença MIT

- **Data:** 2026-11-15
- **Status:** Aceita
- **Contexto:** a licença anterior era privada (todos os direitos reservados), o que impedia o uso do código por terceiros.
- **Decisão:** adotar a licença MIT.
- **Consequências:** qualquer pessoa pode usar, modificar e distribuir o código, desde que mantenha o aviso de copyright. O software é fornecido sem garantia.
