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

O sistema foi arquitetado com base em uma abordagem híbrida de **Pragmatic Clean Architecture** e **MVC**, organizada no padrão **Package by Feature** (Pacotes por Domínio). Essa estrutura visa garantir alta coesão, baixo acoplamento e estrita aderência aos princípios **SOLID**.

```
src/main/
├── java/br/com/blog/
│   ├── BlogApiApplication.java
│   │
│   ├── core/                           # 1. Fundações e Configurações Globais
│   │   ├── config/                     # Beans do Spring
│   │   ├── security/                   # Filtros interceptadores de requisição e tokens JWT
│   │   └── exceptions/                 # Tratamento global de erros
│   │
│   ├── infrastructure/                 # 2. Adaptações Técnicas (Ports & Adapters)
│   │   └── storage/                    # Implementações externas (ex: AWS S3)
│   │
│   └── modules/                        # 3. Domínios de Negócio Isolados
│       ├── user/                       # Gestão de Identidade, Autenticação e Perfis
│       │   ├── controllers/            # Endpoints REST
│       │   ├── dtos/                   # Objetos de transferência puros
│       │   ├── domain/                 # Entidades JPA mapeadas para o banco
│       │   ├── services/               # Casos de uso e lógica de negócio pura
│       │   └── repositories/           # Interfaces de comunicação com o Spring Data JPA
│       │
│       ├── article/                    # Gestão de Publicações (Artigos, Categorias, Tags)
│       │   └── ... (arquitetura interna espelhada: controllers, dtos, domain, etc.)
│       │
│       ├── interaction/                # Engajamento (Comentários, Curtidas, Seguidores)
│       │   └── ... 
│       │
│       ├── moderation/                 # Segurança da Comunidade (Denúncias, Banimentos)
│       │   └── ... 
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

Desenvolvido por **Barbara Duarte**

## Licença

**Copyright © 2026 Barbara Duarte. Todos os direitos reservados.**

Este é um projeto de código fechado desenvolvido com fins de estudo e composição de portfólio. O código-fonte está aberto exclusivamente para visualização. Não é permitida a cópia, modificação, distribuição ou uso comercial deste software sem autorização prévia e expressa da autora.