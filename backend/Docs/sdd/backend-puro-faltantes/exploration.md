# Exploration: backend-puro-faltantes (Sprint 2 backend-puro residual)

SDD explore artifact for change `backend-puro-faltantes` (coffee-system).
Scope: exactly TWO items from `backend/Docs/08_01_sprints_backend_puro_faltantes.md`
(Item 2 — FIFO despacho TAD; Item 3 — `UsuarioResponseDTO` mapper consistency).
Item 1 (role-access / `@PreAuthorize` on catálogos / `GET /api/productos`→ADMIN) is
OUT of scope (owned by `endpoints-admin-sprint-2`). Task 13/14 (contract tests +
BigDecimal / error handler) is OUT of scope (owned by `iam-sprint-2-bundle`).
Dashboard / reportes mensual also OUT (owned by `endpoints-admin-sprint-2`). No
PostgreSQL persistence changes — backend-puro only.

Artifact store: hybrid → this file + Engram topic `sdd/backend-puro-faltantes/explore`.

---

## Scope Revision (locked by orchestrator 2026-06-27)

> **OVERRIDES** the original "Recommendation" and "Risks #1" below, which were written
> before reading `iam-sprint-2-bundle`'s design (Engram obs #9) in full.

The `iam-sprint-2-bundle` design (Decision 3 — Q3 CORRECTED resolution B-(ii)) **already**
shrinks its `pedido-queue` capability to **TAD-only, hermetic unit-tested in isolation**
in that change, and **explicitly escalates REQ-PQ-03 (despacho integration) to a follow-up
change** — section "Despacho follow-up required (escalated to orchestrator)". This change
IS that follow-up.

**Locked scope:**

- **Item 2 — Despacho INTEGRATION only (NOT the TAD).** This change consumes the `tad/Cola`
  + `tad/ColaPrioridad` package shipped by `iam-sprint-2-bundle`. Work in this change:
  - `PedidoRepository.findAllByOrderByFechaPedidoAsc()` (or equivalent ordered query) to
    rehydrate the resident TAD from persisted rows on app start (`@PostConstruct` or
    `ApplicationRunner`), since `Pedidos` has no `estado`/`prioridad` column (in-memory
    despacho state derived from existing rows — remains backend-puro / no DDL).
  - `registrarPedido` enqueues a despacho token after `pedidosRepository.save`.
  - New despacho-facing service method(s) (e.g. `listarDespacho()` / `siguienteDespacho()`)
    and an optional read-only `GET /api/pedidos/despacho` (or admin-scoped) endpoint —
    protocol (polling vs WebSocket) stays UNDECIDED.
  - Integration test `PedidoServiceImplDespachoTest` (the test the iam design explicitly
    said would be "authored in the follow-up despacho-integration change").
  - Priority SOURCE must be defined in spec: default = no priority (pure FIFO), or a
    derived business rule (no persisted `prioridad` column allowed).
  - **TAD creation (`Cola.java` / `ColaPrioridad.java` / `ColaPrioridadTest`) is OUT of
    scope for this change** — owned by `iam-sprint-2-bundle`.
- **Item 3 — `UsuarioMapper` extraction + MockMvc regression + `08_01` doc correction.**
  The backend code is already correct (verified); this change extracts a dedicated
  `mapper/UsuarioMapper` mirroring `mapper/ProductoMapper`, adds a `@WebMvcTest` regression
  asserting `usuarioId` + `rol` shape on `GET /api/admin/usuarios`, and corrects the stale
  Item 3 text in `08_01_sprints_backend_puro_faltantes.md` to ✅ DONE. Does NOT split
  `LoginResponseDTO.nombreCompleto` (breaks the live front login contract → exits
  backend-puro).

**Sequencing prerequisite:** `iam-sprint-2-bundle` must merge BEFORE this change's `sdd-apply`
(the TAD package must exist). This change's `sdd-tasks` MUST annotate the despacho-task as
`depends-on: iam-sprint-2-bundle merged`.

**Cross-change coverage map (authoritative):**
| Capability | Owner change |
|---|---|
| pedidos-contract (detalle, ISO 8601, message body for 3 existing) | iam-sprint-2-bundle |
| error-handling (B-13: 400/401/403/500) | iam-sprint-2-bundle |
| pedido-queue (TAD-only: Cola, ColaPrioridad, ColaPrioridadTest) | iam-sprint-2-bundle |
| backend-contract-tests (@WebMvcTest + hermetic profile) | iam-sprint-2-bundle |
| monetary-precision-runbook (B-18 doc-only) | iam-sprint-2-bundle |
| **pedido-despacho-integration (REQ-PQ-03 — consume the TAD, rehydrate, enqueue, dequeue API)** | **backend-puro-faltantes (this change)** |
| **usuario-mapper-refactor (extract UsuarioMapper + MockMvc regression + 08_01 doc fix)** | **backend-puro-faltantes (this change)** |
| admin endpoints (dashboard, reportes mensual, productos→ADMIN) | endpoints-admin-sprint-2 |
| role-access / @PreAuthorize mapping for catálogos / `GET /api/productos`→ADMIN | endpoints-admin-sprint-2 |

**Item 2 ACID caveats inherited from the original exploration that still hold:**
no frontend consumer exists for despacho → keep this change to TAD-consumer + service-layer
+ optional read-only endpoint; defer bidirectional protocol. `Pedidos` has no
`estado`/`prioridad` column → priority remains in-memory rule only.

---

## Current State

### Item 2 — FIFO despacho (RF-DS-04)
- **There is no despacho concept in the backend today.** `PedidoServiceImpl.registrarPedido`
  (`backend/src/main/java/com/coffee/backend/service/implement/PedidoServiceImpl.java:37-115`)
  validates stock → builds `Pedidos` in memory → persists (`pedidosRepository.save`). No
  in-memory queue, no order-of-atención tracking, no priority.
- `PedidoService` / `PedidoController` expose only: `registrarPedido`, `listarPedidosDeMesero`,
  `obtenerBoleta`. Nothing models "despacho" or subsequent attention order.
- `Pedidos` entity (`entity/Pedidos.java:15-30`) has NO `estado` / `prioridad` / `ordenDespacho`
  column — only `pedidoId`, `aliasTicket`, `fechaPedido`, `usuario`, `detalles`. Adding any
  persisted dispatch field would be a schema change → OUT of scope (backend-puro). The TAD
  must be **in-memory only**, derived from already-persisted rows.
- Frontend has NO consumer of any despacho / cola / queue / FIFO surface — confirmed via
  `grep` of `frontend/` for `despacho|cola|queue|fifo|enqueue|dequeue|Prioridad` → 0 hits.
  The active change `endpoints-admin-sprint-2` explore (Engram obs #4) already flagged:
  "Cola (RF-DS-04) has NO frontend consumer → ambiguous API surface; recommend a separate
  change." This is that separate change.
- No tests exist beyond the smoke `BackendApplicationTests.contextLoads`
  (`src/test/java/com/coffee/backend/BackendApplicationTests.java`). Strict TDD is active
  for backend — every code scope here must be test→impl.

### Item 3 — `UsuarioResponseDTO` mapper consistency (B-06 / B-F-07 residual)
The doc `08_01_sprints_backend_puro_faltantes.md` Item 3 (lines 17-21) claims the backend
produces a "manual concatenación defectuosa (rigid space separator)" and "campos erróneos
in `usuarioId` and `rol`". **Verifying the actual code shows this is STALE — the Sprint 1
Task 7 work (B-06) is ALREADY DONE correctly in the backend:**

- `dto/response/UsuarioResponseDTO.java:4-11` — clean record:
  `Long usuarioId, String nombreUsuario, String apellidoUsuario, String correoUsuario,
  String telefonoUsuario, String rol`. No concatenation, `usuarioId` typed `Long`
  (matches `Usuarios.usuarioId` @Id IDENTITY), `rol` typed `String`.
- `service/implement/UsuarioServiceImpl.java:43-53` (`obtenerUsuarios`) — mapper is INLINE
  (no dedicated `UsuarioMapper` class; only `mapper/ProductoMapper.java` exists). Maps
  `u.getUsuarioId()` → `usuarioId` and `u.getRoles().getNombreRol()` → `rol`. No space
  concatenation, no malformed fields.
- `controller/UsuarioController.java:31-34` — `GET /api/admin/usuarios` returns
  `List<UsuarioResponseDTO>` straight from the service. No transformation.
- `MEMORY.md:37` confirms B-06 ✅ and "UsuarioResponseDTO trae `usuarioId` y `rol`".

**Where the "rigid space separation" defect ACTUALLY lives** (NOT this change's territory,
but flagged for clarity): the frontend `frontend/js/pages/usuarios.js` lines 73, 95-96 —
editing concatenates `nombre + " " + apellido` into a single input, and saving splits by
`" "` (`partes[0]` → nombre, `partes.slice(1).join(" ")` → apellido), which corrupts
compound names like "Maria José". The backend never does this; the backend DTO exposes
the two fields separately and correctly. **No backend refactor is needed to fix the
front's split-by-space bug.**

The only residual Java-side "space concatenation" tied to a user-facing response is
`AuthServiceImpl.iniciarSesion` line 40: `usuario.getNombreUsuario() + " " +
usuario.getApellidoUsuario()` → `LoginResponseDTO.nombreCompleto`. This is a single display
string intended for the `localStorage.usuario` blob the front reads as `nombreCompleto`
(`frontend/js/pages/login.js:49-50`). It is NOT broken (the front treats it as opaque
display text), but it IS the only "rigid space separator" pattern left in the backend —
worth noting because the 08_01 description almost certainly conflates this with the
`UsuarioResponseDTO` confusion.

**Net for Item 3:** the backend code is already consistent with the contract the front
expects. Remaining residual work is documentary / regression-guard only: (a) update
`08_01_sprints_backend_puro_faltantes.md` Item 3 to mark it ✅ DONE, (b) optionally add a
`@WebMvcTest` regression asserting `usuarioId` + `rol` shape on `GET /api/admin/usuarios`
so a future refactor cannot silently drop them, and (c) optionally split
`LoginResponseDTO.nombreCompleto` into `nombre` + `apellido` so the login path stops
carrying the space-concat tradeoff. None of these are strictly required for the
"backend-puro residual" promise.

---

## Affected Areas

### Item 2 (FIFO TAD)
- `backend/src/main/java/com/coffee/backend/service/implement/PedidoServiceImpl.java:37-115` —
  `registrarPedido` must enqueue an in-memory despacho token after `pedidosRepository.save`
  (or the TAD must be populated on demand from existing rows). Decision deferred to spec.
- `backend/src/main/java/com/coffee/backend/service/PedidoService.java:10-14` — add a
  despacho-facing method (e.g. `List<PedidoDespachoDTO> listarDespacho()` / `Long
  siguienteDespacho()`). API shape is open; protocol (polling vs WebSocket) is OUT of
  scope per orchestrator — only in-memory behavior.
- `backend/src/main/java/com/coffee/backend/controller/PedidoController.java:22-47` —
  candidate place for a new `GET /api/pedidos/despacho` (or admin-scoped) endpoint,
  OR a new `DespachoController`. Decision deferred to spec/design; protocol stays open.
- `backend/src/main/java/com/coffee/backend/tad/` — NEW package for the TAD propio
  (`ColaDespacho<T>` / `ColaPrioridad<T>`). Sílabo forbids using `java.util` directly for
  the structure (per `02_requirements.md:66`); a custom singly-linked-list queue with
  priority insertion is the expected academic artifact.
- `backend/src/test/java/com/coffee/backend/...` — NEW unit tests for the TAD
  (pure JUnit, no Spring) + service-layer integration tests. Strict TDD: tests first.
- `backend/src/main/java/com/coffee/backend/repository/PedidoRepository.java:10-12` —
  may need an ordered `findAllByOrderByFechaPedidoAsc` (or similar) if the TAD is
  rehydrated from persisted rows on each call instead of kept resident in memory.

### Item 3 (UsuarioResponseDTO)
- `backend/src/main/java/com/coffee/backend/dto/response/UsuarioResponseDTO.java:4-11` —
  ALREADY CORRECT. No edit needed.
- `backend/src/main/java/com/coffee/backend/service/implement/UsuarioServiceImpl.java:43-53`
  — mapper inline, ALREADY CORRECT. Optional refactor: extract a `UsuarioMapper` class
  matching the `ProductoMapper` pattern (`mapper/ProductoMapper.java:8-20`) for
  consistency with design.md "Mapper Entity↔DTO" decision (today only products have one).
- `backend/src/main/java/com/coffee/backend/dto/response/LoginResponseDTO.java:3-8` and
  `service/implement/AuthServiceImpl.java:33-41` — OPTIONAL: split `nombreCompleto`
  (space-concatenated) into `nombre` + `apellido` to eliminate the last "rigid space
  separator" in backend responses. This is the ONLY real backend refactor candidate
  for Item 3, but it touches the LOGIN contract that the front already consumes
  (`login.js:49-50`), so it would force a coordinated front change → likely OUT of
  scope; flagged for the orchestrator.
- `backend/Docs/08_01_sprints_backend_puro_faltantes.md:17-21` — Item 3 text is STALE and
  should be corrected / marked ✅ as part of this change's doc delta.

---

## Approaches

### Item 2 — FIFO TAD propio

1. **Custom singly-linked-list FIFO + priority banding** (recommended for the sílabo)
   - Custom `Nodo<T>` + head/tail pointers; `enqueue`, `dequeue`, `peek`, `size`,
     `isEmpty` hand-rolled. Priority = a separate band per `prioridad` (e.g. 0 normal,
     1 urgente); each band is its own FIFO, dequeue drains higher band first.
   - Pros: meets "TAD propio, no `java.util`" literally; defensible for the academic
     deliverable; pure-Java, easy to unit-test without Spring.
   - Cons: more code than wrapping JDK; must NOT accidentally use `java.util.LinkedList`
     internally; thread-safety is the implementer's responsibility.
   - Effort: Medium.

2. **Wrap `ConcurrentLinkedQueue` / `PriorityBlockingQueue` behind a TAD interface**
   - A `ColaDespacho<T>` interface + an `Impl` that delegates to a JDK concurrent queue.
   - Pros: trivial thread-safety; less code; "TAD" via the interface contract.
   - Cons: violates the sílabo mandate (`02_requirements.md:66` — "implementar el TAD
     propio (no usar `java.util` directamente)") and design.md:50-52 says "TAD propio".
     A reviewer following the academic rubric would reject this. Risk of burning the
     change for no pedagogical credit.
   - Effort: Low — but WRONG for the project's academic goal.

3. **In-memory resident vs rehydrated-from-DB queue**
   - 3a. **Resident singleton** (`@Component` holding the TAD; `registrarPedido` enqueues
     on save). Pro: real FIFO behavior survives across calls in one boot. Con: lost on
     restart; duplicates rows already persisted; needs reconciliation on boot.
   - 3b. **Rehydrated on each read** (`listarDespacho` runs an ordered `findBy...OrderBy...`
     and feeds results through the TAD). Pro: always consistent with persistence;
     despacho order is deterministic from `fechaPedido` (+ priority column if ever
     added). Con: the TAD is essentially a sorting façade, weaker academically.
   - 3c. **Hybrid**: TAD kept resident, but bootstrapped on app start from persisted
     rows; `registrarPedido` pushes; admin endpoint peeks. Pro: both academic and
     consistent. Con: most code.
   - Effort: 3a Low, 3b Low-Medium, 3c Medium.

### Item 3 — `UsuarioResponseDTO`

1. **Documentation-only assertion** (recommended given the finding)
   - Update `08_01_sprints_backend_puro_faltantes.md` Item 3 to ✅ DONE, citing the
     actual code lines that already satisfy the contract; optionally add a MockMvc
     regression test for `GET /api/admin/usuarios` shape (`usuarioId`, `rol`, separate
     `nombre`/`apellido`).
   - Pros: zero risk to the live login contract; matches reality; honest.
   - Cons: feels like a no-op — because it IS one in the backend.
   - Effort: Low.

2. **Extract `UsuarioMapper` + add regression test**
   - Pull the inline `obtenerUsuarios` map into a `mapper/UsuarioMapper.java` mirroring
     `ProductoMapper`, then lock the shape with a `@WebMvcTest`.
   - Pros: aligns with design.md "Mapper Entity↔DTO" convention; modest cleanliness win.
   - Cons: touches code that already works; small PR-budget cost.
   - Effort: Low-Medium.

3. **Split `LoginResponseDTO.nombreCompleto` into `nombre` + `apellido`**
   - Eliminates the only real "rigid space separator" in backend responses.
   - Pros: closes the literal complaint in the 08_01 description.
   - Cons: breaks the existing login contract used by `frontend/js/pages/login.js:49-50`
     and `frontend/js/core/auth.js` / `router.js` (they store `nombreCompleto`). Forces
     a coordinated front change — contradicts "backend-puro". High coordination cost.
   - Effort: Low-code, High-coordination — NOT recommended for this change.

---

## Recommendation

**Item 2:** Approach 1 (custom linked-list TAD propio, no `java.util`) + Approach 3c
hybrid (resident TAD bootstrapped from persisted rows on app start, enqueued on each
`registrarPedido`). The TAD lives in `com.coffee.backend.tad.ColaDespacho<T>` with a
priority-banded FIFO; strict TDD: pure-JUnit unit tests on the TAD first, then a
service-layer test asserting `registrarPedido` enqueues and `listarDespacho` returns
FIFO+prioridad order. Keep the transport protocol (polling vs WebSocket) UNDECIDED in
this change — only expose a service-layer despacho API and (optionally) a read-only
`GET` endpoint; nothing bidirectional. This satisfies the sílabo academic mandate and
keeps the API surface minimal so a later protocol change can be layered on without
rework.

**Item 3:** Approach 1 (documentation-only assertion) as the primary deliverable, plus
Approach 2 (extract `UsuarioMapper` + `@WebMvcTest` regression) as a low-risk
quality-of-life refactor — both fit comfortably inside the 400-line PR budget and need
NO frontend edits. Explicitly DO NOT split `LoginResponseDTO.nombreCompleto`
(Approach 3) in this change: it breaks the live login contract and contradicts the
"backend-puro" boundary. Surface that option to the orchestrator as a future
coordinated change instead.

**Cross-change scope note for the orchestrator:** the active change `iam-sprint-2-bundle`
(Engram obs #7) currently claims the Cola FIFO TAD as part of its bundle ("Cola FIFO
RF-DS-04: TAD propio (priority queue) + integration into the pedido despacho flow",
capability `pedido-queue`). That change ALSO flagged "Cola FIFO originally Jonathan's per
`08_sprints.md` — cross-assignee conflict risk flagged". `backend-puro-faltantes` (this
change, Jonathan owner per Sprint 2 Task 11 / B-23) is the correct home for the TAD.
**Recommendation: the orchestrator should move the `pedido-queue` capability out of
`iam-sprint-2-bundle` and into `backend-puro-faltantes` before either change reaches
`sdd-spec`, to avoid two changes writing the same `tad/` package and the same
`PedidoServiceImpl` integration simultaneously.** That is a proposal-time decision; this
explore flags it as the highest-priority risk (see Risks #1).

---

## Risks

1. **Scope overlap with `iam-sprint-2-bundle` on the Cola FIFO TAD** — HIGHEST. Both
   changes currently claim `tad/ColaFifo...` + `PedidoServiceImpl` despacho integration.
   Resolution needed BEFORE either change specs: move `pedido-queue` capability to
   `backend-puro-faltantes`. Done via `mem_judge(not_conflict)` for the iam proposal
   artifact after this explore saves.
2. **No frontend consumer for despacho** — the API surface is ambiguous (no front waits
   on it). Risk of designing an endpoint nobody calls. Mitigation: keep this change
   TAD + service-layer only; defer the HTTP surface to a future front-led change once
   the protocol (polling vs WS) is decided.
3. **`Operational state divergence` (Approach 3a/3c)** — a resident in-memory TAD can
   drift from persisted rows across a crash / restart / multi-instance deploy. Mitigation:
   bootstrap from persisted rows on app start; never trust the in-memory TAD as the
   source of truth for "what was sold".
4. **`Pedidos` has no `prioridad` / `estado` column** — priority banding can only live
   in the in-memory TAD (or be derived from a rule like "mesa / producto"). Adding a
   persisted priority column would be a schema change → OUT of scope. The spec must
   define the priority SOURCE explicitly (default: no priority; FIFO only; or FIFO +
   a derived priority).
5. **STALE docs masking reality** — `08_01_sprints_backend_puro_faltantes.md` Item 3
   describes a defect the backend already fixed (Sprint 1 Task 7). If we act on the
   doc without verifying code, we "fix" something already correct and waste budget /
   risk a regression. Mitigation: this explore verifies; the spec must cite the actual
   code state.
6. **Sílabo "no `java.util`" enforcement** — easy to accidentally import `java.util.*`
   or use `LinkedList` inside the "custom" TAD. Strict TDD + a focused PR review catch
   this; flag in spec acceptance criteria.
7. **Login contract coordination** — DO NOT touch `LoginResponseDTO.nombreCompleto`
   in this change (would force frontend edits in `login.js`, `auth.js`, `router.js`).
8. **Strict TDD from a single smoke test** — only `contextLoads` exists today; the
   first real coverage must be built TDD-first for both Item 2 (TAD unit tests) and
   Item 3 (MockMvc regression on `/api/admin/usuarios`). The `@SpringBootTest`
  BackendApplicationTests pins live Supabase and must NOT be touched or relied upon
   (per `iam-sprint-2-bundle` proposal's pattern: use hermetic `@WebMvcTest` slices).

---

## Ready for Proposal
Yes — with one prerequisite the orchestrator must resolve BEFORE `sdd-propose`:
resolve the scope overlap with `iam-sprint-2-bundle` on the Cola FIFO TAD
(Risk #1). Concretely: the orchestrator should confirm that `backend-puro-faltantes`
owns the `tad/` package + `PedidoServiceImpl` despacho integration, and that
`iam-sprint-2-bundle` drops capability `pedido-queue`. Once that is settled, this
change is ready for `sdd-propose` with the two-item scope above (Item 2 = TAD propio
FIFO+prioridad + despacho service integration, in-memory only, protocol undecided;
Item 3 = doc correction + optional `UsuarioMapper` extraction + MockMvc regression
asserting `usuarioId` + `rol` shape).