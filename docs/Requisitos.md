# Requisitos

**Versão:** 9.0 · **Última modificação:** 21/11/2026

Este documento lista os requisitos do Blog-API, divididos em dois tipos:

- **Requisitos funcionais (RF):** descrevem **o que** o sistema deve fazer, ou seja, os serviços que ele oferece aos usuários.
- **Requisitos não funcionais (RNF):** descrevem **como** o sistema deve se comportar: as qualidades e restrições que ele deve cumprir, como segurança, desempenho e manutenibilidade.

Cada requisito tem um identificador único, que é citado nas issues, nas [Regras de Negócio](Regras_de_Negócio.md) e nos testes. Os requisitos são numerados na ordem em que aparecem no documento. Um requisito novo entra na seção do seu módulo, e a numeração é refeita quando necessário. Cada renumeração é registrada em [Decisões.md](Decisões.md), com a correspondência entre os números antigos e os novos, para que documentos antigos, como os relatórios de testes, continuem rastreáveis. As histórias de usuário e os critérios de aceite de cada entrega ficam nas issues do GitHub.

Os perfis e os termos usados aqui seguem a [Definição do Projeto](Definição_do_Projeto.md): o dono é o superadministrador, e a administração reúne os administradores e o dono.

---

## 1. Requisitos funcionais

### 1.1 Identidade e acesso

- **RF-001** – O sistema deve permitir que o visitante se cadastre informando nome, e-mail e senha e aceitando os termos de uso e a política de privacidade.
- **RF-002** – O sistema deve permitir que o visitante se cadastre usando uma conta Google cujo e-mail tenha sido verificado pelo Google, aceitando os termos de uso e a política de privacidade.
- **RF-003** – O sistema deve permitir que o usuário se autentique informando o e-mail ou o nickname e a senha.
- **RF-004** – O sistema deve permitir que o usuário se autentique usando a conta Google vinculada.
- **RF-005** – O sistema deve permitir o cadastro e o login com outros provedores sociais que o dono ativar na plataforma, como o GitHub e a Apple, desde que o e-mail tenha sido verificado pelo provedor e, no cadastro, aceitando os termos de uso e a política de privacidade.
- **RF-006** – O sistema deve exigir no cadastro a idade mínima definida pelo dono.
- **RF-007** – O sistema deve permitir que o usuário cadastre, use e remova passkeys para entrar sem senha.
- **RF-008** – O sistema deve permitir que o usuário entre sem senha, com um código de uso único enviado ao e-mail cadastrado.
- **RF-009** – O sistema deve impedir o acesso de contas não ativadas ou suspensas, em qualquer forma de login.
- **RF-010** – O sistema deve renovar o acesso do usuário autenticado, sem exigir um novo login, enquanto a sessão dele for válida.
- **RF-011** – O sistema deve permitir que o usuário encerre a sessão atual (logout).
- **RF-012** – O sistema deve permitir que o usuário visualize as próprias sessões ativas e encerre qualquer uma delas.
- **RF-013** – O sistema deve mostrar ao usuário o histórico dos acessos recentes à conta, com data, dispositivo e local aproximado.
- **RF-014** – O sistema deve exigir que o usuário confirme o e-mail, por meio de um código enviado a ele, para ativar uma conta criada com e-mail e senha, e permitir que ele peça um novo código.
- **RF-015** – O sistema deve permitir que o usuário redefina a senha esquecida por meio de um código enviado ao e-mail cadastrado.
- **RF-016** – O sistema deve permitir que o usuário autenticado altere a própria senha, informando a senha atual e um código enviado ao e-mail.
- **RF-017** – O sistema deve permitir que o usuário ative e desative a verificação em duas etapas com um aplicativo autenticador, recebendo códigos de recuperação de uso único.
- **RF-018** – O sistema deve exigir o código do aplicativo autenticador, ou um código de recuperação, no login dos usuários que ativaram a verificação em duas etapas.
- **RF-019** – O sistema deve permitir que o usuário marque um dispositivo como confiável, dispensando o código da verificação em duas etapas nele por um período limitado, e desfaça essa marcação.
- **RF-020** – O sistema deve exigir que os moderadores e a administração tenham a verificação em duas etapas ativa para usar as funções desses papéis.
- **RF-021** – O sistema deve pedir de novo a senha ou um código de verificação antes de ações sensíveis, como trocar o e-mail, desativar a verificação em duas etapas, excluir a conta, transferir a posse da plataforma ou baixar e restaurar backups, mesmo com o usuário autenticado.
- **RF-022** – O sistema deve permitir que o usuário vincule e desvincule uma conta Google, ou de outro provedor social ativo, à sua conta.
- **RF-023** – O sistema deve permitir que o usuário edite o próprio perfil: nome, foto, descrição e nickname, com um limite de frequência para a troca do nickname.
- **RF-024** – O sistema deve permitir que o usuário troque o e-mail da conta, confirmando o novo endereço com um código e avisando o endereço antigo.

### 1.2 Privacidade

- **RF-025** – O sistema deve permitir que o usuário escolha quais dados aparecem no próprio perfil público.
- **RF-026** – O sistema deve permitir que o usuário consulte todos os dados pessoais que a plataforma guarda sobre ele.
- **RF-027** – O sistema deve permitir que o usuário exporte os próprios dados em um arquivo.
- **RF-028** – O sistema deve permitir que o usuário exclua a própria conta e os dados pessoais dela, escolhendo se os artigos e comentários públicos dele são apagados ou mantidos de forma anônima.
- **RF-029** – O sistema deve permitir que o usuário desative a conta temporariamente, escondendo o perfil e os conteúdos dele, e a reative depois.
- **RF-030** – O sistema deve aplicar a exclusão da conta só depois de um prazo de carência, durante o qual o usuário pode desistir.
- **RF-031** – O sistema deve registrar a versão dos termos de uso e da política de privacidade aceita por cada usuário e pedir um novo aceite quando esses documentos mudarem.
- **RF-032** – O sistema deve avisar o usuário por e-mail quando a conta for acessada de um dispositivo novo.
- **RF-033** – O sistema deve permitir que o usuário veja, limpe e desligue o histórico de interesses usado nas recomendações.
- **RF-034** – O sistema deve mostrar ao usuário com quais serviços externos os dados dele são compartilhados e para quê.

### 1.3 Perfis e autoria

- **RF-035** – O sistema deve permitir que o dono escolha o modo de autoria da plataforma: por pedido, fechado ou aberto.
- **RF-036** – O sistema deve permitir que o leitor solicite permissão para se tornar autor, quando a plataforma estiver no modo por pedido.
- **RF-037** – O sistema deve permitir que a administração aprove ou recuse as solicitações de autoria, conforme as regras de aprovação da plataforma.
- **RF-038** – O sistema deve permitir que a administração nomeie autores diretamente, quando a plataforma estiver no modo fechado.
- **RF-039** – O sistema deve permitir que a administração retire o papel de autor de um usuário, que volta a ser leitor.
- **RF-040** – O sistema deve permitir que a administração conceda e retire o papel de moderador.
- **RF-041** – O sistema deve permitir a promoção de um moderador a administrador, com a aprovação de outros administradores, ou do superadministrador quando houver poucos administradores, conforme as regras da plataforma.
- **RF-042** – O sistema deve permitir que o superadministrador rebaixe administradores.
- **RF-043** – O sistema deve criar um único superadministrador na instalação da plataforma e impedir que qualquer outro perfil altere ou suspenda a conta dele.
- **RF-044** – O sistema deve permitir que o dono transfira o papel de superadministrador para outra pessoa, com confirmação reforçada, deixando de ser o dono.
- **RF-045** – O sistema deve permitir que o superadministrador promova uma pessoa diretamente para um perfil que ainda não tenha ninguém e nomeie um número limitado de autores iniciais sem passar pelo prazo e pela aprovação.

### 1.4 Conteúdo

- **RF-046** – O sistema deve permitir que qualquer pessoa, mesmo sem login, liste e leia os artigos publicados.
- **RF-047** – O sistema deve permitir que qualquer pessoa, mesmo sem login, leia os comentários dos artigos publicados, as páginas institucionais, os termos de uso e a política de privacidade.
- **RF-048** – O sistema deve mostrar a cada leitor a versão do artigo no idioma dele, quando existir.
- **RF-049** – O sistema deve permitir que o leitor troque a versão de idioma do artigo que está lendo ou volte à versão original.
- **RF-050** – O sistema deve permitir a busca de artigos publicados por texto, categoria, tag e autor.
- **RF-051** – O sistema deve informar o tempo estimado de leitura de cada artigo.
- **RF-052** – O sistema deve permitir que o leitor salve artigos em listas privadas para ler depois.
- **RF-053** – O sistema deve permitir que o autor crie, edite, salve como rascunho, publique e arquive os próprios artigos.
- **RF-054** – O sistema deve permitir que o autor exclua os próprios rascunhos e artigos.
- **RF-055** – O sistema deve aceitar o conteúdo dos artigos em Markdown, LaTeX, HTML com CSS ou texto puro, guardando o formato de cada artigo e permitindo, nos formatos que aceitam formatação, alinhar o texto à esquerda, ao centro, à direita ou justificado.
- **RF-056** – O sistema deve guardar, para cada fórmula de um artigo em LaTeX, uma descrição em texto para leitores de tela.
- **RF-057** – O sistema deve permitir que o autor agende a data e a hora de publicação de um artigo.
- **RF-058** – O sistema deve guardar o histórico de revisões de cada artigo e permitir que o autor compare e restaure versões anteriores.
- **RF-059** – O sistema deve permitir que o autor publique várias versões de idioma do mesmo artigo, que compartilham o mesmo endereço, os mesmos comentários e as mesmas reações.
- **RF-060** – O sistema deve informar o idioma de cada versão do artigo.
- **RF-061** – O sistema deve permitir que o autor convide outros autores como coautores de um artigo, que passam a editá-lo e aparecem como autores dele.
- **RF-062** – O sistema deve permitir que o autor convidado aceite ou recuse o convite de coautoria e saia de uma coautoria depois.
- **RF-063** – O sistema deve avisar quando dois coautores editarem o mesmo artigo ao mesmo tempo e impedir que um apague, sem perceber, as alterações do outro.
- **RF-064** – O sistema deve permitir que o autor agrupe os próprios artigos em séries ordenadas.
- **RF-065** – O sistema deve permitir que o autor gere um link privado, com prazo de validade e que pode ser revogado, para outra pessoa ler um rascunho antes da publicação.
- **RF-066** – O sistema deve permitir que o autor associe tags aos artigos e que qualquer pessoa filtre os artigos por tag.
- **RF-067** – O sistema deve permitir o envio de imagens para a foto de perfil do usuário, para a capa e para o corpo dos artigos, com legenda, alinhamento e posição livre no texto nos formatos que permitem.
- **RF-068** – O sistema deve exigir uma descrição para leitores de tela em cada imagem do corpo de um artigo antes da publicação.
- **RF-069** – O sistema deve permitir que o autor exporte os próprios artigos em um arquivo.
- **RF-070** – O sistema deve permitir que a administração cadastre, edite e remova categorias e subcategorias.
- **RF-071** – O sistema deve permitir que a administração edite, junte e remova tags.

### 1.5 Inteligência artificial

- **RF-072** – O sistema deve traduzir automaticamente, com IA, os artigos que não tenham versão no idioma do leitor, preservando a formatação.
- **RF-073** – O sistema deve permitir que o autor peça à IA a correção de ortografia e gramática do texto e decida quais correções aceitar.
- **RF-074** – O sistema deve permitir que o autor peça à IA sugestões para melhorar o texto e decida quais aceitar.
- **RF-075** – O sistema deve sugerir, com IA, tags e uma categoria para o artigo, cabendo ao autor aceitar ou não.
- **RF-076** – O sistema deve gerar, com IA, um resumo de cada artigo, que o autor pode revisar, para uso nas listagens e na busca.
- **RF-077** – O sistema deve sugerir, com IA, descrições das imagens e das fórmulas dos artigos para leitores de tela, cabendo ao autor aceitar ou editar.
- **RF-078** – O sistema deve classificar, com IA, comentários e denúncias pela chance de serem abusivos, para a moderação analisar primeiro os mais graves, sem remover nada sozinho.
- **RF-079** – O sistema deve permitir que o leitor escolha tags de interesse.
- **RF-080** – O sistema deve recomendar artigos a cada leitor a partir dos sinais que ele dá, como os autores que segue, as reações e as tags de interesse.
- **RF-081** – O sistema deve permitir que o dono ative e desative cada funcionalidade de IA na plataforma.

### 1.6 Social

- **RF-082** – O sistema deve permitir que o usuário autenticado comente artigos publicados, responda comentários e edite ou remova os próprios comentários.
- **RF-083** – O sistema deve permitir que o autor feche e reabra os comentários de um artigo.
- **RF-084** – O sistema deve permitir que o autor fixe um comentário no topo do artigo.
- **RF-085** – O sistema deve permitir que o usuário mencione outra pessoa com @nickname em um comentário, avisando a pessoa mencionada, exceto quando um bloqueio impedir.
- **RF-086** – O sistema deve permitir que o usuário autenticado reaja aos artigos publicados.
- **RF-087** – O sistema deve permitir que o usuário autenticado reaja aos comentários.
- **RF-088** – O sistema deve permitir que o usuário autenticado siga e deixe de seguir autores.
- **RF-089** – O sistema deve permitir que o usuário veja a lista dos autores que ele segue.
- **RF-090** – O sistema deve exibir ao usuário autenticado um feed com os artigos recentes dos autores que ele segue.
- **RF-091** – O sistema deve permitir que o usuário bloqueie outros usuários, que deixam de poder comentar nos artigos dele, responder a ele e segui-lo, e desfaça o bloqueio.
- **RF-092** – O sistema deve exibir o perfil público de cada usuário, apenas com os dados que ele escolheu mostrar, e, no caso dos autores, os artigos publicados por eles.

### 1.7 Notificações

- **RF-093** – O sistema deve avisar o usuário, dentro da plataforma e em tempo real, sobre eventos que o envolvem, como novos comentários, novos seguidores, respostas, menções, anúncios da plataforma e o resultado de pedidos, denúncias e recursos.
- **RF-094** – O sistema deve enviar esses avisos também por e-mail.
- **RF-095** – O sistema deve permitir que o usuário escolha, para cada tipo de aviso, por quais canais quer recebê-lo.
- **RF-096** – O sistema deve permitir que o usuário liste os próprios avisos e os marque como lidos.
- **RF-097** – O sistema deve permitir que o usuário escolha receber os avisos por e-mail reunidos em um resumo diário, em vez de um e-mail por evento.

### 1.8 Moderação

- **RF-098** – O sistema deve permitir que o usuário autenticado denuncie artigos, comentários e perfis de usuários.
- **RF-099** – O sistema deve permitir que os moderadores e a administração analisem as denúncias e apliquem ações de moderação, como arquivar a denúncia, ocultar o conteúdo, remover comentários ou penalizar o autor.
- **RF-100** – O sistema deve informar a quem denunciou o resultado da análise.
- **RF-101** – O sistema deve permitir que o autor recorra de uma ocultação ou penalidade, e garantir que o recurso seja analisado por um moderador ou alguém da administração diferente de quem tomou a decisão.
- **RF-102** – O sistema deve permitir que o dono configure uma lista de palavras bloqueadas, que impede a publicação de comentários que as contenham.

### 1.9 Painéis

- **RF-103** – O sistema deve oferecer a todo usuário cadastrado um painel com os próprios dados e configurações de privacidade, sessões, histórico de interesses, listas de leitura, usuários bloqueados, pedidos feitos e o resultado das denúncias que ele fez.
- **RF-104** – O sistema deve oferecer a cada autor um painel com os próprios artigos, rascunhos e agendamentos, o histórico de revisões, as versões de idioma, estatísticas agregadas por artigo e idioma (leituras, reações e comentários) e o número de seguidores.
- **RF-105** – O sistema deve oferecer à administração um painel com a lista de autores, a atividade de cada um e os pedidos de autoria pendentes.
- **RF-106** – O sistema deve oferecer aos autores um espaço comum, em que a administração publica avisos e diretrizes de escrita.
- **RF-107** – O sistema deve oferecer aos moderadores e à administração um painel com a fila de denúncias por motivo, o conteúdo ocultado e o tempo médio de resolução.
- **RF-108** – O sistema deve oferecer a cada moderador um painel, visível também para a administração, com as denúncias que ele tratou, as ações que tomou e o andamento rumo à promoção.
- **RF-109** – O sistema deve oferecer ao superadministrador um painel com a identidade e as páginas da plataforma, os convites, os usuários e papéis, as regras, o backup, todas as estatísticas e métricas da plataforma, os logs e o registro das ações administrativas.

### 1.10 Administração da plataforma

- **RF-110** – O sistema deve permitir que o dono defina a identidade da plataforma: nome, descrição, logo, idioma padrão e links.
- **RF-111** – O sistema deve permitir que o dono crie e edite páginas institucionais, como Sobre, Contato e Regras da comunidade.
- **RF-112** – O sistema deve permitir que o dono informe o contato do encarregado pelos dados pessoais, que fica público na plataforma.
- **RF-113** – O sistema deve permitir que o dono publique novas versões dos termos de uso e da política de privacidade.
- **RF-114** – O sistema deve permitir que a administração convide pessoas por e-mail para a plataforma ou diretamente para um perfil, respeitando as regras de concessão e de promoção de cada perfil.
- **RF-115** – O sistema deve permitir que o dono faça e baixe um backup dos dados da plataforma.
- **RF-116** – O sistema deve permitir que o dono restaure a plataforma a partir de um backup.
- **RF-117** – O sistema deve permitir que a administração liste e gerencie os usuários e os papéis deles, conforme os limites de cada perfil.
- **RF-118** – O sistema deve permitir que a administração suspenda e reative contas.
- **RF-119** – O sistema deve permitir que o dono ajuste as regras da plataforma, como prazos, quantidade de aprovações e limites de uso, dentro de limites seguros, guardando o histórico de cada mudança.
- **RF-120** – O sistema deve mostrar ao dono a saúde do sistema, como a disponibilidade da API, do banco de dados e dos serviços externos.
- **RF-121** – O sistema deve mostrar ao dono estatísticas agregadas de acesso por período: número de visitas e de visitantes, páginas e artigos mais acessados, horários de pico e idiomas e regiões aproximadas de quem acessa.
- **RF-122** – O sistema deve mostrar ao dono estatísticas agregadas da comunidade por período: novos usuários, usuários ativos, artigos publicados, comentários e reações, autores, categorias e tags mais lidos e termos mais buscados.
- **RF-123** – O sistema deve mostrar ao dono o uso e o custo estimado de cada serviço pago no mês (inteligência artificial, e-mails, armazenamento de imagens e login com Apple), comparados ao teto de custos.
- **RF-124** – O sistema deve avisar o dono quando o gasto do mês com os serviços pagos se aproximar do teto e, se o teto for atingido, pausar automaticamente os serviços não essenciais, mantendo os e-mails de ativação, de recuperação de senha e de segurança.
- **RF-125** – O sistema deve mostrar ao dono métricas técnicas: endpoints mais usados, tempo de resposta, taxa de erros e uso de recursos.
- **RF-126** – O sistema deve mostrar ao dono métricas de crescimento por período: conversão de visitantes em cadastros e de leitores em autores, e a retenção dos usuários.
- **RF-127** – O sistema deve mostrar ao dono métricas de segurança: tentativas de login que falharam, bloqueios por limite de requisições, acessos de dispositivos novos e a porcentagem de contas com verificação em duas etapas.
- **RF-128** – O sistema deve mostrar ao dono o uso das funcionalidades de IA e dos e-mails: quantidade de traduções, correções, sugestões, resumos e triagens, os idiomas mais pedidos nas traduções e os e-mails enviados e recusados.
- **RF-129** – O sistema deve permitir que o dono, e só ele, consulte os logs técnicos e de segurança.
- **RF-130** – O sistema deve registrar todas as ações administrativas, com quem fez, o quê e quando, e mostrar esse registro ao dono.
- **RF-131** – O sistema deve permitir que o dono coloque a plataforma em modo de manutenção, em que só os moderadores e a administração conseguem acessá-la.
- **RF-132** – O sistema deve permitir que o dono publique anúncios para todos os usuários, enviados como aviso dentro da plataforma.

---

## 2. Requisitos não funcionais

### 2.1 Segurança

- **RNF-001** – O sistema deve armazenar as senhas apenas na forma de hash, usando o algoritmo BCrypt.
- **RNF-002** – O sistema deve autenticar as requisições de forma stateless, com tokens JWT assinados e enviados em cookies `HttpOnly` e `SameSite=Strict`.
- **RNF-003** – O sistema deve proteger todos os endpoints por padrão, liberando sem autenticação apenas as rotas explicitamente declaradas como públicas.
- **RNF-004** – O sistema deve enviar os cookies de autenticação com o atributo `Secure` fora do ambiente local, com validade igual à do token.
- **RNF-005** – O sistema deve obter senhas, chaves e credenciais apenas de variáveis de ambiente, sem armazená-las no código-fonte.
- **RNF-006** – O sistema deve armazenar os tokens de renovação, os códigos enviados por e-mail e os códigos de recuperação apenas na forma de hash.
- **RNF-007** – O sistema deve limitar o número de tentativas e a frequência de login, de verificação de códigos e de reenvio de e-mails.
- **RNF-008** – O sistema não deve expor, em nenhuma resposta, senhas, hashes, tokens, identificadores de provedores externos ou dados privados de outros usuários.
- **RNF-009** – O sistema deve limpar o HTML e o CSS dos artigos de qualquer código perigoso antes de salvá-los, aceitando só o que estiver em uma lista de elementos permitidos, que inclui a formatação dos artigos, como imagens, legendas e alinhamentos.
- **RNF-010** – O sistema deve remover os metadados das imagens enviadas, como a localização, e aceitar só formatos de imagem seguros.
- **RNF-011** – O sistema deve bloquear nos artigos imagens e outros recursos carregados de sites externos, aceitando só as imagens enviadas à plataforma.
- **RNF-012** – O sistema deve tratar o conteúdo enviado à IA apenas como dado, sem que ele altere as instruções dela, e validar as respostas da IA antes de usá-las.
- **RNF-013** – O sistema deve responder do mesmo jeito no cadastro, no login e na recuperação de senha, exista ou não a conta, para não revelar quais e-mails estão cadastrados.
- **RNF-014** – O sistema deve guardar os backups criptografados e registrar cada download deles.
- **RNF-015** – O sistema deve limitar a frequência de requisições em toda a API, inclusive nos endpoints públicos, para proteger a plataforma contra abuso e robôs.

### 2.2 Privacidade e proteção de dados

- **RNF-016** – O sistema deve coletar só os dados pessoais necessários para cada funcionalidade e seguir os princípios da LGPD.
- **RNF-017** – O sistema deve enviar à IA só o conteúdo necessário para cada tarefa, nunca os dados das contas, e enviar à triagem da moderação o texto sem nome, e-mail ou qualquer identificador de quem o escreveu.
- **RNF-018** – O sistema deve mostrar as estatísticas dos painéis e da plataforma sempre de forma agregada, sem rastrear pessoas individualmente e sem revelar quem leu, acessou ou reagiu a um conteúdo.
- **RNF-019** – O sistema deve mascarar os dados pessoais nos logs, apagá-los depois de um prazo de retenção definido e registrar cada consulta feita aos logs.
- **RNF-020** – O sistema deve guardar os registros de acesso pelo prazo exigido pela lei e apagá-los depois dele.
- **RNF-021** – O sistema deve apagar também dos backups os dados das contas excluídas, depois de um prazo definido.

### 2.3 Custos e limites de uso

- **RNF-022** – O sistema deve manter o custo somado dos serviços pagos (inteligência artificial, armazenamento de imagens, envio de e-mails e login com Apple) em no máximo US$ 15 por mês.
- **RNF-023** – O sistema deve limitar o uso da IA e o envio de e-mails, por usuário e no total da plataforma, com limites que o dono pode ajustar.
- **RNF-024** – O sistema deve gerar cada tradução e cada resumo uma única vez e reaproveitá-los para todos os leitores, até o artigo mudar.

### 2.4 Usabilidade e padronização

- **RNF-025** – O sistema deve responder com mensagens em português do Brasil ou em inglês, conforme o cabeçalho `Accept-Language` da requisição, usando o idioma padrão definido pelo dono quando o cabeçalho não indicar um idioma disponível, com a estrutura pronta para receber novos idiomas.
- **RNF-026** – O sistema deve devolver todos os erros em um formato único, contendo data e hora, status HTTP, descrição, mensagem traduzida, caminho da requisição e, quando houver, os erros por campo.
- **RNF-027** – O sistema deve disponibilizar documentação interativa da API no padrão OpenAPI (Swagger).
- **RNF-028** – O sistema deve preservar a formatação de cada artigo (Markdown, LaTeX, HTML com CSS ou texto puro) na tradução e na correção feitas pela IA.

### 2.5 Desempenho

- **RNF-029** – O sistema deve paginar todas as listagens, com um tamanho máximo de página.
- **RNF-030** – O sistema deve entregar os avisos dentro da plataforma em tempo real, sem que o front-end precise consultar a API repetidamente.

### 2.6 Manutenibilidade e qualidade

- **RNF-031** – O sistema deve seguir a Pragmatic Clean Architecture, com o código organizado por funcionalidade e dividido nas camadas de domínio, aplicação, infraestrutura e apresentação.
- **RNF-032** – O sistema deve trocar dados com o front-end apenas por meio de DTOs, sem expor as entidades do banco.
- **RNF-033** – O sistema deve ter um teste automatizado para cada classe de produção.
- **RNF-034** – O sistema deve registrar em log os erros internos com a causa original e responder ao front-end com uma mensagem genérica, sem detalhes técnicos.
- **RNF-035** – O sistema deve ser testado sem chamar serviços externos de verdade.
- **RNF-036** – O sistema deve acessar cada serviço externo por meio de um gateway, para que o provedor possa ser trocado sem mudar o resto do sistema.

### 2.7 Dados e compatibilidade

- **RNF-037** – O sistema deve usar o PostgreSQL como banco de dados, com o esquema versionado exclusivamente por migrations do Flyway.
- **RNF-038** – O sistema deve prefixar todas as rotas com a versão da API (`/api/v1`).
- **RNF-039** – O sistema deve ser executável localmente, na versão do Java definida na [Arquitetura](Arquitetura.md) e com PostgreSQL, sem depender de contêineres.

### 2.8 Operação

- **RNF-040** – O sistema deve ter configurações separadas por ambiente, com a de produção pronta para uso, mesmo sem a publicação.
- **RNF-041** – O sistema deve expor as métricas técnicas e o estado de saúde do sistema, acessíveis só ao superadministrador.
- **RNF-042** – O sistema deve continuar funcionando quando a IA estiver indisponível, o limite de uso for atingido ou os serviços pagos estiverem pausados, mostrando o conteúdo original e avisando que a funcionalidade está indisponível.
- **RNF-043** – O sistema deve reenviar automaticamente, algumas vezes, os e-mails cujo envio falhar, antes de considerá-los perdidos.
- **RNF-044** – O sistema deve poder ser executado, com o banco de dados, em contêineres Docker.
