# Roadmap

Visão geral das fases do Blog-API. O detalhe de cada tarefa (objetivo, regras de negócio, critérios de aceite) fica nas [Issues](https://github.com/barbaraguarino/Blog-API/issues), e o andamento no quadro do GitHub Projects.

## Fluxo de trabalho

1. Toda tarefa nasce como uma **Issue**, usando o template de funcionalidade ou de bug.
2. A Issue entra em um **Milestone** (fase abaixo) e no quadro do Projects.
3. O branch segue o padrão `tipo/descricao-curta` (`feature/`, `refactor/`, `fix/`, `docs/`) a partir da `develop`.
4. O PR para a `develop` usa `Closes #N` e cumpre o checklist da **Definição de Pronto** do template de PR.
5. A `main` recebe a `develop` ao final de cada Milestone.

## Fases

| Milestone | Foco | Status |
|---|---|---|
| **v0.0 – Fundação** | Exceções globais, i18n, Spring Security + JWT, cadastro e login local e via Google, login por e-mail ou nickname | Concluído |
| [**v0.1 – Identidade essencial**](https://github.com/barbaraguarino/Blog-API/milestone/1) | Swagger, sessões com Refresh Token, e-mail (SMTP) e ativação de conta, recuperação e troca de senha | Próximo |
| [**v0.2 – Artigos (MVP)**](https://github.com/barbaraguarino/Blog-API/milestone/2) | Pedido e aprovação de autoria, CRUD de artigos e rascunhos, categorias, tags, upload de mídias no S3 | Planejado |
| [**v0.3 – Social**](https://github.com/barbaraguarino/Blog-API/milestone/3) | Comentários, reações, seguidores, feed e busca | Planejado |
| [**v0.4 – Moderação e segurança avançada**](https://github.com/barbaraguarino/Blog-API/milestone/4) | Denúncias, 2FA com app autenticador (TOTP), vincular e desvincular conta Google | Planejado |

## Concluído antes da migração para Issues

As tarefas #01 a #08 do antigo `Backlog_Sequencial.md` foram feitas antes do uso de Issues. Por isso, os números delas não correspondem a Issues ou PRs do GitHub.

- Tratamento global de exceções (`@RestControllerAdvice` + DTO de erro padrão)
- Internacionalização (i18n) via `MessageSource` e cabeçalho `Accept-Language`
- Configuração base de segurança (Spring Security `STATELESS` + JWT)
- Cadastro de usuários local (BCrypt) e via Google ID Token
- Login local (JWT em cookie `HttpOnly`) e via Google
- Login dinâmico por e-mail ou nickname
