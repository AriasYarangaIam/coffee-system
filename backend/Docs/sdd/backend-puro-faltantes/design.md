---
artifact: design
change: backend-puro-faltantes
capabilities:
  - pedido-despacho-integration
  - usuario-mapper-refactor
status: ready-for-tasks
---

# Design: backend-puro-faltantes

## Technical Approach

Two backend-puro capabilities on top of merged `iam-sprint-2-bundle`: (1) **pedido-despacho-integration** consumes the upstream `tad/Cola` + `tad/ColaPrioridad` TAD (Req-PQ-03 follow-up — iam design Decision 3 anticipated this change) — a resident `ColaPrioridad<PedidoDespachoTokenView>` held by a new `PedidoDespachoService` impl, reseeded eagerly at boot by an `ApplicationRunner` over a NEW `PedidoRepository.findAllByOrderByFechaPedidoAsc()`, and enqueued after `pedidosRepository.save` in `registrarPedido`; (2) **usuario-mapper-refactor** extracts a `@Component UsuarioMapper` mirroring `ProductoMapper`, delegates `UsuarioServiceImpl.obtenerUsuarios` (L43-53), and locks `GET /api/admin/usuarios` with a hermetic `@WebMvcTest` regression reusing the upstream `application-test.yaml` profile. No DDL, no frontend edits, no Wire changes.

## Architecture Decisions

### Decision: Resident TAD ownership — separate `PedidoDespachoService` bean
| Choice | New `PedidoDespachoService` interface + `PedidoDespachoServiceImpl` `@Service` holds the resident `ColaPrioridad<PedidoDespachoTokenView>`; `PedidoService` NOT extended |
| Alt | extend `PedidoService` with despacho methods |
| Rationale | Separation of concerns — despacho is a distinct bounded concept; `PedidoService` stays focused on register/list/boleta. Mirrors the existing `service/implement/*` layering. |

### Decision: Rehydration via `ApplicationRunner` (NOT `@PostConstruct`)
| Choice | `@Component DespachoBootstrapRunner implements ApplicationRunner`; reseed in `run(...)` |
| Alt | `@PostConstruct` on a `@Component` |
| Rationale | `ApplicationRunner.run` fires once AFTER the context is fully ready (`PedidoRepository` wired); `@PostConstruct` is fragile across proxy types (e.g. `@Transactional` proxies). Locks IQ #5 (Q3 user answer). |

### Decision: `siguienteDespacho` = peek-only; NO dequeue method this change
| Choice | `siguienteDespacho()` returns `Optional<PedidoDespachoTokenView>` via `ColaPrioridad.peek()`; no `removerDespacho`/`marcarDespachado` shipped |
| Alt | peek + separate `removerDespacho(Long pedidoId)` |
| Rationale | Peek is idempotent, no side-effect, preserves the audit trail of pending despachos. Dequeue requires a `PENDIENTE/DESPACHADO` distinction that needs the `estado` DDL decision (out of scope). Defer dequeue to a future state-machine change. NOTE on REQ-PDI-04. |

### Decision: `PedidoDespachoTokenView` = NEW minimal record (option d)
| Choice | New `dto/response/PedidoDespachoTokenView(Long pedidoId, String aliasTicket, LocalDateTime fechaPedido)` |
| Alt | (a) reuse `PedidoListadoResponseDTO`; (b) raw `Pedidos` entity; (c) `PedidoDespachoDTO` with `total` |
| Rationale | Decoupled from upstream's listado shape (iam adds `detalle` to it — coupling rejected); pure despacho projection; honors DTO-at-the-border (entity-in-TAD rejected). `total` not needed for despacho surface. |

### Decision: `obtenerBoleta` stays read-only — no TAD mutation
| Choice | `obtenerBoleta` MUST NOT dequeue/remove a despacho token; TAD stays idempotent across boleta calls |
| Alt | invoicement side-effect dequeues |
| Rationale | `obtenerBoleta` returns a snapshot today (`PedidoServiceImpl:117-145`), does NOT mutate state. Invoicement side-effect requires the `estado` DDL decision (deferred). NOTE surfaced on REQ-PDI-04. |

### Decision: Enqueue transaction-boundary = AFTER `save` returns (post-commit semantics)
| Choice | `pedidoDespachoService.enqueueDespacho(token)` called AFTER `pedidosRepository.save(pedido)` inside the same `@Transactional` method |
| Alt | `TransactionSynchronizationManager` afterCommit hook |
| Rationale | The resident in-memory TAD is not transactional. If the surrounding `@Transactional` rolls back AFTER enqueue, the TAD would hold a phantom token. Practical mitigation: enqueue is the LAST statement inside `registrarPedido` (after `save` succeeds and the response DTO is built); the only rollback risk after enqueue is the method return — which cannot roll back the save. Documented behavior; the afterCommit hook is over-engineering for this single-call site. |

### Decision: Cross-instance coordination — out of scope
| Choice | NOT designed here |
| Rationale | RNF-05: single backend instance today. Multi-instance coordination (shared store vs sticky routing vs externalization) is a future change. NOTE on REQ-PDI-07. |

## Data Flow

```
Boot:   pedidos rows ──> PedidoRepository.findAllByOrderByFechaPedidoAsc()
                 └──> DespachoBootstrapRunner.run() ──> PedidoDespachoService.residentCola.enqueue(token)

POST /api/pedidos ──> PedidoController ──> PedidoServiceImpl.registrarPedido
                                              ├─ validate stock + cascade save (pedidosRepository.save)
                                              └─ pedidoDespachoService.enqueueDespacho(token) ──> resident ColaPrioridad

listarDespacho() / siguienteDespacho() ──> PedidoDespachoService ──> resident ColaPrioridad (peek / iterate, no drain)

GET /api/admin/usuarios ──> UsuarioController (@PreAuthorize ADMIN) ──> UsuarioServiceImpl.obtenerUsuarios
                                                                    └─ UsuarioMapper.toResponseDTO(u) ──> UsuarioResponseDTO
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `service/PedidoService.java` | (no change) | Despacho lives on the NEW `PedidoDespachoService` — `PedidoService` stays focused on register/list/boleta. |
| `service/implement/PedidoServiceImpl.java` | Modify | After `pedidosRepository.save(pedido)` call `pedidoDespachoService.enqueueDespacho(new PedidoDespachoTokenView(...))`; inject `PedidoDespachoService`. |
| `repository/PedidoRepository.java` | Modify | Add derived `List<Pedidos> findAllByOrderByFechaPedidoAsc();`. |
| `service/PedidoDespachoService.java` | NEW | `enqueueDespacho`, `listarDespacho`, `siguienteDespacho`. |
| `service/implement/PedidoDespachoServiceImpl.java` | NEW | `@Service`; holds resident `ColaPrioridad<PedidoDespachoTokenView>` (injected from upstream bean) and exposes peek/iterate. |
| `despacho/DespachoBootstrapRunner.java` | NEW | `@Component ApplicationRunner`; reseeds on boot via `findAllByOrderByFechaPedidoAsc()` + single-arg `enqueue(item)`. |
| `dto/response/PedidoDespachoTokenView.java` | NEW | Minimal record `(Long pedidoId, String aliasTicket, LocalDateTime fechaPedido)`. |
| `mapper/UsuarioMapper.java` | NEW | `@Component` mirroring `ProductoMapper` — `toResponseDTO(Usuarios)` → `UsuarioResponseDTO`. |
| `service/implement/UsuarioServiceImpl.java` | Modify | `obtenerUsuarios` delegates to `UsuarioMapper` (remove inline map at L43-53). |
| `controller/UsuarioController.java` | (no change) | `@PreAuthorize("hasRole('ADMIN')")` already on `GET /api/admin/usuarios` — re-asserted by the new MockMvc test. |
| `test/.../web/UsuarioControllerWebMvcTest.java` | NEW | `@WebMvcTest` + `@Import(GlobalExceptionHandler)` + `@MockBean UsuarioServiceImpl`; asserts `usuarioId`(Long)+`rol`(String)+nombre+apellido+correo+telefono; 403 for MESERO (REQ-UMR-02, REQ-UMR-03). REUSES upstream `application-test.yaml`. |
| `test/.../service/PedidoServiceImplDespachoTest.java` | NEW | Hermetic integration test (REQ-PDI-06); reuses upstream `application-test.yaml`; covers empty-TAD, N-row reseed, enqueue-on-register, peek, listar FIFO. DOES NOT touch `BackendApplicationTests.contextLoads`. |
| `BackendApplicationTests.java` | (no change) | Untouched per upstream REQ-BCT-05. |
| `backend/Docs/08_01_sprints_backend_puro_faltantes.md` | Modify | Item 3 (lines 17-21) → ✅ DONE citing `UsuarioResponseDTO.java:4-11` + `UsuarioServiceImpl.java:43-53`; note the space-split defect lives in `frontend/js/pages/usuarios.js:73,95-96`, NOT backend (REQ-UMR-04). |

## Interfaces / Contracts

```java
// dto/response/PedidoDespachoTokenView.java  (this change)
public record PedidoDespachoTokenView(
    Long pedidoId,
    String aliasTicket,
    LocalDateTime fechaPedido
) {}

// repository/PedidoRepository.java  (this change — NEW derived query)
List<Pedidos> findAllByOrderByFechaPedidoAsc();

// service/PedidoDespachoService.java  (this change)
public interface PedidoDespachoService {
    void enqueueDespacho(PedidoDespachoTokenView token);
    List<PedidoDespachoTokenView> listarDespacho();        // FIFO snapshot, no drain
    Optional<PedidoDespachoTokenView> siguienteDespacho(); // peek-only
}

// service/implement/PedidoDespachoServiceImpl.java  (this change — skeleton)
@Service
@RequiredArgsConstructor
public class PedidoDespachoServiceImpl implements PedidoDespachoService {
    private final ColaPrioridad<PedidoDespachoTokenView> residentCola; // upstream bean
    @Override public void enqueueDespacho(PedidoDespachoTokenView t) { residentCola.enqueue(t); }   // single-arg, prio 0
    @Override public List<PedidoDespachoTokenView> listarDespacho() { /* iterate without draining */ }
    @Override public Optional<PedidoDespachoTokenView> siguienteDespacho() { return residentCola.peek(); }
}

// despacho/DespachoBootstrapRunner.java  (this change — skeleton)
@Component
@RequiredArgsConstructor
public class DespachoBootstrapRunner implements ApplicationRunner {
    private final PedidoRepository pedidoRepository;
    private final PedidoDespachoService pedidoDespachoService;
    @Override public void run(ApplicationArguments args) {
        pedidoRepository.findAllByOrderByFechaPedidoAsc().stream()
            .map(p -> new PedidoDespachoTokenView(p.getPedidoId(), p.getAliasTicket(), p.getFechaPedido()))
            .forEach(pedidoDespachoService::enqueueDespacho);    // single-arg enqueue (prio 0)
    }
}

// mapper/UsuarioMapper.java  (this change — mirrors ProductoMapper)
@Component
public class UsuarioMapper {
    public UsuarioResponseDTO toResponseDTO(Usuarios u) {
        return new UsuarioResponseDTO(
            u.getUsuarioId(),
            u.getNombreUsuario(),
            u.getApellidoUsuario(),
            u.getCorreoUsuario(),
            u.getTelefonoUsuario(),
            u.getRoles().getNombreRol()
        );
    }
}
```

```java
// === UPSTREAM TAD signatures — owned by iam-sprint-2-bundle. DO NOT create in this change ===
// tad/Cola.java
public interface Cola<T> {
    void enqueue(T item);
    Optional<T> dequeue();
    Optional<T> peek();
    boolean isEmpty();
    int size();
}
// tad/ColaPrioridad.java
public final class ColaPrioridad<T> implements Cola<T> {
    public void enqueue(T item, int prioridad);
    @Override public Optional<T> dequeue();
    public void enqueue(T item);                   // default prioridad 0 — THIS change uses this overload only
}
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Integration | REQ-PDI-02 reseed (empty DB + N rows), REQ-PDI-03 enqueue on `registrarPedido`, REQ-PDI-04 peek/siguiente on empty = empty Optional, REQ-PDI-05 FIFO order | `PedidoServiceImplDespachoTest` with `@SpringBootTest` + hermetic `application-test.yaml` profile (H2, `ddl-auto=none`) from upstream; REUSE not recreate. Strict TDD: test first → impl. |
| Web slice | REQ-UMR-02 wire shape (`usuarioId` JSON number + `rol` String + nombre/apellido/correo/telefono), REQ-UMR-03 403 for MESERO, 200 for ADMIN | `@WebMvcTest(UsuarioController)` + `@Import(GlobalExceptionHandler)` + `@MockBean UsuarioServiceImpl`; `@WithMockUser(roles=...)`. |
| Unit | `UsuarioMapper.toResponseDTO` round-trip (REQ-UMR-01) | Pure JUnit on the mapper `@Component` instance. |

### Strict TDD ordering
1. `UsuarioMapperTest` round-trip (fails) → `UsuarioMapper` (passes).
2. `UsuarioControllerWebMvcTest` (fails: wire shape + 403) → `UsuarioServiceImpl.obtenerUsuarios` delegates (passes; controller unchanged).
3. `PedidoServiceImplDespachoTest` empty-TAD + reseed + enqueue + peek + listar FIFO (fails) → `PedidoDespachoService` + impl + `DespachoBootstrapRunner` + `PedidoRepository.findAllByOrderByFechaPedidoAsc` + `PedidoServiceImpl.registrarPedido` enqueue (passes).
4. `08_01` doc fix (no test).

## Migration / Rollout

No migration required. No DDL. Rollback = revert the change branch; no DB rollback needed. The resident in-memory TAD is rebuilt on next boot by `DespachoBootstrapRunner`. `UsuarioMapper` extraction is reversible to inline. `BackendApplicationTests.contextLoads` untouched.

## Open Questions

- [ ] Multi-instance TAD coordination (shared store vs sticky routing vs externalization) — deferred future change (REQ-PDI-07 NOTE).
- [ ] Despacho state machine (`PENDIENTE/DESPACHADO` + `estado` DDL + dequeue/invoicement side-effect) — deferred future change requiring product-owner sign-off; this change ships the idempotent peek-only surface.
- [ ] HTTP transport for despacho (poll vs WebSocket) — deferred to a front-led change; this change exposes the service-layer API only.