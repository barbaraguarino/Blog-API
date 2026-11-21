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
- **Decisão:** cada tarefa vira uma Issue com template padrão, agrupada em Milestones (fases v0.1 a v0.4) e acompanhada em um quadro do GitHub Projects. O backlog virou o `docs/Roadmap.md`, só com a visão geral.
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

## D-011: Segurança do repositório público e das issues

- **Data:** 2026-11-15
- **Status:** Aceita
- **Contexto:** o repositório é público (portfólio) e será operado também pelo Claude via `gh`. É preciso proteger segredos e a conta, evitar spam e impedir que conteúdo de terceiros seja tratado como instrução.
- **Decisão:** issues públicas, mas travadas automaticamente quando abertas pela autora (workflow `lock-own-issues.yml`, que usa só o `gh` dos runners); quadro do GitHub Projects privado; `.gitignore` reforçado para chaves e credenciais; token do `gh` com escopos mínimos; checklist em `docs/Segurança.md` e regras para o Claude no `CLAUDE.md`.
- **Alternativas consideradas:** desativar as issues (`gh repo edit --enable-issues=false`), que esconde o planejamento do portfólio; deixar as issues abertas a comentários, que expõe a spam.
- **Consequências:** terceiros não comentam nas issues da autora. O workflow só passa a valer depois de chegar à `main`; até lá, as issues são travadas manualmente ou pelo script de criação.

## D-012: Testes no mesmo pacote da classe testada e teste para toda classe

- **Data:** 2026-11-15
- **Status:** Aceita
- **Contexto:** os testes estavam nos pacotes antigos (`modules.user.controllers` e `modules.user.services`), que não existem mais em `src/main` depois da reorganização para a Clean Architecture. Isso quebra a navegação da IDE e deixa ambíguo onde criar testes novos.
- **Decisão:** uma classe de produção → uma classe de teste com o mesmo nome + `Test`, no mesmo pacote. **Toda** classe tem teste unitário, mesmo as simples, e cada tipo de classe tem também os testes do seu tipo (Mockito para casos de uso, `@WebMvcTest` para controllers, handler para exceções). Testes manuais são feitos com o Cliente HTTP do IntelliJ (arquivos `.http`), não com Postman.
- **Consequências:** os 6 testes existentes foram movidos sem mudar a lógica e continuam passando (27/27). Há 29 classes sem teste, listadas no Plano_de_Testes.md.

## D-013: DTOs obrigatórios e hierarquia própria de exceções

- **Data:** 2026-11-15
- **Status:** Aceita
- **Contexto:** formalizar dois padrões que o código já segue, para que nenhuma funcionalidade nova fuja deles.
- **Decisão:** toda entrada e saída da API é um `record` DTO, validado com Bean Validation e convertido por mappers, e entidades nunca saem da API. Erros usam só a hierarquia de `core/exceptions` (`DomainException` / `InfrastructureException`), com chave de i18n, e o `GlobalExceptionHandler` é o único lugar que monta a resposta de erro.
- **Consequências:** regras detalhadas nas seções *DTOs* e *Exceções* do CLAUDE.md. Uma exceção nova exige handler e testes.

## D-014: Scrum com documentação de Engenharia de Software

- **Data:** 2026-11-15
- **Status:** Substituída por D-015
- **Contexto:** nas palavras da autora: "Estou usando Engenharia de Software, método SCRUM, mas com documentação também."
- **Decisão:** o trabalho segue o Scrum adaptado a uma desenvolvedora (Product Backlog em Issues, Sprints no GitHub Projects, Definição de Preparado e de Pronto), e cada etapa de Engenharia de Software tem um documento em `docs/`, escrito com detalhes para explicar o projeto. Os Requisitos são listas simples (RF e RNF); as histórias de usuário e os critérios de aceite ficam nas issues.
- **Consequências:** o processo está descrito em `Processo_de_Desenvolvimento.md`. Os campos *Iteration* e *Story Points* e as colunas do quadro precisam ser configurados no Projects.

## D-015: Kanban simples no lugar do Scrum

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** nas palavras da autora: "O desenvolvimento não vai ser com SCRUM, eu queria, mas como é um projeto sozinha (bem eu e você), não tem porque usar SCRUM." O Scrum pressupõe uma equipe, com papéis, Sprints e cerimônias que, para uma pessoa só, viram burocracia.
- **Decisão:** "Um Kanban simples." O trabalho flui por um quadro no GitHub Projects, puxando uma issue de cada vez, sem Sprints, sem Story Points e sem cerimônias. As versões continuam sendo os Milestones do Roadmap, e a documentação de Engenharia de Software continua em `docs/`.
- **Alternativas consideradas:** manter o Scrum adaptado (D-014), que exigia planejar Sprints, estimar pontos e fazer reuniões consigo mesma; usar só issues e milestones, sem quadro, que perde a visão do que está em andamento.
- **Consequências:** o `Processo_de_Desenvolvimento.md` foi reescrito para Kanban. Os campos *Iteration* e *Story Points* deixam de ser necessários no Projects. A Definição de Preparado e a Definição de Pronto continuam valendo, como regras das colunas do quadro.
