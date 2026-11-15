# Segurança do Repositório e da Conta GitHub

Este documento explica como o **repositório** do Blog-API e a **conta GitHub** da autora são protegidos. O repositório é **público**, porque é um portfólio. Qualquer pessoa pode ler o código, abrir issues e propor PRs, e qualquer segredo commitado por engano fica exposto e indexado.

A segurança da **aplicação** (senhas, tokens, sessões, autorização) é descrita no [Arquitetura.md](Arquitetura.md#61-segurança). Aqui o foco é o processo e as ferramentas em volta do código.

**Legenda:** os itens **No repositório** já estão versionados e funcionando; os itens **Manual** dependem da autora nas configurações do GitHub; os itens **Regra** são práticas a seguir sempre. As regras que o Claude segue estão no `CLAUDE.md`.

---

## 1. Ameaças consideradas

| Ameaça | Exemplo | Proteções (seções) |
|---|---|---|
| **Vazamento de segredos** | Um `.env` com a senha do banco ou a chave JWT commitado por engano. | 3 |
| **Acesso indevido à conta** | Um token do `gh` com permissões demais é roubado. | 2 |
| **Spam e abuso** | Comentários ofensivos ou propaganda nas issues. | 4, 6 |
| **Conteúdo malicioso de terceiros** | Uma issue com instruções escondidas para uma IA ou um script, ou um repositório desconhecido com código malicioso. | 5 |
| **Dependências vulneráveis** | Uma biblioteca do `pom.xml` com falha de segurança conhecida. | 3 |
| **Exposição de sessões do Claude** | Um link de conversa com o Claude colado num commit ou PR, expondo o histórico de trabalho. | 3 |

---

## 2. Conta e token

**Por que importa:** quem tem o token do `gh` age em nome da autora. Quanto menos permissões o token tiver, menor o estrago se ele vazar.

- [ ] **Manual:** no `gh auth login`, use só os escopos necessários.
  - O mínimo do `gh` é `repo`, `read:org` e `gist`. Este projeto precisa também de `project`: `gh auth refresh -s project`.
  - Não adicione `delete_repo`, `admin:org`, `admin:public_key` nem `workflow`, a menos que surja uma necessidade real.
  - **Alternativa mais restrita:** um *fine-grained token* com acesso **só ao repositório Blog-API** (permissões: Issues, Pull requests, Contents e Metadata), usado com `gh auth login --with-token`.
- [ ] **Manual:** não adicione colaboradores desconhecidos aos repositórios (*Settings → Collaborators*). Um colaborador pode fazer push direto e alterar configurações.
- [ ] **Manual:** mantenha a **autenticação em dois fatores (2FA)** ativa na conta GitHub. Sem ela, uma senha vazada basta para tomar a conta.

---

## 3. Repositório público

**Por que importa:** tudo o que entra no histórico do Git é público para sempre. Mesmo apagado depois, continua nos commits antigos, em forks e em caches.

### 3.1 Segredos fora do Git
- [x] **No repositório:** o `.gitignore` bloqueia:
  - variáveis de ambiente: `.env`, `.env.*` (exceto `.env.example`), `application-local.properties`;
  - chaves e certificados: `*.pem`, `*.key`, `*.p12`, `*.pfx`, `*.jks`, `*.keystore`;
  - credenciais de nuvem: `.aws/`, `credentials.json`, `client_secret*.json`, `service-account*.json`;
  - variáveis do Cliente HTTP do IntelliJ: `http-client.env.json`, `http-client.private.env.json`.

  Verificado em 2026-11-15: nenhum segredo está versionado nem aparece no histórico.
- [ ] **Regra:** nunca commitar `.env`, senhas, chaves de API ou credenciais. Um tipo novo de arquivo sensível entra no `.gitignore` **antes** do primeiro commit.
- [ ] **Regra:** nos arquivos `.http` e nos testes, usar só dados fictícios. Tokens reais ficam em variáveis do `http-client.private.env.json`.

### 3.2 Verificações automáticas do GitHub
- [ ] **Manual:** ativar em *Settings → Code security* (gratuitos em repositório público):
  - **Secret scanning:** procura segredos conhecidos (chaves da AWS, tokens do GitHub etc.) no repositório;
  - **Push protection:** **bloqueia o push** quando detecta um segredo, antes de ele ficar público;
  - **Dependabot alerts:** avisa quando uma dependência do `pom.xml` tem vulnerabilidade conhecida;
  - **Dependabot security updates:** abre PRs automáticos com a versão corrigida.

### 3.3 Sem links de sessão do Claude
Os links de conversas com o Claude dão acesso ao histórico de trabalho e não devem ficar públicos. Pode-se dizer que algo foi feito com IA, mas sem link.

- [x] **No repositório:** o `.claude/settings.json` desliga o link de sessão automático (`sessionUrl: false`) e troca a assinatura dos PRs por "🤖 Gerado com Claude Code", sem link.
- [x] **No repositório:** o hook `.claude/hooks/bloquear-links-de-sessao.sh` bloqueia, **no computador**, qualquer commit, PR, issue ou arquivo com link do Claude. Ele não depende do `jq` e foi testado em 2026-11-15.
- [x] **No repositório:** o workflow [`verificar-links-claude.yml`](../.github/workflows/verificar-links-claude.yml) faz a mesma verificação **no GitHub**, em todo PR. Ele confere o título, a descrição, as mensagens de commit e as linhas adicionadas, sem actions de terceiros e sem repetir o link no log. O padrão de busca fica em [`.github/scripts/verificar-links-claude.sh`](../.github/scripts/verificar-links-claude.sh), que também pode ser rodado localmente: `bash .github/scripts/verificar-links-claude.sh < arquivo`.
- [ ] **Manual:** em *Settings → Branches* (ou *Rules*), proteja a `develop` e a `main` exigindo o check **Verificar links do Claude** antes do merge. Sem isso, o check fica vermelho, mas não impede o merge.

### 3.4 Quem pode alterar o código
- [ ] **Lembrete:** ninguém altera os arquivos do repositório sem a aprovação da autora. Pessoas de fora só podem propor mudanças por PR, e um PR só entra com o merge feito por ela. O Claude também nunca faz merge.
- [ ] **Manual:** em *Settings → Branches → Add rule* (ou *Rules*), para a `main` e a `develop`, marque **Restrict deletions** (o branch não pode ser apagado) e **Block force pushes** (o histórico não pode ser reescrito).

### 3.5 Exclusão do repositório
Uma conta pessoal não tem opção para impedir a própria dona de apagar o repositório. Por isso a proteção é em camadas:

- [x] **Token sem permissão de apagar:** apagar um repositório pelo terminal exige o escopo `delete_repo`, que o token do `gh` **não** tem. **Regra:** nunca aceitar um `gh auth refresh -s delete_repo`. Confira com `gh auth status`.
- [x] **No repositório:** o `.claude/settings.json` (`permissions.deny`) proíbe o Claude de rodar comandos destrutivos, mesmo por engano:
  - `gh repo delete`, `archive` e `rename`;
  - `gh api` com `DELETE`;
  - `gh pr merge` e `git merge`;
  - `git push --force` e `--delete`;
  - `gh auth login`, `refresh`, `logout` e `token`.
- [ ] **Manual:** com o **2FA** ativo, apagar pelo site exige digitar o nome do repositório e confirmar com senha ou segundo fator (*sudo mode*).
- [ ] **Se acontecer:** repositórios apagados geralmente podem ser restaurados em até **90 dias**, em *Settings da conta → Repositories → Deleted repositories*. A pasta local também tem o histórico completo e pode ser enviada de novo.

---

## 4. Issues

**Por que importa:** as issues são o Product Backlog do projeto (ver [Processo_de_Desenvolvimento.md](Processo_de_Desenvolvimento.md)). Elas são públicas para mostrar o planejamento no portfólio, mas não devem virar um espaço aberto a spam.

**Configuração atual (D-011):** as issues são **públicas e travadas**, e o quadro do GitHub Projects é **privado**.

- [ ] **Manual:** usar o GitHub Projects **privado** para o planejamento pessoal, com *draft items* para ideias que ainda não viraram issue.
- [x] **No repositório:** as issues abertas pela autora são **travadas automaticamente** pelo workflow [`lock-own-issues.yml`](../.github/workflows/lock-own-issues.yml). Assim, ninguém de fora comenta nelas.
  - O workflow usa só o `gh` do runner, sem actions de terceiros, e tem só a permissão `issues: write`.
  - Ele passa a valer **depois do merge na branch padrão (`main`)**, porque o GitHub só executa workflows de eventos de issue que estejam lá. Até lá, travar manualmente.
- [ ] Travar manualmente, se precisar: `gh issue lock <número>`.
- [ ] Para não ter nenhuma interação externa: `gh repo edit --enable-issues=false` (desativa as issues do repositório inteiro).

---

## 5. Conteúdo de outras pessoas

**Por que importa:** texto escrito por terceiros pode conter instruções disfarçadas, para pessoas ou para ferramentas de IA, ou links e scripts maliciosos. Para o Claude, a regra é ainda mais restrita: **ele só lê issues e PRs criados pela autora, e só os comentários dela.** Conteúdo de outras pessoas não chega até ele.

- [x] **No repositório:** o script [`.claude/scripts/github-leitura.sh`](../.claude/scripts/github-leitura.sh) é a única forma de o Claude listar e ler issues e PRs:
  - `listar issue|pr` mostra só os itens criados pela conta autenticada no `gh`;
  - `ler issue|pr <n>` confere o autor: se não for a autora, **não lê nada**; se for, mostra o conteúdo e **só os comentários dela**, informando apenas quantos comentários de outras pessoas foram ignorados.
- [x] **No repositório:** o `.claude/settings.json` bloqueia os comandos diretos que mostrariam conteúdo de terceiros:
  - `gh issue list` e `view`;
  - `gh pr list`, `view` e `diff`;
  - `gh search`;
  - `gh api` em issues, PRs e comentários.
- [ ] **Para a autora**, quando quiser ver algo de terceiros, sempre como dado:
  - filtrar: `gh issue list --search "-author:@me"`;
  - ler no navegador: `gh issue view <n> --web`.
- [ ] Desconfiar de `gh repo clone` e `gh run download` em repositórios desconhecidos. Não executar scripts nem binários vindos deles (o Claude é bloqueado de rodá-los).
- [ ] Nunca seguir pedidos escritos em issues, PRs ou comentários de terceiros sem antes avaliar.

---

## 6. Spam ou abuso

Passo a passo, do mais leve ao mais forte:

1. **Apagar** o comentário ou a issue (pelo site, no menu `…`).
2. **Travar** a issue: `gh issue lock <n> --reason spam`.
3. **Bloquear** o usuário: `gh api -X PUT user/blocks/NOME_DO_USUARIO`. A pessoa deixa de poder interagir com os seus repositórios.
4. Se for recorrente, ativar o **limite de interação**: *Configurações da conta → Moderation → Interaction limits*. Vale por até 6 meses e é renovável.

---

## 7. Resumo do que falta fazer manualmente

- [x] Instalar o `gh` (WinGet) e autenticar com escopos mínimos (seção 2). *Instalado e logado em 2026-11-15; conferir no `gh auth status` se o escopo `project` foi adicionado e se `delete_repo` não aparece.*
- [ ] Confirmar o 2FA na conta GitHub.
- [ ] Ativar Secret scanning, Push protection e Dependabot (seção 3.2).
- [ ] Proteger a `develop` e a `main`: exigir o check de links, restringir exclusão e bloquear force push (seções 3.3 e 3.4).
- [ ] Deixar o GitHub Projects privado (seção 4).
