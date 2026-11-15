# Definição do Projeto

## 1. Apresentação

O **Blog-API** é o backend de uma plataforma de publicações com comunidade. Leitores encontram, comentam e reagem a artigos; autores aprovados publicam conteúdo organizado por categorias e tags; e uma equipe de moderação mantém o ambiente saudável.

O projeto entrega **somente a API REST**: qualquer front-end, web ou mobile, pode consumi-la. Ele é desenvolvido em **Java com Spring Boot** e **PostgreSQL**, seguindo o método **Scrum** com documentação de Engenharia de Software.

## 2. O problema

Plataformas de blog costumam resolver bem a **publicação**, mas deixam a **comunidade** em segundo plano: identidade frágil dos usuários, nenhum controle sobre quem pode publicar, pouca interação entre leitores e autores e moderação inexistente.

Redes sociais fazem o oposto: resolvem a interação, mas não oferecem um espaço organizado para conteúdo longo, com categorias, rascunhos e curadoria.

## 3. A proposta

Unir os dois mundos em uma única plataforma:

- **Conteúdo organizado:** artigos com rascunho e publicação, categorias em árvore, tags, imagens e busca.
- **Autoria com curadoria:** nem todo usuário publica. Quem quer publicar faz um pedido, que é analisado por um administrador.
- **Comunidade:** comentários, reações, seguidores e um feed com os autores seguidos.
- **Moderação:** denúncias de conteúdo abusivo e ações para tratá-las.
- **Identidade segura:** cadastro com e-mail e senha ou com conta Google, confirmação de e-mail, recuperação de senha, sessões revogáveis e verificação em duas etapas.

## 4. Objetivos

### 4.1 Objetivo geral

Desenvolver uma API REST segura, testada e documentada que sirva de base para uma plataforma de blog com comunidade.

### 4.2 Objetivos específicos

1. Oferecer cadastro e login com e-mail e senha ou com conta Google.
2. Proteger as contas com confirmação de e-mail, recuperação de senha, sessões revogáveis e verificação em duas etapas.
3. Controlar quem pode publicar, por meio de um fluxo de pedido e aprovação de autoria.
4. Permitir que autores criem, editem e publiquem artigos organizados por categorias e tags, com imagens.
5. Permitir que leitores busquem, comentem, reajam, sigam autores e acompanhem um feed.
6. Oferecer ferramentas de moderação para tratar denúncias.
7. Responder em português e inglês, conforme o idioma do cliente.
8. Manter todo o código testado e a documentação sempre de acordo com o que está implementado.

### 4.3 Objetivo de aprendizado

O Blog-API também é um projeto de estudo e portfólio. Ele exercita, em um domínio fácil de entender:
- autenticação e autorização (JWT, OAuth2 com Google, 2FA);
- modelagem de domínio e regras de negócio;
- integração com serviços externos (Google, e-mail, AWS S3);
- arquitetura limpa e testes automatizados;
- um processo de desenvolvimento com Scrum e documentação.

## 5. Público e perfis de usuário

| Perfil | Quem é | O que pode fazer |
|---|---|---|
| **Visitante** | Pessoa sem login. | Ler e buscar artigos publicados, cadastrar-se e entrar. |
| **Leitor** | Todo usuário cadastrado. | Comentar, reagir, seguir autores, denunciar conteúdo, gerenciar a própria conta e pedir para ser autor. |
| **Autor** | Leitor com pedido de autoria aprovado. | Tudo do leitor, mais escrever, publicar e gerenciar os próprios artigos. |
| **Moderador** | Membro da equipe de moderação. | Analisar denúncias, ocultar conteúdo e remover comentários. |
| **Administrador** | Responsável pela plataforma. | Tudo, incluindo aprovar autores, gerenciar categorias e aplicar penalidades. |

## 6. Principais funcionalidades

| Módulo | Funcionalidades |
|---|---|
| **Identidade e acesso** | Cadastro e login (local e com Google), login por e-mail ou nickname, sessões e dispositivos, ativação de conta por e-mail, recuperação e troca de senha, verificação em duas etapas. |
| **Autoria e conteúdo** | Pedido e aprovação de autoria, artigos e rascunhos, categorias, tags, imagens e busca. |
| **Social** | Comentários com respostas, reações, seguidores e feed. |
| **Moderação** | Denúncias e ações de moderação. |
| **Transversal** | Mensagens em pt-BR e en, erros padronizados e documentação interativa da API (Swagger). |

A lista completa e formal está em [Requisitos.md](Requisitos.md), e as regras de cada funcionalidade em [Regras_de_Negócio.md](Regras_de_Negócio.md).

## 7. Escopo

### 7.1 O projeto inclui
- A API REST completa dos módulos acima.
- A documentação do projeto e a documentação interativa da API.
- Os testes automatizados de todas as classes.

### 7.2 O projeto não inclui
- **Front-end:** o foco é o backend.
- **Docker:** a API e o banco rodam localmente nesta fase.
- **Editor de texto rico:** o conteúdo chega pronto do cliente.
- **Monetização, anúncios, newsletter, notificações push ou chat.**
- **Publicação em produção:** o projeto roda em ambiente local.

## 8. Tecnologias

| Tecnologia | Papel no projeto |
|---|---|
| **Java 25** | Linguagem do backend. |
| **Spring Boot 4** | Framework da API (Web MVC, Data JPA, Validation, Security). |
| **PostgreSQL** | Banco de dados relacional. |
| **Flyway** | Versionamento das tabelas do banco. |
| **Spring Security + JWT** | Autenticação stateless com token em cookie seguro. |
| **Google Sign-In** | Cadastro e login com conta Google. |
| **AWS S3** *(planejado)* | Armazenamento de imagens. |
| **Swagger / OpenAPI** *(planejado)* | Documentação interativa da API. |
| **JUnit 5, Mockito, AssertJ** | Testes automatizados. |

Os detalhes técnicos estão em [Arquitetura.md](Arquitetura.md).

## 9. Entregas

O produto é entregue em versões incrementais (Milestones), cada uma desenvolvida em Sprints:

| Versão | Entrega | Situação |
|---|---|---|
| **v0.0 – Fundação** | Base técnica, cadastro e login local e com Google | Concluída |
| **v0.1 – Identidade essencial** | Sessões, ativação de conta, recuperação e troca de senha, Swagger | Próxima |
| **v0.2 – Artigos (MVP)** | Autoria, artigos, categorias, tags e imagens | Planejada |
| **v0.3 – Social** | Comentários, reações, seguidores, feed e busca | Planejada |
| **v0.4 – Moderação e segurança avançada** | Denúncias, moderação, 2FA e vínculo com Google | Planejada |

Detalhes no [Roadmap.md](Roadmap.md).

## 10. Como o projeto é desenvolvido

- **Método:** Scrum adaptado a uma desenvolvedora, com Sprints, backlog no GitHub Issues e quadro no GitHub Projects. Ver [Processo_de_Desenvolvimento.md](Processo_de_Desenvolvimento.md).
- **Versionamento:** Git Flow, com um branch e um Pull Request por tarefa.
- **Qualidade:** toda classe tem teste automatizado, e nenhuma tarefa é concluída sem cumprir a Definição de Pronto.
- **Licença:** MIT.

## 11. Mapa da documentação

| Documento | O que explica |
|---|---|
| **Definição do Projeto** *(este)* | O que é o projeto, para quem e com que objetivos. |
| [Requisitos.md](Requisitos.md) | O que o sistema deve fazer (funcionais) e com que qualidade (não funcionais). |
| [Regras_de_Negócio.md](Regras_de_Negócio.md) | As regras que cada funcionalidade obedece. |
| [Arquitetura.md](Arquitetura.md) | Como o sistema é construído por dentro. |
| [Testes/Plano_de_Testes.md](Testes/Plano_de_Testes.md) | Como o sistema é testado. |
| [Testes/Relatórios/](Testes/Relatórios/README.md) | Os resultados das execuções de testes. |
| [Processo_de_Desenvolvimento.md](Processo_de_Desenvolvimento.md) | Como o trabalho é organizado (Scrum). |
| [Roadmap.md](Roadmap.md) | Versões e ordem de entrega. |
| [Decisões.md](Decisões.md) | Por que cada escolha foi feita. |
| [Segurança.md](Segurança.md) | Como o repositório e a conta são protegidos. |
