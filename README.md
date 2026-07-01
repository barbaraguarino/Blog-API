# BLOG

O Blog-API é uma plataforma de rede social e publicações projetada para conectar produtores de conteúdo e leitores por meio de artigos dinâmicos e interações sociais. O ecossistema foca em um backend robusto desenvolvido com Spring Boot e PostgreSQL, oferecendo funcionalidades que abrangem desde a moderação de conteúdo e sistemas de comentários até o gerenciamento de categorias temáticas. Utilizando AWS S3 para gestão de mídias, o projeto demonstra uma solução escalável e profissional para o gerenciamento de comunidades e compartilhamento de conhecimento.

## Principais Funcionalidades

- **Gestão de Identidade e Acesso:** Cadastro, login com 2FA, recuperação de senha, gestão de papéis (Leitor, Autor, Admin) e personalização de conta.
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
- **AWS SDK (Amazon S3):** Integração com a nuvem para armazenamento isolado e escalável de arquivos de mídia (fotos de perfil, miniaturas e imagens dos artigos).
- **Springdoc OpenAPI (Swagger):** Ferramenta adotada para a geração automatizada e interativa da documentação dos endpoints da API, facilitando testes e integrações futuras.

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

## Status do Projeto

**Em Desenvolvimento**

O projeto Blog-API encontra-se em suas fases iniciais de arquitetura estrutural e modelagem de domínio. No momento, o repositório não está pronto para ser clonado ou executado localmente por terceiros.

As instruções detalhadas de configuração de variáveis de ambiente, preparação do banco de dados e execução da API serão documentadas nesta seção assim que a versão **Alpha** do sistema for consolidada.

## Autora

Desenvolvido por Barbara Duarte.

Este projeto faz parte dos meus estudos em desenvolvimento backend com Java e Spring Boot, servindo como base para praticar construção de APIs REST, autenticação, testes, design de arquitetura limpa (SOLID) e integração com serviços em nuvem (AWS).

GitHub: @barbaraguarino

## Licença

Este projeto possui licença privada e todos os direitos são reservados.

O código-fonte está disponível apenas para fins de estudo, consulta e avaliação acadêmica, não sendo permitida a cópia, modificação, distribuição ou uso sem autorização prévia da autora.

Consulte o arquivo LICENSE para mais detalhes.