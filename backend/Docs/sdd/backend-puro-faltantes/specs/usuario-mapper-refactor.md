# Usuario Mapper Refactor Specification

## Purpose

Backend-puro cleanup of the residual `UsuarioResponseDTO` mapping debt: extract a dedicated `mapper/UsuarioMapper` mirroring `mapper/ProductoMapper`, lock the existing wire shape with a hermetic `@WebMvcTest` regression, and correct in-repo the stale Item 3 of `backend/Docs/08_01_sprints_backend_puro_faltantes.md`. No frontend edits, no DTO wire-shape change, no auth change.

## Requirements

### Requirement: REQ-UMR-01 — UsuarioMapper extraction

The system SHALL introduce a `mapper/UsuarioMapper` class (mirroring `mapper/ProductoMapper.java` in layer and naming) that converts `Usuarios` entity → `UsuarioResponseDTO` record. `UsuarioServiceImpl.obtenerUsuarios` (currently inline at lines ~43-53) MUST delegate the entity→DTO conversion to `UsuarioMapper`. No behavior change at the wire shape (`usuarioId`, `nombreUsuario`, `apellidoUsuario`, `correoUsuario`, `telefonoUsuario`, `rol`).

#### Scenario: Mapper round-trips all fields

- GIVEN a `Usuarios` entity with `usuarioId=7`, `nombreUsuario="Ana"`, apellidoUsuario="Lopez", correoUsuario, telefonoUsuario, and `roles.nombreRol="ADMIN"`
- WHEN `UsuarioMapper.toResponseDTO(usuario)` is called
- THEN the returned `UsuarioResponseDTO` has `usuarioId=7`, `nombreUsuario="Ana"`, matching `apellidoUsuario`/`correoUsuario`/`telefonoUsuario`, and `rol="ADMIN"`

#### Scenario: Service delegates (no inline map)

- GIVEN `UsuarioServiceImpl.obtenerUsuarios` is invoked
- WHEN the result list is built
- THEN each DTO is produced by `UsuarioMapper`
- AND no inline map lambda remains at `UsuarioServiceImpl` lines 43-53

### Requirement: REQ-UMR-02 — MockMvc regression

The system MUST include a hermetic `@WebMvcTest` test (e.g. `UsuarioControllerWebMvcTest` or `UsuarioMapperWebMvcTest`) asserting `GET /api/admin/usuarios` returns a list whose items have `usuarioId` (Long, JSON number), `nombreUsuario` (String), `apellidoUsuario` (String), `correoUsuario` (String), `telefonoUsuario` (String), `rol` (String). The test MUST assert both `usuarioId` and `rol` presence and type explicitly, so a future refactor cannot silently drop them. REUSE the upstream `application-test.yaml` profile (do NOT recreate or modify it).

#### Scenario: Wire shape locked

- GIVEN a `@WebMvcTest` slice for `UsuarioController` with mocked `UsuarioServiceImpl`
- WHEN `GET /api/admin/usuarios` is performed as ADMIN
- THEN each JSON item has `usuarioId` as a JSON number and `rol` as a JSON string
- AND `nombreUsuario`, `apellidoUsuario`, `correoUsuario`, `telefonoUsuario` are present

### Requirement: REQ-UMR-03 — Profile restricted to ADMIN

The MockMvc regression SHALL assert `GET /api/admin/usuarios` requires role `ADMIN` per the existing `@PreAuthorize` constraint. A non-ADMIN caller MUST receive 403. This re-asserts the existing B-06 constraint — the controller contract stays unchanged.

#### Scenario: Non-ADMIN denied

- GIVEN a caller authenticated with role MESERO (not ADMIN)
- WHEN `GET /api/admin/usuarios` is performed
- THEN the response status is 403

#### Scenario: ADMIN allowed

- GIVEN a caller authenticated with role ADMIN
- WHEN `GET /api/admin/usuarios` is performed
- THEN the response status is 200

### Requirement: REQ-UMR-04 — Stale doc correction in-repo

The system SHALL correct the Item 3 text of `backend/Docs/08_01_sprints_backend_puro_faltantes.md` (lines 17-21) IN THE SAME BRANCH as the `UsuarioMapper` extraction + MockMvc regression: mark Item 3 ✅ DONE citing `UsuarioResponseDTO.java:4-11` and `UsuarioServiceImpl.java:43-53`, and explicitly note the "rigid space separator (separación rígida por espacios) / concatenación defectuosa" defect the doc described actually lives in the **frontend** at `frontend/js/pages/usuarios.js:73,95-96` (split-by-space corrupts compound names), NOT the backend. The backend exposes `nombreUsuario` and `apellidoUsuario` separately and correctly. The change MUST NOT edit `LoginResponseDTO.nombreCompleto` (out of scope — breaks the live front login contract).

#### Scenario: Item 3 corrected

- GIVEN the change branch is applied
- WHEN `08_01_sprints_backend_puro_faltantes.md` Item 3 is read
- THEN it shows ✅ DONE with citations to `UsuarioResponseDTO.java:4-11` and `UsuarioServiceImpl.java:43-53`
- AND it notes the frontend defect at `frontend/js/pages/usuarios.js:73,95-96`

### Requirement: REQ-UMR-05 — No behavior change to existing wire contract

The `UsuarioMapper` extraction MUST NOT change the JSON shape, status codes, or role requirements of `GET /api/admin/usuarios`. The MockMvc regression MUST lock the existing shape (this is a regression guard, not a new contract). No frontend edits.

#### Scenario: Regression guard holds

- GIVEN the `UsuarioMapper` extraction is applied
- WHEN the MockMvc regression exercises `GET /api/admin/usuarios`
- THEN the JSON shape matches the pre-refactor shape (same fields, same types, same status codes)
- AND no frontend file is modified