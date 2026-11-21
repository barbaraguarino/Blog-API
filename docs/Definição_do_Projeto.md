# Definição do Projeto

**Versão:** 1.1 · **Última modificação:** 21/11/2026

## 1. Apresentação

O Blog-API é o backend genérico de uma plataforma de blog com vários autores. Ele não tem um tema próprio: cada plataforma, ou seja, cada instalação do Blog-API, ganha o seu, como tecnologia, livros ou culinária, de acordo com o front-end usado e as categorias que o dono cadastra. Cada dono conta com um painel exclusivo, em que ajusta as regras de funcionamento, como prazos e aprovações, gerencia usuários e acompanha métricas e logs.

Qualquer pessoa pode ler os artigos publicados. Leitores cadastrados comentam, reagem e seguem autores. Quem pode escrever depende da plataforma: o dono escolhe se a autoria é aberta a todos, fechada ou concedida por pedido aprovado pela administração. O autor que escreve em mais de um idioma pode publicar várias versões do mesmo artigo, sem precisar criar um artigo para cada língua. Uma equipe de moderação cuida para que o ambiente continue saudável.

Dois pilares orientam o projeto: a segurança e a privacidade dos dados, e o uso de inteligência artificial a serviço da leitura e da escrita.

A segurança e a privacidade não são um complemento: a API coleta só os dados necessários, nunca expõe a outras pessoas as informações privadas de um usuário e trata os dados pessoais com cuidado desde o desenho do sistema (*privacy by design*).

Com a inteligência artificial, a plataforma recomenda conteúdo de acordo com os interesses que cada leitor demonstra, traduz artigos para o idioma de quem lê, ajuda os autores a escrever, sugerindo correções e melhorias no texto, e indica à moderação o que analisar primeiro.

O projeto entrega somente a API REST, que pode ser consumida por qualquer front-end, web ou mobile. Ele é construído em Java com Spring Boot e PostgreSQL, entregue em versões incrementais e acompanhado de documentação de Engenharia de Software.

O Blog-API é um projeto de estudo e de portfólio. É feito como um produto real, pronto para ser publicado, mas não será colocado em produção nem aberto ao público. O objetivo é praticar, de ponta a ponta, o desenvolvimento de um backend seguro, testado e bem documentado.

## 2. O problema

Quem lê, quem escreve e quem quer criar um espaço de escrita na internet esbarram em alguns problemas que as plataformas de publicação costumam resolver mal.

O primeiro é a privacidade. Muitas plataformas coletam mais dados do que precisam, compartilham essas informações com terceiros e não deixam claro o que fazem com elas. A conta vira uma fonte de exposição: perfis mostram mais do que deveriam, sessões que nunca expiram facilitam invasões e o usuário raramente consegue ver ou controlar o que a plataforma guarda sobre ele.

O segundo é o idioma. Um artigo só alcança quem lê a língua em que ele foi escrito. A saída mais comum é a tradução do navegador, como a do Chrome ou a do Safari, mas ela depende do navegador que a pessoa usa, não está disponível dentro de aplicativos e não dá ao autor nenhum controle sobre o resultado. Já o autor que escreve em mais de um idioma precisa lançar um artigo separado para cada um. Com isso, os comentários e as reações se espalham entre as cópias e o mesmo conteúdo aparece repetido nas buscas.

O terceiro é a própria escrita. Muita gente tem conhecimento para compartilhar, mas trava na hora de escrever, seja por erros de ortografia e gramática, pela dificuldade de organizar as ideias ou pela insegurança com o próprio texto.

O quarto é encontrar o que ler. Com muitos autores e assuntos, os artigos que interessariam a cada leitor se perdem entre os demais, e as listagens por data ou por popularidade mostram a mesma coisa para todo mundo.

Por fim, quem quer criar uma comunidade de escrita sobre um tema, como livros ou tecnologia, tem duas saídas ruins: usar uma plataforma que não é sua, sem controle sobre as regras, os dados e o funcionamento, ou construir tudo do zero.

## 3. A proposta

O Blog-API responde a esses problemas com uma plataforma de publicação que trata a privacidade como requisito, e não como detalhe, e que usa a inteligência artificial para aproximar autores e leitores.

Para proteger os dados, a API pede só o necessário no cadastro, mostra nos perfis públicos apenas o que o usuário escolher, permite ver e encerrar as sessões ativas, avisa por e-mail sobre acessos de dispositivos novos, oferece verificação em duas etapas, registra o aceite dos termos de uso e da política de privacidade e deixa o usuário consultar, exportar e excluir os próprios dados. Cada pessoa também pode bloquear quem a incomoda. A IA recebe só o conteúdo necessário para cada tarefa, nunca os dados das contas: a recomendação usa apenas os interesses, e a triagem da moderação recebe só o texto, sem nome, e-mail ou qualquer identificador de quem escreveu.

Para vencer a barreira do idioma, cada artigo pode ter várias versões escritas pelo próprio autor, que compartilham o mesmo endereço, os mesmos comentários e as mesmas reações. Quando não existe uma versão na língua do leitor, a plataforma oferece uma tradução automática feita com IA.

Para apoiar a escrita, o autor pode pedir à IA a correção de ortografia e gramática, sugestões para melhorar o texto, tags e uma categoria para o artigo e descrições das imagens para leitores de tela, sempre decidindo o que aceitar. Cada artigo também ganha um resumo automático, que o autor pode revisar, usado nas listagens e na busca.

Para ajudar o leitor a encontrar o que ler, a plataforma recomenda artigos a partir de sinais que ele mesmo dá, como os autores que segue, as reações e as tags de interesse, com um histórico que ele pode ver, limpar ou desligar. Também há a busca por texto, categoria, tag e autor e o feed com os autores seguidos.

Para quem quer criar uma comunidade, o Blog-API é uma base pronta e genérica. O dono define a identidade da plataforma, como o nome, o logo e o idioma padrão, escolhe o tema pelo front-end e pelas categorias, mantém páginas institucionais, convida pessoas por e-mail, ajusta as regras de autoria e de promoção, faz backup dos dados e acompanha a plataforma por métricas agregadas e logs que respeitam a privacidade dos usuários.

Em volta disso estão as funcionalidades de base de um blog com comunidade: artigos com rascunho, agendamento, publicação e histórico de revisões; categorias e tags; listas para ler depois; autoria aberta, fechada ou por pedido; comentários, reações e seguidores; moderação do conteúdo denunciado, com triagem feita pela IA; notificações configuráveis; e um painel para cada perfil.

## 4. Objetivos

O objetivo geral é desenvolver uma API REST segura, testada e documentada para uma plataforma de blog genérica, com vários autores e adaptável a qualquer tema, que proteja os dados de quem a usa e use a inteligência artificial para apoiar a leitura e a escrita.

Para isso, o projeto deve:

1. oferecer cadastro e login com e-mail e senha ou com conta Google, com as contas protegidas por confirmação de e-mail, recuperação de senha, sessões revogáveis, alerta de acesso em dispositivo novo e verificação em duas etapas;
2. coletar só os dados necessários e dar ao usuário controle sobre eles, permitindo escolher o que aparece no perfil público, registrar o aceite dos termos de uso e consultar, exportar e excluir os próprios dados;
3. permitir que autores, conforme o modo de autoria escolhido pelo dono, criem, editem, agendem e publiquem artigos organizados por categorias e tags, com imagens, versões em vários idiomas e histórico de revisões;
4. usar a inteligência artificial para traduzir os artigos que não tenham versão no idioma do leitor, ajudar os autores a escrever com correções e sugestões de texto, tags, categoria e descrições de imagens, gerar resumos dos artigos, ajudar a moderação a priorizar o que analisar e recomendar artigos a partir dos sinais que cada leitor dá, sob o controle dele;
5. permitir que leitores busquem, comentem, reajam, sigam autores, bloqueiem usuários, salvem artigos para ler depois, acompanhem um feed e recebam notificações pelos canais que escolherem;
6. oferecer ferramentas de moderação para tratar denúncias;
7. oferecer a cada perfil um painel com o que ele precisa para trabalhar e, ao dono, uma administração completa da plataforma, com a identidade e as páginas institucionais, convites, usuários e papéis, regras ajustáveis, backup, métricas e logs que respeitam a privacidade;
8. responder em português e em inglês, conforme o idioma indicado em cada requisição, com a estrutura pronta para receber novos idiomas;
9. manter todo o código testado e a documentação de acordo com o que está implementado.

Como projeto de estudo e de portfólio, o Blog-API também serve para praticar:

- autenticação e autorização (JWT, login com Google e verificação em duas etapas);
- privacidade e proteção de dados pessoais, seguindo os princípios da LGPD;
- integração com modelos de inteligência artificial;
- modelagem de domínio e regras de negócio;
- integração com serviços externos (Google, AWS S3 e AWS SES);
- arquitetura limpa e testes automatizados;
- monitoramento e observabilidade (métricas, logs e saúde do sistema);
- um processo de desenvolvimento com Kanban e documentação de Engenharia de Software;
- conteinerização com Docker.

## 5. Público e perfis de usuário

O Blog-API atende a dois públicos: quem instala e administra uma plataforma e as pessoas que a usam. Cada pessoa na plataforma, inclusive o dono, tem um destes perfis:

| Perfil | Quem é | O que pode fazer |
|---|---|---|
| Visitante | Pessoa sem login. | Ler e buscar artigos publicados, cadastrar-se e entrar. |
| Leitor | Todo usuário cadastrado. | Comentar, reagir, seguir autores, salvar artigos para ler depois, bloquear usuários, denunciar conteúdo, gerenciar a própria conta e os próprios dados e, no modo por pedido, pedir para ser autor. |
| Autor | Leitor com permissão para escrever, conforme o modo de autoria. | Tudo do leitor, mais escrever, publicar e gerenciar os próprios artigos. |
| Moderador | Usuário escolhido por um administrador. | Tudo do leitor, mais analisar denúncias, ocultar conteúdo e remover comentários. Só escreve artigos se também for autor. |
| Administrador | Em regra, um moderador promovido. | Tudo do autor e do moderador, mais aprovar ou nomear autores, conceder o papel de moderador, aprovar novos administradores, gerenciar categorias e aplicar penalidades. |
| Superadministrador | O dono e o responsável final pela plataforma. | Tudo, inclusive rebaixar administradores, ajustar as regras da plataforma e acessar as métricas e os logs. Nenhum outro perfil pode alterar ou suspender a conta dele. |

Todo cadastro começa como leitor. O dono escolhe como a plataforma trata a autoria, entre três modos:

- por pedido, o padrão: o leitor pede para ser autor depois de um tempo mínimo de conta, o que evita contas criadas só para publicar spam, e a administração analisa o pedido;
- fechado: só escreve quem a administração nomear, e não existem pedidos;
- aberto: todo leitor já pode escrever.

O papel de moderador é concedido por um administrador. Já a promoção a administrador exige um período na função de moderador sem denúncias procedentes contra ele e a aprovação de outros administradores, o que evita favoritismo.

O superadministrador é o dono e o responsável final pela plataforma. Há apenas um, criado pela própria API na primeira vez que ela sobe, a partir de dados informados em variáveis de ambiente. É ele quem define, no painel do dono, o modo de autoria e os prazos e a quantidade de aprovações de cada regra, dentro de limites seguros definidos no código, e cada mudança fica registrada. Ele também segue as regras de promoção, para que não haja favoritismo. As exceções existem só para a plataforma começar a funcionar, como nomear diretamente as primeiras pessoas de cada perfil. Os valores padrão de cada regra estão em [Regras_de_Negócio.md](Regras_de_Negócio.md).

## 6. Principais funcionalidades

| Módulo | Funcionalidades |
|---|---|
| Identidade e acesso | Cadastro e login com e-mail e senha ou com conta Google, login por e-mail ou nickname, ativação da conta por e-mail, recuperação e troca de senha, sessões revogáveis e verificação em duas etapas. |
| Privacidade | Escolha do que aparece no perfil público, consulta, exportação e exclusão dos próprios dados, aceite registrado dos termos de uso e da política de privacidade, alerta por e-mail de acesso em dispositivo novo e controle do histórico de interesses usado nas recomendações. |
| Perfis | Modo de autoria escolhido pelo dono (por pedido, fechado ou aberto), pedido e aprovação de autoria, concessão do papel de moderador e promoção a administrador. |
| Conteúdo | Artigos em Markdown, LaTeX, HTML com CSS ou texto puro, com rascunho, agendamento, publicação e histórico de revisões, versões do mesmo artigo em vários idiomas, categorias, tags, imagens, listas privadas para ler depois e busca. |
| Inteligência artificial | Tradução de artigos, correção e sugestões de escrita, sugestão de tags e de categoria, resumo automático, texto alternativo para imagens, triagem de comentários e denúncias para a moderação e recomendação de artigos. |
| Social | Comentários com respostas, reações, seguidores, feed e bloqueio de usuários. |
| Notificações | Avisos dentro da plataforma, em tempo real, e por e-mail, como novos comentários, novos seguidores e respostas a pedidos, com o usuário escolhendo os canais de cada tipo de aviso. |
| Moderação | Denúncias, triagem feita pela IA e ações de moderação. A IA só prioriza: quem decide é sempre uma pessoa. |
| Administração da plataforma | Tudo o que um painel do dono precisa: identidade da plataforma (nome, descrição, logo, idioma padrão e links), páginas institucionais, termos de uso e política de privacidade, convites por e-mail, backup da plataforma, gestão de usuários e papéis, suspensão de contas, regras da plataforma ajustáveis com histórico de mudanças, categorias, métricas agregadas de uso e desempenho, saúde do sistema, logs técnicos e de segurança com dados pessoais mascarados e prazo de retenção, e registro de todas as ações administrativas. |
| Transversal | Mensagens em português e inglês, preparadas para novos idiomas, erros padronizados e documentação interativa da API (Swagger). |

Cada perfil cadastrado tem o seu painel na API, com o que precisa para trabalhar. A tela de cada painel fica a cargo do front-end.

| Painel | Quem vê | O que mostra |
|---|---|---|
| Minha conta | Todo usuário cadastrado. | Dados e privacidade, sessões, histórico de interesses, listas de leitura, usuários bloqueados, pedidos feitos e o resultado das denúncias que a pessoa fez. |
| Do autor | Cada autor, sobre si. | Artigos, rascunhos e agendamentos, histórico de revisões, versões de idioma, estatísticas por artigo (leituras, reações e comentários, por idioma) e o número de seguidores. |
| Dos autores | Administração e autores. | Para a administração, a lista de autores, a atividade de cada um e os pedidos de autoria pendentes. Para os autores, um espaço comum com avisos da administração e diretrizes de escrita. |
| Da moderação | Moderadores e administração. | Fila de denúncias por motivo, conteúdo ocultado e tempo médio de resolução. |
| Do moderador | Cada moderador, sobre si, e a administração. | Denúncias que ele tratou, ações tomadas e o andamento rumo à promoção, com o tempo na função e as denúncias procedentes contra ele. |
| Do dono | Superadministrador. | Identidade e páginas da plataforma, convites, usuários e papéis, regras, backup, métricas, logs e o registro das ações administrativas. |

As estatísticas dos painéis são sempre agregadas: o autor vê quantas pessoas leram um artigo, nunca quem leu.

A lista completa e formal está em [Requisitos.md](Requisitos.md), e as regras de cada funcionalidade em [Regras_de_Negócio.md](Regras_de_Negócio.md).

## 7. Escopo

O projeto inclui:

- a API REST completa dos módulos acima, com os endpoints dos painéis de cada perfil;
- artigos escritos em Markdown, LaTeX, HTML com CSS ou texto puro. O HTML e o CSS são limpos de qualquer código perigoso antes de serem salvos, e a tradução e a correção feitas pela IA preservam a formatação;
- notificações dentro da plataforma, entregues em tempo real, e por e-mail. Cada usuário escolhe, para cada tipo de aviso, por quais canais quer recebê-lo;
- a configuração pronta para produção, como cookies seguros e perfis separados por ambiente, mesmo sem a publicação;
- a documentação do projeto e a documentação interativa da API;
- os testes automatizados de todas as classes;
- a execução da API e do banco com Docker, como a última tarefa do projeto. Até lá, os dois rodam diretamente na máquina.

O projeto não inclui:

- front-end, nem mesmo as telas dos painéis ou os pop-ups de notificação: a API oferece tudo o que essas telas usariam;
- vários blogs na mesma instalação: cada instalação da API é uma única plataforma, com o próprio banco de dados;
- editor de texto: o conteúdo chega pronto do cliente, em um dos formatos aceitos;
- notificações push para o celular, monetização, anúncios, newsletter e chat;
- a publicação em produção: o projeto roda em ambiente local.

Premissas e restrições:

- o projeto é desenvolvido por uma pessoa, com o apoio de um assistente de IA e sem prazo fixo;
- os serviços pagos custam, somados, no máximo US$ 15 por mês: o Claude Haiku 5.5 para a inteligência artificial, o AWS S3 para as imagens e o AWS SES para os e-mails. Para não passar desse valor, a API limita o uso da IA e o envio de e-mails, e as contas dos provedores têm alertas de gasto;
- todo serviço externo fica atrás de um gateway, para poder ser trocado sem mudar o resto do sistema;
- os testes automatizados nunca chamam serviços externos de verdade.

## 8. Tecnologias

O Blog-API é construído com Java 25 e Spring Boot 4 e usa o PostgreSQL como banco de dados, com as tabelas versionadas pelo Flyway. Os testes automatizados usam JUnit 5, Mockito e AssertJ.

As bibliotecas, os serviços externos (como Google, Claude e AWS) e os detalhes técnicos de cada escolha estão em [Arquitetura.md](Arquitetura.md). O motivo de cada decisão está em [Decisões.md](Decisões.md).

## 9. Como o projeto é desenvolvido

O produto é entregue em versões incrementais. Cada versão tem um tema, como identidade, artigos ou moderação, e só é concluída quando todas as suas tarefas estão prontas. A lista de versões, o que cada uma entrega e a situação de cada uma estão no [Roadmap.md](Roadmap.md).

O trabalho segue um Kanban simples. As tarefas são GitHub Issues, organizadas em um quadro no GitHub Projects, e cada uma passa por um branch e um Pull Request próprios, no modelo Git Flow. Toda classe tem teste automatizado, e nenhuma tarefa é concluída sem cumprir a Definição de Pronto. O processo completo está em [Processo_de_Desenvolvimento.md](Processo_de_Desenvolvimento.md).

O código é distribuído sob a licença MIT.
