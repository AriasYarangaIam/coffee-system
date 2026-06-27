# Tasks: backend-puro-faltantes

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~220–320 (6 NEW + 4 Modified files) |
| 400-line budget risk | Medium |
| Chained PRs recommended | Yes |
| Suggested split | PR #1 usuario-mapper (ships first) → PR #2 despacho (depends-on iam-sprint-2-bundle) |
| Delivery strategy | ask-always |
| Chain strategy | stacked-to-develop |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: stacked-to-develop
400-line budget risk: Medium

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | usuario-mapper-refactor end-to-end (mapper extraction + MockMvc regression + Item 3 doc fix) | PR #1 `feat/back/usuario-mapper-refactor` → `develop` | No upstream dep; ships first任何时候 |
| 2 | pedido-despacho-integration end-to-end (consumer of upstream TAD) | PR #2 `feat/back/pedido-despacho-integration` → `develop` | HARD depends-on `iam-sprint-2-bundle` merged (`tad/` + `application-test.yaml` on develop); BLOCKED until then |

## Phase 1: PR #1 `usuario-mapper-refactor` (ships first, no upstream dep)

- [x] 1.1 RED: write failing hermetic `@WebMvcTest` `UsuarioControllerWebMvcTest` — assert `usuarioId` (Long, JSON number) + `rol` (String) + nombre/apellido/correo/telefono on `GET /api/admin/usuarios`, AND non-ADMIN (MESERO) → 403 (REQ-UMR-02, REQ-UMR-03). NOTE: `application-test.yaml` upstream aún NO mergeado → slice hermético vía `@WebMvcTest(UsuarioController.class)` + `@Import(GlobalExceptionHandler + TestSecurityConfig)` con `@EnableMethodSecurity` + `@WithMockUser`; JwtFilter pass-through satisfecho con `@MockitoBean JwtUtil`+`UserDetailsServiceImpl`. RED manifests: stub malformado `rol=null` => `jsonPath("$.rol") expected:<ADMIN> but was:<null>` (403 PASS). DC: `@MockBean`→`@MockitoBean` (Spring Boot 4 / spring 7 renombró el API).
- [x] 1.2 GREEN: creado `mapper/UsuarioMapper` `@Component` mirroring `mapper/ProductoMapper` (`Usuarios` → `UsuarioResponseDTO`) per design "Interfaces / Contracts" (`toResponseDTO`).
- [x] 1.3 GREEN: `service/implement/UsuarioServiceImpl.obtenerUsuarios` (post-refactor L45-49) — removido inline `new UsuarioResponseDTO(...)`; inyectado `final UsuarioMapper usuarioMapper` vía `@RequiredArgsConstructor`; delega `.map(usuarioMapper::toResponseDTO)` (REQ-UMR-01).
- [x] 1.4 GREEN: re-run `UsuarioControllerWebMvcTest` → green; shape 6-campos + 403 locked. Adicional: `UsuarioMapperTest` (pure unit) cubre round-trip REQ-UMR-01 (ADMIN + nombre compuesto "Maria José" / MESERO) — ciclo RED (mapper inexistente → compile error) → GREEN real sobre producción.
- [x] 1.5 Doc: `08_01_sprints_backend_puro_faltantes.md` Item 3 (L17-21) → ✅ DONE citando `UsuarioResponseDTO.java:4-11` + `UsuarioServiceImpl.java:45-49` + `mapper/UsuarioMapper.java:13-22`; nota "separación rígida por espacios" vive en `frontend/js/pages/usuarios.js:73,95-96` (split por espacio corrompe "Maria José"), NO en el backend (REQ-UMR-04).
- [x] 1.6 Verify: `cd backend && ./mvnw test` green (5/5: BackendApplicationTests 1 + UsuarioMapperTest 2 + UsuarioControllerWebMvcTest 2); `cd backend && ./mvnw -DskipTests test-compile` exits 0; `BackendApplicationTests.contextLoads` untouched green.
- [x] 1.7 Commits: `chore/back:` gitignore + 2 docs + `test(back): RED` + `refactor(back): GREEN` + `docs(back): Item 3`. Push `feat/back/usuario-mapper-refactor` → open PR `--base develop`.

## Phase 2: PR #2 `pedido-despacho-integration` (BLOCKED until iam-sprint-2-bundle merged)

- [ ] 2.0 **Pre-apply gate**: verify `iam-sprint-2-bundle` merged to `develop` — `backend/src/main/java/com/coffee/backend/tad/Cola.java` + `tad/ColaPrioridad.java` exist; `backend/src/test/resources/application-test.yaml` exists. If absent: BLOCKED — do NOT start Phase 2. depends-on annotation: `iam-sprint-2-bundle merged`.
- [ ] 2.1 RED: write failing hermetic `PedidoServiceImplDespachoTest` covering REQ-PDI-01..06 (empty-TAD before reseed, N-row reseed via `findAllByOrderByFechaPedidoAsc`, `registrarPedido` enqueues new token, `siguienteDespacho` peeks FIFO head no side-effect, `listarDespacho` returns FIFO order, empty TAD → `Optional.empty()` — no exception). REUSE upstream `application-test.yaml`; do NOT touch `BackendApplicationTests.contextLoads`.
- [ ] 2.2 GREEN: create `dto/response/PedidoDespachoTokenView` record `(Long pedidoId, String aliasTicket, LocalDateTime fechaPedido)` per design Decision "option (d)".
- [ ] 2.3 GREEN: add `PedidoRepository.findAllByOrderByFechaPedidoAsc()` derived query (NEW method; no `estado` column — pure FIFO).
- [ ] 2.4 GREEN: create `service/PedidoDespachoService` interface — `void enqueueDespacho(PedidoDespachoTokenView)`, `Optional<PedidoDespachoTokenView> siguienteDespacho()`, `List<PedidoDespachoTokenView> listarDespacho()` (peek-only per Decision; NO dequeue method this change).
- [ ] 2.5 GREEN: create `service/implement/PedidoDespachoServiceImpl` `@Component` holding resident `ColaPrioridad<PedidoDespachoTokenView>` (Spring-injected upstream bean). Implement three methods (peek-only `siguienteDespacho`, FIFO iterate `listarDespacho`, single-arg `enqueue` for `enqueueDespacho`).
- [ ] 2.6 GREEN: create `service/implement/DespachoBootstrapRunner` `@Component implements ApplicationRunner.run(...)` — iterate `pedidoRepository.findAllByOrderByFechaPedidoAsc()` and `pedidoDespachoService.enqueueDespacho(token)` each row (single-arg, prio 0; do NOT invoke two-arg overload).
- [ ] 2.7 GREEN: modify `service/implement/PedidoServiceImpl.registrarPedido` — after `pedidosRepository.save(pedido)`, build `PedidoDespachoTokenView` and call `pedidoDespachoService.enqueueDespacho(token)`. Enqueue is the LAST statement of `registrarPedido` AFTER save returns (Decision "transaction-boundary"); if save throws, enqueue never runs. Do NOT modify `obtenerBoleta` (stays read-only — Decision "obtenerBoleta no side-effect"); use single-arg enqueue only (prio 0).
- [ ] 2.8 GREEN: re-run `PedidoServiceImplDespachoTest` → all spec REQ-PDI scenarios green.
- [ ] 2.9 Verify: `cd backend && ./mvnw test` green; `cd backend && ./mvnw -DskipTests test-compile` exits 0; `BackendApplicationTests.contextLoads` untouched green.
- [ ] 2.10 Commit `feat(back): integrate despacho FIFO TAD consumer`. Push `feat/back/pedido-despacho-integration` → open PR to `develop` (PR body notes: depends-on iam-sprint-2-bundle merged).

## Phase 3: Cross-change verification (post both PRs merge to develop)

- [ ] 3.1 Smoke `cd backend && ./mvnw test` green on `develop` after both PRs land.
- [ ] 3.2 Confirm `08_01 Item 3 ✅` visible in develop HEAD; Sprint 2 Task 11 (RF-DS-04) delivery documented in PR descriptions.

## Strict TDD ordering notes

- Phase 1 & Phase 2 each start with RED (failing test) BEFORE GREEN (impl) — project's strict_tdd=true capability + explore finding "BackendApplicationTests is the only test today".
- Tests MUST be hermetic — REUSE upstream `application-test.yaml` profile; do NOT recreate/modify it; do NOT mount live Supabase; do NOT touch `BackendApplicationTests.contextLoads`.

## Upstream guard

- `tad/Cola<T>` / `tad/ColaPrioridad<T>` signatures are owned by `iam-sprint-2-bundle` — see Engram obs #9 "Interfaces / Contracts". This change IMPORTS them; MUST NOT redefine or vendor them.