# Decision — Transaction Idempotency

## Context

Transaction creation is a request/response API under retries, duplicates, concurrent submissions, and future async processing. A retry must not create duplicate records. Reuse of a key with a different payload must be rejected.

---

## Decision

`Idempotency-Key` (client-supplied) is the request identity, uniqueness enforced in PostgreSQL.

```text
same key + same payload      → return existing transaction
same key + different payload → 409 Conflict
new key                      → create transaction
concurrent same key          → exactly one persisted, both resolve to it
```

DB uniqueness constraint is the authoritative concurrency mechanism.

---

## Persistence Invariant

```text
∀ idempotency_key:
    count(transactions where key = idempotency_key) ≤ 1
```

Application-level lookup alone can't prevent this — two concurrent requests can both see the key as absent before either commits. The unique constraint closes that race.

---

## Payload Consistency

Key is bound to the original payload. Compared fields:

```text
amount
currency
merchantId
```

Match → existing transaction. Mismatch → `409 Conflict` / `IDEMPOTENCY_CONFLICT`. Original transaction is never mutated.

---

## Concurrent Requests

```text
Request A → lookup → absent ┐
Request B → lookup → absent ┘
              concurrent insert
               /          \
          success     unique violation → resolve existing
```

No dependence on JVM locks, synchronized sections, in-memory state, request ordering, or cache-as-source-of-truth. Holds across multiple service instances.

---

## Failure Semantics

Duplicate-key persistence failure is treated as a concurrency outcome: fetch the persisted transaction, apply the same payload check. If no transaction can be resolved, the original exception propagates — no manufactured result.

---

## Consequences

**Positive:** retries are naturally idempotent; concurrency protected by DB invariant; multi-instance safe with no shared JVM state; no Redis dependency for correctness; directly testable.

**Trade-offs:** every creation does a lookup before insert; DB stays on the critical path; key is persisted with the record, not ephemeral; future caching must preserve PostgreSQL as authority.

---

## Verification

Integration tests against PostgreSQL, including concurrent same-key requests (confirms single persisted transaction) and API tests for the 409 conflict contract.