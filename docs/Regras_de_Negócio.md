# Regras de Negócio

Este documento reúne as **regras que o sistema obedece**: validações, restrições, permissões e comportamentos esperados. Cada regra tem um identificador (RN-xxx), a explicação do **porquê** ela existe, um exemplo e o erro que o cliente recebe quando ela é violada. Os requisitos que as regras detalham estão em [Requisitos.md](Requisitos.md).

---

## 1. Como ler este documento

- **Status:** ✅ implementado · 🟡 parcial · ⬜ planejado.
- **Regras implementadas** descrevem o comportamento **atual** do código. Quando o código não cumpre totalmente a regra, a diferença aparece marcada com ⚠️ e está descrita na seção 5 (*Divergências conhecidas*).
- **Erros:** status HTTP + **chave de i18n**. A chave é traduzida conforme o cabeçalho `Accept-Language` (pt-BR por padrão, ou en) e devolvida no campo `message` do `ErrorResponseDTO`. Para regras planejadas, a chave é uma proposta.
- **Manutenção:** uma regra nova recebe o próximo número livre do bloco do módulo. Toda regra implementada tem pelo menos um teste que cita o ID no `@DisplayName`. Uma regra que muda é editada aqui no mesmo PR da mudança de código.

### Formato de erro

Toda violação de regra devolve um corpo neste formato:

```json
{
  "timestamp": "2026-11-15T14:32:10.123",
  "status": 409,
  "error": "Conflict",
  "message": "Já existe um usuário cadastrado com este e-mail.",
  "path": "/api/v1/register"
}
```

Em erros de validação (`400`), aparece também o campo `validationErrors`, com uma mensagem por campo:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Erro de validação nos dados fornecidos.",
  "path": "/api/v1/register",
  "validationErrors": {
    "email": "Formato de e-mail inválido.",
    "password": "A senha deve conter pelo menos 2 números."
  }
}
```

---

## 2. Identidade e acesso

### 2.1 Cadastro

#### RN-001 – E-mail único ✅
- **Regra:** cada e-mail pertence a uma única conta, seja ela local ou Google.
- **Por quê:** o e-mail identifica a pessoa, é usado no login e será o canal de recuperação de senha. Duas contas com o mesmo e-mail tornariam esses fluxos ambíguos.
- **Como funciona:** antes de criar a conta, o caso de uso consulta `existsByEmail`. A coluna `email` também tem uma restrição `UNIQUE` no banco, como segunda barreira.
- **Exemplo:** `ana@exemplo.com` cadastra-se com senha; depois, alguém tenta cadastrar o mesmo e-mail → recusado.
- **Erro:** `409 Conflict` · `error.user.already_exists` ("Já existe um usuário cadastrado com este e-mail.").
- **Requisitos:** RF-001, RF-002.

#### RN-002 – Dados obrigatórios e limites do cadastro ✅
- **Regra:**

  | Campo | Obrigatório | Formato | Limite |
  |---|---|---|---|
  | `name` | Sim | Texto livre | Até 150 caracteres |
  | `email` | Sim | E-mail válido | Até 150 caracteres |
  | `password` | Sim | Ver RN-004 | Mínimo de 8 caracteres |

- **Por quê:** os limites acompanham as colunas do banco (`VARCHAR(150)`) e evitam erros de banco disfarçados de erro interno.
- **Erro:** `400 Bad Request` · `error.validation.generic`, com mensagens por campo como `error.validation.user.name.not_blank`, `error.validation.user.name.size`, `error.validation.user.email.invalid`, `error.validation.user.email.size`.
- **Requisito:** RF-001.

#### RN-003 – Estado inicial da conta ✅ *(muda com a RN-016)*
- **Regra:** toda conta nova nasce com o papel **READER**, **ativa** (`enabled = true`) e **desbloqueada** (`locked = false`).
- **Por quê:** ninguém nasce autor nem administrador. Os papéis maiores são concedidos por processos próprios (RN-032). A partir da v0.1, a conta local passará a nascer **desativada** até a confirmação do e-mail.
- **Como funciona:** as fábricas `User.createLocalUser` e `User.createGoogleUser` definem esses valores; não é possível criar um usuário por outro caminho.
- **Requisitos:** RF-001, RF-002.

#### RN-004 – Política de senha ✅
- **Regra:** a senha local deve ter **no mínimo 8 caracteres**, com pelo menos **2 letras maiúsculas**, **2 letras minúsculas**, **2 números** e **1 caractere especial**. Ela é armazenada só como hash **BCrypt**.
- **Por quê:** senhas curtas ou sem variedade são fáceis de adivinhar por força bruta. O BCrypt é lento de propósito e usa *salt*, o que dificulta ataques mesmo que o banco vaze.
- **Exemplos:** `SenhaForte12!` ✔ (2 maiúsculas? S e F ✔, minúsculas ✔, 2 números ✔, especial ✔) · `senha123` ✘ (sem maiúsculas e sem especial).
- **Erro:** `400` · uma mensagem por exigência que falhar: `error.validation.user.password.size`, `.uppercase`, `.lowercase`, `.number`, `.special`.
- **Requisitos:** RF-001 e, no futuro, RF-014 e RF-015.

#### RN-005 – Geração automática do nickname 🟡 ⚠️ D1
- **Regra:** o nickname é gerado a partir do nome:
  1. o nome é convertido para minúsculas;
  2. os espaços viram `_`;
  3. os caracteres fora de `a-z`, `0-9` e `_` são removidos (acentos inclusive);
  4. o resultado é cortado em 40 caracteres;
  5. acrescenta-se `_` e um número aleatório de 4 dígitos (1000 a 9999), gerado com `SecureRandom`.
  
  O nickname é **único** (restrição `UNIQUE`, até 50 caracteres).
- **Por quê:** o usuário pode entrar pelo nickname (RN-009) sem precisar escolher um no cadastro. O sufixo aleatório reduz colisões.
- **Exemplo:** "Ana Júlia Souza" → `ana_jlia_souza_4821`.
- **Divergência:** não há nova tentativa em caso de colisão (ver D1). Nomes formados só por caracteres removidos geram um nickname como `_4821`.
- **Requisitos:** RF-001, RF-002.

#### RN-006 – Cadastro com Google ✅ ⚠️ D5
- **Regra:** o cadastro com Google recebe o *ID Token* emitido pelo Google. O backend **valida a assinatura e o destinatário** do token (o `client-id` configurado) e usa o `sub` (vira `googleId`), o `email` e o `name` do token. A conta Google **não tem senha local**.
- **Por quê:** quem prova a identidade é o Google. A API não confia em dados enviados pelo cliente, só no que vem dentro de um token assinado.
- **Erros:**
  - `400` · `error.validation.auth.google_token_required` (token vazio);
  - `401` · `error.auth.google_token_invalid` (token inválido, expirado, de outro aplicativo ou forjado).
- **Divergência:** a API não confere o campo `email_verified` do token (ver D5).
- **Requisito:** RF-002.

#### RN-007 – Conta Google única ✅ ⚠️ D2
- **Regra:** cada conta Google (`googleId`) está ligada a no máximo um usuário.
- **Erro:** `409 Conflict`. Hoje: `error.user.provider_conflict` para `googleId` repetido e `error.user.already_exists` para e-mail repetido. Ver D2, porque as mensagens parecem trocadas.
- **Requisito:** RF-002.

### 2.2 Login

#### RN-008 – Emissão do token de acesso ✅ ⚠️ D4
- **Regra:** um login bem-sucedido emite um **JWT** assinado com HMAC256, com emissor = nome da aplicação, assunto = e-mail do usuário e validade = `JWT_EXPIRATION`. O token é entregue no cabeçalho `Set-Cookie`, em um cookie `HttpOnly`, `SameSite=Strict`, `path=/`. O corpo da resposta traz o perfil do usuário.
- **Por quê:** um cookie `HttpOnly` não pode ser lido por JavaScript, o que protege o token contra roubo via XSS. `SameSite=Strict` impede que outros sites enviem o cookie em requisições forjadas (CSRF).
- **Divergência:** a validade do cookie está fixa em 24 h e `Secure=false` (ver D4).
- **Requisitos:** RF-003, RF-004.

#### RN-009 – Login por e-mail ou nickname ✅
- **Regra:** o campo `login` aceita e-mail ou nickname. Se contém `@`, a busca é por e-mail; senão, por nickname (nicknames nunca contêm `@`, pela RN-005). A senha é comparada com o hash BCrypt.
- **Respostas:**
  - credenciais erradas (usuário inexistente ou senha incorreta) → `401` · `error.auth.bad_credentials`. A mensagem é **a mesma** nos dois casos, para não revelar quais contas existem;
  - conta desativada ou bloqueada → `403` · `error.auth.account_inactive`;
  - `login` ou `password` em branco → `400` · `error.validation.user.login.not_blank` / `error.validation.user.password.not_blank`.
- **Observação:** uma conta só com Google não tem senha, então o login local dela sempre falha com `401`.
- **Requisito:** RF-003.

#### RN-010 – Login com Google 🟡 ⚠️ D3
- **Regra:** o ID Token é validado (como na RN-006) e a conta é buscada pelo `googleId`.
- **Respostas:**
  - conta não encontrada → `404` · `error.auth.user_not_found_google` ("Conta não encontrada. Por favor, realize o cadastro antes de fazer login.");
  - token inválido → `401` · `error.auth.google_token_invalid`.
- **Divergência:** não verifica se a conta está desativada ou bloqueada (ver D3).
- **Requisito:** RF-004.

### 2.3 Sessões *(v0.1)*

#### RN-011 – Tokens de curta e longa duração ⬜
- **Regra:** o token de acesso dura **15 minutos**; o **Refresh Token** dura **7 dias**. Cada login cria uma **sessão** com usuário, hash do Refresh Token, IP, User-Agent, data de criação, último uso, expiração e data de revogação. O logout revoga a sessão atual e apaga os cookies.
- **Por quê:** um token de acesso curto limita o estrago de um token roubado. O Refresh Token, guardado no banco, permite revogar sessões, algo que um JWT sozinho não permite.
- **Requisitos:** RF-009, RF-010.

#### RN-012 – Rotação do Refresh Token ⬜
- **Regra:** cada uso do Refresh Token gera um **novo par** de tokens (acesso + refresh) e invalida o Refresh Token usado.
- **Por quê:** um Refresh Token roubado perde valor assim que o dono legítimo o usa.
- **Requisito:** RF-009.

#### RN-013 – Detecção de reuso ⬜
- **Regra:** se um Refresh Token **já usado** for apresentado de novo, **todas** as sessões do usuário são revogadas e a resposta é `401`.
- **Por quê:** o reuso indica que o token foi copiado por outra pessoa. Encerrar tudo obriga um novo login de quem é o verdadeiro dono.
- **Erro (proposto):** `401` · `error.auth.refresh_token_reused`.
- **Requisito:** RF-009.

#### RN-014 – Gestão das próprias sessões ⬜
- **Regra:** o usuário lista e revoga **só as próprias** sessões. "Revogar todas" mantém a sessão atual. Uma sessão de outro usuário é tratada como inexistente (`404`), para não revelar que ela existe.
- **Requisito:** RF-011.

### 2.4 E-mail, ativação e senha *(v0.1)*

#### RN-015 – Códigos enviados por e-mail ⬜
- **Regra:** os códigos têm **6 dígitos**, valem **10 minutos**, são de **uso único**, aceitam no máximo **5 tentativas** e são guardados **só como hash**. Cada código tem uma finalidade (`ATIVACAO`, `RECUPERACAO_SENHA`, `TROCA_SENHA`) e não serve para outra.
- **Por quê:** a validade curta e o limite de tentativas tornam inviável adivinhar o código (1 milhão de combinações). O hash protege os códigos se o banco vazar.
- **Erros (propostos):** `422` · `error.code.invalid`, `error.code.expired`, `error.code.attempts_exceeded`.
- **Requisitos:** RF-013, RF-014, RF-015.

#### RN-016 – Conta local nasce desativada ⬜
- **Regra:** a partir da v0.1, a conta local nasce com `enabled = false` e só é ativada com o código de ativação. A conta Google nasce ativa, porque o Google já verificou o e-mail (ver também D5).
- **Por quê:** garante que o e-mail pertence a quem se cadastrou, condição para a recuperação de senha funcionar com segurança.
- **Requisito:** RF-013.

#### RN-017 – Limite de reenvio do código de ativação ⬜
- **Regra:** um novo código só pode ser pedido 60 segundos após o anterior, com no máximo 5 pedidos por hora. Um novo código invalida o anterior.
- **Por quê:** evita abuso do envio de e-mails (spam e custo).
- **Erro (proposto):** `429 Too Many Requests` · `error.code.too_many_requests`.
- **Requisito:** RF-013.

#### RN-018 – Resposta neutra na recuperação de senha ⬜
- **Regra:** o pedido de recuperação responde **sempre da mesma forma** ("Se o e-mail estiver cadastrado, você receberá um código"), exista ou não a conta.
- **Por quê:** impede que alguém use a tela de recuperação para descobrir quais e-mails estão cadastrados (*enumeração de usuários*).
- **Requisito:** RF-014.

#### RN-019 – Redefinir a senha encerra as sessões ⬜
- **Regra:** ao redefinir a senha, **todas** as sessões do usuário são revogadas.
- **Por quê:** quem esqueceu a senha pode estar recuperando uma conta comprometida; as sessões antigas podem estar com o invasor.
- **Requisito:** RF-014.

#### RN-020 – Troca de senha estando logado ⬜
- **Regra:** a troca exige a **senha atual** e um **código por e-mail**. A nova senha segue a RN-004 e deve ser **diferente** da atual. As **outras** sessões são revogadas; a atual continua.
- **Requisito:** RF-015.

### 2.5 Segurança avançada *(v0.4)*

#### RN-021 – 2FA com TOTP ⬜
- **Regra:**
  - o usuário recebe um segredo TOTP (em QR Code) e só **ativa** o 2FA depois de confirmar um código válido;
  - na ativação, são gerados **10 códigos de recuperação** de uso único, mostrados uma única vez;
  - com o 2FA ativo, o login com senha **não emite tokens** até o código TOTP ser confirmado;
  - desativar exige a senha e um código.
- **Requisito:** RF-016.

#### RN-022 – Vínculo com Google ⬜
- **Regra:** vincular exige um ID Token válido cujo `googleId` não pertença a outra conta. Desvincular só é permitido se a conta tiver senha local, para que o usuário não perca o acesso.
- **Requisito:** RF-021.

---

## 3. Autoria e conteúdo *(v0.2–v0.3)*

#### RN-030 – Prazo mínimo para pedir autoria ⬜
- **Regra:** só pode pedir autoria quem tem a conta **criada há mais de 2 meses**.
- **Por quê:** reduz contas criadas só para publicar spam e incentiva a participação como leitor antes.
- **Erro (proposto):** `422` · `error.author_request.account_too_new`.
- **Requisito:** RF-034.

#### RN-031 – Um pedido pendente por vez ⬜
- **Regra:** um usuário tem no máximo **um** pedido de autoria pendente. Depois de uma recusa, pode pedir de novo.
- **Erro (proposto):** `409` · `error.author_request.already_pending`.
- **Requisito:** RF-034.

#### RN-032 – Aprovação de autoria ⬜
- **Regra:** só o `ADMIN` aprova ou recusa. A aprovação muda o papel para `AUTHOR`. A recusa exige um motivo, visível para o solicitante.
- **Erro:** `403` · `error.auth.access_denied` para quem não é admin.
- **Requisito:** RF-035.

#### RN-033 – Categorias ⬜
- **Regra:** a categoria tem nome, **slug único** e uma categoria pai opcional, formando uma árvore. Uma categoria não pode ser pai dela mesma, nem de um ancestral. Só o `ADMIN` gerencia categorias.
- **Requisito:** RF-059.

#### RN-034 – Remoção de categorias ⬜
- **Regra:** não é possível remover uma categoria que tenha artigos ou subcategorias.
- **Por quê:** evita artigos sem categoria e árvores quebradas.
- **Erro (proposto):** `422` · `error.category.in_use`.
- **Requisito:** RF-059.

#### RN-035 – Quem escreve artigos ⬜
- **Regra:** só `AUTHOR` ou `ADMIN` criam artigos, e só o próprio autor os edita.
- **Requisito:** RF-048.

#### RN-036 – Ciclo de vida do artigo ⬜
- **Regra:** o artigo tem o status `RASCUNHO`, `PUBLICADO` ou `ARQUIVADO`. As transições permitidas são:
  - rascunho → publicado;
  - publicado → arquivado;
  - arquivado → rascunho.

  A **data de publicação** é registrada na primeira publicação. O **slug** é gerado a partir do título e é único.
- **Requisito:** RF-048.

#### RN-037 – Rascunhos são privados ⬜
- **Regra:** os rascunhos só são visíveis para o autor. Para os demais, o rascunho "não existe" (`404`).
- **Requisito:** RF-048.

#### RN-038 – Listagens públicas ⬜
- **Regra:** as listagens públicas mostram só artigos **publicados**, paginados (padrão 10, máximo 50 por página) e ordenados por data de publicação, do mais recente para o mais antigo.
- **Requisito:** RF-048.

#### RN-039 – Tags ⬜
- **Regra:** as tags são **normalizadas** (minúsculas, sem espaços nas pontas) e **únicas**. Uma tag inexistente é criada ao ser usada. Cada artigo tem no máximo 10 tags.
- **Requisito:** RF-056.

#### RN-040 – Imagens ⬜
- **Regra:** são aceitos só os formatos JPEG, PNG e WebP, com até 5 MB. Ao trocar uma imagem, a anterior é removida do armazenamento.
- **Erro (proposto):** `400` · `error.upload.invalid_type` / `error.upload.too_large`.
- **Requisito:** RF-057.

#### RN-041 – Busca ⬜
- **Regra:** a busca considera só artigos publicados e procura no título e no resumo, ignorando maiúsculas e acentos. Pode ser combinada com filtros de categoria, tag e autor.
- **Requisito:** RF-045.

---

## 4. Social e moderação *(v0.3–v0.4)*

#### RN-050 – Comentários ⬜
- **Regra:** só é possível comentar artigos publicados. Há respostas em **um único nível** (comentário → resposta). Responder uma resposta não é permitido.
- **Requisito:** RF-069.

#### RN-051 – Quem altera comentários ⬜
- **Regra:** o autor do comentário edita e remove o próprio comentário. `ADMIN` e `MODERATOR` removem qualquer comentário.
- **Requisito:** RF-069.

#### RN-052 – Uma reação por artigo ⬜
- **Regra:** cada usuário tem no máximo **uma** reação por artigo. Uma nova reação substitui a anterior.
- **Requisito:** RF-073.

#### RN-053 – Seguir autores ⬜
- **Regra:** não é possível seguir a si mesmo, e seguir alguém que já é seguido não duplica.
- **Requisito:** RF-075.

#### RN-054 – Feed ⬜
- **Regra:** o feed mostra só artigos publicados de autores seguidos, paginados, do mais recente para o mais antigo.
- **Requisito:** RF-076.

#### RN-060 – Denúncias ⬜
- **Regra:** um usuário denuncia o mesmo conteúdo **uma única vez**, escolhendo um motivo de uma lista fixa (spam, assédio, conteúdo impróprio, desinformação, outro) e com uma descrição opcional.
- **Erro (proposto):** `409` · `error.report.already_exists`.
- **Requisito:** RF-084.

#### RN-061 – Ações de moderação ⬜
- **Regra:** uma denúncia pode ser:
  - **arquivada**, quando não procede;
  - resolvida com a **ocultação do conteúdo**, que some das listagens públicas;
  - resolvida com a **penalização do autor**, que pode chegar ao bloqueio da conta (`locked = true`).

  Toda ação registra quem a tomou e quando.
- **Requisito:** RF-085.

---

## 5. Divergências conhecidas

Pontos em que o **código atual** não cumpre totalmente a regra pretendida. Cada um vira uma issue `type:bug` na v0.1 e é removido daqui quando corrigido e testado.

| ID | Regra | O que acontece hoje | Impacto | Correção proposta |
|---|---|---|---|---|
| **D1** | RN-005 | O sufixo do nickname tem só 9000 valores por nome e não há nova tentativa. Uma colisão viola a restrição `UNIQUE` e devolve **500**. | Médio: nomes comuns colidem cedo (cerca de 50% de chance com ~100 pessoas de mesmo nome). | Tentar de novo enquanto `existsByNickname` for verdadeiro, ou usar um sufixo maior. |
| **D2** | RN-001 / RN-007 | No cadastro com Google, um e-mail repetido devolve `already_exists` e um `googleId` repetido devolve `provider_conflict` ("faça o login tradicional"). As mensagens parecem trocadas. | Baixo: só a mensagem fica errada. | Confirmar a intenção e trocar as chaves. |
| **D3** | RN-010 | O login com Google não verifica `enabled` nem `locked`. | **Alto** quando ativação e bloqueio existirem (v0.1 e v0.4). | Verificar os dois campos e devolver `403`. |
| **D4** | RN-008 / RNF-004 | O cookie tem `maxAge` fixo de 24 h (independente de `JWT_EXPIRATION`) e `Secure=false` escrito no código. | Baixo localmente; alto em produção. | Derivar a validade da configuração e tornar `Secure` configurável por perfil. |
| **D5** | RN-006 / RN-016 | O gateway do Google não confere `email_verified` no token. | Médio: uma conta Google com e-mail não verificado ocuparia aquele e-mail. | Recusar tokens com `email_verified = false` (`401`). |
