# Regras de Negócio

**Versão:** 4.0 · **Última modificação:** 27/11/2026

Este documento reúne as regras que o sistema obedece: validações, restrições, permissões e comportamentos esperados, incluindo os valores padrão que o dono da plataforma pode ajustar.

---

## 1. Como ler este documento

Cada regra tem um identificador (RN-xxx), diz o que o sistema obedece e por quê, e, quando pode ser violada, qual erro o cliente recebe. As regras são numeradas na ordem em que aparecem neste documento. Uma regra nova entra na seção do seu módulo e, quando necessário, a numeração é refeita e registrada em [Decisões.md](Decisões.md), com a correspondência entre os números antigos e os novos. Os requisitos que as regras detalham estão em [Requisitos.md](Requisitos.md).

- **Status:** ✅ implementado · 🟡 parcial · ⬜ planejado.
- **Divergências:** quando o código não cumpre totalmente uma regra já implementada, ela recebe a marca ⚠️, e a diferença é descrita na seção *Divergências conhecidas*.
- **Erros:** status HTTP e chave de i18n, traduzida conforme o cabeçalho `Accept-Language` e devolvida no campo `message`. Nas regras planejadas, a chave é uma proposta.
- **Testes:** toda regra implementada tem pelo menos um teste que cita o ID no `@DisplayName`, e uma regra que muda é atualizada aqui no mesmo PR da mudança de código.

### Regras ajustáveis pelo dono

Os prazos, as quantidades e os limites marcados com ⚙️ são valores padrão. O dono pode ajustá-los pela API (RF-119), sempre dentro de um intervalo seguro definido no código, e cada mudança guarda quem a fez, quando, o valor anterior e o novo. Só o perfil de testes aceita valores fora desse intervalo, para que os testes não precisem esperar prazos reais; fora dele, a API não sobe com um valor inseguro. Cada regra ajustável informa o padrão e o intervalo, como em "⚙️ padrão: 2 meses · intervalo: 2 a 12 meses".

### Formato de erro

Toda violação de regra devolve um corpo neste formato:

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

#### RN-003 – Dados obrigatórios e limites do cadastro ✅
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

#### RN-007 – Cadastro com provedor social ✅ ⚠️ D5
O cadastro com provedor social, como o Google, só é aceito com os provedores que o dono ativou. Ele recebe o token de identidade emitido pelo provedor, e a API valida a assinatura e o destinatário do token (o `client-id` configurado) e exige que o provedor tenha confirmado o e-mail (`email_verified`). Do token vêm o identificador da pessoa no provedor, o e-mail e o nome: a API não confia em dados enviados pelo cliente, só no que vem dentro de um token assinado. A conta nasce sem senha local e com o e-mail já confirmado.

- **Erros:**
  - `400` · `error.validation.auth.provider_token_required` (token vazio);
  - `401` · `error.auth.provider_token_invalid` (token inválido, expirado, de outro aplicativo ou forjado);
  - `422` · `error.auth.provider_email_not_verified`;
  - `422` · `error.auth.provider_disabled`.
- **Requisitos:** RF-002, RF-005.

#### RN-008 – Conta de provedor única ✅ ⚠️ D2
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

#### RN-012 – Login por e-mail ou nickname ✅ ⚠️ D6
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
O token de acesso dura 15 minutos, e o token de renovação dura 7 dias. Cada login cria uma sessão com o usuário, o hash do token de renovação, o IP, o dispositivo (User-Agent), a data de criação, o último uso, a expiração e a data em que foi encerrada. O logout encerra a sessão atual e apaga os cookies. Um token de acesso curto limita o estrago de um token roubado, e o token de renovação guardado no banco permite encerrar sessões, o que um JWT sozinho não permite. ⚙️ validade do token de renovação: padrão de 7 dias · intervalo: 1 a 30 dias.

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
Um novo código só pode ser pedido 60 segundos depois do anterior, e um novo código invalida o anterior da mesma finalidade. O limite evita o abuso do envio de e-mails, que gera spam e custo. ⚙️ pedidos por hora, por conta e finalidade: padrão de 5 · intervalo: 3 a 10.

- **Erro (proposto):** `429` · `error.code.too_many_requests`.
- **Requisitos:** RF-014, RNF-007, RNF-023.

#### RN-024 – Resposta neutra na recuperação de senha ⬜
O pedido de recuperação responde sempre da mesma forma ("Se o e-mail estiver cadastrado, você receberá um código"), exista ou não a conta. Isso impede que alguém use a recuperação para descobrir quais e-mails estão cadastrados. Uma conta sem senha, criada só com um provedor social, pode definir uma senha por esse caminho, porque o código prova que a pessoa é dona do e-mail.

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
As ações sensíveis do RF-021 (trocar o e-mail, desativar a verificação em duas etapas, excluir a conta, transferir a posse da plataforma e baixar ou restaurar backups) exigem que a pessoa se confirme de novo, mesmo logada: com a senha, com o código do aplicativo autenticador ou, se a conta não tiver senha, com um código por e-mail. A confirmação vale por 5 minutos.

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
O nickname é sempre público, porque identifica a pessoa nos comentários. O nome, a foto e a descrição só aparecem no perfil público se a pessoa escolher mostrá-los, e o padrão é ficarem ocultos: a privacidade vem ativada desde o início, como pede o art. 46 da LGPD. O e-mail nunca é público. Nos autores, os artigos publicados e o número de seguidores são sempre públicos, mas a lista de seguidores e a de autores seguidos nunca são.

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

Ao fim da carência, os dados pessoais são apagados, o e-mail fica livre para um novo cadastro e o nickname fica reservado por 30 dias (RN-032). Nas coautorias, a pessoa sai do artigo, que continua com os outros coautores. Os registros de acesso são mantidos até completar os 6 meses do Marco Civil da Internet (RN-021), porque a guarda é uma obrigação legal. O dono só pode excluir a própria conta depois de transferir a posse da plataforma, para que ela não fique sem responsável.

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
O histórico de interesses guarda as tags dos artigos que a pessoa leu ou em que reagiu e os autores que ela segue, e vem ligado por padrão. Os registros são apagados depois de um prazo. Limpar o histórico apaga tudo na hora; desligá-lo faz a plataforma parar de registrar, e a recomendação passa a usar só as tags de interesse escolhidas e os autores seguidos. ⚙️ prazo dos registros: padrão de 90 dias · intervalo: 30 a 180 dias.

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
O pedido precisa da aprovação de 2 administradores, ou de 1 se a plataforma tiver só um. O superadministrador conta como administrador e segue as mesmas regras. Uma única recusa, com motivo, encerra o pedido, e o motivo fica visível para o solicitante. A aprovação de mais de uma pessoa evita que alguém vire autor por favoritismo. ⚙️ aprovações: padrão de 2 · intervalo: 1 a 3.

- **Erro:** `403` · `error.auth.access_denied` para quem não é da administração.
- **Requisito:** RF-037.

#### RN-049 – Nomeação direta no modo fechado ⬜
No modo fechado, não existem pedidos: um administrador nomeia o autor, e a pessoa é avisada. Fora do modo fechado, a nomeação direta só vale pelas exceções de início (RN-055).

- **Erro (proposto):** `422` · `error.author.direct_appointment_not_allowed`.
- **Requisito:** RF-038.

#### RN-050 – Retirada do papel de autor ⬜
Um administrador pode retirar o papel de autor, informando um motivo, que fica registrado. Os artigos já publicados continuam publicados e assinados pela pessoa, e os rascunhos ficam guardados, mas não podem mais ser publicados. A pessoa pode pedir autoria de novo, pelas regras normais.

- **Requisito:** RF-039.

#### RN-051 – Papel de moderador ⬜
O papel de moderador é concedido e retirado por um administrador e não exige ser autor. Para usar as funções de moderação, o moderador precisa ter a verificação em duas etapas ativa (RN-028).

- **Requisito:** RF-040.

#### RN-052 – Promoção a administrador ⬜
Só pode ser promovido a administrador o moderador com mais de 12 meses na função e sem nenhuma denúncia procedente contra ele. A promoção começa com a indicação feita por um administrador. Com 3 ou mais administradores, ela exige a aprovação de 2 outros administradores, além de quem indicou e do próprio candidato; com menos de 3, quem aprova é o superadministrador. As exigências evitam o favoritismo na escolha de quem administra a plataforma. ⚙️ tempo como moderador: padrão de 12 meses · intervalo: 6 a 24 meses.

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

## 5. Conteúdo *(v0.2–v0.3)*

#### RN-133 – Categorias ⬜
- **Regra:** a categoria tem nome, **slug único** e uma categoria pai opcional, formando uma árvore. Uma categoria não pode ser pai dela mesma, nem de um ancestral. Só o `ADMIN` gerencia categorias.
- **Requisito:** RF-070.

#### RN-134 – Remoção de categorias ⬜
- **Regra:** não é possível remover uma categoria que tenha artigos ou subcategorias.
- **Por quê:** evita artigos sem categoria e árvores quebradas.
- **Erro (proposto):** `422` · `error.category.in_use`.
- **Requisito:** RF-070.

#### RN-135 – Quem escreve artigos ⬜
- **Regra:** só `AUTHOR` ou `ADMIN` criam artigos, e só o próprio autor os edita.
- **Requisito:** RF-053.

#### RN-136 – Ciclo de vida do artigo ⬜
- **Regra:** o artigo tem o status `RASCUNHO`, `PUBLICADO` ou `ARQUIVADO`. As transições permitidas são:
  - rascunho → publicado;
  - publicado → arquivado;
  - arquivado → rascunho.

  A **data de publicação** é registrada na primeira publicação. O **slug** é gerado a partir do título e é único.
- **Requisito:** RF-053.

#### RN-137 – Rascunhos são privados ⬜
- **Regra:** os rascunhos só são visíveis para o autor. Para os demais, o rascunho "não existe" (`404`).
- **Requisito:** RF-053.

#### RN-138 – Listagens públicas ⬜
- **Regra:** as listagens públicas mostram só artigos **publicados**, paginados (padrão 10, máximo 50 por página) e ordenados por data de publicação, do mais recente para o mais antigo.
- **Requisito:** RF-053.

#### RN-139 – Tags ⬜
- **Regra:** as tags são **normalizadas** (minúsculas, sem espaços nas pontas) e **únicas**. Uma tag inexistente é criada ao ser usada. Cada artigo tem no máximo 10 tags.
- **Requisito:** RF-066.

#### RN-140 – Imagens ⬜
- **Regra:** são aceitos só os formatos JPEG, PNG e WebP, com até 5 MB. Ao trocar uma imagem, a anterior é removida do armazenamento.
- **Erro (proposto):** `400` · `error.upload.invalid_type` / `error.upload.too_large`.
- **Requisito:** RF-067.

#### RN-141 – Busca ⬜
- **Regra:** a busca considera só artigos publicados e procura no título e no resumo, ignorando maiúsculas e acentos. Pode ser combinada com filtros de categoria, tag e autor.
- **Requisito:** RF-050.

---

## 6. Social e moderação *(v0.3–v0.4)*

#### RN-150 – Comentários ⬜
- **Regra:** só é possível comentar artigos publicados. Há respostas em **um único nível** (comentário → resposta). Responder uma resposta não é permitido.
- **Requisito:** RF-082.

#### RN-151 – Quem altera comentários ⬜
- **Regra:** o autor do comentário edita e remove o próprio comentário. `ADMIN` e `MODERATOR` removem qualquer comentário.
- **Requisito:** RF-082.

#### RN-152 – Uma reação por artigo ⬜
- **Regra:** cada usuário tem no máximo **uma** reação por artigo. Uma nova reação substitui a anterior.
- **Requisito:** RF-086.

#### RN-153 – Seguir autores ⬜
- **Regra:** não é possível seguir a si mesmo, e seguir alguém que já é seguido não duplica.
- **Requisito:** RF-088.

#### RN-154 – Feed ⬜
- **Regra:** o feed mostra só artigos publicados de autores seguidos, paginados, do mais recente para o mais antigo.
- **Requisito:** RF-090.

#### RN-160 – Denúncias ⬜
- **Regra:** um usuário denuncia o mesmo conteúdo **uma única vez**, escolhendo um motivo de uma lista fixa (spam, assédio, conteúdo impróprio, desinformação, outro) e com uma descrição opcional.
- **Erro (proposto):** `409` · `error.report.already_exists`.
- **Requisito:** RF-098.

#### RN-161 – Ações de moderação ⬜
- **Regra:** uma denúncia pode ser:
  - **arquivada**, quando não procede;
  - resolvida com a **ocultação do conteúdo**, que some das listagens públicas;
  - resolvida com a **penalização do autor**, que pode chegar ao bloqueio da conta (`locked = true`).

  Toda ação registra quem a tomou e quando.
- **Requisito:** RF-099.

---

## 7. Divergências conhecidas

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
