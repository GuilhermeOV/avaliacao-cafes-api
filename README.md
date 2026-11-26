# ☕ Plataforma de Avaliação de Cafés Locais — API REST

Trabalho **API — Aplicações para Internet (T1)**, projeto **3 – Plataforma de Avaliação de Cafés Locais**.

API RESTful desenvolvida em **Java (Servlets nativos)** seguindo o padrão **MVC** com separação em camadas
**Controller → Service → DAO**, banco **MySQL** e autenticação **JWT**. Continuação do projeto MVC
[feedback-produtos-mvc](https://github.com/GuilhermeOV/feedback-produtos-mvc), agora devolvendo **JSON** em vez de telas JSP.

## 👥 Dupla

- Guilherme Vital — 5160116
- Vitor Gomes — 5160058

## 🛠️ Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 17 + Jakarta Servlets 6.0 | Controllers REST (sem framework) |
| Gson 2.11 | Conversão JSON ↔ objetos Java |
| java-jwt 4.4 (Auth0) | Geração e validação de tokens JWT |
| MySQL 8.4 | Banco de dados |
| Maven | Dependências e build do `.war` |
| Tomcat 10.1 + Docker Compose | Sobe banco e servidor automaticamente |
| Swagger UI 5 (OpenAPI 3) | Documentação interativa dos endpoints |

## 🗂️ Estrutura do projeto

```
avaliacao-cafes-api/
├── docker-compose.yml           # MySQL + Tomcat
├── init.sql                     # Cria o banco, as tabelas e dados de teste
├── pom.xml
├── docs/
│   ├── postman_collection.json  # Coleção pronta para importar no Postman
│   └── roteiro-de-testes.md     # Roteiro de testes / apresentação
└── src/main/
    ├── java/br/com/cafes/
    │   ├── config/
    │   │   ├── MysqlSingleton.java   # Conexão única com o banco (Singleton)
    │   │   └── JwtUtil.java          # Gera e valida tokens JWT
    │   ├── model/                    # MODEL — espelham as tabelas
    │   │   ├── Cafe.java
    │   │   ├── Review.java
    │   │   └── Usuario.java
    │   ├── dto/                      # Objetos de entrada/saída do login
    │   │   ├── LoginRequest.java
    │   │   ├── LoginResponse.java
    │   │   └── RegistroRequest.java
    │   ├── dao/                      # DAO — ÚNICA camada que escreve SQL
    │   │   ├── MysqlDAO.java         # Classe base dos DAOs
    │   │   ├── CafeDAO.java
    │   │   ├── ReviewDAO.java
    │   │   └── UsuarioDAO.java
    │   ├── service/                  # SERVICE — regras de negócio e validações
    │   │   ├── CafeService.java
    │   │   ├── ReviewService.java
    │   │   ├── AuthService.java
    │   │   └── Texto.java            # Ajudantes de validação de texto
    │   ├── controller/               # CONTROLLER — recebem a requisição e devolvem JSON
    │   │   ├── BaseController.java   # Roteamento (inclui PATCH), JSON e tratamento de erros
    │   │   ├── CafeController.java
    │   │   ├── ReviewController.java
    │   │   ├── AuthController.java
    │   │   └── IndexController.java
    │   ├── filter/
    │   │   ├── JwtFilter.java        # Bloqueia escrita sem token (401)
    │   │   └── CorsFilter.java
    │   ├── exception/
    │   │   └── ApiException.java     # Erro de negócio com status HTTP
    │   └── util/
    │       ├── JsonUtil.java         # Gson configurado
    │       └── SenhaUtil.java        # Hash de senha (PBKDF2)
    └── webapp/
        ├── index.html                # Página do Swagger UI
        ├── openapi.yaml              # Especificação OpenAPI de todos os endpoints
        └── WEB-INF/web.xml           # Ordem dos filtros
```

## 🧠 Como as camadas conversam

```
Cliente (Postman) ──HTTP/JSON──> CorsFilter ─> JwtFilter ─> Controller ─> Service ─> DAO ─> MySQL
                                                              │
                     <────────── JSON + status HTTP ──────────┘
```

- **Controller**: lê a URL e o corpo JSON, chama o Service e escreve a resposta com o status correto.
  Não contém regra de negócio nem SQL.
- **Service**: valida dados e aplica as regras (notas de 1 a 5, um review por usuário por café, só o autor
  altera o próprio review, não excluir café com avaliações...). Lança `ApiException` com o status HTTP adequado.
- **DAO**: executa o SQL com `PreparedStatement` (protege contra SQL Injection) e converte `ResultSet` em objetos.

**Exemplo — `DELETE /api/cafes/1`:** `JwtFilter` valida o token → `CafeController.doDelete()` →
`CafeService.deletar()` verifica se o café existe (senão **404**) e se tem reviews (senão **409**) →
`CafeDAO.deletar()` executa o `DELETE` → controller responde **204 No Content**.

## 🧩 Padrões e boas práticas aplicados

- **MVC + Service + DAO** — responsabilidades separadas por camada.
- **Singleton** — `MysqlSingleton` mantém uma única conexão reaproveitada.
- **Herança / Template** — `BaseController` concentra roteamento, JSON e erros; `MysqlDAO` é a base dos DAOs.
- **Tratamento de erros centralizado** — toda exceção vira `{"status": ..., "erro": "..."}` com o status HTTP certo.
- **Segurança** — JWT (stateless) em um `Filter`, senhas com hash **PBKDF2** + salt (nunca em texto puro),
  hash nunca aparece no JSON (`transient`), `PreparedStatement` em todo SQL, segredos por variável de ambiente.
- **Semântica REST** — verbos corretos (GET/POST/PUT/PATCH/DELETE), `201 + Location` ao criar, `204` ao excluir.

## 🗄️ Banco de dados

Baseado no script do enunciado, com melhorias: `NOT NULL`, `CHECK` das notas (1–5) e a tabela `usuarios`
para o login JWT.

| Tabela | Colunas |
|---|---|
| `cafes` | id, nome, endereco, bairro, tipo_cafe_principal |
| `reviews` | id, cafe_id (FK), usuario_id (FK), autor, nota_bebida, nota_ambiente, nota_atendimento, comentario, criado_em |
| `usuarios` | id, nome, email (único), senha_hash, criado_em |

Médias por critério e contagem de reviews são calculadas no SQL (`LEFT JOIN` + `AVG`/`COUNT`).

## ▶️ Como executar

Pré-requisitos: **Docker Desktop**, **Java 17** e **Maven**.

```bash
git clone https://github.com/GuilhermeOV/avaliacao-cafes-api.git
cd avaliacao-cafes-api
mvn clean package          # gera deploy/cafes.war
docker compose up -d       # sobe MySQL (porta 3307) e Tomcat (porta 8081)
```

- **Swagger (documentação interativa): `http://127.0.0.1:8081/cafes/`**
- Base da API: `http://127.0.0.1:8081/cafes/api`

> Use `127.0.0.1` em vez de `localhost` (em algumas máquinas `localhost` vai para IPv6 e não responde).
> Aguarde ~20 s após o `docker compose up -d` para o Tomcat terminar de subir.

Para encerrar: `docker compose down` (use `docker compose down -v` para apagar o banco e recriar com o `init.sql`).

**Usuários de teste** (senha `123456`): `guilherme@email.com` e `vitor@email.com`.

## 🔐 Autenticação

1. `POST /api/auth/login` devolve um `token`.
2. Envie o token nas rotas protegidas: `Authorization: Bearer <token>`.

**Rotas públicas:** todo `GET` e `/api/auth/*`. **Protegidas (JWT):** todo `POST`, `PUT`, `PATCH` e `DELETE`
de cafés e reviews. O token expira em 2 horas.

## 🌐 Endpoints

### Autenticação

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| POST | `/api/auth/registrar` | Cadastra usuário | 201 |
| POST | `/api/auth/login` | Gera token JWT | 200 |

### Cafés

| Método | Rota | Descrição | Auth | Sucesso |
|---|---|---|---|---|
| GET | `/api/cafes` | Lista cafés com médias | — | 200 |
| GET | `/api/cafes?bairro=Centro` | Filtra por bairro | — | 200 |
| GET | `/api/cafes?tipo=cold brew` | Filtra por tipo de café | — | 200 |
| GET | `/api/cafes?ordenar=media` | Ranking (melhor média primeiro) | — | 200 |
| GET | `/api/cafes/{id}` | Detalhe com médias | — | 200 |
| GET | `/api/cafes/{id}/reviews` | Reviews de um café | — | 200 |
| POST | `/api/cafes` | Cria café | JWT | 201 |
| PUT | `/api/cafes/{id}` | Substitui todos os campos | JWT | 200 |
| PATCH | `/api/cafes/{id}` | Altera só os campos enviados | JWT | 200 |
| DELETE | `/api/cafes/{id}` | Exclui (se não tiver reviews) | JWT | 204 |

Os filtros podem ser combinados: `/api/cafes?bairro=Centro&tipo=espresso&ordenar=media`
(não diferenciam maiúsculas/minúsculas).

### Reviews

| Método | Rota | Descrição | Auth | Sucesso |
|---|---|---|---|---|
| GET | `/api/reviews` | Lista todas | — | 200 |
| GET | `/api/reviews?cafeId=1` | Lista de um café | — | 200 |
| GET | `/api/reviews/{id}` | Detalhe | — | 200 |
| POST | `/api/reviews` | Cria review (autor = usuário do token) | JWT | 201 |
| PUT | `/api/reviews/{id}` | Substitui notas e comentário | JWT (autor) | 200 |
| PATCH | `/api/reviews/{id}` | Altera só o que for enviado | JWT (autor) | 200 |
| DELETE | `/api/reviews/{id}` | Exclui | JWT (autor) | 204 |

## 📦 Exemplos

**Login**

```http
POST /cafes/api/auth/login
Content-Type: application/json

{ "email": "guilherme@email.com", "senha": "123456" }
```

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tipo": "Bearer",
  "expiraEmHoras": 2,
  "usuario": { "id": 1, "nome": "Guilherme Vital", "email": "guilherme@email.com" }
}
```

**Criar café** → `201 Created` + cabeçalho `Location: .../api/cafes/5`

```http
POST /cafes/api/cafes
Authorization: Bearer <token>
Content-Type: application/json

{ "nome": "Café Aroma", "endereco": "Rua A, 1", "bairro": "Jardim", "tipoCafePrincipal": "Cold Brew" }
```

```json
{
  "id": 5,
  "nome": "Café Aroma",
  "endereco": "Rua A, 1",
  "bairro": "Jardim",
  "tipoCafePrincipal": "cold brew",
  "totalReviews": 0
}
```

**Avaliar café**

```http
POST /cafes/api/reviews
Authorization: Bearer <token>
Content-Type: application/json

{ "cafeId": 5, "notaBebida": 5, "notaAmbiente": 4, "notaAtendimento": 3, "comentario": "Ótimo café!" }
```

**Café com médias** — `GET /cafes/api/cafes/1`

```json
{
  "id": 1,
  "nome": "Grao Nobre",
  "endereco": "Rua das Flores, 120",
  "bairro": "Centro",
  "tipoCafePrincipal": "espresso",
  "totalReviews": 2,
  "mediaBebida": 4.5,
  "mediaAmbiente": 4.0,
  "mediaAtendimento": 4.0,
  "mediaGeral": 4.17
}
```

## ⚠️ Respostas de erro

Todos os erros seguem o mesmo formato:

```json
{ "status": 404, "erro": "Cafe 999 nao encontrado." }
```

| Status | Quando acontece |
|---|---|
| 400 | Campo obrigatório faltando, nota fora de 1–5, id não numérico, JSON inválido |
| 401 | Token ausente/inválido/expirado, ou login com e-mail/senha errados |
| 403 | Tentar alterar/excluir review de outro usuário |
| 404 | Café, review ou rota inexistente |
| 405 | Método não suportado na rota (ex.: `DELETE /api/cafes` sem id) |
| 409 | E-mail já cadastrado, review duplicado no mesmo café, excluir café que tem reviews |
| 500 | Erro inesperado (registrado no log do Tomcat) |

## 🧪 Testando

### Swagger UI (recomendado para a apresentação)

Abra `http://127.0.0.1:8081/cafes/`. Todos os endpoints aparecem agrupados (Autenticação, Cafés, Reviews)
com exemplos prontos. Execute **POST /auth/login** e o token é aplicado automaticamente no botão
**Authorize** — depois é só clicar em **Execute** nas rotas protegidas. A especificação fica em
`src/main/webapp/openapi.yaml`.

### Postman

Importe [`docs/postman_collection.json`](docs/postman_collection.json) no Postman. A requisição **Login** salva o
token automaticamente na variável `{{token}}`, usada por todas as rotas protegidas.
O passo a passo completo está em [`docs/roteiro-de-testes.md`](docs/roteiro-de-testes.md).
