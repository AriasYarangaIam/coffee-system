# Proposal: backend-puro-faltantes

## Intent

Deliver REQ-PQ-03 despacho integration on top of the `tad/Cola`+`tad/ColaPrioridad` TAD shipped upstream by `iam-sprint-2-bundle` (Decision 3, Q3 resolution B-(ii) explicitly deferred this follow-up), and close the residual Item 3 `UsuarioMapper` debt. Both backend-puro: no DDL, no frontend edits, no Wire/contract changes.

## Scope

### In Scope

- **Q1 Priority = Pure FIFO** — resident `ColaPrioridad` filled via single-arg `enqueue(item)` (default prio 0); strict `findAllByOrderByFechaPedidoAsc`. The two-arg `enqueue(item, prioridad)` overload ships upstream but THIS change never invokes it.
- **Q2 Surface = Service-layer only** — `listarDespacho()` / `siguienteDespacho()` on `PedidoService` + `PedidoServiceImpl`. No controller, no `GET /api/pedidos/despacho`.
- **Q3 Rehydration = `ApplicationRunner` eager reseed** — `@Component` runner iterates `findAllByOrderByFechaPedidoAsc()` and feeds each `Pedidos` row into the resident `ColaPrioridad` at boot. Single-instance cafetería deploy today (RNF-05).
- **Q4 Doc correction = In-repo** — `08_01_sprints_backend_puro_faltantes.md` Item 3 ✅ DONE in the SAME branch; cites `UsuarioServiceImpl.java:43-53`, `UsuarioResponseDTO.java:4-11`; notes the real "rigid space separator" defect lives in `frontend/js/pages/usuarios.js:73,95-96`.
- **Q5 Upstream fallback = Hold** — `iam-sprint-2-bundle` MUST merge first; no temporary TAD vendoring.
- `registrarPedido` enqueues a despacho token after `pedidosRepository.save`.
- Integration test `PedidoServiceImplDespachoTest` (hermetic — reuses upstream `application-test.yaml` profile; do NOT touch `BackendApplicationTests.contextLoads`).
- Extract `mapper/UsuarioMapper` (mirrors `mapper/ProductoMapper`); hermetic `@WebMvcTest` regression asserting `usuarioId` (Long) + `rol` (String) on `GET /api/admin/usuarios`.

### Out of Scope

- The TAD itself (`Cola.java`, `ColaPrioridad.java`, `ColaPrioridadTest`) — owned by `iam-sprint-2-bundle`.
- HTTP endpoint / controller for despacho — deferred to a front-led change once protocol (poll vs WebSocket) is decided.
- Contract adjustments (`detalle`, ISO 8601 fechaPedido, message-body for 3 existing exceptions), B-13 full error handler, B-18 code migration, monetary runbook — `iam-sprint-2-bundle`.
- Hermetic `@WebMvcTest` profile/scaffolding — `iam-sprint-2-bundle` (REUSE here).
- Admin endpoints, `@PreAuthorize` role-access — `endpoints-admin-sprint-2`.
- Frontend code edits — none.
- WebSocket / bidirectional transport — undecided.
- Any DDL / `estado` / `prioridad` persisted column — out (backend-puro).
- `LoginResponseDTO.nombreCompleto` split — breaks live front login contract (`login.js:49-50`, `auth.js`, `router.js`).
- Multi-instance TAD state reconciliation — defer (single-instance today).

## Capabilities

### New Capabilities

- `pedido-despacho-integration`: REQ-PQ-03 follow-up — consume upstream `tad/Cola`+`tad/ColaPrioridad`; `ApplicationRunner` eager reseed via `PedidoRepository.findAllByOrderByFechaPedidoAsc()`; `registrarPedido` enqueues post-save; pure FIFO (default prio 0, two-arg overload NOT invoked); service-layer-only `listarDespacho`/`siguienteDespacho` on `PedidoService`; hermetic `PedidoServiceImplDespachoTest` reusing upstream `application-test.yaml`.
- `usuario-mapper-refactor`: extract `mapper/UsuarioMapper` mirroring `ProductoMapper`; `@WebMvcTest` regression asserting `usuarioId`+`rol` on `GET /api/admin/usuarios`; correct stale Item 3 text in `08_01_sprints_backend_puro_faltantes.md` to ✅ DONE in-repo.

### Modified Capabilities

- None. (Both are NEW; the project has no `openspec/specs/`.)

## Approach

Layered Spring: `PedidoRepository` exposes the ordered query; a new `@Component ApplicationRunner` (`PedidoDespachoBootstrap`) reseeds a resident `ColaPrioridad<PedidoDespachoToken>` on boot; `PedidoServiceImpl.registrarPedido` enqueues after `pedidosRepository.save`; `listarDespacho`/`siguienteDespacho` expose the dequeued state. Strict TDD — hermetic tests reuse `iam-sprint-2-bundle`'s `application-test.yaml` profile; do NOT touch `BackendApplicationTests.contextLoads`. `UsuarioMapper` extracted to a `@Component` mirroring `ProductoMapper`.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `repository/PedidoRepository.java` | M | Add `findAllByOrderByFechaPedidoAsc()`. |
| `service/PedidoService.java` | M | Add `listarDespacho()` / `siguienteDespacho()`. |
| `service/implement/PedidoServiceImpl.java` | M | Enqueue in `registrarPedido`; implement despacho methods; hold resident `ColaPrioridad`. |
| `despacho/PedidoDespachoBootstrap.java` | New | `@Component ApplicationRunner` eager reseed. |
| `mapper/UsuarioMapper.java` | New | `@Component` mirroring `ProductoMapper`; moves `UsuarioServiceImpl.java:43-53` inline map. |
| `mapper/UsuarioServiceImpl.java` | M | Delegate `obtenerUsuarios` to `UsuarioMapper`. |
| `08_01_sprints_backend_puro_faltantes.md` | M | Item 3 ✅ DONE, cite code lines, note front-side defect. |
| `test/...PedidoServiceImplDespachoTest.java` | New | Hermetic despacho integration (reuses upstream profile). |
| `test/...web/UsuarioControllerWebMvcTest.java` | New | `usuarioId`+`rol` serialization regression. |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Hard upstream dependency on `iam-sprint-2-bundle` merge | High | Hold change (Q5); annotate despacho task `depends-on: iam-sprint-2-bundle merged`; no vendoring. |
| Single-instance TAD state diverges in multi-instance deploy | Med | Cafetería runs one instance today (RNF-05); multi-instance reconciliation deferred. |
| No front consumer for despacho service API | Low | Surface is service-layer only; HTTP endpoint deferred. |
| `@SpringBootTest` smoke test pins live Supabase | Med | Reuse upstream hermetic `application-test.yaml` profile; do NOT touch `BackendApplicationTests.contextLoads`. |
| Stale doc masked as a defect | Low | Verified corrected in-repo with cite. |

## Rollback Plan

Revert the change branch. No DDL/data changes, no DB rollback. UsuarioMapper extraction is reversible to inline. ApplicationRunner removal is a single delete. Doc correction is reversible. Smoke test untouched.

## Dependencies

- **HARD**: `iam-sprint-2-bundle` must merge BEFORE `sdd-apply`. It ships the TAD package (`tad/Cola` + `tad/ColaPrioridad` + `ColaPrioridadTest`) and the hermetic `src/test/resources/application-test.yaml` profile. `sdd-tasks` MUST annotate the despacho task `depends-on: iam-sprint-2-bundle merged`. Q5 = Hold: no fallback if upstream is delayed or dropped.
- `endpoints-admin-sprint-2` is independent — no cross-dependency.

## Success Criteria

- [ ] Pure-FIFO despacho observable via `siguienteDespacho()` matching `findAllByOrderByFechaPedidoAsc` order after `ApplicationRunner` reseed.
- [ ] `registrarPedido` enqueues a despacho token after `pedidosRepository.save`.
- [ ] `PedidoServiceImplDespachoTest` hermetic green (reuses upstream `application-test.yaml`).
- [ ] `mapper/UsuarioMapper` extracted; `UsuarioServiceImpl.obtenerUsuarios` delegates.
- [ ] `@WebMvcTest` asserts `usuarioId` (Long) + `rol` (String) on `GET /api/admin/usuarios`.
- [ ] `08_01_sprints_backend_puro_faltantes.md` Item 3 corrected to ✅ DONE with code cite + front-side defect note.
- [ ] `./mvnw test` passes; `./mvnw -DskipTests test-compile` exits clean.