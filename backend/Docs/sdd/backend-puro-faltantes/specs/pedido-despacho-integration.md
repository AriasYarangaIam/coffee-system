# Pedido Despacho Integration Specification

## Purpose

Backend-puro despacho integration of REQ-PQ-03: consume the merged `tad/Cola` + `tad/ColaPrioridad` TAD from `iam-sprint-2-bundle`, maintain a resident despacho queue, eager-reseed on boot, enqueue on `registrarPedido`, and expose a service-layer read API. Pure FIFO (default priority 0 only); no HTTP surface; no DDL; no frontend edits. The TAD type itself is upstream's contract — this spec MUST NOT redefine or vendor it.

## Requirements

### Requirement: REQ-PDI-01 — Resident despacho TAD

The system SHALL maintain a single resident `ColaPrioridad<PedidoDespachoToken>` (or equivalent despacho token type) in the application context, owned by a Spring `@Component`. The TAD (`tad/Cola` + `tad/ColaPrioridad`) is provided by the merged `iam-sprint-2-bundle`; this change MUST NOT redefine or vendor it.

#### Scenario: Single resident instance

- GIVEN the application context is running
- WHEN any component asks for the despacho TAD
- THEN the same `ColaPrioridad` instance is returned across all callers
- AND no second instance is created

### Requirement: REQ-PDI-02 — Eager reseed on boot

The system SHALL reseed the resident TAD at application start via a Spring `ApplicationRunner` that calls `pedidoRepository.findAllByOrderByFechaPedidoAsc()` and enqueues each row (as a despacho token) via the single-arg `enqueue(item)` (default priority 0). The runner MUST NOT call the two-arg `enqueue(item, prioridad)` overload for any row.

#### Scenario: Empty database reseed

- GIVEN `pedidos` has zero rows at startup
- WHEN the `ApplicationRunner` executes
- THEN the resident TAD is empty
- AND no despacho token is enqueued

#### Scenario: N persisted rows reseeded in FIFO order

- GIVEN `pedidos` has rows P1 (fechaPedido 12:00), P2 (12:05), P3 (12:10)
- WHEN the `ApplicationRunner` executes
- THEN the resident TAD contains [P1, P2, P3] strictly by `fechaPedido` ASC
- AND each token was enqueued via single-arg `enqueue(item)` (priority 0)

### Requirement: REQ-PDI-03 — Enqueue on pedido registration

After `pedidosRepository.save(pedido)` completes in `PedidoServiceImpl.registrarPedido`, the system MUST enqueue the new pedido's despacho token into the resident TAD via single-arg `enqueue(item)` (default priority 0, pure FIFO). The two-arg `enqueue(item, prioridad)` overload MUST NOT be invoked.

#### Scenario: New pedido appended to queue tail

- GIVEN the resident TAD contains [P1, P2]
- WHEN a new pedido P3 is registered and `pedidosRepository.save` returns
- THEN P3's despacho token is enqueued at the tail
- AND the queue becomes [P1, P2, P3] in FIFO order

### Requirement: REQ-PDI-04 — Service-layer despacho API

`PedidoService` SHALL expose `listarDespacho()` returning the current in-memory TAD contents in FIFO order, and `siguienteDespacho()` returning the head token WITHOUT removing it (peek semantics, default recommendation). NO HTTP controller, NO `@RestController`, NO `@GetMapping` for despacho in this change; HTTP surface is deferred to a future front-led change.

> Open Question for sdd-design: confirm peek-vs-dequeue semantics for `siguienteDespacho` (default = peek; require explicit dequeue via a separate method to preserve the audit trail of pending despachos). Also: despacho token shape (id-only vs full `PedidoListadoResponseDTO` projection); whether invoicement via `obtenerBoleta` should dequeue (default = NO, this method does not mutate state today).

#### Scenario: listarDespacho returns ordered snapshot

- GIVEN the resident TAD contains [P1, P2, P3] in FIFO order
- WHEN `listarDespacho()` is called
- THEN [P1, P2, P3] is returned in that order
- AND the TAD contents are unchanged

#### Scenario: siguienteDespacho peeks without removal

- GIVEN the resident TAD contains [P1, P2]
- WHEN `siguienteDespacho()` is called
- THEN P1 (the head) is returned
- AND the TAD still contains [P1, P2]

#### Scenario: siguienteDespacho on empty TAD

- GIVEN the resident TAD is empty
- WHEN `siguienteDespacho()` is called
- THEN an empty `Optional` is returned
- AND no exception is raised

### Requirement: REQ-PDI-05 — Pure-FIFO guarantee

Until a priority business rule is product-approved (OUT of scope here), the resident TAD SHALL produce strict FIFO ordering by `fechaPedido` ASC. The priority banding capability of `ColaPrioridad` is unused but retained for forward-compat.

#### Scenario: FIFO preserved across mixed reseed + registration

- GIVEN reseed populated the TAD with [P1, P2] and a later `registrarPedido` added P3
- WHEN `listarDespacho()` is called
- THEN the returned order is [P1, P2, P3] strictly by `fechaPedido` ASC
- AND no priority reordering is observed

### Requirement: REQ-PDI-06 — Hermetic integration test

The system MUST include `PedidoServiceImplDespachoTest` (hermetic — REUSE the `application-test.yaml` profile + Spring slice pattern from the merged `iam-sprint-2-bundle`). The test MUST NOT mount live Supabase and MUST NOT rely on or modify `BackendApplicationTests.contextLoads` (which stays untouched).

#### Scenario: Despacho lifecycle coverage

- GIVEN the hermetic `application-test.yaml` profile is active
- WHEN the despacho integration test runs
- THEN it asserts: empty-TAD at startup before reseed, eager reseed from N persisted rows, `registrarPedido` enqueues the new token, `siguienteDespacho` returns the FIFO head, and `listarDespacho` returns contents in FIFO order
- AND no live Supabase connection is required

### Requirement: REQ-PDI-07 — Single-instance assumption

The resident in-memory TAD holds despacho state for the current single backend instance ONLY. Multi-instance deploy correctness is OUT of scope.

> Open Question for sdd-design (future change): cross-instance TAD coordination (shared store vs sticky routing vs externalization).

#### Scenario: TAD not shared across instances

- GIVEN two backend instances running with independent resident TADs
- WHEN instance A enqueues a despacho token
- THEN instance B's resident TAD does NOT contain that token
- AND the system documentation notes this single-instance constraint