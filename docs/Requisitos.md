# Requisitos

**Versão:** 3.0 · **Última modificação:** 21/11/2026

Este documento lista os requisitos do Blog-API, divididos em dois tipos:

- **Requisitos funcionais (RF):** descrevem **o que** o sistema deve fazer, ou seja, os serviços que ele oferece aos usuários.
- **Requisitos não funcionais (RNF):** descrevem **como** o sistema deve se comportar: as qualidades e restrições que ele deve cumprir, como segurança, desempenho e manutenibilidade.

Cada requisito tem um identificador único, que é citado nas issues, nas [Regras de Negócio](Regras_de_Negócio.md) e nos testes. Os identificadores nunca são reaproveitados: um requisito que muda de sentido ganha um identificador novo, e o antigo fica marcado como substituído. Um requisito novo recebe o próximo número livre, por isso os números não seguem a ordem dentro de cada seção. As histórias de usuário e os critérios de aceite de cada entrega ficam nas issues do GitHub.

---

## 1. Requisitos Funcionais

### 1.1 Identidade e acesso

- **RF-001** – O sistema deve permitir que o visitante se cadastre informando nome, e-mail e senha e aceitando os termos de uso e a política de privacidade.
- **RF-002** – O sistema deve permitir que o visitante se cadastre usando uma conta Google cujo e-mail tenha sido verificado pelo Google, aceitando os termos de uso e a política de privacidade.
- **RF-003** – O sistema deve permitir que o usuário se autentique informando o e-mail ou o nickname e a senha.
- **RF-004** – O sistema deve permitir que o usuário se autentique usando a conta Google vinculada.
- **RF-056** – O sistema deve permitir o cadastro e o login com outros provedores sociais, como o GitHub e a Apple, e o dono deve escolher quais provedores ficam ativos na plataforma.
- **RF-054** – O sistema deve permitir que o usuário cadastre, use e remova passkeys para entrar sem senha.
- **RF-055** – O sistema deve permitir que o usuário entre sem senha, com um código de uso único enviado ao e-mail cadastrado.
- **RF-051** – O sistema deve impedir o acesso de contas não ativadas ou suspensas, em qualquer forma de login.
- **RF-005** – O sistema deve renovar o acesso do usuário autenticado, sem exigir um novo login, enquanto a sessão dele for válida.
- **RF-006** – O sistema deve permitir que o usuário encerre a sessão atual (logout).
- **RF-007** – O sistema deve permitir que o usuário visualize as próprias sessões ativas e encerre qualquer uma delas.
- **RF-060** – O sistema deve mostrar ao usuário o histórico dos acessos recentes à conta, com data, dispositivo e local aproximado.
- **RF-008** – O sistema deve exigir que o usuário confirme o e-mail, por meio de um código enviado a ele, para ativar uma conta criada com e-mail e senha, e permitir que ele peça um novo código.
- **RF-009** – O sistema deve permitir que o usuário redefina a senha esquecida por meio de um código enviado ao e-mail cadastrado.
- **RF-010** – O sistema deve permitir que o usuário autenticado altere a própria senha, informando a senha atual e um código enviado ao e-mail.
- **RF-011** – O sistema deve permitir que o usuário ative e desative a verificação em duas etapas com um aplicativo autenticador, recebendo códigos de recuperação de uso único.
- **RF-050** – O sistema deve exigir o código do aplicativo autenticador, ou um código de recuperação, no login dos usuários que ativaram a verificação em duas etapas.
- **RF-057** – O sistema deve permitir que o usuário marque um dispositivo como confiável, dispensando o código da verificação em duas etapas nele por um período limitado, e desfaça essa marcação.
- **RF-059** – O sistema deve exigir que moderadores, administradores e o superadministrador tenham a verificação em duas etapas ativa para usar as funções desses papéis.
- **RF-058** – O sistema deve pedir a senha ou um código de verificação de novo antes de ações sensíveis, como trocar o e-mail, desativar a verificação em duas etapas ou excluir a conta, mesmo com o usuário autenticado.
- **RF-012** – O sistema deve permitir que o usuário vincule e desvincule uma conta Google, ou de outro provedor social ativo, à sua conta.
- **RF-052** – O sistema deve permitir que o usuário edite o próprio perfil: nome, foto, descrição e nickname, com um limite de frequência para a troca do nickname.
- **RF-053** – O sistema deve permitir que o usuário troque o e-mail da conta, confirmando o novo endereço com um código e avisando o endereço antigo.

### 1.2 Privacidade

- **RF-061** – O sistema deve permitir que o usuário escolha quais dados aparecem no próprio perfil público.
- **RF-062** – O sistema deve permitir que o usuário consulte todos os dados pessoais que a plataforma guarda sobre ele.
- **RF-063** – O sistema deve permitir que o usuário exporte os próprios dados em um arquivo.
- **RF-064** – O sistema deve permitir que o usuário exclua a própria conta e os dados pessoais dela.
- **RF-122** – O sistema deve permitir que o usuário desative a conta temporariamente, escondendo o perfil e os conteúdos dele, e a reative depois.
- **RF-123** – O sistema deve aplicar a exclusão da conta só depois de um prazo de carência, durante o qual o usuário pode desistir.
- **RF-065** – O sistema deve registrar a versão dos termos de uso e da política de privacidade aceita por cada usuário e pedir um novo aceite quando esses documentos mudarem.
- **RF-066** – O sistema deve avisar o usuário por e-mail quando a conta for acessada de um dispositivo novo.
- **RF-067** – O sistema deve permitir que o usuário veja, limpe e desligue o histórico de interesses usado nas recomendações.

### 1.3 Perfis e autoria

- **RF-068** – O sistema deve permitir que o dono escolha o modo de autoria da plataforma: por pedido, fechado ou aberto.
- **RF-020** – O sistema deve permitir que o leitor solicite permissão para se tornar autor, quando a plataforma estiver no modo por pedido.
- **RF-021** – O sistema deve permitir que a administração aprove ou recuse as solicitações de autoria, conforme as regras de aprovação da plataforma.
- **RF-069** – O sistema deve permitir que a administração nomeie autores diretamente, quando a plataforma estiver no modo fechado.
- **RF-070** – O sistema deve permitir que um administrador conceda e retire o papel de moderador.
- **RF-071** – O sistema deve permitir a promoção de um moderador a administrador, com a aprovação de outros administradores, ou do superadministrador quando houver poucos administradores, conforme as regras da plataforma.
- **RF-072** – O sistema deve permitir que o superadministrador rebaixe administradores.
- **RF-073** – O sistema deve criar um único superadministrador na instalação da plataforma e impedir que qualquer outro perfil altere ou suspenda a conta dele.
- **RF-074** – O sistema deve permitir que o superadministrador promova uma pessoa diretamente para um perfil que ainda não tenha ninguém e nomeie um número limitado de autores iniciais sem passar pelo prazo e pela aprovação.

### 1.4 Conteúdo

- **RF-115** – O sistema deve permitir que qualquer pessoa, mesmo sem login, liste e leia os artigos publicados.
- **RF-023** – O sistema deve permitir que o autor crie, edite, salve como rascunho, publique e arquive os próprios artigos.
- **RF-075** – O sistema deve aceitar o conteúdo dos artigos em Markdown, LaTeX, HTML com CSS ou texto puro, guardando o formato de cada artigo.
- **RF-076** – O sistema deve permitir que o autor agende a data e a hora de publicação de um artigo.
- **RF-077** – O sistema deve guardar o histórico de revisões de cada artigo e permitir que o autor compare e restaure versões anteriores.
- **RF-078** – O sistema deve permitir que o autor publique várias versões de idioma do mesmo artigo, que compartilham o mesmo endereço, os mesmos comentários e as mesmas reações.
- **RF-022** – O sistema deve permitir que a administração cadastre, edite e remova categorias e subcategorias.
- **RF-024** – O sistema deve permitir que o autor associe tags aos artigos e que qualquer pessoa filtre os artigos por tag.
- **RF-025** – O sistema deve permitir o envio de imagens para a foto de perfil do usuário, para a capa e para o corpo dos artigos.
- **RF-026** – O sistema deve permitir a busca de artigos publicados por texto, categoria, tag e autor.
- **RF-079** – O sistema deve permitir que o leitor salve artigos em listas privadas para ler depois.
- **RF-117** – O sistema deve permitir que o autor convide outros autores como coautores de um artigo, que passam a editá-lo e aparecem como autores dele.
- **RF-119** – O sistema deve permitir que o autor agrupe os próprios artigos em séries ordenadas.
- **RF-118** – O sistema deve permitir que o autor gere um link privado, com validade e revogável, para outra pessoa ler um rascunho antes da publicação.
- **RF-116** – O sistema deve informar o tempo estimado de leitura de cada artigo.
- **RF-080** – O sistema deve mostrar a cada leitor a versão do artigo no idioma dele, quando existir.

### 1.5 Inteligência artificial

- **RF-081** – O sistema deve traduzir automaticamente, com IA, os artigos que não tenham versão no idioma do leitor, preservando a formatação.
- **RF-082** – O sistema deve permitir que o autor peça à IA a correção de ortografia e gramática do texto e decida quais correções aceitar.
- **RF-083** – O sistema deve permitir que o autor peça à IA sugestões para melhorar o texto e decida quais aceitar.
- **RF-084** – O sistema deve sugerir, com IA, tags e uma categoria para o artigo, cabendo ao autor aceitar ou não.
- **RF-085** – O sistema deve gerar, com IA, um resumo de cada artigo, que o autor pode revisar, para uso nas listagens e na busca.
- **RF-086** – O sistema deve sugerir, com IA, descrições das imagens dos artigos para leitores de tela, cabendo ao autor aceitar ou editar.
- **RF-087** – O sistema deve classificar, com IA, comentários e denúncias pela chance de serem abusivos, para a moderação analisar primeiro os mais graves, sem remover nada sozinho.
- **RF-091** – O sistema deve permitir que o leitor escolha tags de interesse.
- **RF-088** – O sistema deve recomendar artigos a cada leitor a partir dos sinais que ele dá, como os autores que segue, as reações e as tags de interesse.

### 1.6 Social

- **RF-030** – O sistema deve permitir que o usuário autenticado comente artigos publicados, responda comentários e edite ou remova os próprios comentários.
- **RF-120** – O sistema deve permitir que o usuário mencione outra pessoa com @nickname num comentário, avisando a pessoa mencionada, exceto quando um bloqueio impedir.
- **RF-031** – O sistema deve permitir que o usuário autenticado reaja aos artigos publicados.
- **RF-121** – O sistema deve permitir que o usuário autenticado reaja aos comentários.
- **RF-032** – O sistema deve permitir que o usuário autenticado siga e deixe de seguir autores.
- **RF-033** – O sistema deve exibir ao usuário autenticado um feed com os artigos recentes dos autores que ele segue.
- **RF-089** – O sistema deve permitir que o usuário bloqueie outros usuários, que deixam de poder comentar nos artigos dele, responder a ele e segui-lo, e desfaça o bloqueio.
- **RF-090** – O sistema deve exibir o perfil público de cada autor, apenas com os dados que ele escolheu mostrar, e os artigos publicados por ele.

### 1.7 Notificações

- **RF-092** – O sistema deve avisar o usuário, dentro da plataforma e em tempo real, sobre eventos que o envolvem, como novos comentários, novos seguidores, respostas, menções, anúncios da plataforma e o resultado de pedidos, denúncias e recursos.
- **RF-093** – O sistema deve enviar esses avisos também por e-mail.
- **RF-094** – O sistema deve permitir que o usuário escolha, para cada tipo de aviso, por quais canais quer recebê-lo.
- **RF-095** – O sistema deve permitir que o usuário liste os próprios avisos e os marque como lidos.

### 1.8 Moderação

- **RF-040** – O sistema deve permitir que o usuário autenticado denuncie artigos e comentários.
- **RF-041** – O sistema deve permitir que o moderador e o administrador analisem as denúncias e apliquem ações de moderação, como arquivar a denúncia, ocultar o conteúdo, remover comentários ou penalizar o autor.
- **RF-096** – O sistema deve informar a quem denunciou o resultado da análise.

### 1.9 Painéis

- **RF-097** – O sistema deve oferecer a todo usuário cadastrado um painel com os próprios dados e configurações de privacidade, sessões, histórico de interesses, listas de leitura, usuários bloqueados, pedidos feitos e o resultado das denúncias que ele fez.
- **RF-098** – O sistema deve oferecer a cada autor um painel com os próprios artigos, rascunhos e agendamentos, o histórico de revisões, as versões de idioma, estatísticas agregadas por artigo e idioma (leituras, reações e comentários) e o número de seguidores.
- **RF-124** – O sistema deve permitir que o autor recorra de uma ocultação ou penalidade, e o recurso deve ser analisado por outra pessoa da equipe de moderação.
- **RF-125** – O sistema deve permitir que o dono configure uma lista de palavras bloqueadas, que impede a publicação de comentários que as contenham.
- **RF-099** – O sistema deve oferecer à administração um painel com a lista de autores, a atividade de cada um e os pedidos de autoria pendentes.
- **RF-100** – O sistema deve oferecer aos autores um espaço comum, em que a administração publica avisos e diretrizes de escrita.
- **RF-101** – O sistema deve oferecer aos moderadores e à administração um painel com a fila de denúncias por motivo, o conteúdo ocultado e o tempo médio de resolução.
- **RF-102** – O sistema deve oferecer a cada moderador um painel, visível também para a administração, com as denúncias que ele tratou, as ações que tomou e o andamento rumo à promoção.
- **RF-103** – O sistema deve oferecer ao superadministrador um painel com a identidade e as páginas da plataforma, os convites, os usuários e papéis, as regras, o backup, as métricas, os logs e o registro das ações administrativas.

### 1.10 Administração da plataforma

- **RF-104** – O sistema deve permitir que o dono defina a identidade da plataforma: nome, descrição, logo, idioma padrão e links.
- **RF-105** – O sistema deve permitir que o dono crie e edite páginas institucionais, como Sobre, Contato e Regras da comunidade.
- **RF-106** – O sistema deve permitir que o dono publique novas versões dos termos de uso e da política de privacidade.
- **RF-107** – O sistema deve permitir que a administração convide pessoas por e-mail para a plataforma ou diretamente para um perfil.
- **RF-108** – O sistema deve permitir que o dono faça e baixe um backup dos dados da plataforma.
- **RF-109** – O sistema deve permitir que a administração liste e gerencie os usuários e os papéis deles, conforme os limites de cada perfil.
- **RF-110** – O sistema deve permitir que a administração suspenda e reative contas.
- **RF-111** – O sistema deve permitir que o dono ajuste as regras da plataforma, como prazos, quantidade de aprovações e limites de uso, dentro de limites seguros, guardando o histórico de cada mudança.
- **RF-112** – O sistema deve mostrar ao dono métricas agregadas de uso e de desempenho e a saúde do sistema.
- **RF-113** – O sistema deve permitir que o dono consulte os logs técnicos e de segurança.
- **RF-114** – O sistema deve registrar todas as ações administrativas, com quem fez, o quê e quando, e mostrar esse registro ao dono.
- **RF-126** – O sistema deve permitir que o dono coloque a plataforma em modo de manutenção, em que só a equipe consegue acessá-la.
- **RF-127** – O sistema deve permitir que o dono publique anúncios para todos os usuários, enviados como aviso dentro da plataforma.

---

## 2. Requisitos Não Funcionais

### 2.1 Segurança

- **RNF-001** – O sistema deve armazenar as senhas apenas na forma de hash, usando o algoritmo BCrypt.
- **RNF-002** – O sistema deve autenticar as requisições de forma stateless, com tokens JWT assinados e enviados em cookies `HttpOnly` e `SameSite=Strict`.
- **RNF-003** – O sistema deve proteger todos os endpoints por padrão, liberando sem autenticação apenas as rotas explicitamente declaradas como públicas.
- **RNF-004** – O sistema deve enviar os cookies de autenticação com o atributo `Secure` fora do ambiente local, com validade igual à do token.
- **RNF-005** – O sistema deve obter senhas, chaves e credenciais apenas de variáveis de ambiente, sem armazená-las no código-fonte.
- **RNF-006** – O sistema deve armazenar os tokens de renovação, os códigos enviados por e-mail e os códigos de recuperação apenas na forma de hash.
- **RNF-007** – O sistema deve limitar o número de tentativas e a frequência de login, de verificação de códigos e de reenvio de e-mails.
- **RNF-008** – O sistema não deve expor, em nenhuma resposta, senhas, hashes, tokens, identificadores de provedores externos ou dados privados de outros usuários.
- **RNF-020** – O sistema deve limpar o HTML e o CSS dos artigos de qualquer código perigoso antes de salvá-los, aceitando só o que estiver numa lista de elementos permitidos.

### 2.2 Privacidade e proteção de dados

- **RNF-021** – O sistema deve coletar só os dados pessoais necessários para cada funcionalidade e seguir os princípios da LGPD.
- **RNF-022** – O sistema deve enviar à IA só o conteúdo necessário para cada tarefa, nunca os dados das contas. A triagem da moderação recebe o texto sem nome, e-mail ou qualquer identificador de quem o escreveu.
- **RNF-023** – O sistema deve mostrar as estatísticas dos painéis sempre de forma agregada, sem revelar quem leu ou reagiu a um conteúdo.
- **RNF-024** – O sistema deve mascarar os dados pessoais nos logs, apagá-los depois de um prazo de retenção definido e registrar cada consulta feita aos logs.
- **RNF-025** – O sistema deve guardar os registros de acesso pelo prazo exigido pela lei e apagá-los depois dele.

### 2.3 Custos e limites de uso

- **RNF-026** – O sistema deve manter o custo somado dos serviços pagos (inteligência artificial, armazenamento de imagens, envio de e-mails e login com Apple) em no máximo US$ 15 por mês.
- **RNF-027** – O sistema deve limitar o uso da IA e o envio de e-mails, por usuário e no total da plataforma, com limites que o dono pode ajustar.
- **RNF-028** – O sistema deve acessar cada serviço externo por meio de um gateway, para que o provedor possa ser trocado sem mudar o resto do sistema.

### 2.4 Usabilidade e padronização

- **RNF-009** – O sistema deve responder com mensagens em português do Brasil (padrão) ou em inglês, conforme o cabeçalho `Accept-Language` da requisição, com a estrutura pronta para receber novos idiomas.
- **RNF-010** – O sistema deve devolver todos os erros em um formato único, contendo data e hora, status HTTP, descrição, mensagem traduzida, caminho da requisição e, quando houver, os erros por campo.
- **RNF-011** – O sistema deve disponibilizar documentação interativa da API no padrão OpenAPI (Swagger).
- **RNF-029** – O sistema deve preservar a formatação de cada artigo (Markdown, LaTeX, HTML com CSS ou texto puro) na tradução e na correção feitas pela IA.

### 2.5 Desempenho

- **RNF-012** – O sistema deve paginar todas as listagens, com um tamanho máximo de página.
- **RNF-030** – O sistema deve entregar os avisos dentro da plataforma em tempo real, sem que o cliente precise consultar a API repetidamente.

### 2.6 Manutenibilidade e qualidade

- **RNF-013** – O sistema deve seguir a Pragmatic Clean Architecture, com o código organizado por funcionalidade e dividido nas camadas de domínio, aplicação, infraestrutura e apresentação.
- **RNF-014** – O sistema deve trocar dados com os clientes apenas por meio de DTOs, sem expor as entidades do banco.
- **RNF-015** – O sistema deve ter um teste automatizado para cada classe de produção.
- **RNF-016** – O sistema deve registrar em log os erros internos com a causa original e responder ao cliente com uma mensagem genérica, sem detalhes técnicos.
- **RNF-031** – Os testes automatizados não devem chamar serviços externos de verdade.

### 2.7 Dados e compatibilidade

- **RNF-017** – O sistema deve usar o PostgreSQL como banco de dados, com o esquema versionado exclusivamente por migrations do Flyway.
- **RNF-018** – O sistema deve prefixar todas as rotas com a versão da API (`/api/v1`).
- **RNF-019** – O sistema deve ser executável localmente com Java 25 e PostgreSQL, sem depender de contêineres.

### 2.8 Operação

- **RNF-032** – O sistema deve ter configurações separadas por ambiente, com a de produção pronta para uso, mesmo sem a publicação.
- **RNF-033** – O sistema deve expor métricas de uso e de desempenho e o estado de saúde, acessíveis só ao superadministrador.
- **RNF-034** – O sistema deve poder ser executado, com o banco de dados, em contêineres Docker.
