# Definição do Projeto

Este documento apresenta o **Blog-API**: o problema que ele resolve, para quem, o que faz e o que não faz, como será entregue e como saberemos que deu certo. É o ponto de partida para entender todos os outros documentos.

---

## 1. Contexto e problema

Plataformas de blog costumam resolver bem a **publicação** de textos, mas tratam a **comunidade** como algo secundário. Ficam faltando uma identidade confiável dos usuários, o controle de quem pode publicar, a interação estruturada entre leitores e autores e a moderação do que é publicado. Por outro lado, redes sociais resolvem a interação, mas não oferecem um espaço organizado para conteúdo longo, com categorias, rascunhos e curadoria.

O Blog-API propõe o **backend** de uma plataforma que une os dois mundos:

- **artigos** bem organizados (categorias hierárquicas, tags, rascunhos, busca);
- uma **comunidade** com interações sociais (comentários, reações, seguidores, feed);
- um **processo de autoria** controlado: nem todo usuário publica, e quem quer publicar pede e é aprovado;
- **moderação** para manter o ambiente saudável;
- **segurança de identidade** desde o início: cadastro local ou com Google, sessões revogáveis, confirmação de e-mail e 2FA.

## 2. Justificativa

Além de resolver o problema acima, o projeto é um **estudo prático de Engenharia de Software e desenvolvimento backend**. Ele foi escolhido porque reúne, em um domínio fácil de entender, desafios técnicos reais:

| Desafio | Onde aparece no projeto |
|---|---|
| Autenticação e autorização | JWT, OAuth2/OIDC com Google, papéis, sessões, 2FA |
| Modelagem de domínio | Usuários, artigos, categorias em árvore, comentários com respostas |
| Regras de negócio não triviais | Prazo mínimo de conta para virar autor, visibilidade de rascunhos, reações únicas |
| Integração com serviços externos | Google, envio de e-mail, AWS S3 |
| Arquitetura limpa | Camadas, casos de uso, DTOs e gateways |
| Qualidade | Testes automatizados de toda classe e rastreabilidade entre requisitos e testes |
| Processo | Scrum com documentação de Engenharia de Software (ver [Processo_de_Desenvolvimento.md](Processo_de_Desenvolvimento.md)) |

## 3. Visão do produto (Meta do Produto)

> **Para** leitores e produtores de conteúdo **que** querem um espaço organizado para publicar e discutir artigos, **o Blog-API é** o backend de uma plataforma de publicações com comunidade **que** oferece identidade segura, autoria com curadoria, interação social e moderação. **Diferente de** blogs comuns, que só publicam, **e de** redes sociais, que só interagem, **o Blog-API** une conteúdo organizado e comunidade em uma API única, pronta para qualquer front-end.

## 4. Objetivos

### 4.1 Objetivo geral

Construir uma API REST robusta, segura, testada e documentada que sirva de base para qualquer cliente (web ou mobile) de uma plataforma de blog com comunidade.

### 4.2 Objetivos específicos

1. Permitir **cadastro e login** com e-mail e senha ou com conta Google, com sessões seguras e revogáveis.
2. Proteger as contas com **confirmação de e-mail**, **recuperação de senha** e **2FA**.
3. Controlar **quem pode publicar**, com um fluxo de pedido e aprovação de autoria.
4. Permitir que autores **criem, editem e publiquem artigos**, organizados por categorias e tags e com imagens.
5. Permitir que leitores **encontrem e interajam** com o conteúdo: busca, comentários, reações, seguidores e feed.
6. Oferecer ferramentas de **moderação**: denúncias e penalidades.
7. Responder em **mais de um idioma** (pt-BR e en).
8. Manter **toda classe testada** e a documentação sempre em dia com o código.

## 5. Partes interessadas

| Parte interessada | Interesse |
|---|---|
| **Autora** (desenvolvedora, Product Owner) | Aprender e demonstrar Engenharia de Software e backend com Java/Spring, e manter um portfólio de qualidade. |
| **Avaliadores** (professores, recrutadores) | Entender rapidamente o projeto, a arquitetura, as decisões e a qualidade do código. |
| **Desenvolvedores de front-end** (futuros clientes da API) | Uma API estável, documentada (Swagger) e com erros padronizados. |
| **Usuários finais** (leitores, autores, moderadores) | Uma plataforma segura, organizada e agradável. Indiretos, porque o projeto não tem interface própria. |

## 6. Papéis de usuário

O sistema reconhece cinco perfis. O papel é guardado no usuário (`UserRole`) e define o que cada um pode fazer.

| Papel | Quem é | Como obtém o papel | Permissões |
|---|---|---|---|
| **Visitante** | Pessoa não autenticada. | — | Ler artigos publicados, buscar, ver perfis públicos, cadastrar-se e entrar. |
| **Leitor** (`READER`) | Todo usuário cadastrado. | Automaticamente, no cadastro. | Tudo do visitante, mais comentar, reagir, seguir autores, denunciar conteúdo, gerenciar as próprias sessões e a conta, e pedir para virar autor. |
| **Autor** (`AUTHOR`) | Leitor com pedido de autoria aprovado. | Aprovação de um administrador. | Tudo do leitor, mais criar, editar, salvar como rascunho, publicar e arquivar os **próprios** artigos, com imagens e tags. |
| **Moderador** (`MODERATOR`) | Membro da equipe de moderação. | Atribuído por um administrador. | Analisar denúncias, ocultar conteúdo e remover comentários. *(Papel já existe no código; permissões a partir da v0.4.)* |
| **Administrador** (`ADMIN`) | Responsável pela plataforma. | Configuração direta. | Tudo, incluindo gerenciar categorias, aprovar ou recusar autores e aplicar penalidades. |

## 7. Escopo

### 7.1 Dentro do escopo

| Módulo | Funcionalidades |
|---|---|
| **Identidade e acesso** (`user`) | Cadastro local e via Google; login por e-mail ou nickname; login via Google; sessões com Refresh Token, listagem e revogação de dispositivos; ativação de conta por e-mail; recuperação e troca de senha; 2FA com app autenticador; vínculo e desvínculo de conta Google. |
| **Autoria e conteúdo** (`article`) | Pedido e aprovação de autoria; artigos com rascunho, publicação e arquivamento; categorias hierárquicas; tags; upload de avatar e de capa (AWS S3); busca. |
| **Social** (`social`) | Comentários com respostas; reações; seguidores; feed. |
| **Moderação** (`moderation`) | Denúncias de artigos e comentários; análise e ações (arquivar, ocultar, penalizar). |
| **Transversal** (`core`) | Erros padronizados, internacionalização (pt-BR e en), documentação OpenAPI/Swagger, segurança. |

### 7.2 Fora do escopo

| Item | Motivo |
|---|---|
| **Front-end** (site ou app) | O foco do estudo é o backend. A API é consumível por qualquer cliente. |
| **Docker e orquestração** | Para simplificar o ambiente nesta fase, a API e o PostgreSQL rodam localmente. Pode ser revisto no futuro. |
| **Editor de texto rico** | O conteúdo chega pronto do cliente (ex. Markdown ou HTML). A API só armazena e entrega. |
| **Monetização, anúncios e assinaturas** | Fora do objetivo do produto. |
| **Notificações push, newsletter e chat** | Podem ser avaliadas depois da v0.4. |
| **Deploy em produção** | O projeto roda localmente. Configurações de produção (cookies `Secure`, perfis) estão previstas, mas o deploy não. |

## 8. Entregas (Milestones)

O produto é entregue em versões incrementais. Cada versão é um Milestone no GitHub, dividido em Sprints (ver [Roadmap.md](Roadmap.md)).

| Versão | Tema | Entrega principal | Situação |
|---|---|---|---|
| **v0.0 – Fundação** | Base técnica | Exceções globais, i18n, segurança JWT, cadastro e login local e com Google, login por e-mail ou nickname | Concluída |
| **v0.1 – Identidade essencial** | Contas seguras | Swagger, sessões com Refresh Token, e-mail transacional, ativação de conta, recuperação e troca de senha, correção das divergências conhecidas, cobertura de testes | Próxima |
| **v0.2 – Artigos (MVP)** | Publicar conteúdo | Autoria, artigos e rascunhos, categorias, tags, imagens no S3 | Planejada |
| **v0.3 – Social** | Interagir | Comentários, reações, seguidores, feed, busca | Planejada |
| **v0.4 – Moderação e segurança avançada** | Proteger a comunidade | Denúncias, moderação, 2FA TOTP, vínculo de conta Google | Planejada |

## 9. Premissas

- A autora desenvolve sozinha, sem prazo fixo, em Sprints de cerca de duas semanas.
- Haverá acesso a uma conta Google Cloud (para o client-id do OAuth), a um provedor de e-mail e a uma conta AWS quando essas funcionalidades forem implementadas.
- Os clientes da API conseguem armazenar cookies `HttpOnly`. A autenticação depende disso.
- O PostgreSQL local está disponível para desenvolvimento e testes de integração.

## 10. Restrições

| Restrição | Detalhe |
|---|---|
| **Tecnológica** | Java 25, Spring Boot 4, PostgreSQL, Flyway, Maven. Mudanças passam pelo [Decisões.md](Decisões.md). |
| **Arquitetural** | Pragmatic Clean Architecture por funcionalidade (ver [Arquitetura.md](Arquitetura.md)). |
| **Qualidade** | Toda classe tem teste unitário. A Definição de Pronto deve ser cumprida antes de fechar qualquer issue. |
| **Ambiente** | Sem Docker. Testes manuais só com o Cliente HTTP do IntelliJ. Nada é instalado sem aprovação. |
| **Segurança** | Repositório público: nenhum segredo versionado. Ver [Segurança.md](Segurança.md). |
| **Licença** | MIT: o código pode ser usado, modificado e distribuído, desde que mantido o aviso de copyright. |

## 11. Riscos

| Risco | Probabilidade | Impacto | Mitigação |
|---|---|---|---|
| Escopo grande para uma pessoa só | Alta | Alto | Entregas incrementais (Milestones), Sprints curtas, priorização do essencial. |
| Falhas de segurança na autenticação | Média | Alto | Regras de segurança explícitas (RN), testes de cada cenário, revisão das divergências conhecidas. |
| Dependência de serviços externos (Google, e-mail, AWS) | Média | Médio | Integrações atrás de gateways, simuladas nos testes e trocáveis sem afetar os casos de uso. |
| Documentação desatualizada | Média | Médio | Atualização obrigatória no mesmo PR (Definição de Pronto). |
| Vazamento de segredos no repositório público | Baixa | Alto | `.gitignore` reforçado, secret scanning, hook e regras do `CLAUDE.md`. |
| Pausas longas no desenvolvimento | Média | Médio | Documentação detalhada e backlog refinado para retomar com facilidade. |

## 12. Critérios de sucesso

O projeto será considerado bem-sucedido, a cada versão, se:

1. **Funcionalidade:** todos os requisitos do Milestone estiverem com as issues fechadas e todos os critérios de aceite cumpridos.
2. **Qualidade:** toda classe de produção tiver teste, a suíte completa passar e houver um relatório de testes do Milestone.
3. **Documentação:** Swagger, README e documentos de `docs/` estiverem coerentes com o código entregue.
4. **Segurança:** nenhum segredo estiver versionado e não houver divergência de segurança conhecida e aberta na versão entregue.
5. **Processo:** cada mudança tiver passado por issue → branch → PR → revisão, seguindo o Git Flow.

## 13. Glossário

| Termo | Significado |
|---|---|
| **API REST** | Interface que expõe recursos por HTTP (ex. `POST /api/v1/register`), trocando dados em JSON. |
| **JWT** | *JSON Web Token*: token assinado que identifica o usuário autenticado. |
| **Refresh Token** | Token de longa duração usado para obter um novo JWT sem fazer login de novo. |
| **ID Token (Google)** | Token emitido pelo Google que comprova a identidade da pessoa. A API valida a assinatura dele. |
| **OAuth2 / OIDC** | Protocolos de autorização e identidade usados no login com Google. |
| **2FA / TOTP** | Autenticação em dois fatores com códigos temporários gerados por um aplicativo. |
| **OTP** | *One-Time Password*: código de uso único enviado por e-mail. |
| **DTO** | *Data Transfer Object*: objeto que define o formato dos dados de entrada e saída da API. |
| **Caso de uso** | Classe que executa uma ação de negócio (ex. "registrar usuário local"). |
| **Gateway** | Classe que isola a comunicação com um serviço externo. |
| **Migration** | Script versionado (Flyway) que cria ou altera tabelas do banco. |
| **i18n** | Internacionalização: respostas no idioma pedido pelo cliente. |
| **Slug** | Versão do título própria para URLs (ex. `minha-primeira-publicacao`). |
| **Milestone / Sprint** | Versão do produto / ciclo de trabalho de cerca de duas semanas. |
| **RF / RNF / RN** | Requisito funcional / requisito não funcional / regra de negócio. |

## 14. Documentos relacionados

| Documento | Conteúdo |
|---|---|
| [Processo_de_Desenvolvimento.md](Processo_de_Desenvolvimento.md) | Scrum adaptado, papéis, eventos, Definição de Preparado e de Pronto |
| [Requisitos.md](Requisitos.md) | Requisitos funcionais (com histórias de usuário) e não funcionais |
| [Regras_de_Negócio.md](Regras_de_Negócio.md) | Regras numeradas, com justificativa, exemplos e erros |
| [Arquitetura.md](Arquitetura.md) | Documento de Arquitetura de Software |
| [Testes/Plano_de_Testes.md](Testes/Plano_de_Testes.md) | Estratégia, critérios e cobertura de testes |
| [Testes/Relatórios/](Testes/Relatórios/README.md) | Relatórios de execução dos testes |
| [Roadmap.md](Roadmap.md) | Versões, Sprints e ordem de entrega |
| [Decisões.md](Decisões.md) | Registro de decisões |
| [Segurança.md](Segurança.md) | Segurança do repositório e da conta |
