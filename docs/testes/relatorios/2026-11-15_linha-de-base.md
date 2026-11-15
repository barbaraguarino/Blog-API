# Relatório de Testes: Linha de base (v0.0)

- **Data:** 2026-11-15
- **Branch / commit:** `develop` @ `67cd442`
- **Comando:** `./mvnw -o test` (modo offline)
- **Ambiente:** Java 25, Windows 11

## Resultado

**Não executado.** Em modo offline, o Maven Wrapper precisa de plugins (`maven-resources-plugin` e dependências) que ainda não estão no repositório local `~/.m2`. Eles teriam de ser baixados da Maven Central, e pela regra do projeto nada é baixado sem aprovação prévia. Este relatório registra só o **inventário** dos testes existentes, para servir de linha de base.

Para gerar o próximo relatório com resultado real, rode `./mvnw test` com acesso à internet (só a Maven Central oficial é usada) ou execute os testes pelo IntelliJ.

## Inventário

| Classe | Tipo | Testes |
|---|---|---|
| `BlogApiApplicationTests` | Contexto completo (precisa do PostgreSQL e do `.env`) | 1 |
| `AuthControllerTest` | Controller (`@WebMvcTest`) | 8 |
| `UserRegistrationControllerTest` | Controller (`@WebMvcTest`) | 8 |
| `AuthenticateGoogleUserServiceTest` | Unitário (caso de uso) | 3 |
| `AuthenticateLocalUserServiceTest` | Unitário (caso de uso) | 2 |
| `RegisterGoogleUserServiceTest` | Unitário (caso de uso) | 4 |
| `RegisterLocalUserServiceTest` | Unitário (caso de uso) | 2 |
| **Total** | | **28** |

## Cobertura de regras

As regras cobertas e as lacunas estão na matriz do [PLANO_DE_TESTES.md §6](../PLANO_DE_TESTES.md#6-matriz-de-rastreabilidade-regras-implementadas). Resumo das lacunas:

- Sem testes de domínio para `User` (RN-003, RN-005).
- Política de senha e limites de campos testados só de forma genérica (RN-002, RN-004).
- Sem testes de `CustomUserDetailsService`, `TokenService` e `SecurityFilter`.
- Sem teste de conta desativada ou bloqueada → 403 (RN-009).
- Nenhum teste cobre as divergências D1–D4 do REGRAS_DE_NEGOCIO.md.

## Conclusão

Linha de base registrada, sem resultado de execução. Próximos passos: executar a suíte com aprovação para baixar os plugins do Maven, cobrir as lacunas acima e configurar CI.
