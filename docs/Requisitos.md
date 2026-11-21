# Requisitos

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

### 1.2 Autoria e conteúdo

- **RF-020** – O sistema deve permitir que o leitor solicite permissão para se tornar autor.
- **RF-021** – O sistema deve permitir que o administrador aprove ou recuse as solicitações de autoria.
- **RF-022** – O sistema deve permitir que o administrador cadastre, edite e remova categorias e subcategorias.
- **RF-023** – O sistema deve permitir que o autor crie, edite, salve como rascunho, publique e arquive os próprios artigos.
- **RF-024** – O sistema deve permitir que o autor associe tags aos artigos e que qualquer pessoa filtre os artigos por tag.
- **RF-025** – O sistema deve permitir o envio de imagens para a foto de perfil do usuário e para a capa dos artigos.
- **RF-026** – O sistema deve permitir a busca de artigos publicados por texto, categoria, tag e autor.

### 1.3 Social

- **RF-030** – O sistema deve permitir que o usuário autenticado comente artigos publicados e responda comentários.
- **RF-031** – O sistema deve permitir que o usuário autenticado reaja aos artigos publicados.
- **RF-032** – O sistema deve permitir que o usuário autenticado siga e deixe de seguir autores.
- **RF-033** – O sistema deve exibir ao usuário autenticado um feed com os artigos recentes dos autores que ele segue.

### 1.4 Moderação

- **RF-040** – O sistema deve permitir que o usuário autenticado denuncie artigos e comentários.
- **RF-041** – O sistema deve permitir que o moderador e o administrador analisem as denúncias e apliquem ações de moderação.

---

## 2. Requisitos Não Funcionais

### 2.1 Segurança

- **RNF-001** – O sistema deve armazenar as senhas apenas na forma de hash, usando o algoritmo BCrypt.
- **RNF-002** – O sistema deve autenticar as requisições de forma stateless, com tokens JWT assinados e enviados em cookies `HttpOnly` e `SameSite=Strict`.
- **RNF-003** – O sistema deve proteger todos os endpoints por padrão, liberando sem autenticação apenas as rotas explicitamente declaradas como públicas.
- **RNF-004** – O sistema deve enviar os cookies de autenticação com o atributo `Secure` fora do ambiente local, com validade igual à do token.
- **RNF-005** – O sistema deve obter senhas, chaves e credenciais apenas de variáveis de ambiente, sem armazená-las no código-fonte.
- **RNF-006** – O sistema deve armazenar os tokens de renovação e os códigos enviados por e-mail apenas na forma de hash.
- **RNF-007** – O sistema deve limitar o número de tentativas e a frequência de login, de verificação de códigos e de reenvio de e-mails.
- **RNF-008** – O sistema não deve expor, em nenhuma resposta, senhas, hashes, tokens, identificadores de provedores externos ou dados privados de outros usuários.

### 2.2 Usabilidade e padronização

- **RNF-009** – O sistema deve responder com mensagens em português do Brasil (padrão) ou em inglês, conforme o cabeçalho `Accept-Language` da requisição.
- **RNF-010** – O sistema deve devolver todos os erros em um formato único, contendo data e hora, status HTTP, descrição, mensagem traduzida, caminho da requisição e, quando houver, os erros por campo.
- **RNF-011** – O sistema deve disponibilizar documentação interativa da API no padrão OpenAPI (Swagger).

### 2.3 Desempenho

- **RNF-012** – O sistema deve paginar todas as listagens, com um tamanho máximo de página.

### 2.4 Manutenibilidade e qualidade

- **RNF-013** – O sistema deve seguir a Pragmatic Clean Architecture, com o código organizado por funcionalidade e dividido nas camadas de domínio, aplicação, infraestrutura e apresentação.
- **RNF-014** – O sistema deve trocar dados com os clientes apenas por meio de DTOs, sem expor as entidades do banco.
- **RNF-015** – O sistema deve ter um teste automatizado para cada classe de produção.
- **RNF-016** – O sistema deve registrar em log os erros internos com a causa original e responder ao cliente com uma mensagem genérica, sem detalhes técnicos.

### 2.5 Dados e compatibilidade

- **RNF-017** – O sistema deve usar o PostgreSQL como banco de dados, com o esquema versionado exclusivamente por migrations do Flyway.
- **RNF-018** – O sistema deve prefixar todas as rotas com a versão da API (`/api/v1`).
- **RNF-019** – O sistema deve ser executável localmente com Java 25 e PostgreSQL, sem depender de contêineres.
