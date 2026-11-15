# Blog-API

![Java](https://img.shields.io/badge/Java-25-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-brightgreen) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Flyway-blue) ![Licença](https://img.shields.io/badge/licen%C3%A7a-MIT-green)

O Blog-API é uma plataforma de rede social e publicações projetada para conectar produtores de conteúdo e leitores por meio de artigos dinâmicos e interações sociais. O ecossistema foca em um backend robusto desenvolvido com Spring Boot e PostgreSQL, oferecendo funcionalidades que abrangem desde a moderação de conteúdo e sistemas de comentários até o gerenciamento de categorias temáticas. Utilizando AWS S3 para gestão de mídias, o projeto demonstra uma solução escalável e profissional para o gerenciamento de comunidades e compartilhamento de conhecimento.

## Principais Funcionalidades

- **Gestão de Identidade e Acesso:** Cadastro, login com 2FA, recuperação de senha, gestão de papéis (Leitor, Autor, Moderador, Admin) e personalização de conta.
- **Gestão de Conteúdo**: CRUD completo de publicações, com suporte a categorização hierárquica, uso de tags e sistema de busca avançada. 
- **Sistema de Autoria**: Fluxo de aprovação de novos autores e ferramentas para criar, editar, salvar rascunhos e publicar artigos. 
- **Interações Sociais**: Sistema de seguidores, feed de atividades dinâmico, comentários estruturados e reações nas publicações. 
- **Moderação de Conteúdo**: Endpoints para denúncia de conteúdos inadequados e painel administrativo para análise e aplicação de penalidades. 
- **Internacionalização (i18n)**: Suporte nativo a múltiplos idiomas, adaptando as mensagens e respostas da API conforme a região do cliente.

## Tecnologias e Ferramentas

- **Java:** Linguagem base do backend, garantindo forte tipagem e robustez orientada a objetos.
- **Spring Boot:** Framework orquestrador que simplifica a configuração da infraestrutura da API RESTful.
- **PostgreSQL:** Banco de dados relacional robusto, escolhido para garantir a integridade das relações complexas do ecossistema da plataforma.
- **Flyway:** Versionamento do esquema de banco de dados, garantindo um histórico automatizado e confiável das migrações (migrations) das tabelas.
- **Spring Security + JWT:** Autenticação stateless com token assinado entregue em cookie `HttpOnly`, além de login com conta Google (validação do ID Token).
- **JUnit 5, Mockito e AssertJ:** Testes unitários dos casos de uso e testes de controller com `MockMvc`.
- **AWS SDK (Amazon S3)** *(planejado)*: Integração com a nuvem para armazenamento isolado e escalável de arquivos de mídia (fotos de perfil, miniaturas e imagens dos artigos).
- **Springdoc OpenAPI (Swagger)** *(planejado)*: Geração automatizada e interativa da documentação dos endpoints da API, facilitando testes e integrações futuras.

## Arquitetura do Projeto

O sistema foi arquitetado com base em uma abordagem de **Pragmatic Clean Architecture**, organizada primeiramente pelo padrão **Package by Feature** (Pacotes por Domínio) e, internamente, subdividida em 4 camadas estritas. Essa estrutura visa garantir alta coesão, baixo acoplamento, isolamento de infraestrutura e estrita aderência aos princípios **SOLID**.

```text
src/main/
├── java/br/com/blog/
│   ├── BlogApiApplication.java
│   │
│   ├── core/                           # 1. Fundações e Configurações Globais
│   │   ├── config/                     # Beans globais do framework (ex: i18n)
│   │   ├── exceptions/                 # Tratamento global de erros e exceções de domínio/infraestrutura
│   │   └── security/                   # Configurações do Spring Security, JWT e UserDetails
│   │
│   ├── infrastructure/                 # 2. Adaptações Técnicas Globais (Ports & Adapters)
│   │   └── providers/                  # Implementações de serviços externos
│   │       └── google/                 # Gateway e configurações do Google Auth
│   │
│   └── modules/                        # 3. Domínios de Negócio Isolados (Contextos)
│       ├── user/                       # Módulo de Gestão de Identidade, Autenticação e Perfis
│       │   ├── domain/                 # ⮑ CORAÇÃO: Entidades de negócio ricas (Models, Enums)
│       │   ├── application/            # ⮑ ORQUESTRAÇÃO: Casos de Uso (Use Cases), DTOs e Mappers
│       │   ├── infrastructure/         # ⮑ DETALHES TÉCNICOS: Persistência de dados (Spring Data JPA)
│       │   └── presentation/           # ⮑ DELIVERY: Porta de entrada da API (Controllers REST)
│       │
│       ├── article/                    # Módulo de Gestão de Publicações (Artigos, Categorias, Tags)
│       │   └── ... (arquitetura interna espelhada em 4 camadas)
│       │
│       └── ...
│
└── resources/                          # 4. Arquivos Estáticos e Configurações
    ├── db/migration/                   # Scripts de versionamento do Flyway
    ├── i18n/                           # Dicionários de internacionalização
    └── application.properties          # Variáveis de ambiente e configurações locais
```

## Restrições de Escopo

- **Sem Front-End:** O projeto tem foco 100% no back-end, consistindo exclusivamente na entrega de uma API RESTful robusta.
- **Sem Docker:** Para simplificar a configuração do ambiente de desenvolvimento neste estágio, a execução da aplicação e do PostgreSQL é feita de forma local e direta.

## Documentação

| Documento | Conteúdo |
|---|---|
| [Definição do Projeto](docs/DEFINICAO_DO_PROJETO.md) | Visão, objetivos, papéis, escopo e entregas |
| [Requisitos](docs/REQUISITOS.md) | Requisitos funcionais e não funcionais |
| [Regras de Negócio](docs/REGRAS_DE_NEGOCIO.md) | Regras numeradas e divergências conhecidas |
| [Arquitetura](docs/ARQUITETURA.md) | Documento de Arquitetura de Software |
| [Plano de Testes](docs/testes/PLANO_DE_TESTES.md) | Estratégia, critérios e matriz de rastreabilidade |
| [Relatórios de Testes](docs/testes/relatorios/README.md) | Resultado das execuções |
| [Decisões](docs/DECISOES.md) | Registro de decisões de arquitetura e processo |
| [Roadmap](docs/ROADMAP.md) | Fases (Milestones) e fluxo de trabalho |
| [Segurança](docs/SEGURANCA.md) | Checklist de segurança do repositório e da conta |

## Endpoints Disponíveis

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| POST | `/api/v1/register` | Cadastro com nome, e-mail e senha | Público |
| POST | `/api/v1/register/google` | Cadastro com Google ID Token | Público |
| POST | `/api/v1/auth/login` | Login com e-mail ou nickname e senha | Público |
| POST | `/api/v1/auth/login/google` | Login com Google ID Token | Público |

Exemplos de requisição estão em [`requests/`](requests/) (HTTP Client do IntelliJ).

## Status do Projeto

**Em Desenvolvimento**: a fundação (v0.0) está concluída e a próxima fase é a **v0.1 – Identidade essencial**. Veja as fases no [Roadmap](docs/ROADMAP.md) e as tarefas em [Issues](https://github.com/barbaraguarino/Blog-API/issues).

No momento, o repositório ainda não está pronto para ser clonado e executado por terceiros.

As instruções detalhadas de configuração de variáveis de ambiente, preparação do banco de dados e execução da API serão documentadas nesta seção assim que a versão **Alpha** do sistema for consolidada.

## Autora

Desenvolvido por Barbara Duarte.

Este projeto faz parte dos meus estudos em desenvolvimento backend com Java e Spring Boot, servindo como base para praticar construção de APIs REST, autenticação, testes, design de arquitetura limpa (SOLID) e integração com serviços em nuvem (AWS).

GitHub: @barbaraguarino

## Licença

Este projeto está sob a licença **MIT**: você pode usar, copiar, modificar e distribuir o código, desde que mantenha o aviso de copyright e a licença. Consulte o arquivo [LICENSE](LICENSE) para mais detalhes.