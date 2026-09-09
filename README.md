# Cocos Challenge — Backend

Kotlin + Spring Boot implementation of the [Cocos Capital backend challenge](https://github.com/cocoscap/cocos-challenge/blob/main/backend-challenge.md).

## Stack

- Kotlin 2.0 / JVM 21
- Spring Boot 3.3 (Web + Data JPA + Validation)
- PostgreSQL 16 (Docker Compose for local dev)
- Flyway for schema migrations and seed data
- H2 in PostgreSQL compatibility mode for the integration test suite

## Running locally

```bash
docker compose up --build
```

- API available on `http://localhost:8080`
- Postgres exposed on `localhost:5432` (user/password/db all `cocos`)

Flyway runs the migrations on boot and seeds two users (Alice `id=1`, Bob `id=2`), four instruments (`DYCA`, `GGAL`, `PAMP`, `YPFD`), the `ARS` cash instrument, market data and initial cash + one GGAL position for Alice.

Tearing down:

```bash
docker compose down          # keep data
docker compose down -v       # wipe the volume
```

## Running tests

```bash
./gradlew test
```

Tests run against H2 in PostgreSQL compatibility mode — no Docker required.

## Endpoints

| Method | Path | Description |
| --- | --- | --- |
| `GET`  | `/users/{userId}/portfolio` | Total account value, available cash and held positions with market value + total return % |
| `GET`  | `/instruments?ticker=&name=&type=` | List instruments filtered by any combination of optional query params (ticker/name are case-insensitive substring, type is an exact enum match) |
| `POST` | `/orders` | Submit BUY / SELL / CASH_IN / CASH_OUT — MARKET or LIMIT |
| `DELETE` | `/orders/{orderId}` | Cancel an order in `NEW` status |

### Order payload

```json
{
  "userId": 1,
  "ticker": "PAMP",          // OR "instrumentId": 4
  "side": "BUY",              // BUY | SELL | CASH_IN | CASH_OUT
  "type": "MARKET",           // MARKET | LIMIT
  "size": 10,                 // exact share count, OR
  "amount": 5000,             // peso amount → floor(amount / price) shares
  "price": 103.5              // required for LIMIT, ignored otherwise
}
```

- `MARKET` orders use the latest `close` from `marketdata` as the fill price and settle as `FILLED` immediately.
- `LIMIT` orders keep the user-supplied `price` and are stored as `NEW`.
- `CASH_IN` and `CASH_OUT` operate on the `ARS` instrument and are always `MARKET` / `FILLED`.
- Only one of `size` or `amount` may be supplied.

## Architecture

```
api            ← HTTP layer + use case interfaces + Request/Response DTOs
application    ← Use case implementations (Spring @Service) + repository interfaces (ports)
domain         ← Models, enums, business rules — no Spring, no JPA
infrastructure ← JPA entities + Spring Data repos + application repository implementations
```

- **API** exposes controllers and defines one use case **interface** per feature (`GetPortfolioUseCase`, `SearchInstrumentsUseCase`, `SubmitOrderUseCase`, `CancelOrderUseCase`). Controllers depend only on those interfaces.
- **Application** contains the use case **implementations** (`*UseCaseImpl`) and the repository **interfaces** (ports). Implementations return the API `Response` types directly, so controllers stay thin. All input validation happens in the request (Bean Validation) — the use case receives clean, typed data.
- **Domain** owns the invariants:
  - `Order.create(...)` is the domain factory that decides the initial status (`FILLED` / `NEW` / `REJECTED`) from the user's order history and the balance rules. The use case doesn't decide — it just hands the inputs to the domain.
  - `Order.isCancellable` and `Order.cancelled()` live on `Order`, keeping status transitions cohesive with the entity.
  - `BalanceCalculator` computes available cash, available shares, held quantity and average buy price from an order list.
  - `PositionBuilder(instrument, orders, marketData).build()` returns the `Position` for an instrument (or `null` for cash / zero-quantity holdings) — keeping the "what is a position" rule in the domain instead of the use case.
  - Enums (`OrderSide`, `OrderStatus`, `InstrumentType`) expose their traits as plain properties (e.g. `OrderSide.CASH_IN.isCashMovement`, `InstrumentType.MONEDA.isCash`).
- **Infrastructure/database** provides Spring Data JPA repos over JPA `*Entity` classes and implements the application repository interfaces. Each `*Entity` maps itself to/from the domain type (`toDomain()`, `fromDomain()`), so the database layer speaks only in domain types outward. Dynamic filtering (instrument search) uses the JPA Criteria API via `JpaSpecificationExecutor`.

### Business rules (in `BalanceCalculator`)

- `availableCash = Σ(FILLED cash-flow orders) − Σ(NEW BUY orders)`. NEW buys reserve funds so they cannot be spent twice.
- `availableShares(instrument) = FILLED buys − FILLED sells − NEW sells`. NEW sells reserve inventory.
- `heldQuantity(instrument)` uses FILLED orders only (accounting quantity for market-value math).
- `averageBuyPrice(instrument)` is the size-weighted average of FILLED BUY prices; used for `totalReturnPercentage = (currentPrice − avgBuyPrice) / avgBuyPrice`.

### Order submission flow (`SubmitOrderUseCaseImpl`)

1. Bean Validation on `SubmitOrderRequest` rejects malformed input **before** the use case runs: required fields, positive numbers, `size` XOR `amount`, `price` for LIMIT, and enum membership for `side` / `type`. The use case never has to `parse`.
2. Resolve the user (or 404).
3. Resolve the instrument (by `instrumentId` or `ticker`; cash sides resolve to the configured `cocos.cash-instrument-ticker`, default `ARS`). Reject BUY/SELL against a MONEDA instrument.
4. Resolve the price: `MARKET` reads the latest `marketdata.close`; `LIMIT` uses the request price; cash flows use `1.00`.
5. Resolve the size: either the supplied `size` or `floor(amount / price)` — if that rounds to `0`, reject.
6. Hand everything to `Order.create(...)`, which loads the user's order history and returns an `Order` with the correct status (`FILLED` / `NEW` / `REJECTED`). Status computation is domain logic, not use-case logic.
7. Persist and return the resulting `Order` as an `OrderResponse` (HTTP 201).

## Design decisions and assumptions

- **Modelling cash as orders.** The `ARS` MONEDA instrument holds all cash movements. `CASH_IN` and `CASH_OUT` are `MARKET` orders on `ARS` with `price = 1.00`. Buys / sells adjust cash implicitly through `BalanceCalculator`.
- **Reservations for NEW orders.** A NEW LIMIT BUY reserves cash and a NEW LIMIT SELL reserves shares in the availability calculation, mirroring real-broker semantics. Cancelling releases the reservation.
- **Rejected orders are persisted.** Rejection is a domain outcome, not an HTTP error — the endpoint returns `201 Created` with `status: "REJECTED"`. Client-side input errors (missing fields, unknown sides) still return `4xx`.
- **`totalReturnPercentage` uses cost basis.** Computed as `(currentPrice − weightedAvgBuyPrice) / weightedAvgBuyPrice × 100`. This is the unrealized return since the position was opened; daily performance (using `previousClose`) is not exposed in this endpoint but is available in the `marketdata` table.
- **Ports vs. adapters.** The application layer declares repository interfaces (ports); the infrastructure layer implements them. The domain has no dependency on either — it's pure.
- **Exceptions are segmented by concern and layer.** Two generic exceptions cover the whole app: `application.exception.NotFoundException` for missing resources (mapped to `404`) and `domain.exception.ValidationException` for domain-rule violations (mapped to `422`). Use cases throw them with a specific message; the exception handler doesn't need to know about individual cases. Bean Validation and JSON parsing errors are handled separately as `400`.
- **Domain entities inside the DB layer.** `*Entity` classes expose `toDomain()` / `fromDomain()` and the domain repository implementations only ever hand domain types back up — there is no separate persistence mapper class.
- **Schema follows the challenge.** I added indexes on `orders (user_id)` and `(user_id, instrument_id, status)` to keep the balance calculation cheap as the order table grows, and `(instrument_id, date DESC)` on `marketdata` for latest-close lookups. Columns match the challenge names except `previousClose` is stored as `previous_close` (snake_case per Postgres convention).
- **Instrument search is filter-style.** `GET /instruments` accepts any combination of `ticker`, `name` and `type` as optional query params. Missing params are ignored. Ticker/name are case-insensitive substring matches; `type` is an exact enum match. Trigram / full-text search would be the natural upgrade for a real dataset.
- **No auth.** Per the challenge; `userId` comes from the URL / request body.
- **Concurrency.** For this scope I rely on the JPA transaction to serialize a single user's balance check + insert. Under real concurrency the balance check + insert should be `SELECT … FOR UPDATE` on a row that represents the user's cash position, or a compare-and-swap on an aggregate.

## Sample requests

```bash
# Search (filter-style — any combination of ticker/name/type)
curl "http://localhost:8080/instruments?ticker=gal"
curl "http://localhost:8080/instruments?type=ACCIONES"
curl "http://localhost:8080/instruments?ticker=ypf&type=ACCIONES"

# Portfolio
curl "http://localhost:8080/users/1/portfolio"

# Market buy
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"ticker":"PAMP","side":"BUY","type":"MARKET","size":3}'

# Limit sell
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"ticker":"GGAL","side":"SELL","type":"LIMIT","size":5,"price":300}'

# Cash in
curl -X POST "http://localhost:8080/orders" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"side":"CASH_IN","type":"MARKET","size":50000}'

# Cancel
curl -X DELETE "http://localhost:8080/orders/5"
```
