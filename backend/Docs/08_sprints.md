> SDD coffee-system · Documento 8 de 11 · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 08 · Sprints y metodología

## Contexto académico

Proyecto final del curso **Algoritmos y Estructuras de Datos** (UTP, 2026-1). Grupal.

> ⏰ **Quedan 2 semanas para la entrega** (a 2026-06-26). Plan reducido a **2 sprints
> de 1 semana**. Foco: integrar front↔back y dejar funcionando las **estructuras de
> datos del sílabo** (es lo que califica el proyecto).

| Hito | Ventana | Entregable |
|---|---|---|
| **Sprint 1** | 2026-06-26 → 2026-07-02 | Desbloqueo + contrato front↔back alineado |
| **Sprint 2** | 2026-07-03 → 2026-07-09 | Estructuras de datos + integración + demo |
| **Entrega final** | ~2026-07-10 | Proyecto funcional + esta documentación |

## Equipo (5 integrantes)

| Frente | Integrantes |
|---|---|
| **Backend** | Jose, Iam, Jonathan |
| **Frontend** | Joaquin, Yefrie |

> El sílabo exige aplicar estructuras de datos. Mapeo en
> [02_requirements.md](02_requirements.md) (RF-DS-*): **matriz** (reporte mensual, back),
> **cola FIFO+prioridad** (despacho, back), **pila** (deshacer carrito, front).

## Metodología

Spec-Driven Development en modo *brownfield*: la documentación SDD (estos `Docs/`) es
el contrato; los cambios al contrato se hacen por documentos incrementales, no
reescribiendo todo (ver Fase 2 del `PROMPT_MAESTRO_SDD.md`).

## Flujo Git (reglas estrictas — de `PROMPT_MAESTRO_CAFETERIA_MYPE.md`)

Repo oficial: `https://github.com/AriasYarangaIam/coffee-system.git`

```
main          ← Producción. NUNCA tocar directamente.
└── develop   ← Integración. Base de todo el trabajo.
    ├── feat/back/<tarea>
    ├── feat/front/<tarea>
    ├── fix/<entorno>/<tarea>
    └── chore/<entorno>/<tarea>
```

- Ramas: `tipo/entorno/nombre-tarea` (`feat|fix|chore` / `back|front` / kebab-case).
- Prohibido: commit directo a `main` o `develop`; merge sin PR revisado; nombres
  genéricos (`rama-juan`, `prueba`).
- Commits: Conventional Commits → `tipo(entorno): descripción en imperativo`.
- Ciclo: `git checkout develop && git pull` → crear rama → commits atómicos → push →
  **PR hacia `develop`** (nunca `main`) → Code Review.
- Entornos: Front abre `frontend/` (VS Code), Back abre `backend/` (IntelliJ). No abrir
  la raíz `coffee-system/` en el IDE.

## Definition of Done

- [ ] Respeta el stack y la arquitectura N-capas.
- [ ] Usa DTOs en el controller (no expone entidades).
- [ ] Operación de venta dentro de `@Transactional` con validación de stock.
- [ ] Endpoint protegido con `@PreAuthorize` según rol.
- [ ] Sin romper el contrato vivo que el front ya consume (o documentado en 09).
- [ ] Compila (`mvnw compile`) y arranca con config local.
- [ ] Rama y PR siguen las reglas de arriba; PR apunta a `develop`.

## Plan de 2 sprints — Backend (Jose, Iam, Jonathan)

Derivado del [backlog 09](09_backlog_brechas.md). El track del front está en
[frontend/Docs/08_sprints.md](../../frontend/Docs/08_sprints.md). La línea de tiempo es
compartida: el **back debe fijar el contrato al inicio del Sprint 1** para que el front
integre en el Sprint 2.

### Sprint 1 (26 jun → 02 jul) — Desbloqueo + contrato

| # | Tarea | Backlog | Responsable |
|---|---|---|---|
| 1 | Crear `DeleteUserDTO` → el proyecto **compila** | B-01 | **Iam** |
| 2 | Config local (`application-local`/env): datasource Supabase + `jwt.secret`/`jwt.expiration` + plantilla versionada | B-02 | **Iam** |
| 3 | Unificar rol admin a **`ADMIN`** (`@PreAuthorize`, `UserDetailsServiceImpl`, seed `roles`) | B-03 | **Iam** |
| 4 | `GET /api/pedidos` (pedidos del mesero) + incluir `total`; `PedidoRequestDTO` = solo `detalles` (quitar `usuarioId` y `aliasTicket`); back toma usuario del JWT y **genera `aliasTicket`** (UUID corto/secuencial) | B-07, B-08, B-14, B-F-01 | **Jose** |
| 5 | CRUD `/api/admin/productos` e `/api/admin/insumos` | B-04 | **Jose** |
| 6 | CRUD `/api/admin/stocks` (ingreso + listado) | B-04 | **Jonathan** |
| 7 | Mover usuarios a `/api/admin/usuarios`; `UsuarioResponseDTO` con `usuarioId` y `rol`; permitir crear ADMIN | B-06, B-12, B-F-07 | **Jonathan** |
| 8 | Quitar endpoint debug `/api/auth/get` | B-11 | **Iam** |
| ▶ | **Publicar el contrato (05) al front el día 2-3** para desbloquear su integración | — | Jose + Iam |

### Sprint 2 (03 jul → 09 jul) — Estructuras de datos + métricas + cierre

| # | Tarea | Backlog | Responsable |
|---|---|---|---|
| 9 | ✅ `GET /api/admin/dashboard` (ventas día, pedidos día, producto estrella, stock bajo) con `@Query` | B-05 | **Jose** |
| 10 | ✅ `GET /api/admin/reportes/mensual` con **RF-DS-02 (matriz producto × día)** — `ReporteService` arma el `double[][]`; front lo grafica apilado (rama `feat/back/reporte-mensual-matriz`) | B-05, B-22 | **Jonathan** |
| 11 | ✅ **RF-DS-04 Cola FIFO + prioridad** de despacho (TAD propio `tad/ColaPrioridad`) + enqueue en `registrarPedido` + rehidratación al boot + `GET /api/admin/despacho` (polling) (rama `feat/back/cola-despacho-fifo`) | B-23 | **Jonathan** |
| 12 | ✅ Ampliar `GET /api/productos` a `MESERO` y `ADMIN` | B-10 | **Jose** |
| 13 | Soporte de pruebas de integración con el front + ajustes de contrato | — | **Iam** |
| 14 | (si da tiempo) precisión monetaria `BigDecimal`/handler de errores completo | B-13, B-18 | **Iam** |

> Diferido sin riesgo de nota: migraciones versionadas (B-21), tipos de insumo
> fraccionarios (B-19), zona horaria (B-20). Quedan en 09 para después de la entrega.

### Ciclo extra (2026-07-10) — Ingresos, traza de stock, métricas y guardas ✅

| # | Tarea | Estructura / Nota | Estado |
|---|---|---|---|
| E1 | **RF-DS-03 Pila en Java** (`tad/Pila`+`PilaEnlazada`, TAD propio) para deshacer el último ingreso de stock | Pila LIFO, test `PilaEnlazadaTest` | ✅ |
| E2 | Entidad `MovimientoStock` + `POST /stocks/deshacer` + `GET /stocks/movimientos` ("Últimos Ingresos") | requiere `db/001_movimientos_stock.sql` | ✅ |
| E3 | Módulo de ingresos: `/reportes/ingresos/comparativa`, `/ingresos/semanal`, `/boletas` (DTOs en `BigDecimal`) | reutiliza `sumarVentasEntre` | ✅ |
| E4 | `GET /pedidos/mis-metricas` (turno del mesero: pedidos, total, ticket promedio, estrella) | — | ✅ |
| E5 | Usuarios: `PUT`/`DELETE /admin/usuarios/{id}`; borrado **lógico** (`activo`); guardas 409 (auto-borrado, último admin); quitado `DELETE` legacy con body y `CascadeType.REMOVE` | requiere `db/002_usuarios_activo.sql` | ✅ |

> **Antes de arrancar el back tras este ciclo:** ejecutar en Supabase `backend/db/001` y `002`
> (`ddl-auto: validate`). Suite completa: **55 tests verdes** (`./mvnw test`). Pendiente de nota:
> demo e2e integrada. `DELETE` legacy de usuarios eliminado — avisar a Jose/Iam/Jonathan en el PR.

### Ciclo 2 (2026-07-10) — Pila del carrito a Java, dinero, endurecimiento ✅

| # | Tarea | Estructura / Nota | Estado |
|---|---|---|---|
| E6 | **RF-DS-03 Pila del carrito movida a Java** (`CarritoUndoService`, `/pedidos/carrito/{push,undo}`); se vacía al confirmar | Pila propia por mesero | ✅ |
| E7 | **Dinero a `BigDecimal`/`numeric(10,2)`** (B-18): entidades, DTOs y sumas; matriz mensual queda en `double` | requiere `db/003_dinero_numeric.sql` | ✅ |
| E8 | Test de contrato `PUT`/`DELETE /admin/usuarios/{id}` (200/403/409) | blinda edición de roles | ✅ |

> **Antes de arrancar:** ejecutar además `backend/db/003`. Suite: **63 tests verdes**.
> `frontend/js/utils/pila.js` se eliminó (la Pila del carrito ahora vive en el back).

---
Anterior: [« 07 · Seguridad](07_security.md) · Siguiente: [09 · Backlog de brechas »](09_backlog_brechas.md)
