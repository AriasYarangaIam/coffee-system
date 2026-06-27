> SDD coffee-system · Documento (08 del esquema SDD) · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 08 · Sprints — Frontend (track del front)

Plan del **frontend** (Joaquin, Yefrie) dentro del plan general de 2 semanas. La
metodología, el flujo Git, la Definition of Done y el **track del backend** están en el
doc maestro [backend/Docs/08_sprints.md](../../backend/Docs/08_sprints.md). La línea de
tiempo es **compartida**.

> ⏰ Quedan 2 semanas (a 2026-06-26). 2 sprints de 1 semana. Foco: arreglar lo roto,
> integrar con el back y dejar funcionando la **estructura de datos del sílabo** (Pila).

## Equipo front

| Integrante | Foco |
|---|---|
| **Joaquin** | Bugs críticos del flujo mesero + estructura de datos (Pila) |
| **Yefrie** | Limpieza de alcance + cableado de pantallas admin a endpoints reales |

## Dependencia con el backend

- Lo que **no depende del back** (bugs de DOM, rol, quitar estados) se hace **ya** en el
  Sprint 1.
- El **cableado admin** (Sprint 2) depende de que el back publique sus endpoints
  `/api/admin/*` y el `GET /api/pedidos` a inicios del Sprint 1 (ver track back, tareas
  4-7). Coordinar el contrato exacto vía [05_api_design.md](05_api_design.md).

## Sprint 1 (26 jun → 02 jul) — Arreglar y limpiar

| # | Tarea | Backlog | Responsable |
|---|---|---|---|
| F1 | Arreglar **crear pedido**: renombrar `detalle` → `detalles` (contrato fijado: el back toma `usuarioId` del JWT y genera `aliasTicket`; el front **no** los envía) | B-F-01 | **Joaquin** |
| F2 | Arreglar **boleta**: alinear `boleta.js` a los IDs reales (`#boleta-detalle`/`#boleta-lineas`/`#boleta-total`) | B-F-02 | **Joaquin** |
| F3 | Unificar rol admin a **`ADMIN`** en `router.js` (coincidir con `auth.js` y back) | B-F-04 | **Joaquin** |
| F4 | **Quitar estados de pedido** (fuera de alcance): acciones Cobrado/Cancelar, columna estado, `PATCH /pedidos/:id/estado`, `renderBadgeEstado`; dejar `mis-pedidos` como histórico | B-F-05 | **Yefrie** |
| F5 | Alinear **categoría**: usar `categoriaId` y poblar el `<select>` desde el back (no hardcode) | B-F-06 | **Yefrie** |
| F6 | Poblar `select-almacen` en stock | B-F-09 | **Yefrie** |

## Sprint 2 (03 jul → 09 jul) — Integrar + estructura de datos

| # | Tarea | Backlog | Responsable |
|---|---|---|---|
| F7 | **RF-DS-03 Pila (deshacer en carrito)**: TAD propio en JS; push al modificar el carrito, botón "Deshacer" = pop | B-F-15 | **Joaquin** |
| F8 | Cablear **dashboard** (KPIs + gráfico mensual) a `/admin/dashboard` y `/admin/reportes/mensual` reales | B-F-08 | **Yefrie** |
| F9 | Cablear **productos / insumos / stock** a los CRUD `/admin/*` reales | B-F-08 | **Yefrie** |
| F10 | Cablear **usuarios**: leer `usuarioId`/`rol`; dejar de partir el nombre por espacios | B-F-07 | **Joaquin** |
| F11 | Integración end-to-end con el back + preparar demo | — | **Joaquin + Yefrie** |

> Diferido sin riesgo de nota: unificar patrones de modal (B-F-10), escape XSS (B-F-11),
> búsqueda/filtros (B-F-14). Quedan en [09](09_backlog_brechas.md) para después.

## Definition of Done (front)

- [ ] La pantalla consume el endpoint real del back (sin datos hardcodeados de más).
- [ ] Maneja error y estado vacío (toast/spinner).
- [ ] Respeta rol con `requireRole` y el valor canónico `ADMIN`/`MESERO`.
- [ ] Servida en `:5500` e integrada contra el back en `:8080`.
- [ ] Rama `feat/front/...` y PR hacia `develop` (ver doc maestro).

---
Anterior: [« 07 · Seguridad](07_security.md) · Siguiente: [09 · Backlog de brechas »](09_backlog_brechas.md)
