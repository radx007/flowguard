# Sprint 1 Architecture — Transaction Service

## Scope

Synchronous transaction-ingestion boundary + persistence guarantees. Accepts creation requests, enforces idempotency, persists to PostgreSQL, returns state. No coupling to downstream processing.

---

## Runtime Flow

```text
Client → POST /transactions (Idempotency-Key)
  → validation → idempotency resolution → creation
  → PostgreSQL (transactions)
```

Transaction service is system of record for creation. Success → `PROCESSING` state. Further transitions deferred.

---

## Structure

```text
com.flowguard.transaction
├── api
├── application
├── domain
└── infrastructure
```

```text
api → application → domain
infrastructure → application / domain
```

Domain independent of Spring/persistence. JPA entity + repository handle persistence; application service orchestrates.

---

## Transaction Model

```text
Transaction
├── id
├── idempotencyKey
├── amount
├── currency
├── merchantId
├── status
└── createdAt
```

Only state: `PROCESSING`.

---

## Persistence

PostgreSQL is authoritative. Schema via Flyway; Hibernate validates only.

```text
UNIQUE(idempotency_key)
```

This is the final concurrency boundary, not an optimization.

---

## Idempotency Resolution

```text
key → existing?
        yes → payload matches? → yes: return existing | no: 409
        no  → insert → result
```

DB uniqueness constraint resolves the race where two requests both see the key as absent. Duplicate-key violation → fetch winning row → same payload check. Correctness is independent of request ordering.

---

## Request Semantics

| Condition | Result |
|---|---|
| Key absent | Create |
| Key exists, same payload | Return existing |
| Key exists, different payload | `409 Conflict` |
| Concurrent same key | One persisted, other resolves to it |

---
