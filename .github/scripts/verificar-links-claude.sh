#!/usr/bin/env bash
# Lê texto da entrada padrão e falha (saída 1) se houver link do Claude:
# sessão, chat, compartilhamento, artefato, identificador de sessão ou o site.
# Usado pelo workflow verificar-links-claude.yml. Não imprime o link encontrado,
# para não repeti-lo nos logs públicos do Actions.
#
# Uso: echo "texto" | bash .github/scripts/verificar-links-claude.sh
#
# Os padrões usam classes como [:] para que este arquivo não case consigo mesmo.

padrao='https?://[A-Za-z0-9.-]*claude\.(ai|com)|claude\.ai/[A-Za-z0-9_./?=#-]+|#local_[A-Za-z0-9_-]+|session_[A-Za-z0-9]{8,}|Claude-Session[:]'

if grep -qiE "$padrao"; then
  exit 1
fi
exit 0
