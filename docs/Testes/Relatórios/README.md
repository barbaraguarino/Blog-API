# Relatórios de Testes

Um arquivo por execução relevante (fechamento de Milestone, antes de uma release ou depois de uma mudança grande), nomeado `AAAA-MM-DD_<contexto>.md`. Relatórios antigos não são editados.

## Modelo

```markdown
# Relatório de Testes: <contexto>

- **Data:** AAAA-MM-DD
- **Branch / commit:** `<branch>` @ `<hash curto>`
- **Comando:** `./mvnw test`
- **Ambiente:** Java X, SO, banco (se usado)

## Resultado

| Total | Passou | Falhou | Erro | Ignorado | Tempo |
|---|---|---|---|---|---|
| 0 | 0 | 0 | 0 | 0 | 0 s |

## Por classe

| Classe | Testes | Resultado |
|---|---|---|

## Falhas e erros

<!-- Teste, mensagem, causa provável e issue aberta. -->

## Cobertura de regras

<!-- Regras novas cobertas nesta versão e lacunas que continuam abertas (ver Plano_de_Testes.md §6). -->

## Conclusão

<!-- Aprovado / reprovado para merge ou release, e próximos passos. -->
```

## Histórico

| Data | Contexto | Resultado |
|---|---|---|
| 2026-11-15 | [Linha de base (v0.0)](2026-11-15_Linha_de_Base.md) | Não executado: inventário apenas |
