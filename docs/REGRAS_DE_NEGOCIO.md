# Regras de Negócio

Regras numeradas (RN-xxx) que o sistema deve cumprir. Cada regra aponta para o requisito que atende em [REQUISITOS.md](REQUISITOS.md). As regras já implementadas descrevem o comportamento **atual** do código. As divergências conhecidas estão marcadas com ⚠️ e listadas no fim.

**Status:** ✅ Implementado · 🟡 Parcial · ⬜ Planejado

**Como manter:** uma regra nova recebe o próximo número livre do bloco. Uma regra que mudar deve ser editada aqui, com a decisão registrada no [DECISOES.md](DECISOES.md). Cada regra deve ter pelo menos um teste automatizado.

---

## 1. Identidade e acesso

### Cadastro

| ID | Regra | Requisito | Status |
|---|---|---|---|
| RN-001 | O e-mail é único no sistema. Cadastro com e-mail já existente retorna **409** (`error.user.already_exists`). | RF-001, RF-002 | ✅ |
| RN-002 | Nome: obrigatório, até 150 caracteres. E-mail: obrigatório, formato válido, até 150 caracteres. | RF-001 | ✅ |
| RN-003 | Toda conta nova nasce com o papel **READER**, ativa (`enabled = true`) e desbloqueada (`locked = false`). | RF-001, RF-002 | ✅ (muda com a RN-015) |
| RN-004 | A senha local precisa ter no mínimo 8 caracteres, com pelo menos 2 letras maiúsculas, 2 minúsculas, 2 números e 1 caractere especial. Ela é guardada só como hash BCrypt. | RF-001 | ✅ |
| RN-005 | O nickname é gerado automaticamente a partir do nome: minúsculas, espaços viram `_`, só `a-z`, `0-9` e `_`, até 40 caracteres, mais `_` e um sufixo aleatório de 4 dígitos (1000–9999). O nickname é único. | RF-001, RF-002 | 🟡 ⚠️ D1 |
| RN-006 | O cadastro via Google valida o ID Token no backend e usa nome, e-mail e `googleId` vindos do Google. Uma conta Google não tem senha local. | RF-002 | ✅ |
| RN-007 | O `googleId` é único. Cadastro Google com `googleId` já existente retorna **409**. | RF-002 | ✅ ⚠️ D2 |

### Login

| ID | Regra | Requisito | Status |
|---|---|---|---|
| RN-008 | Um login bem-sucedido emite um JWT (assunto = e-mail do usuário) em cookie `HttpOnly`, `SameSite=Strict`, e devolve o perfil do usuário. | RF-003, RF-004 | ✅ |
| RN-009 | O campo de login aceita e-mail ou nickname: se contém `@`, busca por e-mail; senão, por nickname. Credenciais erradas retornam **401**. Conta desativada ou bloqueada retorna **403** (`error.auth.account_inactive`). | RF-003 | ✅ |
| RN-010 | O login Google busca a conta pelo `googleId`. Se não existir conta, retorna **404** pedindo cadastro. Token Google inválido retorna **401**. | RF-004 | 🟡 ⚠️ D3 |

### Sessões *(v0.1)*

| ID | Regra | Requisito | Status |
|---|---|---|---|
| RN-011 | O token de acesso dura 15 minutos e o Refresh Token dura 7 dias. Cada sessão guarda IP, User-Agent, criação, último uso e expiração. O logout revoga a sessão atual. | RF-005, RF-006 | ⬜ |
| RN-012 | Cada refresh emite novos tokens de acesso e de refresh e invalida o refresh anterior (rotação). | RF-005 | ⬜ |
| RN-013 | Se um Refresh Token já usado for reapresentado, todas as sessões do usuário são revogadas (detecção de reuso). | RF-005 | ⬜ |
| RN-014 | O usuário só lista e revoga as próprias sessões. Ele pode revogar todas, menos a atual. | RF-007 | ⬜ |

### E-mail, ativação e senha *(v0.1)*

| ID | Regra | Requisito | Status |
|---|---|---|---|
| RN-015 | Códigos por e-mail têm 6 dígitos, valem 10 minutos, são de uso único, têm limite de tentativas e são guardados como hash. | RF-008 a RF-010 | ⬜ |
| RN-016 | A conta local nasce desativada até a confirmação do e-mail. A conta Google nasce ativa. | RF-008 | ⬜ |
| RN-017 | O reenvio do código de ativação tem limite de frequência. | RF-008 | ⬜ |
| RN-018 | O pedido de recuperação de senha responde sempre igual, exista ou não o e-mail (não revela contas). | RF-009 | ⬜ |
| RN-019 | Redefinir a senha revoga todas as sessões do usuário. | RF-009 | ⬜ |
| RN-020 | Trocar a senha exige a senha atual e um código por e-mail. A nova senha segue a RN-004 e deve ser diferente da atual. As outras sessões são revogadas. | RF-010 | ⬜ |

### Segurança avançada *(v0.4)*

| ID | Regra | Requisito | Status |
|---|---|---|---|
| RN-021 | O 2FA TOTP só é ativado depois de confirmar um código válido. São gerados códigos de recuperação de uso único. Com 2FA ativo, o login só emite tokens após o código TOTP. | RF-011 | ⬜ |
| RN-022 | Vincular conta Google exige que o `googleId` não pertença a outro usuário. Desvincular só é permitido se a conta tiver senha local. | RF-012 | ⬜ |

## 2. Autoria e conteúdo *(v0.2–v0.3)*

| ID | Regra | Requisito | Status |
|---|---|---|---|
| RN-030 | Só pode pedir autoria quem tem conta criada há mais de 2 meses. | RF-020 | ⬜ |
| RN-031 | Um usuário tem no máximo um pedido de autoria pendente. | RF-020 | ⬜ |
| RN-032 | Só ADMIN aprova ou recusa pedidos. Aprovar promove o usuário a AUTHOR. Recusar exige motivo. | RF-021 | ⬜ |
| RN-033 | A categoria tem slug único e pode ter uma categoria pai. Só ADMIN a gerencia. | RF-022 | ⬜ |
| RN-034 | Não é possível remover categoria com artigos ou subcategorias. | RF-022 | ⬜ |
| RN-035 | Só AUTHOR ou ADMIN cria artigos, e só o próprio autor os edita. | RF-023 | ⬜ |
| RN-036 | O artigo tem status RASCUNHO, PUBLICADO ou ARQUIVADO. O slug é único. | RF-023 | ⬜ |
| RN-037 | Rascunhos só são visíveis para o autor. Para os demais, retornam **404**. | RF-023 | ⬜ |
| RN-038 | As listagens públicas mostram só artigos publicados, paginados e ordenados por data de publicação. | RF-023 | ⬜ |
| RN-039 | As tags são normalizadas (minúsculas, sem espaços nas pontas) e únicas. Uma tag inexistente é criada ao ser usada. | RF-024 | ⬜ |
| RN-040 | O upload aceita só imagens até o tamanho máximo configurado. Ao trocar uma imagem, a anterior é removida do bucket. | RF-025 | ⬜ |
| RN-041 | A busca considera só artigos publicados e ignora maiúsculas e acentos. | RF-026 | ⬜ |

## 3. Social *(v0.3)*

| ID | Regra | Requisito | Status |
|---|---|---|---|
| RN-050 | Só é possível comentar em artigos publicados, com respostas em um único nível. | RF-030 | ⬜ |
| RN-051 | O autor do comentário edita e remove o próprio comentário. ADMIN e MODERATOR removem qualquer um. | RF-030 | ⬜ |
| RN-052 | Cada usuário tem no máximo uma reação por artigo. Uma nova reação substitui a anterior. | RF-031 | ⬜ |
| RN-053 | Não é possível seguir a si mesmo, e seguir de novo não duplica. | RF-032 | ⬜ |
| RN-054 | O feed mostra só artigos publicados de autores seguidos, paginados e do mais recente para o mais antigo. | RF-033 | ⬜ |

## 4. Moderação *(v0.4)*

| ID | Regra | Requisito | Status |
|---|---|---|---|
| RN-060 | O usuário denuncia um conteúdo uma única vez, com um motivo de uma lista fixa e uma descrição opcional. | RF-040 | ⬜ |
| RN-061 | Uma denúncia pode ser arquivada, levar à ocultação do conteúdo (que some das listagens públicas) ou à penalização do autor. | RF-041 | ⬜ |

---

## 5. Divergências conhecidas

Pontos em que o código atual não cumpre totalmente a regra pretendida. Cada um deve virar uma issue `type:bug`.

| ID | Regra | Situação atual | Risco |
|---|---|---|---|
| D1 | RN-005 | O sufixo tem só 9000 valores por nome e não há nova tentativa em caso de colisão. Dois cadastros que geram o mesmo nickname violam a constraint `UNIQUE` e resultam em erro **500**. | Médio: nomes comuns colidem cedo (em torno de 100 pessoas com o mesmo nome já dão cerca de 50% de chance). |
| D2 | RN-001 / RN-007 | No cadastro Google, e-mail já existente retorna `error.user.already_exists`, e `googleId` repetido retorna `error.user.provider_conflict` ("faça o login tradicional"). As mensagens parecem trocadas. | Baixo: só afeta a mensagem. |
| D3 | RN-010 | O login Google não verifica se a conta está desativada ou bloqueada; só o login local verifica. | Alto quando bloqueio e ativação existirem (v0.1). |
| D4 | RN-008 | O `maxAge` do cookie está fixo em 24 h (`86400`), independente de `JWT_EXPIRATION`. | Baixo: cookie e token podem expirar em momentos diferentes. |
