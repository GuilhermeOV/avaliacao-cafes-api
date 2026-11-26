# Roteiro de testes e apresentação

Base da API: `http://127.0.0.1:8081/cafes/api`. Antes de começar: `mvn clean package` e `docker compose up -d`.
Para voltar o banco ao estado inicial: `docker compose down -v` e `docker compose up -d`.

**Pelo Swagger (recomendado):** abra `http://127.0.0.1:8081/cafes/`, execute **POST /auth/login** (o token é
aplicado sozinho no Authorize) e use *Execute* em cada rota abaixo. Para testar como outro usuário (item 4.6),
faça login de novo com `vitor@email.com`.

**Pelo Postman:** importe `docs/postman_collection.json` e rode **Auth → Login** primeiro (o token fica salvo).

## 1. Leitura pública (sem token)

| # | Requisição | Esperado |
|---|---|---|
| 1.1 | `GET /cafes` | 200, 4 cafés com `totalReviews` e médias |
| 1.2 | `GET /cafes?bairro=centro` | 200, só cafés do Centro (não diferencia maiúsculas) |
| 1.3 | `GET /cafes?tipo=Cold Brew` | 200, só "Cold Corner" |
| 1.4 | `GET /cafes?ordenar=media` | 200, maior `mediaGeral` primeiro; cafés sem review por último |
| 1.5 | `GET /cafes/1` | 200, `mediaBebida` 4.5, `mediaGeral` 4.17 |
| 1.6 | `GET /cafes/1/reviews` | 200, 2 reviews |
| 1.7 | `GET /reviews?cafeId=2` | 200, 1 review |

## 2. Autenticação

| # | Requisição | Esperado |
|---|---|---|
| 2.1 | `POST /cafes` sem token | 401 "Token JWT obrigatorio" |
| 2.2 | `POST /cafes` com `Authorization: Bearer abc` | 401 "Token JWT invalido ou expirado" |
| 2.3 | `POST /auth/login` com senha errada | 401 "E-mail ou senha invalidos" |
| 2.4 | `POST /auth/login` (guilherme@email.com / 123456) | 200 com `token` |
| 2.5 | `POST /auth/registrar` com e-mail já existente | 409 |
| 2.6 | `POST /auth/registrar` novo usuário | 201, sem a senha/hash na resposta |

## 3. CRUD de cafés (com token)

| # | Requisição | Esperado |
|---|---|---|
| 3.1 | `POST /cafes` só com `nome` | 400 "Campo 'endereco' e obrigatorio" |
| 3.2 | `POST /cafes` completo | 201 + cabeçalho `Location`; `tipoCafePrincipal` salvo em minúsculas |
| 3.3 | `PATCH /cafes/{id}` com `{"bairro":"Centro"}` | 200, só o bairro mudou |
| 3.4 | `PUT /cafes/{id}` incompleto | 400 (PUT exige todos os campos) |
| 3.5 | `PUT /cafes/{id}` completo | 200 |
| 3.6 | `DELETE /cafes/1` (tem reviews) | 409 |
| 3.7 | `DELETE /cafes/{id novo}` | 204; depois `GET` dá 404 |
| 3.8 | `GET /cafes/abc` | 400 "Id invalido" |
| 3.9 | `DELETE /cafes` (sem id) | 405 |

## 4. Reviews (com token)

| # | Requisição | Esperado |
|---|---|---|
| 4.1 | `POST /reviews` com `notaBebida: 9` | 400 "deve ser de 1 a 5" |
| 4.2 | `POST /reviews` com `cafeId: 999` | 404 |
| 4.3 | `POST /reviews` válido (mesmo enviando `"autor":"hacker"`) | 201, `autor` = usuário do token |
| 4.4 | Repetir 4.3 no mesmo café | 409 "Voce ja avaliou este cafe" |
| 4.5 | `GET /cafes/{id}` | médias atualizadas |
| 4.6 | Logar como `vitor@email.com` e `DELETE` a review do Guilherme | 403 |
| 4.7 | `PATCH /reviews/{id}` pelo autor | 200 |
| 4.8 | `DELETE /reviews/{id}` pelo autor | 204 |

## Sugestão de divisão na apresentação

A avaliação é individual, então cada um deve dominar a sua parte e saber explicar o fluxo completo.

**Guilherme**
- Visão geral do projeto, banco (`init.sql`) e Docker.
- Camadas **DAO** e **Singleton**: `MysqlSingleton`, `MysqlDAO`, `CafeDAO` (LEFT JOIN + AVG para as médias, filtros).
- **Service** de cafés: validações, PUT vs PATCH, regra do 409 ao excluir.
- Demonstração: seções 1 e 3.

**Vitor**
- **Controllers**: `BaseController` (por que sobrescrever `service()` para ter PATCH, leitura do id pela URL,
  tratamento central de erros, códigos 201/204).
- **JWT**: `AuthService`, `JwtUtil` (header.payload.signature), `JwtFilter` (401 antes do controller),
  hash de senha PBKDF2.
- **Service** de reviews: notas 1–5, review único por usuário, 403 para quem não é autor.
- Demonstração: seções 2 e 4.

Roteiro sugerido da demonstração (pelo Swagger): login → `GET /cafes?ordenar=media` → criar café → avaliar o café
→ `GET /cafes/{id}` mostrando as médias → erros (nota 9 = 400, sem token = 401, Vitor apagando review do
Guilherme = 403, excluir café com review = 409) → abrir o código e seguir Controller → Service → DAO.

Perguntas prováveis do professor:
- *Por que o Controller não acessa o DAO direto?* Para manter regra de negócio em um único lugar (Service).
- *Diferença entre PUT e PATCH?* PUT substitui o recurso inteiro (todos os campos obrigatórios); PATCH altera só o que veio.
- *Por que JWT e não sessão?* API stateless: o servidor não guarda estado; o token assinado carrega o usuário.
- *Como evitam SQL Injection?* Todo SQL usa `PreparedStatement` com parâmetros `?`.
- *O que é o Swagger?* Uma página gerada a partir do `openapi.yaml` (padrão OpenAPI) que documenta e permite
  testar a API. Não é parte da API: é só a documentação interativa, servida como arquivo estático.
