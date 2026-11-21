# Definição do Projeto

## 1. Apresentação

O Blog-API é o backend genérico de uma plataforma de blog com vários autores. Ele não tem um tema próprio: cada instalação vira um blog diferente, como um blog de tecnologia, de livros ou de culinária, de acordo com o front-end e as categorias escolhidas por quem a administra. As regras de funcionamento, como prazos e aprovações, também podem ser ajustadas pelo dono de cada plataforma.

Qualquer pessoa pode ler os artigos publicados. Leitores cadastrados comentam, reagem e seguem autores. Quem quer escrever pede autorização e passa a publicar depois de aprovado. Uma equipe de moderação cuida para que o ambiente continue saudável.

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

O objetivo geral é desenvolver uma API REST segura, testada e documentada para uma plataforma de blog com vários autores, que proteja os dados de quem a usa e use a inteligência artificial para apoiar a leitura e a escrita.

Para isso, o projeto deve:

1. oferecer cadastro e login com e-mail e senha ou com conta Google, com as contas protegidas por confirmação de e-mail, recuperação de senha, sessões revogáveis e verificação em duas etapas;
2. coletar só os dados necessários e dar ao usuário controle sobre eles, permitindo escolher o que aparece no perfil público e consultar e excluir os próprios dados;
3. permitir que autores aprovados por um administrador criem, editem e publiquem artigos organizados por categorias e tags, com imagens e com versões em vários idiomas;
4. usar a inteligência artificial para traduzir os artigos que não tenham versão no idioma do leitor, ajudar os autores a escrever com correções e sugestões e recomendar artigos de acordo com os interesses de cada leitor;
5. permitir que leitores busquem, comentem, reajam, sigam autores e acompanhem um feed;
6. oferecer ferramentas de moderação para tratar denúncias;
7. responder em português e em inglês, conforme o idioma do cliente, com a estrutura pronta para receber novos idiomas;
8. manter todo o código testado e a documentação de acordo com o que está implementado.

Como projeto de estudo e de portfólio, o Blog-API também serve para praticar:

- autenticação e autorização (JWT, login com Google e verificação em duas etapas);
- privacidade e proteção de dados pessoais, seguindo os princípios da LGPD;
- integração com modelos de inteligência artificial;
- modelagem de domínio e regras de negócio;
- integração com serviços externos (Google, e-mail e armazenamento de imagens);
- arquitetura limpa e testes automatizados;
- monitoramento e observabilidade (métricas, logs e saúde do sistema);
- um processo de desenvolvimento com Kanban e documentação de Engenharia de Software;
- conteinerização com Docker, como a última etapa do projeto.

## 5. Público e perfis de usuário

| Perfil | Quem é | O que pode fazer |
|---|---|---|
| Visitante | Pessoa sem login. | Ler e buscar artigos publicados, cadastrar-se e entrar. |
| Leitor | Todo usuário cadastrado. | Comentar, reagir, seguir autores, denunciar conteúdo, gerenciar a própria conta e pedir para ser autor. |
| Autor | Leitor com o pedido de autoria aprovado. | Tudo do leitor, mais escrever, publicar e gerenciar os próprios artigos. |
| Moderador | Usuário escolhido por um administrador. | Tudo do leitor, mais analisar denúncias, ocultar conteúdo e remover comentários. Só escreve artigos se também for autor. |
| Administrador | Moderador promovido. | Tudo do autor e do moderador, mais aprovar autores, conceder o papel de moderador, aprovar novos administradores, gerenciar categorias e aplicar penalidades. |
| Superadministrador | O dono e o responsável final pela plataforma. | Tudo, inclusive rebaixar administradores, ajustar as regras da plataforma e acessar as métricas e os logs. Nenhum outro perfil pode alterar ou bloquear a conta dele. |

Todo cadastro começa como leitor. Para virar autor, o leitor precisa ter a conta há pelo menos dois meses e fazer um pedido, que é aprovado por dois administradores, ou por um só se a plataforma tiver apenas um.

O papel de moderador é concedido por um administrador. Um moderador pode se tornar administrador depois de mais de doze meses na função sem nenhuma denúncia procedente contra ele. Se a plataforma tiver três administradores ou mais, a promoção precisa da aprovação de dois deles; com menos de três, quem aprova é o superadministrador.

Há apenas um superadministrador, criado pela própria API na primeira vez que ela sobe, a partir de dados informados em variáveis de ambiente. Nas aprovações, ele conta como administrador e segue as mesmas regras que os demais, para que não haja favoritismo. As únicas exceções existem para a plataforma começar a funcionar:

- quando um perfil ainda não tem ninguém, ele pode promover uma pessoa diretamente para esse perfil;
- ele pode nomear até cinco autores no total sem passar pelo prazo e pela aprovação.

Os prazos e as quantidades desta seção são os valores padrão. O superadministrador pode ajustá-los pela API, dentro de limites seguros definidos no código, e cada mudança fica registrada com quem a fez e quando.

## 6. Principais funcionalidades

| Módulo | Funcionalidades |
|---|---|
| Identidade e acesso | Cadastro e login com e-mail e senha ou com conta Google, login por e-mail ou nickname, ativação da conta por e-mail, recuperação e troca de senha, sessões revogáveis e verificação em duas etapas. |
| Privacidade | Escolha do que aparece no perfil público, consulta e exclusão dos próprios dados e controle do histórico de interesses usado nas recomendações. |
| Perfis | Pedido e aprovação de autoria, concessão do papel de moderador e promoção a administrador. |
| Conteúdo | Artigos com rascunho e publicação, versões do mesmo artigo em vários idiomas, categorias, tags, imagens e busca. |
| Inteligência artificial | Tradução de artigos, correção e sugestões de escrita e recomendação de artigos, com um histórico de interesses que o leitor pode ver, limpar ou desligar. |
| Social | Comentários com respostas, reações, seguidores e feed. |
| Moderação | Denúncias e ações de moderação. |
| Administração da plataforma | Tudo o que um painel do dono precisa: gestão de usuários e papéis, bloqueio de contas, regras da plataforma ajustáveis com histórico de mudanças, categorias, métricas agregadas de uso e desempenho, saúde do sistema, logs técnicos e de segurança com dados pessoais mascarados e prazo de retenção, e registro de todas as ações administrativas. |
| Transversal | Mensagens em português e inglês, preparadas para novos idiomas, erros padronizados e documentação interativa da API (Swagger). |

A lista completa e formal está em [Requisitos.md](Requisitos.md), e as regras de cada funcionalidade em [Regras_de_Negócio.md](Regras_de_Negócio.md).

## 7. Escopo

### 7.1 O projeto inclui
- A API REST completa dos módulos acima.
- A documentação do projeto e a documentação interativa da API.
- Os testes automatizados de todas as classes.
- A execução da API e do banco com Docker, como a última tarefa do projeto. Até lá, os dois rodam diretamente na máquina.

### 7.2 O projeto não inclui
- **Front-end:** o foco é o backend. Isso inclui o painel de administração: a API oferece os endpoints que um painel usaria, mas não a tela.
- **Vários blogs na mesma instalação:** cada instalação da API é um único blog, com o próprio banco de dados.
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
