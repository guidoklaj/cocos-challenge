# Cocos Challenge — Backend

Kotlin + Spring Boot implementation of the [Cocos Capital backend challenge](https://github.com/cocoscap/cocos-challenge/blob/main/backend-challenge.md).

## Stack

- Kotlin 2.0 / JVM 21
- Spring Boot 3.3 (Web, Data JPA, Validation)
- PostgreSQL (Neon)
- Redis (idempotency)
- Flyway (migrations)

## Running locally

### Prerequisites

- Docker and Docker Compose
- Copy `.env.example` to `.env` and fill in your credentials:

```bash
cp .env.example .env
```

The `.env` is read by Spring Boot via `spring-dotenv` and by Docker Compose for container environment variables.

### With Docker (full stack)

```bash
docker compose up --build
```

Starts the API, a local Postgres and a local Redis. The API will be available at `http://localhost:8080`.

To tear down:

```bash
docker compose down       # keep data
docker compose down -v    # wipe volumes
```

### Without Docker (app only)

Requires a running Postgres and Redis reachable from the values in your `.env`.

```bash
./gradlew bootRun
```

Flyway migrations (schema + seed data) run automatically on startup.

### Running tests

No Docker required — tests use H2 in PostgreSQL compatibility mode.

```bash
./gradlew test

# Single test class
./gradlew test --tests "com.cocos.challenge.SubmitOrderIntegrationTest"
```

## API

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/users/{userId}/portfolio` | Account value, available cash and open positions |
| `GET` | `/instruments?ticker=&name=&type=` | Search instruments (ticker/name are substring, type is exact) |
| `POST` | `/orders` | Submit an order |
| `DELETE` | `/orders/{orderId}` | Cancel a NEW order |

### Order payload

```json
{
  "userId": 1,
  "ticker": "PAMP",
  "side": "BUY",
  "type": "MARKET",
  "size": 10
}
```

- `side`: `BUY` | `SELL` | `CASH_IN` | `CASH_OUT`
- `type`: `MARKET` | `LIMIT`
- Supply either `size` (shares) or `amount` (pesos → floor divided by price), not both
- `price` is required for `LIMIT` orders
- `MARKET` orders fill immediately; `LIMIT` orders are stored as `NEW`
- Orders with insufficient funds or shares are stored as `REJECTED` (HTTP 201)

### Idempotency

Include an `Idempotency-Key` header on `POST /orders` to avoid duplicate submissions. The response is cached in Redis for 24 hours.

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: my-unique-key-123" \
  -d '{"userId":1,"ticker":"PAMP","side":"BUY","type":"MARKET","size":3}'
```

## Architecture

```
api             ← controllers, DTOs, use case interfaces
application     ← use case implementations, repository ports
domain          ← models, business rules — no Spring/JPA dependencies
infrastructure  ← JPA entities, Spring Data repos, repository implementations
```

Business rules (funding checks, status transitions, position math) live entirely in the domain layer. Use cases orchestrate domain objects and delegate persistence to the infrastructure layer.
