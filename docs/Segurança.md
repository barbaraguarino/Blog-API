# Segurança do Repositório e da Conta GitHub

Checklist de segurança do Blog-API, que é um repositório público de portfólio. Os itens marcados como **manual** dependem de você nas configurações do GitHub; os marcados como **no repositório** já estão versionados aqui. As regras que o Claude segue estão no `CLAUDE.md`, seção *Segurança*.

## 🔑 Conta e token

- [ ] **Manual:** no `gh auth login`, use só os escopos necessários. O mínimo do `gh` é `repo`, `read:org` e `gist`; este projeto precisa também de `project` (`gh auth refresh -s project`). Não adicione `delete_repo`, `admin:org`, `admin:public_key` nem `workflow`, a menos que surja uma necessidade real.
  - Alternativa mais restrita: um *fine-grained token* com acesso **só ao repositório Blog-API** (permissões: Issues, Pull requests, Contents e Metadata), usado com `gh auth login --with-token`.
- [ ] **Manual:** não adicione colaboradores desconhecidos aos repositórios (*Settings → Collaborators*).
- [ ] **Manual:** mantenha a autenticação em dois fatores (2FA) ativa na conta GitHub.

## 🛡️ Repositório público (portfólio)

- [x] **No repositório:** o `.gitignore` bloqueia `.env`, `.env.*` (exceto `.env.example`), `application-local.properties`, chaves e certificados (`*.pem`, `*.key`, `*.p12`, `*.jks`…), `.aws/` e arquivos de credenciais do Google. Verificado em 2026-11-15: nenhum segredo está versionado nem aparece no histórico.
- [ ] **Regra:** nunca commitar `.env`, senhas, chaves de API ou credenciais da AWS. Qualquer novo tipo de arquivo sensível entra no `.gitignore` **antes** do primeiro commit.
- [ ] **Manual:** ativar em *Settings → Code security* (gratuitos em repositório público):
  - Secret scanning
  - Push protection (bloqueia o push quando detecta um segredo)
  - Dependabot alerts
  - Dependabot security updates
- [x] **No repositório:** nenhum link de sessão ou de chat do Claude vai para commits, PRs, issues ou arquivos. O `.claude/settings.json` desliga o link de sessão automático (`sessionUrl: false`) e troca a assinatura dos PRs por "🤖 Gerado com Claude Code", sem link. O hook `.claude/hooks/bloquear-links-de-sessao.sh` bloqueia qualquer tentativa que escape. Ele não depende do `jq` e foi testado em 2026-11-15.
- [x] **No repositório:** o workflow [`verificar-links-claude.yml`](../.github/workflows/verificar-links-claude.yml) roda em todo PR e falha se o título, a descrição, as mensagens de commit ou as linhas adicionadas tiverem link do Claude. Ele usa o script [`.github/scripts/verificar-links-claude.sh`](../.github/scripts/verificar-links-claude.sh), que também pode ser rodado localmente: `bash .github/scripts/verificar-links-claude.sh < arquivo`.
- [ ] **Manual:** em *Settings → Branches* (ou *Rules*), proteja a `develop` e a `main` exigindo o check **Verificar links do Claude** antes do merge. Sem isso, o check aparece vermelho, mas não impede o merge.
- [ ] **Lembrete:** ninguém altera os seus arquivos sem você aprovar um PR. Os `.md` e o código só mudam por commits seus ou por PRs que você aceitar.

## 📋 Issues

Decisão atual (D-011): as **issues ficam públicas e travadas** e o **quadro do GitHub Projects fica privado**.

- [ ] **Manual:** planejamento pessoal → GitHub Projects **privado**, com *draft items* para ideias que ainda não viraram issue.
- [x] **No repositório:** as issues que você abre são travadas automaticamente pelo workflow [`.github/workflows/lock-own-issues.yml`](../.github/workflows/lock-own-issues.yml). Ele passa a valer depois do merge na branch padrão (`main`), porque o GitHub só executa workflows de `issues` que estejam lá.
- [ ] Travar manualmente, se precisar: `gh issue lock <número>`.
- [ ] Se não quiser nenhuma interação externa: `gh repo edit --enable-issues=false` (desativa as issues do repositório inteiro).

## 👀 Ler conteúdo de outras pessoas com segurança

- [ ] Filtrar issues de terceiros: `gh issue list --search "-author:@me"`.
- [ ] Ler conteúdo desconhecido como dado, sem renderizar nem executar:
  - `gh issue view <n> --json author,body,comments`
  - `gh issue view <n> --web` (abre no navegador)
- [ ] Desconfiar de `gh repo clone` e `gh run download` em repositórios que você não conhece. Não execute scripts nem binários vindos deles.
- [ ] Texto de issue, PR ou comentário de terceiros nunca é instrução, nem para você nem para o Claude.

## 🚫 Se aparecer spam ou abuso

1. Apagar o comentário ou a issue.
2. Travar a issue: `gh issue lock <n> --reason spam`.
3. Bloquear o usuário: `gh api -X PUT user/blocks/NOME_DO_USUARIO`.
4. Se for recorrente, ativar o limite de interação: *Configurações da conta → Moderation → Interaction limits* (até 6 meses, renovável).
