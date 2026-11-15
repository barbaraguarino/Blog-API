# Definição do Projeto

## 1. Visão

O **Blog-API** é o backend de uma plataforma de publicações e rede social. Leitores acompanham, comentam e reagem a artigos, e autores aprovados publicam conteúdo organizado por categorias e tags. A plataforma tem moderação para manter a comunidade saudável.

## 2. Objetivo

Entregar uma API REST robusta, segura e bem testada que sirva de base para qualquer front-end (web ou mobile), usando o projeto também como estudo prático de:

- construção de APIs REST com Java e Spring Boot;
- autenticação e segurança (JWT, OAuth2/OIDC com Google, 2FA);
- arquitetura limpa e princípios SOLID;
- testes automatizados;
- integração com serviços em nuvem (AWS).

## 3. Público e papéis

| Papel | Quem é | O que pode fazer |
|---|---|---|
| **Visitante** | Pessoa não autenticada | Ler artigos publicados, buscar, cadastrar-se e fazer login |
| **Leitor** (`READER`) | Papel padrão de toda conta nova | Comentar, reagir, seguir autores, denunciar conteúdo e pedir para virar autor |
| **Autor** (`AUTHOR`) | Leitor aprovado pela moderação | Tudo do leitor, mais criar, editar e publicar os próprios artigos |
| **Moderador** (`MODERATOR`) | Equipe de moderação | Analisar denúncias e ocultar conteúdo |
| **Administrador** (`ADMIN`) | Responsável pela plataforma | Tudo, incluindo gerenciar categorias, aprovar autores e aplicar penalidades |

O papel `MODERATOR` já existe no código, mas as permissões dele só serão usadas a partir da v0.4.

## 4. Escopo

### Dentro do escopo
- Identidade e acesso: cadastro local e via Google, login, sessões, ativação de conta, recuperação e troca de senha, 2FA.
- Autoria: pedido e aprovação de autores.
- Conteúdo: artigos, rascunhos, categorias hierárquicas, tags, busca e mídias (S3).
- Social: comentários, reações, seguidores e feed.
- Moderação: denúncias e penalidades.
- Internacionalização das mensagens da API (pt-BR e en).

### Fora do escopo
- **Front-end:** o projeto entrega apenas a API.
- **Docker:** a API e o PostgreSQL rodam localmente, sem contêineres, nesta fase.
- Monetização, anúncios, newsletter e notificações push.
- Editor de texto rico: o conteúdo é recebido pronto do cliente.

## 5. Entregas (Milestones)

| Versão | Entrega |
|---|---|
| v0.0 – Fundação | Exceções globais, i18n, segurança JWT, cadastro e login local e Google *(concluída)* |
| v0.1 – Identidade essencial | Swagger, sessões com Refresh Token, e-mail, ativação, recuperação e troca de senha |
| v0.2 – Artigos (MVP) | Autoria, artigos e rascunhos, categorias, tags, upload no S3 |
| v0.3 – Social | Comentários, reações, seguidores, feed e busca |
| v0.4 – Moderação e segurança avançada | Denúncias, 2FA TOTP, vínculo de conta Google |

O detalhe e a ordem atual estão no [ROADMAP.md](ROADMAP.md) e nas Issues do GitHub.

## 6. Critérios de sucesso

- Todos os requisitos de uma versão com issue fechada, cumprindo os critérios de aceite.
- Testes automatizados cobrindo as regras de negócio de cada caso de uso.
- Documentação da API (Swagger) completa e atualizada.
- Nenhum segredo versionado; a configuração sensível fica em variáveis de ambiente.

## 7. Restrições e premissas

- Projeto individual de estudo, sem prazo fixo.
- Stack fixa: Java 25, Spring Boot 4, PostgreSQL, Flyway, AWS S3.
- Licença MIT: o código é aberto e pode ser usado, modificado e distribuído, desde que o aviso de copyright seja mantido (ver `LICENSE`).

## 8. Documentos relacionados

| Documento | Conteúdo |
|---|---|
| [REQUISITOS.md](REQUISITOS.md) | Requisitos funcionais e não funcionais |
| [REGRAS_DE_NEGOCIO.md](REGRAS_DE_NEGOCIO.md) | Regras de negócio numeradas |
| [ARQUITETURA.md](ARQUITETURA.md) | Documento de Arquitetura de Software |
| [testes/PLANO_DE_TESTES.md](testes/PLANO_DE_TESTES.md) | Plano de testes |
| [testes/relatorios/](testes/relatorios/) | Relatórios de execução dos testes |
| [DECISOES.md](DECISOES.md) | Registro de decisões |
| [ROADMAP.md](ROADMAP.md) | Fases e fluxo de trabalho |
