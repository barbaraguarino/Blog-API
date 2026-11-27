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

## D-016: Docker como a última tarefa do projeto

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** o projeto é construído como um produto real, pronto para ser publicado, mas sem deploy. O Docker estava fora do escopo, e a autora decidiu incluí-lo como tema de estudo.
- **Decisão:** nas palavras da autora: "se vamos usar Docker ele deve ser a última coisa, a última tarefa." A API e o banco passam a ter execução com Docker, entregue depois de todas as funcionalidades.
- **Alternativas consideradas:** deixar o Docker fora do escopo, como antes; adotá-lo já no início, o que mudaria o ambiente de desenvolvimento no meio das funcionalidades.
- **Consequências:** até a última tarefa, a API e o PostgreSQL continuam rodando diretamente na máquina (RNF-039). O Roadmap precisa reservar essa tarefa para o fim.

## D-017: Plataforma genérica, um blog por instalação, com regras ajustáveis pelo dono

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** ao definir os perfis e as regras de promoção, a autora percebeu que "isso são regras que cada um gostaria de administrar no próprio sistema" e explicou: "Queria criar algo genérico pra um mesmo servidor poder virar um blog só de dev, um blog só de livros e etc. [...] quem iria criar o tema seria o front."
- **Decisão:** o backend não tem tema próprio. Cada instalação da API é um único blog, com o próprio banco (*single-tenant*). Há um único superadministrador, o dono, criado na primeira subida da API a partir de variáveis de ambiente. Os prazos e as quantidades das regras de perfis são valores padrão, que o superadministrador ajusta pela API, dentro de limites seguros definidos no código, com registro de quem mudou e quando. Um painel de administração é front-end e fica fora do escopo; a API oferece os endpoints que ele usaria.
- **Alternativas consideradas:** várias plataformas na mesma instalação (*multi-tenant*), que exige separar todos os dados e consultas por blog e, em caso de erro, vaza dados de um blog para outro; regras ajustadas só por arquivos de configuração ou variáveis de ambiente, que exigem reiniciar a API e não deixam histórico.
- **Consequências:** surge um módulo de configurações da plataforma, com endpoints de administração e histórico de mudanças. Os limites de teste, como um prazo menor para pedir autoria, só valem no perfil de testes.

## D-018: Modo de autoria escolhido pelo dono e um painel para cada perfil

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** com a plataforma genérica (D-017), a curadoria de autores deixou de servir para todos os casos: um blog pessoal não precisa de pedidos de autoria, e uma comunidade aberta pode querer que todos escrevam. Além disso, a autora pediu "um painel para moderação e para cada moderador e também um painel para autores e para cada autor".
- **Decisão:** o dono escolhe o modo de autoria: "o dono escolhe o modo", entre por pedido (padrão), fechado e aberto. Cada perfil ganha um painel na API: minha conta, do autor, dos autores, da moderação, do moderador e do dono. O painel dos autores tem as duas faces pedidas pela autora: "visão da administração e um espaço comum". As regras de perfis aparecem na Definição sem números; os valores padrão ficam nas Regras de Negócio.
- **Alternativas consideradas:** manter sempre a autoria por pedido, que não serve a blogs pessoais nem a comunidades abertas; um único painel de administração, que deixaria autores e moderadores sem visão do próprio trabalho.
- **Consequências:** as estatísticas dos painéis são sempre agregadas, para respeitar a privacidade dos leitores. Os painéis são endpoints da API; as telas continuam fora do escopo.

## D-019: Vários formatos para o conteúdo dos artigos

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** era preciso definir em que formato o conteúdo dos artigos chega à API, já que o editor de texto fica no front-end.
- **Decisão:** nas palavras da autora: "Pode ser markdown, latex ou em HTML CSS, ou txt." Cada artigo guarda o formato junto com o conteúdo.
- **Alternativas consideradas:** aceitar só Markdown, mais simples e seguro, mas limitado para quem quer fórmulas ou um layout próprio.
- **Consequências:** o HTML e o CSS precisam ser limpos de código perigoso antes de serem salvos (lista do que é permitido), por causa do risco de XSS e de CSS malicioso. A tradução e a correção feitas pela IA precisam preservar a formatação de cada formato. A renderização, inclusive a do LaTeX, fica com o front-end.

## D-020: Notificações configuráveis por canal

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** as notificações estavam fora do escopo, como ideia para depois da v0.4.
- **Decisão:** nas palavras da autora: "Entram, pode ser email, popup, site, o que for, e a pessoa pode configurar cada um das notificações." A API entrega avisos dentro da plataforma, em tempo real, e por e-mail, e cada usuário escolhe os canais de cada tipo de aviso.
- **Alternativas consideradas:** deixar as notificações para depois da v0.4; enviar só por e-mail.
- **Consequências:** os pop-ups e a lista de avisos são telas do front-end, fora do escopo. Notificações push para o celular continuam fora, porque dependem de serviços da Apple e do Google.

## D-021: Claude Haiku 5.5 como modelo de IA

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** a tradução, a correção de escrita e a recomendação precisam de um modelo de IA, e o orçamento do projeto é limitado (D-022).
- **Decisão:** nas palavras da autora: "Usa Claude mais barato." Hoje esse modelo é o Claude Haiku 5.5 (`claude-haiku-5-5`), por US$ 0,10 por milhão de tokens de entrada e US$ 0,50 por milhão de saída.
- **Alternativas consideradas:** um modelo local e gratuito, que exige uma máquina mais forte e tem qualidade menor; modelos mais caros do Claude, desnecessários para essas tarefas.
- **Consequências:** a IA fica atrás de um gateway, para que o modelo ou o provedor possam ser trocados. O SDK Java da Anthropic é uma dependência nova e só entra com aprovação. Os testes nunca chamam a API de verdade, e a chave fica só no `.env`.

## D-022: Orçamento de até US$ 15 por mês, com Claude e AWS

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** a restrição de que todo serviço externo tivesse uma opção gratuita ou local tirava o AWS S3 do projeto. A autora explicou: "pode ser o AWS, eu vi que o valor é baixo, assim como serviço de email deles e o Claude é pago, não ligo de pagar, mas não posso pagar mais de 15 dolares por mes".
- **Decisão:** os serviços pagos são o Claude Haiku 5.5 (IA), o AWS S3 (imagens) e o AWS SES (e-mails), com um teto de US$ 15 por mês somando todos.
- **Alternativas consideradas:** só serviços gratuitos ou locais (disco local para imagens, servidor de e-mail de teste e modelo de IA local), que evitam custo mas fogem dos serviços usados em produtos reais.
- **Consequências:** a API limita o uso da IA e o envio de e-mails, por usuário e no total, para que nenhum uso inesperado estoure o teto. A autora configura alertas de gasto nas contas da Anthropic e da AWS. Todos os serviços ficam atrás de gateways e são simulados nos testes, que nunca geram custo.

## D-023: Funcionalidades acrescentadas para fechar o escopo

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** para a Definição do Projeto não mudar a cada nova ideia, a autora pediu para avaliar o que mais seria interessante acrescentar antes de seguir para os outros documentos.
- **Decisão:** a autora escolheu, entre as sugestões:
  - privacidade e segurança: exportação dos próprios dados, termos de uso e política de privacidade com aceite registrado, alerta por e-mail de acesso em dispositivo novo e bloqueio de usuários;
  - conteúdo: listas para ler depois, agendamento da publicação e histórico de revisões;
  - inteligência artificial: resumo automático, sugestão de tags e de categoria, triagem de moderação e texto alternativo para imagens;
  - plataforma do dono: identidade da plataforma, páginas institucionais, convites por e-mail e backup da plataforma.
- **Alternativas consideradas:** séries de artigos e feed RSS, que ficaram de fora.
- **Consequências:** a triagem de moderação recebe só o texto do conteúdo, sem dados de quem o escreveu, e nunca remove nada sozinha. O texto alternativo usa imagens, o que custa mais tokens, e entra nos limites de uso que protegem o teto de US$ 15 por mês (D-022).

## D-024: Definição do Projeto congelada como escopo mínimo

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** a Definição do Projeto mudava a cada nova ideia. A autora pediu: "imagina que esse documento deve ser congelado, ele não deve ser mexido, então tudo que tiver nele deve ser uma decisão fixa". E explicou: "isso seria um documento de requisitos mínimos, deve ter no mínimo isso tudo, pode ter mais coisas, mas não menos. [...] É como se esse documento fosse escrito junto ao cliente." Sobre o cliente: "Eu sou o cliente, [...] mas não existe cliente de verdade."
- **Decisão:** a Definição do Projeto (versão 2.0) é o acordo de escopo mínimo, e a autora faz o papel do cliente. Ela guarda só decisões de produto. Escolhas que podem mudar, como versões de tecnologias, nomes de provedores, ferramentas, ordem de entrega e processo de trabalho, saem dela e ficam na Arquitetura, no Roadmap, no Processo de Desenvolvimento e nestas Decisões.
- **Alternativas consideradas:** manter a Definição como um documento vivo, atualizado a cada mudança, o que a tornava instável e misturava produto com implementação.
- **Consequências:** a Definição só muda com um pedido explícito da autora, em uma nova versão. Os outros documentos podem detalhar e ir além dela, nunca entregar menos. O provedor de IA (D-021), o S3 e o SES (D-022), o Kanban (D-015) e o momento do Docker (D-016) continuam valendo, mas como decisões revisáveis fora da Definição.

## D-025: Login com Apple dentro do teto de custos

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** a autora escolheu oferecer outros logins sociais além do Google (RF-005). Entrar com Apple exige o programa de desenvolvedor da Apple, que custa US$ 99 por ano, cerca de US$ 8 por mês.
- **Decisão:** a autora pediu para incluir a Apple mesmo assim ("Adicionar isso"). O GitHub e a Apple são os exemplos de provedores, e o dono escolhe quais ficam ativos.
- **Alternativas consideradas:** oferecer só o GitHub, que é gratuito.
- **Consequências:** o custo da Apple entra no teto de US$ 15 por mês (D-022), deixando cerca de US$ 7 por mês para a IA, as imagens e os e-mails. Como o provedor é opcional, a Apple pode ficar desativada enquanto o custo não couber.

## D-026: Funcionalidades extras nos Requisitos

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** ao completar os Requisitos, a autora perguntou "Mais alguma ideia?" e escolheu, entre as sugestões, o que entra além do mínimo da Definição.
- **Decisão:** entram tempo estimado de leitura, coautoria, prévia do rascunho por link privado, séries de artigos, menções com @nickname, reações em comentários, desativação temporária da conta, prazo de carência para a exclusão, recurso do autor contra ocultações e penalidades, lista de palavras bloqueadas, modo de manutenção e anúncios da plataforma (RF-029, RF-030, RF-051, RF-061, RF-064, RF-065, RF-085, RF-087, RF-101, RF-102, RF-131 e RF-132).
- **Alternativas consideradas:** ficar só no mínimo da Definição.
- **Consequências:** as séries, que tinham ficado de fora da Definição (D-023), entram como requisito além do mínimo; o feed RSS continua de fora. Os prazos (validade da prévia, carência da exclusão) e as listas (palavras bloqueadas) ficam nas Regras de Negócio e são ajustáveis pelo dono.

## D-027: Estatísticas para o dono e novas funcionalidades nos Requisitos

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** na revisão dos Requisitos, a autora aceitou as ideias sugeridas e perguntou: "onde fica as ideias do dono ter acesso a numero de acessos, paginas mais usadas, e etc".
- **Decisão:** entram a retirada do papel de autor, o resumo diário de avisos por e-mail, fechar e fixar comentários, restaurar backups, exportar os próprios artigos e o limite de requisições em toda a API. O dono ganha estatísticas agregadas de acesso, da comunidade, do uso e do custo dos serviços pagos e métricas técnicas (RF-039, RF-069, RF-083, RF-084, RF-097, RF-116, RF-121, RF-122, RF-123 e RF-125, RNF-015).
- **Alternativas consideradas:** deixar as métricas do dono só no nível genérico do RF-120.
- **Consequências:** as estatísticas são sempre agregadas e não rastreiam pessoas (RNF-018). Contar visitantes sem identificar ninguém e estimar regiões a partir do IP são decisões da Arquitetura. O painel de custos ajuda a manter o teto de US$ 15 por mês (D-022).

## D-028: Renumeração dos requisitos na ordem do documento

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** com a revisão, os requisitos ganharam números fora de ordem dentro de cada seção, o que dificultava a leitura. A autora pediu: "renumera por favor, mas a ordem certa".
- **Decisão:** os requisitos funcionais e não funcionais passam a ser numerados na ordem em que aparecem nos Requisitos (versão 6.0). As referências nas Regras de Negócio, no Roadmap, na Arquitetura, nestas Decisões, no template de issue e no `CLAUDE.md` foram atualizadas. O relatório de testes de 2026-11-15 não é editado e continua com os números antigos.
- **Alternativas consideradas:** manter os números antigos fora de ordem; numerar por módulo (por exemplo, RF-ID-01), que evita renumerações futuras, mas muda o formato usado em todo o projeto.
- **Consequências:** uma renumeração futura segue o mesmo procedimento e é registrada aqui. Correspondência entre os números antigos e os novos (os que não aparecem aqui não mudaram: RF-001, RF-002, RF-003, RF-004, RF-113, RF-114, RNF-001, RNF-002, RNF-003, RNF-004, RNF-005, RNF-006, RNF-007, RNF-008):
  - Funcionais: RF-005 → RF-009; RF-006 → RF-010; RF-007 → RF-011; RF-008 → RF-013; RF-009 → RF-014; RF-010 → RF-015; RF-011 → RF-016; RF-012 → RF-021; RF-020 → RF-034; RF-021 → RF-035; RF-022 → RF-059; RF-023 → RF-048; RF-024 → RF-056; RF-025 → RF-057; RF-026 → RF-045; RF-030 → RF-069; RF-031 → RF-073; RF-032 → RF-075; RF-033 → RF-076; RF-040 → RF-084; RF-041 → RF-085; RF-050 → RF-017; RF-051 → RF-008; RF-052 → RF-022; RF-053 → RF-023; RF-054 → RF-006; RF-055 → RF-007; RF-056 → RF-005; RF-057 → RF-018; RF-058 → RF-020; RF-059 → RF-019; RF-060 → RF-012; RF-061 → RF-024; RF-062 → RF-025; RF-063 → RF-026; RF-064 → RF-027; RF-065 → RF-030; RF-066 → RF-031; RF-067 → RF-032; RF-068 → RF-033; RF-069 → RF-036; RF-070 → RF-038; RF-071 → RF-039; RF-072 → RF-040; RF-073 → RF-041; RF-074 → RF-042; RF-075 → RF-049; RF-076 → RF-050; RF-077 → RF-051; RF-078 → RF-052; RF-079 → RF-047; RF-080 → RF-044; RF-081 → RF-060; RF-082 → RF-061; RF-083 → RF-062; RF-084 → RF-063; RF-085 → RF-064; RF-086 → RF-065; RF-087 → RF-066; RF-088 → RF-068; RF-089 → RF-077; RF-090 → RF-078; RF-091 → RF-067; RF-092 → RF-079; RF-093 → RF-080; RF-094 → RF-081; RF-095 → RF-082; RF-096 → RF-086; RF-097 → RF-089; RF-098 → RF-090; RF-099 → RF-091; RF-100 → RF-092; RF-101 → RF-093; RF-102 → RF-094; RF-103 → RF-095; RF-104 → RF-096; RF-105 → RF-097; RF-106 → RF-098; RF-107 → RF-099; RF-108 → RF-100; RF-109 → RF-102; RF-110 → RF-103; RF-111 → RF-104; RF-112 → RF-105; RF-115 → RF-043; RF-116 → RF-046; RF-117 → RF-053; RF-118 → RF-055; RF-119 → RF-054; RF-120 → RF-072; RF-121 → RF-074; RF-122 → RF-028; RF-123 → RF-029; RF-124 → RF-087; RF-125 → RF-088; RF-126 → RF-115; RF-127 → RF-116; RF-128 → RF-037; RF-129 → RF-083; RF-130 → RF-070; RF-131 → RF-071; RF-132 → RF-101; RF-133 → RF-058; RF-134 → RF-106; RF-135 → RF-107; RF-136 → RF-108; RF-137 → RF-109; RF-138 → RF-110; RF-139 → RF-111; RF-140 → RF-112.
  - Não funcionais: RNF-009 → RNF-018; RNF-010 → RNF-019; RNF-011 → RNF-020; RNF-012 → RNF-022; RNF-013 → RNF-024; RNF-014 → RNF-025; RNF-015 → RNF-026; RNF-016 → RNF-027; RNF-017 → RNF-030; RNF-018 → RNF-031; RNF-019 → RNF-032; RNF-020 → RNF-009; RNF-021 → RNF-011; RNF-022 → RNF-012; RNF-023 → RNF-013; RNF-024 → RNF-014; RNF-025 → RNF-015; RNF-026 → RNF-016; RNF-027 → RNF-017; RNF-028 → RNF-029; RNF-029 → RNF-021; RNF-030 → RNF-023; RNF-031 → RNF-028; RNF-032 → RNF-033; RNF-033 → RNF-034; RNF-034 → RNF-035; RNF-035 → RNF-010.

## D-029: Requisitos vistos por perfil, erros e limites, e nova renumeração

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** a autora pediu para olhar os Requisitos por três ângulos: cada perfil usando a plataforma, o que acontece quando algo falha ou acaba, e ideias novas.
- **Decisão:** entram a leitura pública de comentários, páginas e termos; a troca da versão de idioma pelo leitor; a exclusão de artigos; o aceite e a saída de coautorias; o aviso de conflito entre coautores; a gestão de tags; ligar e desligar cada funcionalidade de IA; a lista de autores seguidos; a transferência da posse da plataforma; a denúncia de perfis; o alerta e o corte automático de custos; o funcionamento sem a IA; e o reenvio de e-mails que falharem. Na exclusão da conta, a autora escolheu que o usuário decide se os artigos e comentários públicos dele são apagados ou mantidos de forma anônima. Novos requisitos: RF-042, RF-045, RF-047, RF-052, RF-058, RF-059, RF-066, RF-076, RF-084, RF-118, RNF-035, RNF-036.
- **Alternativas consideradas:** na exclusão da conta, anonimizar sempre ou apagar sempre.
- **Consequências:** os Requisitos passam para a versão 8.0 e foram renumerados de novo na ordem do documento (D-028). As referências nos outros documentos foram atualizadas, exceto a tabela da D-028, que é histórica. Correspondência entre a versão 7.2 e a 8.0 (os números que não aparecem aqui não mudaram):
  - Funcionais: RF-042 → RF-043; RF-043 → RF-044; RF-044 → RF-046; RF-045 → RF-048; RF-046 → RF-049; RF-047 → RF-050; RF-048 → RF-051; RF-049 → RF-053; RF-050 → RF-054; RF-051 → RF-055; RF-052 → RF-056; RF-053 → RF-057; RF-054 → RF-060; RF-055 → RF-061; RF-056 → RF-062; RF-057 → RF-063; RF-058 → RF-064; RF-059 → RF-065; RF-060 → RF-067; RF-061 → RF-068; RF-062 → RF-069; RF-063 → RF-070; RF-064 → RF-071; RF-065 → RF-072; RF-066 → RF-073; RF-067 → RF-074; RF-068 → RF-075; RF-069 → RF-077; RF-070 → RF-078; RF-071 → RF-079; RF-072 → RF-080; RF-073 → RF-081; RF-074 → RF-082; RF-075 → RF-083; RF-076 → RF-085; RF-077 → RF-086; RF-078 → RF-087; RF-079 → RF-088; RF-080 → RF-089; RF-081 → RF-090; RF-082 → RF-091; RF-083 → RF-092; RF-084 → RF-093; RF-085 → RF-094; RF-086 → RF-095; RF-087 → RF-096; RF-088 → RF-097; RF-089 → RF-098; RF-090 → RF-099; RF-091 → RF-100; RF-092 → RF-101; RF-093 → RF-102; RF-094 → RF-103; RF-095 → RF-104; RF-096 → RF-105; RF-097 → RF-106; RF-098 → RF-107; RF-099 → RF-108; RF-100 → RF-109; RF-101 → RF-110; RF-102 → RF-111; RF-103 → RF-112; RF-104 → RF-113; RF-105 → RF-114; RF-106 → RF-115; RF-107 → RF-116; RF-108 → RF-117; RF-109 → RF-119; RF-110 → RF-120; RF-111 → RF-121; RF-112 → RF-122; RF-113 → RF-123; RF-114 → RF-124; RF-115 → RF-125; RF-116 → RF-126.
  - Não funcionais: RNF-035 → RNF-037.

## D-030: Requisitos de segurança, LGPD e acessibilidade, e nova renumeração

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** a autora pediu para olhar os Requisitos como um atacante, pela LGPD e pela acessibilidade, além de uma revisão de texto da versão 8.0. Um dos pontos encontrados: se o teto de custos parasse todos os e-mails, ninguém conseguiria recuperar a senha, e um atacante poderia provocar isso gastando a cota de IA.
- **Decisão:** a autora escolheu todas as propostas: limpar os metadados das imagens, bloquear recursos externos nos artigos, proteger a IA contra prompt injection, não revelar quais contas existem, manter os e-mails essenciais quando o teto é atingido, reaproveitar traduções e resumos, criptografar os backups, mostrar com quem os dados são compartilhados, publicar o contato do encarregado, apagar dos backups os dados de contas excluídas, exigir idade mínima no cadastro, exigir descrição das imagens, informar o idioma de cada versão e descrever as fórmulas em texto. Novos requisitos: RF-006, RF-034, RF-056, RF-060, RF-068, RF-112, RNF-010, RNF-011, RNF-012, RNF-013, RNF-014, RNF-021, RNF-024.
- **Alternativas consideradas:** deixar esses pontos para as Regras de Negócio ou para a Arquitetura, o que os tornaria opcionais.
- **Consequências:** os Requisitos passam para a versão 9.0 e foram renumerados de novo (D-028). Correspondência entre a versão 8.0 e a 9.0 (os números que não aparecem aqui não mudaram):
  - Funcionais: RF-006 → RF-007; RF-007 → RF-008; RF-008 → RF-009; RF-009 → RF-010; RF-010 → RF-011; RF-011 → RF-012; RF-012 → RF-013; RF-013 → RF-014; RF-014 → RF-015; RF-015 → RF-016; RF-016 → RF-017; RF-017 → RF-018; RF-018 → RF-019; RF-019 → RF-020; RF-020 → RF-021; RF-021 → RF-022; RF-022 → RF-023; RF-023 → RF-024; RF-024 → RF-025; RF-025 → RF-026; RF-026 → RF-027; RF-027 → RF-028; RF-028 → RF-029; RF-029 → RF-030; RF-030 → RF-031; RF-031 → RF-032; RF-032 → RF-033; RF-033 → RF-035; RF-034 → RF-036; RF-035 → RF-037; RF-036 → RF-038; RF-037 → RF-039; RF-038 → RF-040; RF-039 → RF-041; RF-040 → RF-042; RF-041 → RF-043; RF-042 → RF-044; RF-043 → RF-045; RF-044 → RF-046; RF-045 → RF-047; RF-046 → RF-048; RF-047 → RF-049; RF-048 → RF-050; RF-049 → RF-051; RF-050 → RF-052; RF-051 → RF-053; RF-052 → RF-054; RF-053 → RF-055; RF-054 → RF-057; RF-055 → RF-058; RF-056 → RF-059; RF-057 → RF-061; RF-058 → RF-062; RF-059 → RF-063; RF-060 → RF-064; RF-061 → RF-065; RF-062 → RF-066; RF-063 → RF-067; RF-064 → RF-069; RF-065 → RF-070; RF-066 → RF-071; RF-067 → RF-072; RF-068 → RF-073; RF-069 → RF-074; RF-070 → RF-075; RF-071 → RF-076; RF-072 → RF-077; RF-073 → RF-078; RF-074 → RF-079; RF-075 → RF-080; RF-076 → RF-081; RF-077 → RF-082; RF-078 → RF-083; RF-079 → RF-084; RF-080 → RF-085; RF-081 → RF-086; RF-082 → RF-087; RF-083 → RF-088; RF-084 → RF-089; RF-085 → RF-090; RF-086 → RF-091; RF-087 → RF-092; RF-088 → RF-093; RF-089 → RF-094; RF-090 → RF-095; RF-091 → RF-096; RF-092 → RF-097; RF-093 → RF-098; RF-094 → RF-099; RF-095 → RF-100; RF-096 → RF-101; RF-097 → RF-102; RF-098 → RF-103; RF-099 → RF-104; RF-100 → RF-105; RF-101 → RF-106; RF-102 → RF-107; RF-103 → RF-108; RF-104 → RF-109; RF-105 → RF-110; RF-106 → RF-111; RF-107 → RF-113; RF-108 → RF-114; RF-109 → RF-115; RF-110 → RF-116; RF-111 → RF-117; RF-112 → RF-118; RF-113 → RF-119; RF-114 → RF-120; RF-115 → RF-121; RF-116 → RF-122; RF-117 → RF-123; RF-118 → RF-124; RF-119 → RF-125; RF-120 → RF-126; RF-121 → RF-127; RF-122 → RF-128; RF-123 → RF-129; RF-124 → RF-130; RF-125 → RF-131; RF-126 → RF-132.
  - Não funcionais: RNF-010 → RNF-015; RNF-011 → RNF-016; RNF-012 → RNF-017; RNF-013 → RNF-018; RNF-014 → RNF-019; RNF-015 → RNF-020; RNF-016 → RNF-022; RNF-017 → RNF-023; RNF-018 → RNF-025; RNF-019 → RNF-026; RNF-020 → RNF-027; RNF-021 → RNF-028; RNF-022 → RNF-029; RNF-023 → RNF-030; RNF-024 → RNF-031; RNF-025 → RNF-032; RNF-026 → RNF-033; RNF-027 → RNF-034; RNF-028 → RNF-035; RNF-029 → RNF-036; RNF-030 → RNF-037; RNF-031 → RNF-038; RNF-032 → RNF-039; RNF-033 → RNF-040; RNF-034 → RNF-041; RNF-035 → RNF-042; RNF-036 → RNF-043; RNF-037 → RNF-044.

## D-031: Valores padrão e regras combinados para as Regras de Negócio

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** durante a revisão da Definição e dos Requisitos, a autora definiu valores e regras que não cabem nesses documentos (a Definição não tem números, e os Requisitos dizem só o que deve existir). Eles precisam ficar registrados até a revisão das Regras de Negócio.
- **Decisão:** os valores abaixo são os padrões da plataforma. Os prazos e as quantidades são ajustáveis pelo dono, dentro de limites seguros (RF-119).
  - Autoria por pedido: só pode pedir quem tem a conta há mais de 2 meses ("Deixa dois meses"), e o pedido é aprovado por 2 administradores, ou por 1 se a plataforma tiver só um.
  - O prazo de autoria pode ser reduzido para os testes, "mas também seguro": proposta aceita de que só o perfil de testes aceite um valor menor e que, fora dele, a API não suba com menos de 2 meses.
  - Moderador: o papel é concedido por um administrador e não exige ser autor. Um moderador que não é autor não escreve artigos.
  - Promoção a administrador: mais de 12 meses como moderador sem nenhuma denúncia procedente contra ele ("reclamação" é só denúncia procedente). Com 3 ou mais administradores, exige a aprovação de 2 outros; com menos de 3, quem aprova é o superadministrador.
  - Superadministrador: há um só, o dono, criado na primeira subida da API a partir de variáveis de ambiente. Nas aprovações, ele conta como administrador e segue as mesmas regras, "se não tem favoritismo". As exceções de início: promover diretamente quando um perfil ainda não tem ninguém e nomear até 5 autores no total sem prazo nem aprovação.
  - Hierarquia: "Usuário > Autor > Moderador > Administrador". O administrador também escreve artigos.
  - Perfil: tem descrição (bio). O nickname pode ser trocado no máximo a cada 30 dias. A troca de e-mail exige um código no endereço novo e avisa o endereço antigo.
  - Tradução: quando o autor escreve uma versão em um idioma, ela é usada no lugar da tradução automática.
- **Alternativas consideradas:** a verificação de senhas vazadas (consulta a uma base de senhas conhecidas) foi oferecida e recusada pela autora. As séries de artigos e o feed RSS ficaram fora da Definição; as séries entraram depois nos Requisitos (D-026), e o RSS continua fora.
- **Consequências:** esses valores entram nas Regras de Negócio como padrões. Ainda estão em aberto: quais idiomas a tradução automática oferece; o prazo de validade da prévia por link, a carência da exclusão de conta, o tempo de um dispositivo confiável e a idade mínima padrão; e os limites de trabalho em andamento do Kanban (1 em andamento e 2 em revisão), que foram uma proposta ainda não confirmada.

## D-032: Convenções de escrita e de versão dos documentos

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** ao revisar a documentação, a autora definiu como os documentos devem ser escritos, versionados e commitados.
- **Decisão:**
  - A Definição do Projeto e os Requisitos têm, abaixo do título, a versão e a data da última modificação. Os outros documentos só ganham esse cabeçalho se a autora pedir.
  - Regra de versão: uma mudança de conteúdo sobe o número da frente (1.2 → 2.0), um ajuste de redação sobe o de trás (1.0 → 1.1), e a data muda sempre.
  - Commits na revisão de documentos: "a cada seção modificada faz um commit e se modificamos outra seção por causa de uma a gente coloca no mesmo commit".
  - Estilo: texto corrido, sem subseções demais, e sem abrir frases com um termo em negrito seguido de ponto (padrão que a autora vê como vício de texto gerado por IA). O termo vira parte do texto, vai para um título ou termina com dois-pontos.
  - Os Requisitos dizem só o que deve existir: "Deve ficar no documento o que deve ter e somente isso". Regras vão para as Regras de Negócio, detalhes de entrega para os critérios de aceite das issues e o como fazer para a Arquitetura.
- **Alternativas consideradas:** versionar só pelo Git, sem cabeçalho nos documentos.
- **Consequências:** cada revisão atualiza a versão e a data. A numeração dos requisitos segue a ordem do documento, com cada renumeração registrada aqui (D-028).

## D-033: Formato das Regras de Negócio, respostas no cadastro e valores em aberto

- **Data:** 2026-11-21
- **Status:** Aceita
- **Contexto:** a revisão das Regras de Negócio precisava de um formato que comportasse as regras dos requisitos novos, de uma solução para o conflito entre a RN-001, que devolvia `409` para um e-mail já cadastrado, e o RNF-013, que pede respostas que não revelem quais contas existem, e dos valores que a D-031 deixou em aberto. A autora aprovou as propostas: "Aprovo tudo".
- **Decisão:**
  - As regras são numeradas na ordem do documento, como os Requisitos (D-028), e cada renumeração é registrada aqui. O documento ganha o cabeçalho de versão e data (D-032), começando na versão 1.0.
  - Cada regra é escrita em texto corrido, com o erro e os requisitos em lista. As regras ajustáveis pelo dono são marcadas com ⚙️ e informam o padrão e o intervalo seguro. Só o perfil de testes aceita valores fora do intervalo.
  - O cadastro com e-mail e senha responde sempre `202 Accepted`, com a mesma mensagem. Se o e-mail já pertencer a uma conta, nada é criado, e o próprio endereço recebe um aviso da tentativa.
  - No cadastro com Google ou outro provedor social, o token prova que a pessoa é dona do e-mail, então a resposta pode ser clara: o identificador do provedor é conferido antes do e-mail, cada caso tem a sua mensagem (`409`), e as contas nunca são vinculadas automaticamente. Isso corrige a divergência D2.
  - Nova divergência D6: o login confere se a conta está ativa antes de conferir a senha, e assim uma senha errada em uma conta suspensa revela que ela existe. A situação da conta passa a ser conferida só depois da senha.
  - Valores padrão que estavam em aberto na D-031: a tradução automática oferece português, inglês e espanhol, e o dono escolhe entre os idiomas suportados; o link de prévia vale 7 dias (de 1 a 30); a carência da exclusão de conta é de 30 dias (de 7 a 30); o dispositivo confiável vale 30 dias (de 1 a 90); a idade mínima é de 18 anos (de 16 a 18).
- **Alternativas consideradas:** manter os blocos de números por módulo, que estourariam com as regras novas; manter o `409` no cadastro, que revela quais e-mails estão cadastrados.
- **Consequências:** a idade mínima abaixo de 18 anos só deve ser liberada depois de conferir a Lei 15.211/2025 (ECA Digital). Os limites de trabalho em andamento do Kanban, a última questão em aberto da D-031, ficam para a revisão do Processo de Desenvolvimento.

## D-034: Aprovações na promoção a administrador

- **Data:** 2026-11-27
- **Status:** Aceita
- **Contexto:** a D-031 diz que, com 3 ou mais administradores, a promoção de um moderador a administrador "exige a aprovação de 2 outros". Ao escrever a regra (RN-052), não estava claro se quem indica o moderador conta como uma dessas aprovações.
- **Decisão:** a autora aprovou a proposta ("Aprovo assim"): a promoção começa com a indicação de um administrador e exige a aprovação de 2 administradores além de quem indicou e do próprio candidato.
- **Alternativas consideradas:** contar quem indicou como uma das aprovações, o que deixaria a promoção depender de só mais uma pessoa.
- **Consequências:** com exatamente 3 administradores, todos participam da promoção (um indica e dois aprovam). Com menos de 3, quem aprova continua sendo o superadministrador.

## D-035: Recomendação calculada pela própria API

- **Data:** 2026-11-27
- **Status:** Aceita
- **Contexto:** a recomendação de artigos (RF-080) está entre as funcionalidades de inteligência artificial, mas pode ser feita sem chamar o modelo de IA, só com os sinais que cada leitor dá.
- **Decisão:** a autora aprovou a proposta ("Aprovo a seção 6"): a recomendação é calculada pela própria API, a partir das tags de interesse, dos autores seguidos, das reações e do histórico de interesses, sem enviar dados ao modelo. As outras funcionalidades de IA continuam usando o modelo.
- **Alternativas consideradas:** pedir as recomendações ao modelo, o que gastaria parte do teto de US$ 15 por mês (D-022) e enviaria dados de leitura a um serviço externo.
- **Consequências:** a recomendação não tem custo, não entra nos limites de uso da IA e continua funcionando quando a IA está indisponível. O cálculo fica descrito na Arquitetura.

## D-036: Renumeração das Regras de Negócio na ordem do documento

- **Data:** 2026-11-27
- **Status:** Aceita
- **Contexto:** a D-033 decidiu numerar as regras na ordem do documento, como os Requisitos (D-028). As Regras de Negócio foram reescritas seção por seção (versões 1.0 a 10.0), e as regras antigas ganharam números novos. A autora aprovou registrar a correspondência ("Aplica os três").
- **Decisão:** as regras passam a ir de RN-001 a RN-140, na ordem do documento. Correspondência entre os números antigos e os novos (os números novos que não aparecem aqui são de regras novas):
  - RN-001 → RN-001; RN-002 → RN-003; RN-003 → RN-011; RN-004 → RN-005; RN-005 → RN-006; RN-006 → RN-007; RN-007 → RN-008; RN-008 → RN-017; RN-009 → RN-012; RN-010 → RN-013;
  - RN-011 → RN-018; RN-012 → RN-019; RN-013 → RN-020; RN-014 → RN-021; RN-015 → RN-022; RN-016 → RN-010; RN-017 → RN-023; RN-018 → RN-024; RN-019 → RN-025; RN-020 → RN-026;
  - RN-021 → RN-027; RN-022 → RN-031; RN-030 → RN-046; RN-031 → RN-047; RN-032 → RN-048; RN-033 e RN-034 → RN-075; RN-035 → RN-057; RN-036 → RN-058; RN-037 → RN-059; RN-038 → RN-077;
  - RN-039 → RN-074; RN-040 → RN-076; RN-041 → RN-078; RN-050 → RN-092; RN-051 → RN-093; RN-052 → RN-098; RN-053 → RN-100; RN-054 → RN-101; RN-060 → RN-113; RN-061 → RN-115.
- **Alternativas consideradas:** manter os blocos de números por módulo, que não comportavam as regras novas.
- **Consequências:** os números provisórios RN-1xx, usados durante a reescrita, não valem mais. O relatório de testes de 2026-11-15 não é editado e continua com os números antigos. O Plano de Testes é atualizado na revisão dele, com esta tabela.
