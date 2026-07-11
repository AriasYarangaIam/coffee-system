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

## Sprint 1 (26 jun → 02 jul) — Arreglar y limpiar — ✅ COMPLETADO

> **Estado:** F1–F4 en commit `494b839`; F5–F6 en `e887998` (con los GET de
> categorías/almacenes que el back entregó en `5aea1ae`). Sprint 1 cerrado.

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
| F7 | ✅ **RF-DS-03 Pila (deshacer en carrito)**: TAD propio `js/utils/pila.js` (+`pila.test.js`); push al modificar el carrito, botón "Deshacer" = pop | B-F-15 | **Joaquin** |
| F8 | ✅ Cablear **dashboard**: KPIs a `/admin/dashboard` y gráfico **apilado producto×día** a `/admin/reportes/mensual` (matriz RF-DS-02) reales | B-F-08 | **Yefrie** |
| F9 | ✅ Cablear **productos / insumos / stock** a los CRUD `/admin/*` reales | B-F-08 | **Yefrie** |
| F10 | ✅ Cablear **usuarios**: lee `usuarioId`/`rol`; campos Nombre/Apellido separados (sin partir por espacios) | B-F-07 | **Joaquin** |
| F11 | Integración end-to-end con el back + preparar demo | — | **Joaquin + Yefrie** |

> Diferido sin riesgo de nota: unificar patrones de modal (B-F-10), escape XSS (B-F-11),
> búsqueda/filtros (B-F-14). Quedan en [09](09_backlog_brechas.md) para después.

### Ciclo extra (2026-07-10) — Ingresos, stock, mesero y arreglos ✅

| # | Tarea | Nota | Estado |
|---|---|---|---|
| FE1 | **Pantalla nueva `ingresos.html`** (BI admin): comparativa mes vs anterior, gráfico semanal (Chart.js) y tabla de boletas con filtro de fechas | enlazada en el sidebar de las 6 páginas admin | ✅ |
| FE2 | Stock: "Últimos Ingresos" real (`/admin/stocks/movimientos`) + botón "Deshacer" (`/admin/stocks/deshacer`) | antes era un spinner eterno | ✅ |
| FE3 | Mesero: cablear modal de detalle y botón Refrescar en `mis-pedidos`; KPIs del turno (`/pedidos/mis-metricas`); POS con filtro por categoría y pulso al agregar | — | ✅ |
| FE4 | Usuarios: la propia fila del admin no ofrece "Eliminar" (el back devuelve 409 igual) | guarda de cortesía en UI | ✅ |
| FE5 | Fix: el nombre del usuario no salía en ningún sidebar (`.nombre` → `.nombreCompleto`); quitado el enlace muerto "¿Olvidó su contraseña?" del login | transversal a 7 HTML | ✅ |
| FE6 | Helper `escaparHtml` en `js/utils/dom.js`, usado en todo el render nuevo con `innerHTML` (B-F-11 en el código nuevo) | — | ✅ |

### Ciclo 2 (2026-07-10) — Pila al back, XSS, responsive, 403 ✅

| # | Tarea | Nota | Estado |
|---|---|---|---|
| FE7 | **Deshacer del carrito movido al backend**: `pedidos.js` consume `/pedidos/carrito/{push,undo}`; se borró `js/utils/pila.js` y `pila.test.js` | RF-DS-03 ahora en Java | ✅ |
| FE8 | Escape XSS completado en tablas viejas (usuarios/insumos/productos/dashboard) — cierra B-F-11 | — | ✅ |
| FE9 | Responsive: media query en `layout.css` (sidebar → barra superior, grids apilados en ≤768px) | CSS-only | ✅ |
| FE10 | Manejo de 403 en `api.js` (toast "No tienes permiso") — cierra B-F-13 | — | ✅ |
| FE11 | Fix login: el toggle de contraseña usa la clase `.hidden` (SVG no respetaba el atributo) | — | ✅ |

## Definition of Done (front)

- [ ] La pantalla consume el endpoint real del back (sin datos hardcodeados de más).
- [ ] Maneja error y estado vacío (toast/spinner).
- [ ] Respeta rol con `requireRole` y el valor canónico `ADMIN`/`MESERO`.
- [ ] Servida en `:5500` e integrada contra el back en `:8080`.
- [ ] Rama `feat/front/...` y PR hacia `develop` (ver doc maestro).

---
Anterior: [« 07 · Seguridad](07_security.md) · Siguiente: [09 · Backlog de brechas »](09_backlog_brechas.md)
