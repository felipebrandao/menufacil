# MenuFácil (API)

API REST para cadastro e planejamento de refeições (receitas), com agenda (schedule) e geração de lista de compras.

- Linguagem: **Java**
- Framework: **Spring Boot**
- Persistência: **MongoDB** (Spring Data MongoDB)
- Build: **Maven**

## Como rodar

### Pré-requisitos

- Java (compatível com o projeto via Maven Wrapper `mvnw`)
- MongoDB acessível via URI

### Configuração

A aplicação lê as configurações em `src/main/resources/application.properties`:

- `server.port=${PORT:8080}`
- `spring.data.mongodb.uri=${MONGODB_URI}`
- `spring.data.mongodb.auto-index-creation=true`

Defina as variáveis de ambiente:

- `MONGODB_URI` (**obrigatória**) — URI do MongoDB
- `PORT` (opcional) — porta da aplicação (padrão: `8080`)

### Executar (Maven Wrapper)

No Windows:

```powershell
cd C:\fiap\menufacil
.\mvnw.cmd spring-boot:run
```

A API sobe, por padrão, em:

- `http://localhost:8080`

## Endpoints

Base path: `/api`

### Categorias de ingredientes

`/api/categories-ingredient`

- `GET /api/categories-ingredient` — lista categorias
- `POST /api/categories-ingredient` — cria categoria
- `PUT /api/categories-ingredient/{id}` — atualiza categoria
- `DELETE /api/categories-ingredient/{id}` — remove categoria

### Unidades

`/api/units`

- `GET /api/units` — lista unidades
- `POST /api/units` — cria unidade
- `PUT /api/units/{id}` — atualiza unidade
- `DELETE /api/units/{id}` — remove unidade

### Ingredientes

`/api/ingredients`

- `GET /api/ingredients` — lista ingredientes
  - Query params opcionais:
    - `query` (texto)
    - `categoryId` (id de categoria)
    - `limit` (padrão: 5)
  - Observação: se `query` e `categoryId` não forem informados, retorna `ingredientService.list()`.

- `GET /api/ingredients/search?query=...&limit=10` — autocomplete

- `GET /api/ingredients/{id}` — busca por id

- `POST /api/ingredients` — cria ingrediente (`IngredientRequest`)

- `PUT /api/ingredients/{id}` — atualiza ingrediente (`IngredientRequest`)

- `DELETE /api/ingredients/{id}` — remove ingrediente

### Categorias de receita

`/api/recipe-categories`

- `GET /api/recipe-categories` — lista categorias
- `POST /api/recipe-categories` — cria categoria
- `PUT /api/recipe-categories/{id}` — atualiza categoria
- `DELETE /api/recipe-categories/{id}` — remove categoria

### Receitas

`/api/recipes`

- `POST /api/recipes` — cria receita (`CreateRecipeRequest`)
  - Retorna `201` com `Location: /api/recipes/{id}`

- `GET /api/recipes` — lista receitas (paginado)
  - Query params:
    - `query` (opcional)
    - `category` (opcional)
    - `page` (padrão: 1)
    - `limit` (padrão: 8)

- `GET /api/recipes/{id}` — busca por id
- `PUT /api/recipes/{id}` — atualiza (`UpdateRecipeRequest`)
- `DELETE /api/recipes/{id}` — remove

### Agenda (Schedule)

`/api/schedule`

- `GET /api/schedule?view=weekly&start=YYYY-MM-DD` — lista agenda por visão
- `GET /api/schedule?view=monthly&year=YYYY&month=MM` — lista agenda por mês

- `GET /api/schedule/{date}` — busca dia (date em ISO `YYYY-MM-DD`)

- `POST /api/schedule/{date}/recipes` — adiciona receita no dia (`CreateScheduleRecipeRequest`)

- `PATCH /api/schedule/{date}/reorder` — reordena receitas do dia (`ReorderRequest`)

- `DELETE /api/schedule/scheduled/{scheduledId}` — remove um item agendado

### Lista de compras

`/api/shopping-list`

- `GET /api/shopping-list?view=weekly&start=YYYY-MM-DD`
- `GET /api/shopping-list?view=monthly&year=YYYY&month=MM`

Retorna uma lista consolidada a partir do que estiver agendado no período.

## Regras de negócio (resumo do que está implementado)

- Ingredientes têm:
  - categoria (`CategoryIngredient`) e unidade padrão (`UnitType`)
  - conversões (`UnitConversion`) para transformar quantidade usada (unidade selecionada) em quantidade na unidade padrão
- Receitas calculam/armazenam `quantityInDefaultUnit` para facilitar consolidação em lista de compras.
- A lista de compras consolida ingredientes por categoria e por ingrediente, somando quantidades na unidade padrão.


