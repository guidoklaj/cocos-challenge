# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run all tests (H2 in-memory, no Docker required)
./gradlew test

# Run a single test class
./gradlew test --tests "com.cocos.challenge.SubmitOrderIntegrationTest"

# Run a single test method
./gradlew test --tests "com.cocos.challenge.SubmitOrderIntegrationTest.market buy order for a stock fills immediately at the latest close"

# Build the JAR
./gradlew build

# Run locally (requires Docker)
docker compose up --build

# Wipe the Postgres volume
docker compose down -v
```

Local API is on `http://localhost:8080`. Postgres on `localhost:5432` (user/password/db all `cocos`).

## Architecture

Four layers, no cross-layer leakage:

```
api             ← controllers, request/response DTOs, use case interfaces
application     ← use case @Service implementations, repository port interfaces, exceptions
domain          ← pure Kotlin models, enums, domain logic — zero Spring/JPA dependencies
infrastructure  ← JPA entities, Spring Data repos, repository implementations
```

**Dependency direction:** `api → application → domain`; `infrastructure → application`. Domain has no outbound dependencies.

### Key conventions

- **Use case interfaces** (`GetPortfolioUseCase`, `SubmitOrderUseCase`, etc.) live in `api/usecase`. Controllers depend only on those interfaces.
- **Use case implementations** (`*UseCaseImpl`) live in `application/usecase` and are annotated `@Service`. They return API response types directly — controllers stay thin.
- **Repository ports** are interfaces in `application/repository`. The infrastructure layer provides the implementations.
- **JPA entities** map themselves via `toDomain()` / `fromDomain()` — there are no separate mapper classes.
- **Exceptions** are segmented by concern: `application.exception.NotFoundException` → HTTP 404; `domain.exception.ValidationException` → HTTP 422. Bean Validation / JSON parse errors are handled separately as 400.

### Domain model

- `User` holds `availableCash` (pre-computed balance, not derived from order history). `User.canAfford(amount)` and `User.applyOrder(order)` mutate this field.
- `UserHolding` (also in domain) tracks `availableShares`, `heldShares`, and `totalBuyCost` per `(userId, instrumentId)`. `availableShares` ≠ `heldShares` when there are NEW SELL orders reserving inventory.
- `Order.create(...)` is the **domain factory** that decides the initial status (`FILLED` / `NEW` / `REJECTED`) — the use case does not decide status, it just passes `funded: Boolean` to the factory.
- `PositionBuilder(instrument, holding, marketData).build()` produces a `Position` (or `null` for cash / zero-held instruments).
- Cash is modelled as orders on the `ARS` MONEDA instrument (`price = 1.00`). Configurable via `cocos.cash-instrument-ticker` (default `ARS`).
- NEW LIMIT BUY orders reserve cash in `User.availableCash`; NEW LIMIT SELL orders reserve shares in `UserHolding.availableShares`.

### Concurrency

`SubmitOrderUseCaseImpl` calls `userRepository.findByIdForUpdate(userId)` to acquire a row-level write lock before reading and writing the user's balance, serializing concurrent submissions for the same user.

### Tests

Integration tests use `@SpringBootTest` + `MockMvc` against H2 (PostgreSQL compatibility mode). Seed data is applied via Flyway on startup. No Docker needed for the test suite.
