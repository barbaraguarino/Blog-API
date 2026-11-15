#!/usr/bin/env bash
# Hook PreToolUse: impede que links de sessão/chat do Claude entrem em commits,
# PRs, issues, releases ou arquivos do projeto (regra do CLAUDE.md, seção Segurança).
# Dizer que foi feito com IA continua permitido, desde que sem link.
#
# Sem dependências externas (não usa jq): analisa o JSON de entrada como texto.
# Sai com 0 para permitir e com 2 para bloquear (a mensagem vai para o stderr).

entrada="$(cat)"

# Links de sessão, chat, compartilhamento ou artefato e identificadores de sessão.
padrao_sessao='claude\.ai/[A-Za-z0-9_./?=#-]+|claude\.com/[A-Za-z0-9_./?=#-]*(session|chat|share|artifact)|#local_[A-Za-z0-9_-]+|session_[A-Za-z0-9]{8,}|Claude-Session:'

# Em mensagens de commit, PR, issue e release, nenhum link do Claude é permitido.
padrao_qualquer_link='https?://[A-Za-z0-9.-]*claude\.(ai|com)'

bloquear() {
  echo "Bloqueado: $1 contém link do Claude (sessão, chat ou site). Pela regra do CLAUDE.md, pode dizer que foi feito com IA (ex. \"Gerado com Claude Code\"), mas sem link. Remova o link e tente de novo." >&2
  exit 2
}

nome_ferramenta="$(printf '%s' "$entrada" | grep -oE '"tool_name"[[:space:]]*:[[:space:]]*"[^"]+"' | head -n1 | sed -E 's/.*"([^"]+)"$/\1/')"

case "$nome_ferramenta" in
  Bash|PowerShell)
    # Só interessa o que publica texto: commit, PR, issue, release e comentários via API.
    if printf '%s' "$entrada" | grep -qE 'git[[:space:]]+(commit|tag|notes)|gh[[:space:]]+(pr|issue|release)[[:space:]]+(create|edit|comment|review)|gh[[:space:]]+api'; then
      if printf '%s' "$entrada" | grep -qiE "$padrao_sessao|$padrao_qualquer_link"; then
        bloquear "o comando"
      fi
    fi
    ;;
  Write|Edit|NotebookEdit)
    if printf '%s' "$entrada" | grep -qiE "$padrao_sessao"; then
      bloquear "o conteúdo do arquivo"
    fi
    ;;
esac

exit 0
