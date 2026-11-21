# Definição do Projeto

## 1. Apresentação

O Blog-API é o backend de uma plataforma de blog com vários autores e de tema livre. Qualquer pessoa pode ler os artigos publicados. Leitores cadastrados comentam, reagem e seguem autores. Quem quer escrever pede autorização e passa a publicar depois de aprovado. Uma equipe de moderação cuida para que o ambiente continue saudável.

Dois pilares orientam o projeto: a segurança e a privacidade dos dados, e o uso de inteligência artificial a serviço da leitura e da escrita.

As contas são protegidas com confirmação de e-mail, sessões revogáveis, verificação em duas etapas e senhas guardadas apenas como hash. A API coleta só os dados necessários, nunca expõe informações privadas de outros usuários e trata os dados pessoais com cuidado desde o desenho do sistema (*privacy by design*).

Com a inteligência artificial, a plataforma recomenda conteúdo de acordo com os interesses de cada leitor, traduz artigos para o idioma de quem lê e ajuda os autores a escrever, sugerindo correções e melhorias no texto. Além disso, o autor que escreve em mais de um idioma pode publicar várias versões do mesmo artigo, sem precisar criar um artigo para cada língua.

O projeto entrega somente a API REST, que pode ser consumida por qualquer front-end, web ou mobile. Ele é construído em Java com Spring Boot e PostgreSQL, em versões incrementais, e acompanhado da documentação de Engenharia de Software.

O Blog-API é um projeto de estudo e de portfólio. Ele é construído como um produto real, pronto para ser publicado, mas não será colocado em produção nem aberto ao público. O objetivo é praticar, de ponta a ponta, o desenvolvimento de um backend seguro, testado e bem documentado.

## 2. O problema

Quem escreve e quem lê na internet esbarra em alguns problemas que as plataformas de publicação costumam resolver mal.

O primeiro é a privacidade. Muitas plataformas coletam mais dados do que precisam, compartilham essas informações com terceiros e não deixam claro o que fazem com elas. A conta vira uma fonte de exposição: perfis mostram mais do que deveriam, sessões que nunca expiram facilitam invasões e o usuário raramente consegue ver ou controlar o que a plataforma guarda sobre ele.

O segundo é o idioma. Um artigo só alcança quem lê a língua em que ele foi escrito. A saída mais comum é a tradução do navegador, como a do Chrome ou a do Safari, mas ela depende do navegador que a pessoa usa, não está disponível dentro de aplicativos e não dá ao autor nenhum controle sobre o resultado. Já o autor que escreve em mais de um idioma precisa lançar um artigo separado para cada um. Com isso, os comentários e as reações se espalham entre as cópias e o mesmo conteúdo aparece repetido nas buscas.

O terceiro é a própria escrita. Muita gente tem conhecimento para compartilhar, mas trava na hora de escrever, seja por erros de ortografia e gramática, pela dificuldade de organizar as ideias ou pela insegurança com o próprio texto.

Por fim, encontrar o que ler também é difícil. Com muitos autores e temas, os artigos que interessariam a cada leitor se perdem entre os demais, e as listagens por data ou por popularidade mostram a mesma coisa para todo mundo.

## 3. A proposta

O Blog-API responde a esses problemas com uma plataforma de publicação que trata a privacidade como requisito, e não como detalhe, e que usa a inteligência artificial para aproximar autores e leitores.

Para proteger os dados, a API pede só o necessário no cadastro, mostra nos perfis públicos apenas o que o usuário escolher, permite ver e encerrar as sessões ativas, oferece verificação em duas etapas e deixa o usuário consultar e excluir os próprios dados. A IA recebe apenas o texto dos artigos, nunca dados pessoais dos usuários.

Para vencer a barreira do idioma, cada artigo pode ter várias versões escritas pelo próprio autor, que compartilham o mesmo endereço, os mesmos comentários e as mesmas reações. Quando não existe uma versão na língua do leitor, a plataforma oferece uma tradução automática feita com IA.

Para apoiar a escrita, o autor pode pedir à IA a correção de ortografia e gramática e sugestões para melhorar o texto, sempre decidindo o que aceitar.

Para ajudar o leitor a encontrar o que ler, a plataforma recomenda artigos de acordo com os interesses dele, além da busca por texto, categoria, tag e autor e do feed com os autores seguidos.

Em volta disso estão as funcionalidades de base de um blog com comunidade: artigos com rascunho e publicação, categorias e tags, pedido e aprovação de autoria, comentários, reações, seguidores e moderação do conteúdo denunciado.

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
- um processo de desenvolvimento com Kanban e documentação.

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

O produto é entregue em versões incrementais (Milestones):

| Versão | Entrega | Situação |
|---|---|---|
| **v0.0 – Fundação** | Base técnica, cadastro e login local e com Google | Concluída |
| **v0.1 – Identidade essencial** | Sessões, ativação de conta, recuperação e troca de senha, Swagger | Próxima |
| **v0.2 – Artigos (MVP)** | Autoria, artigos, categorias, tags e imagens | Planejada |
| **v0.3 – Social** | Comentários, reações, seguidores, feed e busca | Planejada |
| **v0.4 – Moderação e segurança avançada** | Denúncias, moderação, 2FA e vínculo com Google | Planejada |

Detalhes no [Roadmap.md](Roadmap.md).

## 10. Como o projeto é desenvolvido

- **Método:** Kanban simples, com o backlog no GitHub Issues e o quadro no GitHub Projects. Ver [Processo_de_Desenvolvimento.md](Processo_de_Desenvolvimento.md).
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
| [Processo_de_Desenvolvimento.md](Processo_de_Desenvolvimento.md) | Como o trabalho é organizado (Kanban). |
| [Roadmap.md](Roadmap.md) | Versões e ordem de entrega. |
| [Decisões.md](Decisões.md) | Por que cada escolha foi feita. |
| [Segurança.md](Segurança.md) | Como o repositório e a conta são protegidos. |
