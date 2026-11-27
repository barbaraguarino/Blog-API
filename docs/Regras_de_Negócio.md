# Regras de Negócio

**Versão:** 12.0 · **Última modificação:** 27/11/2026

Este documento reúne as regras que o sistema obedece: validações, restrições, permissões e comportamentos esperados, incluindo os valores padrão que o dono da plataforma pode ajustar.

---

## 1. Como ler este documento

Cada regra tem um identificador (RN-xxx), diz o que o sistema obedece e por quê, e, quando pode ser violada, qual erro o cliente recebe. As regras são numeradas na ordem em que aparecem neste documento. Uma regra nova entra na seção do seu módulo e, quando necessário, a numeração é refeita e registrada em [Decisões.md](Decisões.md), com a correspondência entre os números antigos e os novos. Os requisitos que as regras detalham estão em [Requisitos.md](Requisitos.md).

- **Status:** ✅ implementado · 🟡 parcial · ⬜ planejado.
- **Divergências:** quando o código atual contraria uma regra ou não a cumpre totalmente, ela recebe a marca ⚠️, e a diferença é descrita na seção *Divergências conhecidas*.
- **Erros:** status HTTP e chave de i18n, traduzida conforme o cabeçalho `Accept-Language` e devolvida no campo `message`. Nas regras planejadas, a chave é uma proposta.
- **Testes:** toda regra implementada tem pelo menos um teste que cita o ID no `@DisplayName`, e uma regra que muda é atualizada aqui no mesmo PR da mudança de código.

### Regras ajustáveis pelo dono

Os prazos, as quantidades e os limites marcados com ⚙️ são valores padrão. O dono pode ajustá-los pela API (RF-119), sempre dentro de um intervalo seguro definido no código, e cada mudança guarda quem a fez, quando, o valor anterior e o novo. Só o perfil de testes aceita valores fora desse intervalo, para que os testes não precisem esperar prazos reais; fora dele, a API não sobe com um valor inseguro. Cada regra ajustável informa o padrão e o intervalo, como em "⚙️ padrão: 2 meses · intervalo: 2 a 12 meses".

### Formato de erro

Toda violação de regra devolve um corpo neste formato, que é o formato único de erro da API (RNF-026):

```json
{
  "timestamp": "2026-11-21T14:32:10.123",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "A conta precisa ter mais de 2 meses para pedir autoria.",
  "path": "/api/v1/author-requests"
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
Cada e-mail pertence a uma única conta, seja qual for a forma de cadastro. O e-mail identifica a pessoa, serve para entrar e é o canal de recuperação da conta; duas contas com o mesmo e-mail tornariam esses fluxos ambíguos. O caso de uso confere se o e-mail já existe antes de criar a conta, e a coluna `email` tem uma restrição `UNIQUE` no banco, como segunda barreira.

- **Requisitos:** RF-001, RF-002, RF-005.

#### RN-002 – Resposta neutra no cadastro ⬜ ⚠️ D7
O cadastro com e-mail e senha responde sempre `202 Accepted`, com a mensagem "Se o e-mail puder ser usado, enviaremos um código de confirmação". Se o e-mail for novo, a conta é criada aguardando a confirmação (RN-010). Se ele já pertencer a uma conta, nada é criado, e o próprio endereço recebe um aviso: "Alguém tentou criar uma conta com este e-mail. Se foi você, entre ou recupere a senha." Assim, o cadastro não revela quais e-mails estão cadastrados. O e-mail é enviado fora da requisição, para que o tempo de resposta também não revele qual dos dois casos aconteceu.

- **Erro:** só os de validação (`400`, RN-003).
- **Requisitos:** RF-001, RNF-013.

#### RN-003 – Dados obrigatórios e limites do cadastro 🟡
O cadastro com e-mail e senha exige estes campos:

| Campo | Obrigatório | Formato | Limite |
|---|---|---|---|
| `name` | Sim | Texto livre | Até 150 caracteres |
| `email` | Sim | E-mail válido | Até 150 caracteres |
| `password` | Sim | Ver RN-005 | Mínimo de 8 caracteres |
| Confirmação da idade mínima | Sim | Verdadeiro | Ver RN-004 |
| Aceite dos termos de uso e da política de privacidade | Sim | Verdadeiro | Versão vigente |

Os limites acompanham as colunas do banco (`VARCHAR(150)`) e evitam erros de banco disfarçados de erro interno. O cadastro com provedor social exige a confirmação da idade e o aceite dos termos da mesma forma.

- **Erro:** `400` · `error.validation.generic`, com mensagens por campo, como `error.validation.user.name.not_blank`, `error.validation.user.name.size`, `error.validation.user.email.invalid`, `error.validation.user.email.size`, `error.validation.user.age.required` e `error.validation.user.terms.required`.
- **Requisitos:** RF-001, RF-002, RF-005.

#### RN-004 – Idade mínima ⬜
A pessoa declara, no cadastro, que tem a idade mínima da plataforma. A data de nascimento não é pedida, porque só a declaração é necessária (RNF-016). ⚙️ padrão: 18 anos · intervalo: 16 a 18 anos. Um mínimo abaixo de 18 anos só é liberado depois de conferir a Lei 15.211/2025 (ECA Digital), conforme a D-033.

- **Erro:** `400` · `error.validation.user.age.required`.
- **Requisito:** RF-006.

#### RN-005 – Política de senha ✅
A senha local deve ter no mínimo 8 caracteres, com pelo menos 2 letras maiúsculas, 2 letras minúsculas, 2 números e 1 caractere especial, e é guardada só como hash BCrypt. Senhas curtas ou sem variedade são fáceis de adivinhar por força bruta, e o BCrypt é lento de propósito e usa *salt*, o que dificulta os ataques mesmo que o banco vaze. Por exemplo, `SenhaForte12!` é aceita, e `senha123` é recusada por não ter maiúsculas nem caractere especial.

- **Erro:** `400` · uma mensagem por exigência que falhar: `error.validation.user.password.size`, `.uppercase`, `.lowercase`, `.number` e `.special`.
- **Requisitos:** RF-001, RF-015, RF-016, RNF-001.

#### RN-006 – Geração automática do nickname 🟡 ⚠️ D1
No cadastro, o nickname é gerado a partir do nome, para que a pessoa possa entrar por ele (RN-012) sem precisar escolher um:

1. o nome é convertido para minúsculas;
2. os espaços viram `_`;
3. os caracteres fora de `a-z`, `0-9` e `_` são removidos, inclusive os acentos;
4. o resultado é cortado em 25 caracteres, e um nome sem nenhum caractere válido usa a base `leitor`;
5. acrescenta-se `_` e um número aleatório de 4 dígitos (1000 a 9999), gerado com `SecureRandom`;
6. se o nickname já existir, outro número é sorteado, até 5 tentativas.

Por exemplo, "Ana Júlia Souza" vira `ana_jlia_souza_4821`. O resultado segue o formato da RN-032 e pode ser trocado depois.

- **Requisitos:** RF-001, RF-002, RF-005.

#### RN-007 – Cadastro com provedor social 🟡 ⚠️ D5
O cadastro com provedor social, como o Google, só é aceito com os provedores que o dono ativou. Ele recebe o token de identidade emitido pelo provedor, e a API valida a assinatura e o destinatário do token (o `client-id` configurado) e exige que o provedor tenha confirmado o e-mail (`email_verified`). Do token vêm o identificador da pessoa no provedor, o e-mail e o nome: a API não confia em dados enviados pelo cliente, só no que vem dentro de um token assinado. A conta nasce sem senha local e com o e-mail já confirmado.

- **Erros:**
  - `400` · `error.validation.auth.provider_token_required` (token vazio);
  - `401` · `error.auth.provider_token_invalid` (token inválido, expirado, de outro aplicativo ou forjado);
  - `422` · `error.auth.provider_email_not_verified`;
  - `422` · `error.auth.provider_disabled`.
- **Requisitos:** RF-002, RF-005.

#### RN-008 – Conta de provedor única 🟡 ⚠️ D2
Cada conta de um provedor social está ligada a no máximo um usuário. O identificador do provedor é conferido antes do e-mail. Como o token prova que a pessoa é dona do e-mail, a resposta pode dizer claramente o que aconteceu, sem revelar contas a quem não é dono delas. As contas nunca são vinculadas automaticamente: o vínculo é sempre uma ação explícita do usuário (RN-031).

- **Erros:**
  - `409` · `error.user.provider_already_registered` ("Esta conta Google já está cadastrada. Entre com o Google.");
  - `409` · `error.user.email_in_use` ("Já existe uma conta com este e-mail. Entre nela e vincule o Google em Minha conta.").
- **Requisitos:** RF-002, RF-005.

### 2.2 Situação da conta

#### RN-009 – Situações da conta ⬜
Uma conta está sempre em uma destas situações:

- e-mail ainda não confirmado;
- ativa;
- desativada pelo próprio usuário (RF-029);
- suspensa pela administração (RF-118);
- com exclusão agendada (RF-030).

Só a conta ativa usa a plataforma normalmente. A conta desativada volta a ficar ativa quando a pessoa entra de novo. Na conta com exclusão agendada, a pessoa pode entrar só para desistir da exclusão ou exportar os próprios dados. "Desativada" é sempre a escolha do próprio usuário, e "bloqueio" é só o bloqueio entre usuários (RF-091).

- **Requisitos:** RF-009, RF-029, RF-030, RF-118.

#### RN-010 – Conta local só é usada depois da confirmação do e-mail ⬜
A conta criada com e-mail e senha nasce com o e-mail ainda não confirmado e só fica ativa com o código de confirmação (RN-022). Isso garante que o e-mail pertence a quem se cadastrou, condição para a recuperação de senha ser segura. A conta criada com provedor social já nasce ativa, porque o provedor confirmou o e-mail (RN-007). Uma conta que não for confirmada no prazo é apagada, o que libera o e-mail se alguém o tiver usado sem ser o dono. ⚙️ padrão: 7 dias · intervalo: 1 a 30 dias.

- **Requisito:** RF-014.

#### RN-011 – Papel inicial ✅
Toda conta nasce com o papel de leitor. Ninguém nasce autor, moderador ou administrador: os outros papéis são concedidos pelas regras da seção 4. As fábricas `User.createLocalUser` e `User.createGoogleUser` definem o papel, e não é possível criar um usuário por outro caminho.

- **Requisitos:** RF-001, RF-002, RF-005.

### 2.3 Login

#### RN-012 – Login por e-mail ou nickname 🟡 ⚠️ D6
O campo `login` aceita o e-mail ou o nickname: se contém `@`, a busca é por e-mail; senão, por nickname, que nunca contém `@` (RN-032). A senha é comparada com o hash BCrypt. A situação da conta só é conferida depois de a senha estar certa, para que uma senha errada nunca revele que a conta existe.

- **Erros:**
  - `401` · `error.auth.bad_credentials`, a mesma resposta para conta inexistente, senha errada ou conta sem senha local;
  - `403` · `error.auth.email_not_confirmed`, com a opção de pedir um novo código (RN-023);
  - `403` · `error.auth.account_suspended`;
  - `400` · `error.validation.user.login.not_blank` e `error.validation.user.password.not_blank`.
- **Requisitos:** RF-003, RF-009, RNF-013.

#### RN-013 – Login com provedor social 🟡 ⚠️ D3
O token é validado como na RN-007, e a conta é buscada pelo identificador do provedor. A situação da conta é conferida como na RN-012. A resposta para uma conta não encontrada pode ser clara, porque o token prova que a pessoa é dona do e-mail.

- **Erros:**
  - `404` · `error.auth.provider_account_not_found` ("Conta não encontrada. Faça o cadastro antes de entrar.");
  - `401` · `error.auth.provider_token_invalid`;
  - `403` · `error.auth.email_not_confirmed` e `error.auth.account_suspended`.
- **Requisitos:** RF-004, RF-005, RF-009.

#### RN-014 – Limite de tentativas de login ⬜
Depois de 5 falhas seguidas com o mesmo identificador (e-mail ou nickname) em 15 minutos, as novas tentativas com ele ficam bloqueadas por 15 minutos. Cada IP também tem um limite de 20 tentativas em 15 minutos. O contador vale também para identificadores que não existem, para que o bloqueio não revele quais contas existem. ⚙️ padrão: 5 falhas e 15 minutos · intervalo: 3 a 10 falhas e 5 a 60 minutos.

- **Erro:** `429` · `error.auth.too_many_attempts`.
- **Requisitos:** RNF-007, RNF-013.

#### RN-015 – Login com código por e-mail ⬜
O pedido do código responde sempre da mesma forma, exista ou não a conta, como na RN-002. O código segue a RN-022. Se a conta tiver a verificação em duas etapas ativa, o código do aplicativo autenticador continua sendo exigido.

- **Requisitos:** RF-008, RNF-013.

#### RN-016 – Passkeys ⬜
Para cadastrar uma passkey, a pessoa precisa estar logada e passar pela reautenticação (RN-030). Cada conta tem no máximo 10 passkeys, e cada uma pode ser removida a qualquer momento. Entrar com uma passkey dispensa o código da verificação em duas etapas, porque a passkey já combina a posse do aparelho com o desbloqueio dele.

- **Requisito:** RF-007.

### 2.4 Sessões

#### RN-017 – Token de acesso em cookie ✅ ⚠️ D4
Um login bem-sucedido emite um JWT assinado com HMAC256, com emissor igual ao nome da aplicação, assunto igual ao e-mail do usuário e validade definida por `JWT_EXPIRATION`. O token vai no cabeçalho `Set-Cookie`, em um cookie `HttpOnly`, `SameSite=Strict` e `path=/`, e o corpo da resposta traz o perfil do usuário. Um cookie `HttpOnly` não pode ser lido por JavaScript, o que protege o token contra roubo via XSS, e `SameSite=Strict` impede que outros sites enviem o cookie em requisições forjadas (CSRF).

- **Requisitos:** RF-003, RF-004, RNF-002, RNF-004.

#### RN-018 – Sessões com renovação ⬜
O token de acesso passa a durar 15 minutos (o `JWT_EXPIRATION` da RN-017), e o token de renovação dura 7 dias. Cada login cria uma sessão com o usuário, o hash do token de renovação, o IP, o dispositivo (User-Agent), a data de criação, o último uso, a expiração e a data em que foi encerrada. O logout encerra a sessão atual e apaga os cookies. Um token de acesso curto limita o estrago de um token roubado, e o token de renovação guardado no banco permite encerrar sessões, o que um JWT sozinho não permite. ⚙️ validade do token de renovação: padrão de 7 dias · intervalo: 1 a 30 dias.

- **Requisitos:** RF-010, RF-011, RNF-006.

#### RN-019 – Rotação do token de renovação ⬜
Cada uso do token de renovação gera um novo par de tokens (acesso e renovação) e invalida o token usado. Assim, um token de renovação roubado perde o valor assim que o dono legítimo o usa.

- **Requisito:** RF-010.

#### RN-020 – Detecção de reuso ⬜
Se um token de renovação já usado for apresentado de novo, todas as sessões do usuário são encerradas. O reuso indica que o token foi copiado por outra pessoa, e encerrar tudo obriga quem é o verdadeiro dono a entrar de novo.

- **Erro (proposto):** `401` · `error.auth.refresh_token_reused`.
- **Requisito:** RF-010.

#### RN-021 – Gestão das sessões e histórico de acessos ⬜
O usuário lista e encerra só as próprias sessões, e "encerrar todas" mantém a sessão atual. Uma sessão de outro usuário é tratada como inexistente (`404`), para não revelar que ela existe. O histórico de acessos mostra os últimos 6 meses, com a data, o dispositivo e a cidade ou o país aproximados a partir do IP; depois desse prazo, os registros são apagados. Os 6 meses são o prazo de guarda dos registros de acesso a aplicações definido pelo Marco Civil da Internet.

- **Requisitos:** RF-012, RF-013, RNF-020.

### 2.5 Códigos por e-mail e senha

#### RN-022 – Códigos enviados por e-mail ⬜
Os códigos têm 6 dígitos, valem 10 minutos, são de uso único, aceitam no máximo 5 tentativas e são guardados só como hash. Cada código tem uma finalidade e não serve para outra: confirmação do e-mail, login, recuperação de senha, troca de senha, troca de e-mail ou reautenticação. A validade curta e o limite de tentativas tornam inviável adivinhar o código, que tem 1 milhão de combinações, e o hash protege os códigos se o banco vazar.

- **Erros (propostos):** `422` · `error.code.invalid`, `error.code.expired` e `error.code.attempts_exceeded`.
- **Requisitos:** RF-008, RF-014, RF-015, RF-016, RF-021, RF-024, RNF-006.

#### RN-023 – Limite de envio de códigos ⬜
Um novo código só pode ser pedido 60 segundos depois do anterior, e um novo código invalida o anterior da mesma finalidade. O limite evita o abuso do envio de e-mails, que gera spam e custo. O limite é contado por endereço de e-mail e finalidade, para valer também nos pedidos feitos com um e-mail sem conta. ⚙️ pedidos por hora: padrão de 5 · intervalo: 3 a 10.

- **Erro (proposto):** `429` · `error.code.too_many_requests`.
- **Requisitos:** RF-014, RNF-007, RNF-023.

#### RN-024 – Resposta neutra na recuperação de senha ⬜
O pedido de recuperação responde sempre da mesma forma ("Se o e-mail estiver cadastrado, você receberá um código"), exista ou não a conta. Isso impede que alguém use a recuperação para descobrir quais e-mails estão cadastrados. Uma conta sem senha, criada só com um provedor social, pode definir uma senha por esse caminho, porque o código prova que a pessoa é dona do e-mail. Pelo mesmo motivo, recuperar a senha de uma conta com o e-mail ainda não confirmado também confirma o e-mail; assim, quem teve o e-mail usado por outra pessoa no cadastro (RN-002) retoma a conta sem esperar o prazo da RN-010.

- **Requisitos:** RF-015, RNF-013.

#### RN-025 – Redefinir a senha encerra as sessões ⬜
Ao redefinir a senha, todas as sessões do usuário são encerradas, e todos os dispositivos confiáveis deixam de ser confiáveis. Quem esqueceu a senha pode estar recuperando uma conta invadida, e as sessões antigas podem estar com o invasor. A redefinição não desliga a verificação em duas etapas.

- **Requisito:** RF-015.

#### RN-026 – Troca de senha estando logado ⬜
A troca exige a senha atual e um código por e-mail. A nova senha segue a RN-005 e deve ser diferente da atual. As outras sessões são encerradas, e a atual continua.

- **Requisito:** RF-016.

### 2.6 Verificação em duas etapas

#### RN-027 – Verificação em duas etapas com aplicativo autenticador ⬜
O usuário recebe um segredo TOTP, em QR Code, e só ativa a verificação depois de confirmar um código válido. Na ativação, são gerados 10 códigos de recuperação de uso único, mostrados uma única vez; gerar novos códigos invalida os anteriores. Com a verificação ativa, o login não emite tokens até o código do aplicativo, ou um código de recuperação, ser confirmado. Desativar exige a reautenticação (RN-030).

- **Requisitos:** RF-017, RF-018, RNF-006.

#### RN-028 – Verificação obrigatória para moderação e administração ⬜
Moderadores, administradores e o dono só usam as funções desses papéis com a verificação em duas etapas ativa. Enquanto ela não estiver ativa, essas funções ficam bloqueadas, e o resto da conta funciona normalmente. O dono configura a dele no primeiro acesso.

- **Erro:** `403` · `error.auth.two_factor_required`.
- **Requisito:** RF-020.

#### RN-029 – Dispositivos confiáveis ⬜
O usuário pode marcar o dispositivo em que entrou como confiável, e o código da verificação em duas etapas deixa de ser pedido nele durante o prazo. A marcação pode ser desfeita a qualquer momento, e a redefinição de senha desfaz todas (RN-025). Para quem é moderador ou da administração, o dispositivo confiável não dispensa o código. ⚙️ padrão: 30 dias · intervalo: 1 a 90 dias.

- **Requisito:** RF-019.

#### RN-030 – Reautenticação antes de ações sensíveis ⬜
As ações sensíveis do RF-021 (trocar o e-mail, desativar a verificação em duas etapas, excluir a conta, transferir a posse da plataforma e baixar ou restaurar backups) e as outras indicadas nestas regras, como cadastrar uma passkey e exportar os próprios dados, exigem que a pessoa se confirme de novo, mesmo logada: com a senha, com o código do aplicativo autenticador ou, se a conta não tiver senha, com um código por e-mail. A confirmação vale por 5 minutos.

- **Erro:** `403` · `error.auth.reauthentication_required`.
- **Requisito:** RF-021.

### 2.7 Provedores e perfil

#### RN-031 – Vínculo com provedores sociais ⬜
Vincular exige um token válido de um provedor ativo, cujo identificador não pertença a outra conta. Desvincular só é permitido se restar outra forma de entrar (senha, passkey ou outro provedor), para que o usuário não perca o acesso.

- **Erro (proposto):** `409` · `error.user.provider_already_registered`; `422` · `error.user.last_login_method`.
- **Requisito:** RF-022.

#### RN-032 – Perfil e nickname ⬜
O nickname tem de 3 a 30 caracteres, só letras minúsculas, números e `_`, é único sem diferenciar maiúsculas de minúsculas e nunca contém `@`. Ele pode ser trocado só depois de um intervalo desde a última troca, e o nickname antigo fica reservado por 30 dias, para que ninguém se passe pela pessoa. A descrição tem até 300 caracteres. ⚙️ intervalo entre trocas: padrão de 30 dias · intervalo: 7 a 90 dias.

- **Erros (propostos):** `400` · `error.validation.user.nickname.format`; `409` · `error.user.nickname_in_use`; `422` · `error.user.nickname_change_too_soon`.
- **Requisito:** RF-023.

#### RN-033 – Troca de e-mail ⬜
A troca exige a reautenticação (RN-030). O código vai para o endereço novo, e o antigo recebe um aviso da troca. Se o endereço novo já pertencer a uma conta, a resposta é a mesma, e esse endereço recebe o aviso da RN-002, para que a troca também não revele quais e-mails estão cadastrados.

- **Requisitos:** RF-024, RNF-013.

---

## 3. Privacidade

#### RN-034 – Perfil público escolhido pelo usuário ⬜
O nickname é sempre público, porque identifica a pessoa nos comentários. O nome, a foto e a descrição só aparecem no perfil público se a pessoa escolher mostrá-los, e o padrão é ficarem ocultos: a privacidade vem ativada desde o início, como pede o art. 46 da LGPD. O e-mail nunca é público. Nos autores, os artigos publicados e o número de seguidores são sempre públicos, mas a lista de seguidores e a de autores seguidos nunca são públicas.

- **Requisitos:** RF-025, RF-092.

#### RN-035 – Consulta dos próprios dados ⬜
O usuário vê tudo o que a plataforma guarda sobre ele, agrupado por assunto: conta, perfil, sessões, histórico de acessos, provedores vinculados, aceites dos termos, interesses, listas de leitura, bloqueios, pedidos, denúncias e preferências de avisos. Hashes, segredos e tokens nunca aparecem, nem para o próprio dono dos dados (RNF-008).

- **Requisito:** RF-026.

#### RN-036 – Exportação dos próprios dados ⬜
A exportação exige a reautenticação (RN-030). O arquivo é um JSON compactado em ZIP, com os dados da RN-035 e também os artigos e comentários da pessoa. Ele é gerado fora da requisição, a pessoa é avisada quando fica pronto, e o link para baixá-lo vale 24 horas. É possível pedir uma nova exportação a cada 24 horas.

- **Erro (proposto):** `429` · `error.export.too_soon`.
- **Requisito:** RF-027.

#### RN-037 – Exclusão da conta ⬜
A exclusão exige a reautenticação (RN-030), e a pessoa escolhe se os artigos e comentários públicos dela são apagados ou mantidos como de um "usuário excluído". A exclusão só é aplicada depois de um prazo de carência, durante o qual a conta fica escondida, como na desativação, e a pessoa pode desistir ao entrar (RN-009). ⚙️ carência: padrão de 30 dias · intervalo: 7 a 30 dias.

Ao fim da carência, os dados pessoais são apagados, o e-mail fica livre para um novo cadastro e o nickname fica reservado por 30 dias (RN-032). Nas coautorias, a pessoa sai do artigo, que continua com os outros coautores; se ela for a autora principal, o coautor mais antigo passa a ser o autor principal. A escolha entre apagar e manter de forma anônima vale para os artigos sem coautores. Os registros de acesso são mantidos até completar os 6 meses do Marco Civil da Internet (RN-021), porque a guarda é uma obrigação legal. O dono só pode excluir a própria conta depois de transferir a posse da plataforma, para que ela não fique sem responsável.

- **Erro (proposto):** `422` · `error.account.owner_cannot_delete`.
- **Requisitos:** RF-028, RF-030, RF-044.

#### RN-038 – Desativação temporária ⬜
Com a conta desativada, o perfil, os artigos e os comentários da pessoa ficam escondidos. No lugar de cada comentário aparece "comentário indisponível", para que as conversas não percam o sentido. Os avisos param, e os seguidores são mantidos. A conta volta a ficar ativa quando a pessoa entra de novo (RN-009). O dono não pode desativar a própria conta, para que a plataforma não fique sem responsável.

- **Erro (proposto):** `422` · `error.account.owner_cannot_deactivate`.
- **Requisito:** RF-029.

#### RN-039 – Aceite dos termos e da política de privacidade ⬜
Cada aceite guarda a versão dos termos de uso e da política de privacidade e a data. O IP não é guardado, porque não é necessário para comprovar o aceite. Toda nova versão publicada pelo dono exige um novo aceite no próximo acesso; até aceitar, a pessoa só pode ler a plataforma, exportar os próprios dados ou excluir a conta.

- **Erro (proposto):** `403` · `error.terms.acceptance_required`.
- **Requisitos:** RF-031, RF-113.

#### RN-040 – Alerta de acesso em dispositivo novo ⬜
Um dispositivo é reconhecido por um identificador guardado em um cookie, junto com o navegador e o sistema informados pelo User-Agent. No primeiro acesso de um dispositivo, a pessoa recebe um e-mail com a data, o dispositivo, o local aproximado e um link para encerrar as sessões. Esse é um e-mail de segurança: ele não pode ser desligado nas preferências e não é pausado quando o teto de custos é atingido.

- **Requisitos:** RF-032, RF-124.

#### RN-041 – Histórico de interesses ⬜
O histórico de interesses guarda as tags dos artigos que a pessoa leu ou aos quais reagiu e os autores que ela segue, e vem ligado por padrão. Os registros são apagados depois de um prazo. Limpar o histórico apaga tudo na hora; desligá-lo faz a plataforma parar de registrar, e a recomendação passa a usar só as tags de interesse escolhidas e os autores seguidos. ⚙️ prazo dos registros: padrão de 90 dias · intervalo: 30 a 180 dias.

- **Requisitos:** RF-033, RF-080.

#### RN-042 – Serviços externos que recebem dados ⬜
A plataforma mantém uma lista pública, parte da política de privacidade, com cada serviço externo que recebe dados e o que ele recebe:

- a IA recebe o conteúdo dos artigos, comentários e denúncias, nunca os dados das contas;
- o armazenamento de imagens recebe as imagens, já sem metadados;
- o envio de e-mails recebe o endereço e o texto de cada aviso;
- cada provedor social troca dados só com quem vinculou a conta a ele.

A lista é atualizada junto com qualquer troca de provedor.

- **Requisitos:** RF-034, RNF-017.

#### RN-043 – Retenção e mascaramento dos dados ⬜
Nos logs técnicos, e-mails e IPs aparecem mascarados (por exemplo, `a***@exemplo.com` e `189.40.*.*`). Os logs são apagados depois de um prazo, e cada consulta feita a eles fica registrada. Os registros de acesso são guardados por 6 meses (RN-021). Os backups também expiram depois de um prazo, o que faz os dados das contas excluídas sumirem deles, e uma restauração aplica de novo as exclusões feitas depois do backup, para que nenhuma conta excluída volte.

- ⚙️ prazo dos logs técnicos: padrão de 30 dias · intervalo: 7 a 90 dias.
- ⚙️ prazo dos backups: padrão de 30 dias · intervalo: 7 a 90 dias.
- **Requisitos:** RF-116, RNF-016, RNF-019, RNF-020, RNF-021.

---

## 4. Perfis e autoria

#### RN-044 – Papéis e hierarquia ⬜
Os papéis seguem a ordem leitor, autor, moderador, administrador e superadministrador, e cada papel só é concedido ou retirado por alguém acima dele, conforme as regras desta seção. O moderador pode ter também a permissão de autor; sem ela, não escreve artigos. O administrador e o superadministrador escrevem artigos.

- **Erro:** `403` · `error.auth.access_denied`.
- **Requisitos:** RF-040, RF-117.

#### RN-045 – Modo de autoria ⬜
O dono escolhe um dos três modos de autoria: por pedido, que é o padrão, fechado ou aberto. Ao mudar o modo, os autores atuais continuam autores. Ao sair do modo por pedido, os pedidos pendentes são cancelados, e cada solicitante é avisado. No modo aberto, todo leitor ativo pode escrever enquanto o modo durar; ao sair dele, quem publicou pelo menos um artigo vira autor, e os demais voltam a ser só leitores.

- **Requisito:** RF-035.

#### RN-046 – Prazo mínimo para pedir autoria ⬜
No modo por pedido, só pode pedir autoria quem tem a conta há mais de 2 meses. O prazo reduz as contas criadas só para publicar spam e incentiva a participação como leitor antes. Só o perfil de testes aceita um prazo menor que o mínimo. ⚙️ padrão: 2 meses · intervalo: 2 a 12 meses.

- **Erro (proposto):** `422` · `error.author_request.account_too_new`.
- **Requisito:** RF-036.

#### RN-047 – Um pedido pendente por vez ⬜
Cada pessoa tem no máximo um pedido de autoria pendente, com uma motivação de até 1.000 caracteres. Depois de uma recusa, um novo pedido só pode ser feito após um intervalo, para que os pedidos repetidos não sobrecarreguem a administração. ⚙️ intervalo depois de uma recusa: padrão de 30 dias · intervalo: 0 a 90 dias.

- **Erros (propostos):** `409` · `error.author_request.already_pending`; `422` · `error.author_request.too_soon`.
- **Requisito:** RF-036.

#### RN-048 – Aprovação do pedido de autoria ⬜
O pedido precisa da aprovação de 2 administradores. O superadministrador conta como administrador e segue as mesmas regras. Se houver menos administradores do que o número exigido, bastam as aprovações de todos eles. Uma única recusa, com motivo, encerra o pedido, e o motivo fica visível para o solicitante. A aprovação de mais de uma pessoa evita que alguém vire autor por favoritismo. ⚙️ aprovações: padrão de 2 · intervalo: 1 a 3.

- **Erro:** `403` · `error.auth.access_denied` para quem não é da administração.
- **Requisito:** RF-037.

#### RN-049 – Nomeação direta no modo fechado ⬜
No modo fechado, não existem pedidos: um administrador nomeia o autor, e a pessoa é avisada. Fora do modo fechado, a nomeação direta só vale pelas exceções de início (RN-055).

- **Erro (proposto):** `422` · `error.author.direct_appointment_not_allowed`.
- **Requisito:** RF-038.

#### RN-050 – Retirada do papel de autor ⬜
Um administrador pode retirar o papel de autor, informando um motivo, que fica registrado. Os artigos já publicados continuam publicados e assinados pela pessoa, os rascunhos ficam guardados, mas não podem mais ser publicados, e os artigos agendados voltam a ser rascunhos. A pessoa pode pedir autoria de novo, pelas regras normais.

- **Requisito:** RF-039.

#### RN-051 – Papel de moderador ⬜
O papel de moderador é concedido e retirado por um administrador e não exige ser autor. Para usar as funções de moderação, o moderador precisa ter a verificação em duas etapas ativa (RN-028).

- **Requisito:** RF-040.

#### RN-052 – Promoção a administrador ⬜
Só pode ser promovido a administrador o moderador com mais de 12 meses na função e sem nenhuma denúncia procedente contra ele. A promoção começa com a indicação feita por um administrador. Com 3 ou mais administradores, contando o superadministrador, ela exige a aprovação de 2 outros administradores, além de quem indicou e do próprio candidato; com menos de 3, quem aprova é o superadministrador. As exigências evitam o favoritismo na escolha de quem administra a plataforma. ⚙️ tempo como moderador: padrão de 12 meses · intervalo: 6 a 24 meses.

- **Erro (proposto):** `422` · `error.promotion.requirements_not_met`.
- **Requisito:** RF-041.

#### RN-053 – Rebaixamento de administradores ⬜
Só o superadministrador rebaixa um administrador, que volta a ser moderador. O motivo é obrigatório e fica no registro das ações administrativas (RF-130).

- **Requisito:** RF-042.

#### RN-054 – Superadministrador único ⬜
O superadministrador é criado na primeira subida da API, a partir de variáveis de ambiente que trazem só o nome e o e-mail do dono. No primeiro acesso, o dono define a senha com um código enviado ao e-mail e configura a verificação em duas etapas; assim, nenhuma senha fica guardada em variável de ambiente. Se as variáveis faltarem na primeira subida, a API não sobe. Nunca há mais de um superadministrador, e nenhum outro perfil altera ou suspende a conta dele.

- **Erro (proposto):** `403` · `error.auth.owner_protected`.
- **Requisito:** RF-043.

#### RN-055 – Exceções de início ⬜
Para a plataforma começar a funcionar, o superadministrador pode promover alguém diretamente para um perfil que ainda não tem ninguém, como o primeiro moderador e o primeiro administrador, e pode nomear até 5 autores no total sem prazo nem aprovação. Fora dessas exceções, ele segue as regras normais, para que não haja favoritismo. ⚙️ autores iniciais: padrão de 5 · intervalo: 0 a 10.

- **Requisito:** RF-045.

#### RN-056 – Transferência da posse da plataforma ⬜
A transferência exige a reautenticação (RN-030). Quem recebe precisa ser um administrador ativo, com a verificação em duas etapas ativa, e deve aceitar em até 7 dias. Depois do aceite, o dono anterior vira administrador. A transferência fica no registro das ações administrativas.

- **Erro (proposto):** `422` · `error.ownership.invalid_recipient`.
- **Requisito:** RF-044.

---

## 5. Conteúdo

### 5.1 Artigos

#### RN-057 – Quem escreve e edita ⬜
Escrevem artigos os autores, os moderadores com permissão de autor, a administração e, no modo aberto, todo leitor ativo (RN-045). Só o autor e os coautores editam um artigo. A administração não edita artigos de outras pessoas: ela só pode ocultá-los pela moderação.

- **Erro:** `403` · `error.auth.access_denied`.
- **Requisitos:** RF-053, RF-061.

#### RN-058 – Ciclo de vida do artigo ⬜
O artigo está sempre em uma destas situações: rascunho, agendado, publicado ou arquivado. As mudanças permitidas são:

- rascunho → publicado ou agendado;
- agendado → rascunho, quando o agendamento é cancelado;
- agendado → publicado, no horário marcado;
- publicado → arquivado;
- arquivado → rascunho.

A data de publicação é a da primeira publicação. O slug é gerado a partir do título, é único (um slug repetido ganha `-2`, `-3` e assim por diante) e não muda depois da publicação, para que os links para o artigo não quebrem. Para ser publicado, o artigo precisa ter título de 5 a 200 caracteres, conteúdo de até 200.000 caracteres, uma categoria, o idioma informado e as descrições de todas as imagens e fórmulas (RN-065).

- **Erros (propostos):** `422` · `error.article.invalid_transition`; `400` · `error.validation.article.<campo>.<regra>`.
- **Requisitos:** RF-053, RF-060.

#### RN-059 – Rascunhos são privados ⬜
Um rascunho só pode ser visto pelo autor, pelos coautores e por quem tiver um link de prévia válido (RN-073). Para todas as outras pessoas, o rascunho não existe (`404`), para não revelar o que ainda não foi publicado.

- **Requisitos:** RF-053, RF-065.

#### RN-060 – Agendamento da publicação ⬜
A data agendada deve ficar entre 5 minutos e 1 ano no futuro, e o artigo é publicado em até 1 minuto depois do horário marcado. As exigências da publicação (RN-058) são conferidas de novo nessa hora; se alguma falhar, o artigo volta a ser rascunho, e o autor é avisado.

- **Erro (proposto):** `422` · `error.article.invalid_schedule`.
- **Requisito:** RF-057.

#### RN-061 – Exclusão de artigos ⬜
Só o autor principal, quem criou o artigo, pode excluí-lo. A exclusão é definitiva e apaga também as versões de idioma, as revisões, os comentários, as reações e as imagens do artigo.

- **Erro (proposto):** `403` · `error.article.not_owner`.
- **Requisito:** RF-054.

### 5.2 Formato e segurança do conteúdo

#### RN-062 – Formatos e alinhamento ⬜
O conteúdo pode estar em Markdown, LaTeX, HTML com CSS ou texto puro. O formato é escolhido na criação do artigo e só pode mudar enquanto ele for rascunho. O alinhamento do texto (à esquerda, ao centro, à direita ou justificado) vale para Markdown, LaTeX e HTML; o texto puro não tem formatação.

- **Requisito:** RF-055.

#### RN-063 – Limpeza do HTML e do CSS ⬜
Antes de ser salvo, o HTML, inclusive o que aparece dentro do Markdown, e o CSS passam por uma lista de elementos, atributos e propriedades permitidos, que inclui a formatação dos artigos, como imagens, legendas e alinhamentos. Scripts, atributos de evento (`on…`), `iframe`, formulários e `url()` em CSS são removidos, e os links externos ganham `rel="noopener noreferrer nofollow"`. A resposta informa ao autor o que foi removido. A limpeza protege os leitores contra XSS e contra CSS malicioso.

- **Requisitos:** RF-055, RNF-009.

#### RN-064 – Recursos externos bloqueados ⬜
Imagens e outros recursos carregados pelo artigo só podem vir do armazenamento da plataforma; os links para outros sites continuam permitidos. Um recurso externo impede o salvamento, para que o autor saiba o que corrigir. O bloqueio evita que sites de fora rastreiem quem lê os artigos.

- **Erro (proposto):** `422` · `error.article.external_resource`.
- **Requisito:** RNF-011.

#### RN-065 – Descrições de imagens e fórmulas ⬜
Cada imagem do corpo do artigo precisa de uma descrição de até 250 caracteres, e cada fórmula de um artigo em LaTeX precisa de uma descrição em texto, para que leitores de tela consigam lê-las. As descrições são conferidas ao publicar e ao agendar.

- **Erros (propostos):** `422` · `error.article.missing_image_description` e `error.article.missing_formula_description`.
- **Requisitos:** RF-056, RF-068.

#### RN-066 – Tempo estimado de leitura ⬜
O tempo de leitura é calculado para cada versão de idioma, com 200 palavras por minuto, arredondando para cima, com o mínimo de 1 minuto.

- **Requisito:** RF-051.

### 5.3 Revisões, idiomas e coautoria

#### RN-067 – Histórico de revisões ⬜
Cada salvamento cria uma revisão, com quem salvou, a data e o conteúdo, e cada artigo guarda as últimas 50 revisões. Restaurar uma revisão cria uma revisão nova e não apaga as anteriores. A comparação mostra as diferenças de texto entre duas revisões.

- **Requisito:** RF-058.

#### RN-068 – Versões de idioma ⬜
Cada versão tem um idioma, no padrão BCP 47 (como `pt-BR`), e só pode haver uma versão por idioma. A primeira versão é a original e não pode ser excluída enquanto houver outras. As versões compartilham o endereço, os comentários e as reações, mas cada uma tem o próprio título, conteúdo e resumo, e é publicada de forma independente. Uma versão escrita pelo autor sempre é usada no lugar da tradução automática.

- **Erro (proposto):** `409` · `error.article.language_exists`.
- **Requisitos:** RF-059, RF-060.

#### RN-069 – Versão mostrada ao leitor ⬜
A versão do artigo mostrada a cada leitor é escolhida nesta ordem:

1. a versão que o leitor escolheu explicitamente;
2. a versão do autor no idioma do cabeçalho `Accept-Language`;
3. a tradução automática, se estiver ligada e oferecer esse idioma;
4. a versão original.

A resposta informa o idioma mostrado e se ele é uma tradução automática.

- **Requisitos:** RF-048, RF-049, RF-060, RF-072.

#### RN-070 – Coautoria ⬜
Só quem pode escrever artigos (RN-057) pode ser convidado como coautor, e cada artigo tem até 5 coautores. O convite vale 14 dias. O coautor edita o artigo e aparece como autor dele, mas não o exclui nem remove outros coautores, o que cabe só ao autor principal. O coautor pode sair da coautoria quando quiser. Quem bloqueou o autor principal, ou foi bloqueado por ele, não pode ser convidado.

- **Erros (propostos):** `422` · `error.coauthor.limit_reached` e `error.coauthor.not_allowed`.
- **Requisitos:** RF-061, RF-062.

#### RN-071 – Edição simultânea ⬜
Cada salvamento informa o número da revisão em que foi baseado. Se o artigo já tiver uma revisão mais nova, o salvamento é recusado, e a resposta traz a versão atual, para que ninguém apague sem perceber as alterações de outro coautor. O artigo também mostra quem está editando agora: quem salvou ou abriu o artigo para edição nos últimos 5 minutos.

- **Erro (proposto):** `409` · `error.article.edit_conflict`.
- **Requisito:** RF-063.

#### RN-072 – Séries ⬜
O autor principal agrupa os próprios artigos em séries ordenadas. Cada artigo fica em no máximo uma série, cada série tem até 50 artigos, e o nome da série é único entre as séries do autor. O público só vê os artigos publicados de cada série.

- **Erro (proposto):** `409` · `error.series.name_exists`.
- **Requisito:** RF-064.

#### RN-073 – Link de prévia ⬜
O link de prévia usa um token aleatório, guardado só como hash, vale por um prazo e pode ser revogado a qualquer momento. Ele dá acesso só à leitura, sem comentários, e vale apenas para rascunhos e artigos agendados. Cada artigo tem até 5 links ativos. ⚙️ validade: padrão de 7 dias · intervalo: 1 a 30 dias.

- **Requisito:** RF-065.

### 5.4 Organização, imagens e leitura

#### RN-074 – Tags ⬜
As tags são normalizadas (minúsculas e sem espaços nas pontas), têm de 2 a 30 caracteres e são únicas sem diferenciar acentos. Uma tag que não existe é criada ao ser usada, e cada artigo tem até 10 tags. A administração edita, junta e remove tags: ao juntar, os artigos passam para a tag de destino; ao remover, a tag sai dos artigos.

- **Requisitos:** RF-066, RF-071.

#### RN-075 – Categorias ⬜
As categorias formam uma árvore de até 3 níveis, e cada uma tem um nome e um slug único. Uma categoria não pode ser pai de si mesma nem de um ancestral. Cada artigo tem uma categoria. Uma categoria só pode ser removida se não tiver artigos nem subcategorias, para que nenhum artigo fique sem categoria e a árvore não se quebre.

- **Erro (proposto):** `422` · `error.category.in_use`.
- **Requisito:** RF-070.

#### RN-076 – Imagens ⬜
São aceitas imagens em JPEG, PNG e WebP, com até 5 MB. Cada imagem é regravada antes de ser guardada, o que remove os metadados, como a localização. A legenda tem até 300 caracteres, e cada artigo tem até 30 imagens. Ao trocar uma imagem, a anterior é apagada, e uma imagem enviada e não usada em 24 horas também é apagada.

- **Erros (propostos):** `400` · `error.upload.invalid_type` e `error.upload.too_large`; `422` · `error.upload.limit_reached`.
- **Requisitos:** RF-067, RNF-010.

#### RN-077 – Listagens públicas e paginação ⬜
As listagens públicas mostram só os artigos publicados, que não estejam ocultados e cujo autor não esteja com a conta desativada nem com exclusão agendada, da publicação mais recente para a mais antiga. Toda listagem da API é paginada, com 10 itens por página por padrão e o máximo de 50.

- **Requisitos:** RF-046, RNF-029.

#### RN-078 – Busca ⬜
A busca procura no título, no resumo e nas tags dos artigos publicados, ignorando maiúsculas e acentos, e pode ser combinada com os filtros de categoria, tag e autor. Os termos buscados entram só nas estatísticas agregadas da plataforma, sem ligação com quem buscou.

- **Requisitos:** RF-050, RF-122, RNF-018.

#### RN-079 – Listas de leitura ⬜
As listas de leitura são sempre privadas. Cada pessoa tem até 20 listas, com até 500 artigos em cada. Um artigo que sai do ar aparece na lista como indisponível.

- **Erro (proposto):** `422` · `error.reading_list.limit_reached`.
- **Requisito:** RF-052.

#### RN-080 – Exportação dos próprios artigos ⬜
A exportação gera um ZIP com todas as versões de idioma dos artigos da pessoa, inclusive aqueles de que ela é coautora, no formato original e com as imagens. É possível pedir uma nova exportação a cada 24 horas.

- **Erro (proposto):** `429` · `error.export.too_soon`.
- **Requisito:** RF-069.

---

## 6. Inteligência artificial

#### RN-081 – Funcionalidades de IA ligáveis pelo dono ⬜
O dono liga e desliga separadamente cada funcionalidade de IA: tradução, correção, sugestões de texto, sugestão de tags e de categoria, resumo, descrições de imagens e fórmulas, triagem da moderação e recomendação. Todas vêm ligadas por padrão.

- **Requisito:** RF-081.

#### RN-082 – Limites de uso da IA ⬜
Cada autor tem um limite diário de pedidos de correção, sugestões de texto e descrições, e a plataforma tem uma cota mensal de gasto com IA. A cota padrão vem da conta do teto de custos: dos US$ 15 por mês, cerca de US$ 8 vão para o login com Apple, quando ele está ativo (D-025), e sobram uns US$ 7 para a IA, as imagens e os e-mails.

- ⚙️ pedidos por autor: padrão de 20 por dia · intervalo: 5 a 100.
- ⚙️ cota mensal da plataforma: padrão de US$ 4 · intervalo: US$ 0 a US$ 10.
- **Erro (proposto):** `429` · `error.ai.quota_exceeded`.
- **Requisitos:** RNF-022, RNF-023.

#### RN-083 – Tradução automática ⬜
A tradução automática é oferecida nos idiomas escolhidos pelo dono entre os suportados (D-033). Cada tradução é gerada na primeira vez que alguém a pede e é guardada para todos os leitores. Quando o original muda, ela é refeita só na primeira leitura seguinte, e não a cada edição. A tradução preserva o formato do artigo e aparece marcada como automática. O autor não edita uma tradução automática: para isso, ele escreve a própria versão naquele idioma (RN-068). ⚙️ idiomas: padrão de português, inglês e espanhol · o dono escolhe entre os idiomas suportados.

- **Requisitos:** RF-072, RNF-024, RNF-028.

#### RN-084 – Correção e sugestões de texto ⬜
O autor envia o artigo inteiro ou um trecho, e a IA devolve uma lista de sugestões, cada uma com a sua posição no texto. Nada é aplicado sozinho: o autor aceita ou recusa cada sugestão.

- **Requisitos:** RF-073, RF-074, RNF-028.

#### RN-085 – Sugestões de tags, categoria e resumo ⬜
A IA sugere até 5 tags, de preferência entre as que já existem, e uma categoria, escolhida só entre as existentes; o autor decide se aceita. O resumo tem até 300 caracteres e é gerado na publicação, para cada versão de idioma. Um resumo editado pelo autor nunca é sobrescrito, e um novo resumo só é gerado se o autor pedir. Sem a IA, o autor pode escrever o resumo; se não escrever, as listagens mostram o começo do texto.

- **Requisitos:** RF-075, RF-076, RNF-024.

#### RN-086 – Descrições sugeridas para imagens e fórmulas ⬜
A descrição de uma imagem ou fórmula só é gerada quando o autor pede, uma por vez, porque enviar imagens ao modelo custa mais, e cada pedido conta no limite diário (RN-082). A sugestão nunca é aceita sozinha: o autor a aceita ou edita, respeitando o limite de 250 caracteres da RN-065.

- **Requisito:** RF-077.

#### RN-087 – Proteção contra instruções no conteúdo ⬜
O conteúdo enviado ao modelo vai sempre separado das instruções e marcado como dado, para que um texto escrito para enganar a IA (*prompt injection*) não mude o que ela faz. Cada resposta é validada antes do uso: o formato esperado, o tamanho e, nas traduções, a mesma estrutura de formatação do original. O HTML devolvido passa de novo pela limpeza da RN-063. Uma resposta inválida é descartada, e a funcionalidade aparece como indisponível. Nenhuma ação do sistema é decidida pelo texto que a IA devolve.

- **Requisito:** RNF-012.

#### RN-088 – Dados enviados à IA ⬜
A IA recebe só o conteúdo necessário para cada tarefa, nunca e-mails, nomes ou outros dados das contas. Na triagem da moderação, até os `@nickname` mencionados no texto são trocados por um marcador, para que nenhum identificador de pessoa saia da plataforma.

- **Requisitos:** RF-078, RNF-017.

#### RN-089 – Triagem da moderação ⬜
Todo comentário novo e todo conteúdo denunciado recebem uma classificação (baixa, média ou alta chance de abuso) e um motivo provável, e a fila da moderação é ordenada por essa classificação. Um comentário classificado com alta chance entra na fila mesmo sem denúncia, marcado como "triagem automática". A IA nunca remove, oculta nem penaliza nada: quem decide é sempre uma pessoa.

- **Requisito:** RF-078.

#### RN-090 – Interesses e recomendação ⬜
Cada leitor escolhe até 20 tags de interesse. A recomendação usa essas tags, os autores seguidos, as reações e o histórico de interesses, quando ele está ligado (RN-041), e deixa de fora os artigos já lidos, os próprios artigos e os de usuários bloqueados. O cálculo é feito pela própria API, sem enviar dados ao modelo de IA (D-035).

- **Requisitos:** RF-079, RF-080.

#### RN-091 – Funcionamento sem a IA ⬜
Quando uma funcionalidade de IA está desligada, a cota acabou, o provedor está fora do ar ou os serviços pagos foram pausados pelo teto de custos, a plataforma mostra o conteúdo original, e a resposta avisa que a funcionalidade está indisponível e diz o motivo. Publicar nunca depende da IA: as descrições de imagens e fórmulas continuam obrigatórias e são escritas pelo autor.

- **Erro (proposto):** `503` · `error.ai.unavailable`, só nos pedidos feitos diretamente pelo autor.
- **Requisitos:** RF-124, RNF-042.

---

## 7. Social

#### RN-092 – Comentários ⬜
Só contas ativas comentam, e só em artigos publicados que estejam com os comentários abertos. As respostas têm um único nível: não é possível responder a uma resposta. O comentário é texto puro, com até 2.000 caracteres. O autor do comentário pode editá-lo quando quiser, e ele passa a aparecer marcado como "editado". Um comentário removido pelo autor some; se tiver respostas, fica no lugar dele a mensagem "comentário removido", para que a conversa não perca o sentido.

- **Erros (propostos):** `422` · `error.comment.closed` e `error.comment.reply_depth`; `400` · `error.validation.comment.content.size`.
- **Requisito:** RF-082.

#### RN-093 – Quem remove comentários ⬜
Um comentário pode ser removido pelo próprio autor, pelos moderadores e pela administração. O autor do artigo não remove comentários de outras pessoas: ele pode fechar os comentários (RN-095) ou denunciar.

- **Erro:** `403` · `error.auth.access_denied`.
- **Requisitos:** RF-082, RF-099.

#### RN-094 – Limite de frequência de comentários ⬜
Cada conta faz no máximo 1 comentário a cada 30 segundos e um número limitado de comentários por dia, para proteger a plataforma contra spam. ⚙️ comentários por dia: padrão de 50 · intervalo: 10 a 200.

- **Erro (proposto):** `429` · `error.comment.too_many`.
- **Requisitos:** RNF-007, RNF-015.

#### RN-095 – Fechar e reabrir os comentários ⬜
O autor principal e os coautores fecham e reabrem os comentários de um artigo. Com os comentários fechados, os que já existem continuam visíveis e continuam recebendo reações, mas não entram comentários novos nem respostas.

- **Requisito:** RF-083.

#### RN-096 – Comentário fixado ⬜
Cada artigo tem no máximo um comentário fixado no topo, e fixar outro substitui o anterior. Só um comentário de primeiro nível pode ser fixado, e ele pode ser desfixado a qualquer momento.

- **Requisito:** RF-084.

#### RN-097 – Menções ⬜
Uma menção é um `@nickname` existente no texto do comentário, e cada comentário avisa no máximo 5 pessoas mencionadas. Não há aviso quando existe um bloqueio entre as duas pessoas, quando a conta mencionada não está ativa ou quando a pessoa menciona a si mesma. Se a pessoa mencionada trocar o nickname, o texto do comentário não muda.

- **Requisitos:** RF-085, RF-093.

#### RN-098 – Reações aos artigos ⬜
Há cinco tipos fixos de reação: gostei, amei, útil, interessante e engraçado. Cada pessoa tem no máximo uma reação por artigo; uma nova reação substitui a anterior, e a reação pode ser retirada. Ninguém reage aos próprios artigos, para não inflar as estatísticas.

- **Erro (proposto):** `422` · `error.reaction.own_content`.
- **Requisito:** RF-086.

#### RN-099 – Reações aos comentários ⬜
As reações aos comentários usam os mesmos tipos da RN-098. Cada pessoa tem no máximo uma reação por comentário e não reage aos próprios comentários.

- **Erro (proposto):** `422` · `error.reaction.own_content`.
- **Requisito:** RF-087.

#### RN-100 – Seguir autores ⬜
Só é possível seguir quem é ou já foi autor e tem artigos publicados. Ninguém segue a si mesmo, e seguir de novo alguém já seguido não duplica. A lista de autores seguidos só é vista pela própria pessoa. Se o autor perder o papel, os seguidores são mantidos.

- **Erros (propostos):** `422` · `error.follow.self` e `error.follow.not_author`.
- **Requisitos:** RF-088, RF-089.

#### RN-101 – Feed ⬜
O feed mostra os artigos publicados dos autores seguidos, inclusive os escritos em coautoria, do mais recente para o mais antigo. Ficam de fora os artigos ocultados e os de quem tem um bloqueio com a pessoa.

- **Requisito:** RF-090.

#### RN-102 – Bloqueio entre usuários ⬜
Quem foi bloqueado não comenta nos artigos de quem bloqueou, não responde a essa pessoa, não a segue, não a menciona com aviso e não a convida para uma coautoria. Ao bloquear, os "seguir" entre as duas pessoas são desfeitos nos dois sentidos. Quem foi bloqueado não é avisado. Os comentários antigos continuam visíveis para as outras pessoas e aparecem recolhidos para quem bloqueou. O bloqueio não impede as ações de moderação nem os avisos da plataforma. Cada pessoa bloqueia até 1.000 contas.

- **Erro (proposto):** `403` · `error.block.interaction_not_allowed`.
- **Requisito:** RF-091.

#### RN-103 – Perfil público ⬜
O perfil público mostra os dados que a pessoa escolheu mostrar (RN-034) e, no caso dos autores, os artigos publicados e as séries. O perfil de uma conta desativada, suspensa, com exclusão agendada ou excluída responde `404`. O perfil sai do ar nesses casos, mas o conteúdo da pessoa segue as regras de cada situação (RN-038, RN-077 e RN-117).

- **Requisitos:** RF-092, RF-064.

---

## 8. Notificações

#### RN-104 – Tipos de aviso e canais ⬜
A plataforma avisa sobre estes eventos:

- comentário em um artigo da pessoa;
- resposta a um comentário dela;
- menção;
- novo seguidor;
- novo artigo de um autor seguido;
- convite de coautoria;
- resultado de pedidos, denúncias e recursos;
- ação de moderação sobre o conteúdo dela;
- anúncio da plataforma.

Os avisos chegam por dois canais: dentro da plataforma e por e-mail. Por padrão, todos os tipos chegam dentro da plataforma, mas por e-mail chegam só os resultados, as ações de moderação e os convites, para não encher a caixa de entrada. A pessoa muda os canais de cada tipo.

- **Requisitos:** RF-093, RF-094, RF-095.

#### RN-105 – E-mails essenciais ⬜
São essenciais os e-mails com códigos e os de dispositivo novo, troca de e-mail, tentativa de cadastro com o e-mail da pessoa, redefinição de senha, exclusão agendada, suspensão e novas versões dos termos de uso e da política de privacidade. Eles não podem ser desligados, não entram no resumo diário e continuam sendo enviados quando o teto de custos é atingido, para que ninguém perca o acesso à conta. Os convites (RN-129) não são essenciais e param junto com os outros e-mails quando o teto é atingido.

- **Requisitos:** RF-095, RF-124.

#### RN-106 – Entrega em tempo real ⬜
Os avisos dentro da plataforma chegam por uma conexão contínua (*Server-Sent Events*), sem que o front-end precise consultar a API de tempos em tempos. Todo aviso é guardado antes de ser enviado, e quem estava desconectado recebe os pendentes ao se conectar de novo.

- **Requisitos:** RF-093, RNF-030.

#### RN-107 – Lista de avisos ⬜
A lista de avisos é paginada, com os mais recentes primeiro, e informa quantos ainda não foram lidos. A pessoa marca um aviso ou todos como lidos. Os avisos são apagados depois de 90 dias.

- **Requisito:** RF-096.

#### RN-108 – Agrupamento de avisos ⬜
Eventos do mesmo tipo sobre o mesmo item, ocorridos em até 10 minutos, viram um único aviso, como "Ana e mais 3 pessoas comentaram no seu artigo".

- **Requisito:** RF-093.

#### RN-109 – Resumo diário por e-mail ⬜
Quem escolhe o resumo diário recebe um único e-mail por dia, às 8h no fuso da plataforma, e só se houver avisos não lidos. O resumo traz até 50 avisos e termina com "e mais N avisos" quando houver mais.

- **Requisito:** RF-097.

#### RN-110 – Limites de e-mail ⬜
Cada pessoa recebe um número limitado de e-mails de aviso por dia, e o que passar do limite vai para o resumo do dia. A plataforma também tem uma cota mensal de e-mails, que custa por volta de US$ 1 no padrão; quando ela acaba, só os e-mails essenciais (RN-105) continuam.

- ⚙️ e-mails de aviso por pessoa: padrão de 20 por dia · intervalo: 5 a 50.
- ⚙️ cota mensal da plataforma: padrão de 10.000 e-mails · intervalo: 1.000 a 50.000.
- **Requisitos:** RNF-022, RNF-023.

#### RN-111 – Reenvio de e-mails que falharam ⬜
Um e-mail cujo envio falhar é tentado de novo até 3 vezes, depois de 1, 5 e 30 minutos; se continuar falhando, é marcado como perdido e entra nas estatísticas de e-mails. Um endereço que devolve os e-mails de forma permanente deixa de receber os e-mails não essenciais até a pessoa confirmar o endereço de novo.

- **Requisitos:** RF-128, RNF-043.

#### RN-112 – Avisos, bloqueios e situação da conta ⬜
Ninguém recebe avisos causados por alguém com quem tem um bloqueio (RN-102). Contas que não estão ativas (RN-009) recebem só os e-mails essenciais.

- **Requisitos:** RF-091, RF-093.

---

## 9. Moderação

#### RN-113 – Denúncias ⬜
O usuário autenticado pode denunciar artigos, comentários e perfis. Cada pessoa denuncia o mesmo item uma única vez, e ninguém denuncia o próprio conteúdo. O motivo é escolhido de uma lista fixa (spam, assédio, discurso de ódio, conteúdo impróprio, desinformação, violação de direitos autorais ou outro), e a descrição, de até 1.000 caracteres, é obrigatória no motivo "outro". No momento da denúncia, uma cópia do conteúdo é guardada, para que a análise não dependa de uma edição feita depois. Cada pessoa faz até 20 denúncias por dia. Quem denunciou nunca é revelado a quem foi denunciado.

- **Erros (propostos):** `409` · `error.report.already_exists`; `422` · `error.report.own_content`; `429` · `error.report.too_many`.
- **Requisito:** RF-098.

#### RN-114 – Fila e conflito de interesse ⬜
A fila de denúncias é ordenada pela classificação da triagem (RN-089) e, depois, pela data, da mais antiga para a mais nova. As denúncias sobre o mesmo item são analisadas juntas. Ninguém analisa uma denúncia sobre o próprio conteúdo nem uma denúncia que fez. A análise exige a verificação em duas etapas ativa (RN-028).

- **Erro (proposto):** `403` · `error.moderation.conflict_of_interest`.
- **Requisitos:** RF-099, RF-107.

#### RN-115 – Ações de moderação ⬜
Os moderadores e a administração arquivam a denúncia quando ela não procede, ocultam artigos e comentários e removem comentários. Só a administração aplica penalidades ao autor: advertência, suspensão temporária, de 1 a 90 dias, ou suspensão por tempo indeterminado. Toda ação exige um motivo e fica registrada com quem a tomou e quando. Uma denúncia é procedente quando leva a qualquer ação além do arquivamento.

- **Erro:** `403` · `error.auth.access_denied`.
- **Requisitos:** RF-099, RF-130.

#### RN-116 – Conteúdo ocultado ⬜
O conteúdo ocultado some das listagens, da busca, do feed e do perfil público, e um artigo ocultado leva junto os comentários dele. O autor continua vendo o conteúdo, marcado como ocultado e com o motivo. A ocultação só é desfeita pela moderação ou por um recurso aceito (RN-119).

- **Requisitos:** RF-099, RF-101.

#### RN-117 – Suspensão de contas ⬜
Só a administração suspende uma conta, sempre com um motivo e um prazo, ou por tempo indeterminado. Ao suspender, as sessões da pessoa são encerradas, e o login passa a responder `403` (RN-012). O conteúdo dela continua visível, a menos que também seja ocultado. Como a pessoa não consegue entrar na conta, o e-mail de suspensão traz um link de uso único para recorrer. Ninguém suspende o superadministrador (RN-054), e só o superadministrador suspende administradores. A conta volta a ficar ativa ao fim do prazo ou quando a administração a reativa.

- **Erro:** `403` · `error.auth.access_denied`.
- **Requisitos:** RF-043, RF-118.

#### RN-118 – Resultado da análise ⬜
Quem denunciou fica sabendo só se a denúncia foi procedente ou não, sem os detalhes da ação, para proteger a privacidade de quem foi denunciado. Quem foi denunciado fica sabendo a ação tomada e o motivo, mas nunca quem denunciou.

- **Requisito:** RF-100.

#### RN-119 – Recurso ⬜
O autor pode recorrer de uma ocultação ou de uma penalidade uma única vez, dentro de um prazo, com um texto de até 1.000 caracteres. O recurso é analisado por alguém diferente de quem tomou a decisão, e o recurso contra uma penalidade só pode ser analisado pela administração. A decisão do recurso é final. Se o recurso for aceito, o conteúdo volta e a penalidade é desfeita, e a ação passa a contar como revertida no painel de quem a tomou. ⚙️ prazo para recorrer: padrão de 30 dias · intervalo: 7 a 90 dias.

- **Erros (propostos):** `422` · `error.appeal.deadline_passed`; `409` · `error.appeal.already_exists`.
- **Requisitos:** RF-101, RF-108.

#### RN-120 – Denúncia procedente contra um moderador ⬜
Para a promoção a administrador (RN-052), conta como denúncia procedente contra um moderador toda denúncia procedente sobre um conteúdo ou o perfil dele. As ações dele que forem revertidas aparecem no painel do moderador, mas não contam como denúncia procedente (D-031).

- **Requisitos:** RF-041, RF-108.

#### RN-121 – Palavras bloqueadas ⬜
O dono mantém uma lista de até 500 palavras ou expressões bloqueadas. A comparação ignora maiúsculas e acentos e considera só palavras inteiras. Um comentário com uma palavra da lista é recusado com uma mensagem genérica, sem dizer qual palavra foi encontrada, para não ensinar a contornar a lista. Os comentários barrados entram, de forma agregada, no painel da moderação.

- **Erro (proposto):** `422` · `error.comment.blocked_word`.
- **Requisitos:** RF-102, RF-107.

---

## 10. Painéis e administração da plataforma

### 10.1 Painéis

#### RN-122 – Quem vê cada painel ⬜
Cada painel é visto só por quem precisa dele:

- minha conta: todo usuário cadastrado;
- do autor: cada autor, sobre si, contando as coautorias;
- dos autores: a administração vê a lista de autores e os pedidos de autoria, e os autores veem o espaço comum;
- da moderação: os moderadores e a administração;
- do moderador: o próprio moderador e a administração;
- do dono: só o superadministrador.

O conteúdo detalhado de cada painel fica nos critérios de aceite das issues.

- **Erro:** `403` · `error.auth.access_denied`.
- **Requisitos:** RF-103, RF-104, RF-105, RF-106, RF-107, RF-108, RF-109.

#### RN-123 – Estatísticas agregadas e anônimas ⬜
Os painéis mostram só números. Um grupo com menos de 5 pessoas aparece como "menos de 5", para que ninguém seja identificado pelo número, como aconteceria com um único leitor de uma cidade pequena. Cada leitura conta uma vez por visitante, por artigo e por dia, com um identificador temporário (um hash que muda todos os dias), sem guardar quem leu.

- **Requisitos:** RF-104, RNF-018.

#### RN-124 – Espaço comum dos autores ⬜
No espaço comum, a administração publica avisos e diretrizes de escrita e pode fixar alguns no topo. Os autores só leem.

- **Requisito:** RF-106.

### 10.2 Identidade, páginas e termos

#### RN-125 – Identidade da plataforma ⬜
A identidade da plataforma tem o nome, com até 100 caracteres, a descrição, com até 500, o logo, que segue as regras das imagens (RN-076), o idioma padrão, escolhido entre os idiomas suportados, até 10 links e o fuso horário da plataforma, usado no resumo diário e no agendamento.

- **Requisitos:** RF-110, RNF-025.

#### RN-126 – Páginas institucionais ⬜
O dono cria até 20 páginas institucionais, cada uma com um slug único. As páginas usam os mesmos formatos e a mesma limpeza dos artigos (RN-062 e RN-063) e são públicas.

- **Requisitos:** RF-047, RF-111.

#### RN-127 – Plataforma pronta para cadastros ⬜
A plataforma só aceita cadastros depois de o dono informar o contato do encarregado pelos dados pessoais e publicar a primeira versão dos termos de uso e da política de privacidade, porque a LGPD exige os dois antes da coleta de dados.

- **Erro (proposto):** `503` · `error.platform.not_ready`.
- **Requisitos:** RF-112, RF-113.

#### RN-128 – Versões dos termos ⬜
As versões dos termos de uso e da política de privacidade são numeradas e têm data de vigência. Uma versão publicada não muda mais: qualquer alteração vira uma versão nova, que exige um novo aceite (RN-039).

- **Requisito:** RF-113.

### 10.3 Convites e usuários

#### RN-129 – Convites ⬜
O convite é um link de uso único enviado por e-mail, e cada administrador envia até 20 convites por dia. O convite para um perfil segue as regras de concessão: para autor, só no modo fechado ou pelas exceções de início; para moderador, por um administrador; para administrador, só pelas exceções de início, porque a promoção normal segue a RN-052. A pessoa convidada aceita os termos e confirma a idade como em qualquer cadastro. Se o e-mail já tiver uma conta, a resposta é a mesma, e a pessoa recebe o convite ao entrar. ⚙️ validade: padrão de 7 dias · intervalo: 1 a 30 dias.

- **Erro (proposto):** `429` · `error.invite.too_many`.
- **Requisito:** RF-114.

#### RN-130 – Gestão de usuários ⬜
A administração lista os usuários com filtros por papel e situação. Cada pessoa só age sobre os perfis abaixo do seu (RN-044), e toda ação fica registrada (RN-140). O e-mail dos usuários só é visto pelo dono, para expor menos dados pessoais.

- **Erro:** `403` · `error.auth.access_denied`.
- **Requisito:** RF-117.

### 10.4 Regras e operação

#### RN-131 – Ajuste das regras ⬜
Todo valor marcado com ⚙️ só muda dentro do intervalo seguro, com o histórico descrito na seção 1, e a mudança exige a verificação em duas etapas ativa (RN-028).

- **Erro (proposto):** `422` · `error.settings.out_of_range`.
- **Requisito:** RF-119.

#### RN-132 – Backup ⬜
O backup é feito automaticamente uma vez por dia, e o dono também pode pedir um backup manual por dia. Ele é gerado fora da requisição e criptografado com uma chave que fica só no `.env`. Baixar um backup exige a reautenticação (RN-030), e cada download fica registrado. Os backups expiram no prazo da RN-043.

- **Requisitos:** RF-115, RNF-014.

#### RN-133 – Restauração ⬜
A restauração exige a reautenticação (RN-030). Durante ela, a plataforma fica em manutenção, e todas as sessões são encerradas. A restauração aplica de novo as exclusões de conta feitas depois do backup (RN-043) e fica registrada.

- **Requisito:** RF-116.

#### RN-134 – Modo de manutenção ⬜
Durante a manutenção, só os moderadores e a administração entram na plataforma. As outras pessoas recebem `503`, com uma mensagem escrita pelo dono, inclusive na leitura pública. Os artigos agendados para esse período são publicados quando a manutenção termina.

- **Erro (proposto):** `503` · `error.platform.maintenance`.
- **Requisito:** RF-131.

#### RN-135 – Anúncios ⬜
O anúncio tem até 1.000 caracteres e chega a todas as contas ativas como um aviso do tipo "anúncio" (RN-104). O dono publica no máximo 1 anúncio por dia.

- **Erro (proposto):** `429` · `error.announcement.too_many`.
- **Requisitos:** RF-093, RF-132.

#### RN-136 – Limite de requisições em toda a API ⬜
Cada IP faz um número limitado de requisições por minuto, menor nas rotas públicas e maior com login, para proteger a plataforma contra abuso e robôs.

- ⚙️ rotas públicas: padrão de 100 por minuto · intervalo: 30 a 1.000.
- ⚙️ com login: padrão de 300 por minuto · intervalo: 30 a 1.000.
- **Erro (proposto):** `429` · `error.rate_limit.exceeded`.
- **Requisito:** RNF-015.

### 10.5 Custos, métricas e registros

#### RN-137 – Teto de custos ⬜
O custo de cada serviço pago é estimado a partir do uso e dos preços configurados. O teto pode ser baixado pelo dono, nunca subido acima de US$ 15. O dono recebe avisos quando o gasto do mês chega a 50%, 80% e 100% do teto. Ao chegar a 100%, a IA e os e-mails não essenciais são pausados; o envio de imagens continua, porque custa centavos e bloqueá-lo impediria publicar, e os e-mails essenciais (RN-105) também continuam. Tudo volta no mês seguinte, ou antes, se o dono aumentar o teto dentro do limite. ⚙️ teto: padrão de US$ 15 · intervalo: US$ 1 a US$ 15.

- **Requisitos:** RF-123, RF-124, RNF-022.

#### RN-138 – Métricas e estatísticas ⬜
Os visitantes são contados sem cookies e sem guardar quem são, com o mesmo identificador temporário da RN-123. A região aproximada vem de uma base de geolocalização local, e o IP não é guardado para as estatísticas. A saúde do sistema e as métricas técnicas são vistas só pelo dono. Os dados agregados ficam guardados por 24 meses.

- **Requisitos:** RF-120, RF-121, RF-122, RF-123, RF-125, RF-126, RF-127, RF-128, RNF-018, RNF-041.

#### RN-139 – Logs ⬜
Os logs técnicos e de segurança são vistos só pelo dono. Neles, os dados pessoais aparecem mascarados, cada consulta fica registrada, e os registros são apagados no prazo da RN-043.

- **Requisitos:** RF-129, RNF-019.

#### RN-140 – Registro das ações administrativas ⬜
Cada ação administrativa guarda quem a fez, o que foi feito, sobre quem ou sobre o quê, quando e o motivo. O registro não pode ser editado nem apagado por ninguém, nem pelo dono, e é guardado por 2 anos.

- **Requisito:** RF-130.

---

## 11. Divergências conhecidas

Pontos em que o código atual não cumpre totalmente a regra pretendida. Cada um vira uma issue `type:bug` e é removido daqui quando corrigido e testado.

| ID | Regra | O que acontece hoje | Impacto | Correção proposta |
|---|---|---|---|---|
| **D1** | RN-006 | O sufixo do nickname tem só 9000 valores por nome e não há nova tentativa. Uma colisão viola a restrição `UNIQUE` e devolve `500`. Um nome sem caracteres válidos gera um nickname como `_4821`. | Médio: nomes comuns colidem cedo (cerca de 50% de chance com umas 100 pessoas de mesmo nome). | Sortear de novo até 5 vezes e usar a base `leitor` (RN-006). |
| **D2** | RN-008 | No cadastro com Google, o e-mail é conferido antes do `googleId`, então, para a mesma pessoa, o caso da conta Google já cadastrada nunca é alcançado. As mensagens também estão trocadas: o `googleId` repetido devolveria `provider_conflict` ("faça o login tradicional"). | Baixo: só a mensagem fica errada. | Conferir primeiro o identificador do provedor e usar as chaves da RN-008. |
| **D3** | RN-013 | O login com Google não confere a situação da conta. | Alto quando a confirmação de e-mail e a suspensão existirem. | Conferir a situação como na RN-012. |
| **D4** | RN-017 / RNF-004 | O cookie tem `maxAge` fixo de 24 h, independente de `JWT_EXPIRATION`, e `Secure=false` escrito no código. | Baixo localmente; alto em produção. | Derivar a validade da configuração e tornar `Secure` configurável por perfil. |
| **D5** | RN-007 | O gateway do Google não confere `email_verified` no token. | Médio: uma conta Google com e-mail não confirmado ocuparia aquele e-mail. | Recusar o token com `422` · `error.auth.provider_email_not_verified`. |
| **D6** | RN-012 / RNF-013 | O Spring Security confere se a conta está ativa e desbloqueada antes de conferir a senha. Uma senha errada em uma conta inativa devolve `403`, e não `401`. | Alto quando a confirmação de e-mail e a suspensão existirem: revela que a conta existe. | Conferir a situação da conta só depois de a senha estar certa. |
| **D7** | RN-002 / RNF-013 | O cadastro devolve `409` · `error.user.already_exists` para um e-mail já cadastrado. | Médio: revela quais e-mails estão cadastrados. | Responder sempre `202` e avisar o endereço (RN-002). |
