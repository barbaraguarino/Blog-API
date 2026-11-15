#!/usr/bin/env bash
# Única forma permitida de o Claude listar e ler issues e PRs (ver CLAUDE.md, regra 7).
# Só mostra o que foi criado pela dona da conta autenticada no gh, e só os comentários dela.
# Conteúdo de outras pessoas nunca é exibido: aparece apenas a contagem do que foi ignorado.
#
# Uso:
#   bash .claude/scripts/github-leitura.sh listar issue|pr [--state open|closed|all]
#   bash .claude/scripts/github-leitura.sh ler issue|pr <número>

set -euo pipefail

# Usa o gh do PATH ou, se o terminal ainda não o enxergar, o local padrão do instalador no Windows.
if ! command -v gh >/dev/null 2>&1; then
  for candidato in "/c/Program Files/GitHub CLI/gh.exe" "/c/Program Files (x86)/GitHub CLI/gh.exe"; do
    if [ -x "$candidato" ]; then gh() { "$candidato" "$@"; }; break; fi
  done
  command -v gh >/dev/null 2>&1 || { echo "gh não encontrado. Instale com: winget install --id GitHub.cli" >&2; exit 1; }
fi

acao="${1:-}"; tipo="${2:-}"
case "$tipo" in
  issue|pr) ;;
  *) echo "Uso: $0 listar|ler issue|pr [número ou --state ...]" >&2; exit 1 ;;
esac

eu="$(gh api user --jq .login)"

case "$acao" in
  listar)
    shift 2
    gh "$tipo" list --author "$eu" --limit 100 "$@" \
      --json number,title,state,labels,milestone \
      --jq '.[] | "#\(.number) [\(.state)] \(.title)" + (if .milestone then "  · " + .milestone.title else "" end) + (if (.labels | length) > 0 then "  · " + ([.labels[].name] | join(", ")) else "" end)'
    ;;

  ler)
    numero="${3:-}"
    [[ "$numero" =~ ^[0-9]+$ ]] || { echo "Informe o número da $tipo." >&2; exit 1; }

    autor="$(gh "$tipo" view "$numero" --json author --jq .author.login)"
    if [ "$autor" != "$eu" ]; then
      echo "#$numero foi criada por outra pessoa. Conteúdo não lido (regra: só ler o que é da autora)."
      exit 0
    fi

    gh "$tipo" view "$numero" --json number,title,state,labels,milestone,body \
      --jq '"#\(.number) [\(.state)] \(.title)\n" + (if .milestone then "Milestone: " + .milestone.title + "\n" else "" end) + (if (.labels | length) > 0 then "Labels: " + ([.labels[].name] | join(", ")) + "\n" else "" end) + "\n" + .body'

    echo
    echo "--- Comentários da autora ---"
    gh "$tipo" view "$numero" --json comments \
      --jq ".comments[] | select(.author.login == \"$eu\") | \"[\(.createdAt)]\n\(.body)\n\""

    outros="$(gh "$tipo" view "$numero" --json comments --jq "[.comments[] | select(.author.login != \"$eu\")] | length")"
    if [ "$outros" -gt 0 ]; then
      echo "($outros comentário(s) de outras pessoas ignorado(s), não lidos.)"
    fi
    ;;

  *) echo "Ação desconhecida: $acao (use listar ou ler)." >&2; exit 1 ;;
esac
