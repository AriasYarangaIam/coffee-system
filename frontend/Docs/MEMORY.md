# MEMORY — Frontend (memoria viva)

Contexto durable del frontend para retomar trabajo rápido. Mantener al día.

## Qué es

UI web Vanilla JS (sin framework, sin bundler) del POS de cafetería. Multi-página:
un `.html` por pantalla + un controlador en `js/pages/`. Detalle en [00_index.md](00_index.md).

## Cómo correrlo

- Servir con **Live Server** en `http://localhost:5500` (lo exige el CORS del back).
- El back debe correr en `http://localhost:8080` (ver [backend/Docs](../../backend/Docs/MEMORY.md)).
- Sin build ni `npm install`. Chart.js y fuentes vienen por CDN.

## Convenciones

- Llamadas siempre vía `apiFetch` (`js/core/api.js`); JSON camelCase; sin envelope.
- Sesión en `localStorage`: `jwt_token` + `usuario` ({correo, rol, nombreCompleto}).
- Cada página: `requireRole(...)` arriba, render con `innerHTML`, feedback con toast/spinner.

## Estado Sprint 1 — ✅ CERRADO (F1–F6)

- ✅ **F1–F4** (commit `494b839`):
  - F1 crear pedido envía `{detalles:[...]}` (B-F-01).
  - F2 boleta renderiza en los IDs reales del HTML (B-F-02).
  - F3 rol canónico `ADMIN` en `router.js` (B-F-04).
  - F4 estados de pedido eliminados; `mis-pedidos` es histórico (B-F-05).
- ✅ **F5–F6** (commit `e887998`): productos usa `categoriaId`/`precioActual`/`nombreCategoria`
  y puebla el select desde `GET /admin/categorias`; stock puebla `select-almacen` desde
  `GET /admin/almacenes` y envía `almacenId` (B-F-06, B-F-09). El back entregó esos GET +
  seed en su commit `5aea1ae`; el contrato coincide, F5/F6 funcionan end-to-end.

## Gotchas (no tropezar de nuevo)

- El back expone `/admin/{productos,insumos,stocks,usuarios,categorias,almacenes,dashboard}` y
  **`/admin/reportes/mensual` ya existe** (la nota antigua de que rompía el `Promise.all` de
  `dashboard.js` **ya no aplica**). `GET /api/productos` lo lee ADMIN además de MESERO.
- **La sesión guarda `nombreCompleto`**, no `nombre`. Los sidebars leen
  `obtenerUsuario().nombreCompleto` (antes leían `.nombre` y el nombre salía vacío en todas las páginas).
- **Pantalla nueva `pages/admin/ingresos.html`** (BI admin): comparativa mes vs anterior
  (`/admin/reportes/ingresos/comparativa`), gráfico semanal (`/admin/reportes/ingresos/semanal`,
  Chart.js) y tabla de boletas con filtro de fechas (`/admin/reportes/boletas`). Enlazada en el
  sidebar de las 6 páginas admin.
- **Stock**: "Últimos Ingresos" se puebla desde `/admin/stocks/movimientos` (antes era un spinner
  eterno, código que no existía) y el botón "Deshacer" llama `/admin/stocks/deshacer`.
- **Mesero**: `mis-pedidos.js` cablea el modal de detalle, el botón Refrescar y las métricas del
  turno (`/pedidos/mis-metricas`, KPIs arriba de la tabla). El POS (`pedidos.js`) suma filtro por
  categoría (chips) y pulso al agregar.
- **Pila del carrito ahora en el backend** (2026-07-10): `pedidos.js` hace push/undo contra
  `/pedidos/carrito/{push,undo}`; cada acción del carrito es una ida al servidor. Se borró
  `js/utils/pila.js`. RF-DS-03 pasó a Java.
- **Dinero llega como número JSON** desde `BigDecimal` (`12.30`); `formatearMoneda` y Chart.js no
  cambian. `api.js` ahora también trata el **403** (toast "No tienes permiso").
- **Responsive**: `layout.css` tiene una media query ≤768px (sidebar → barra superior; grids apilados).
- **Usuarios**: editar/eliminar apuntan a `/admin/usuarios/{id}` (antes rotos). La propia fila del
  admin no ofrece "Eliminar" (badge "Tú"); el backend igual lo rechaza con 409.
- Render con `innerHTML` escapa datos de usuario con `escaparHtml` (`js/utils/dom.js`) — usarlo en
  toda tabla nueva (B-F-11).

## Decisiones

- El front se construyó fiel a la spec (`PROMPT_MAESTRO_CAFETERIA_MYPE.md`),
  adelantándose al back. Las divergencias se resuelven con el back, no rompiendo el front.
- Estructura del sílabo en el front: **Pila** para deshacer en el carrito (RF-DS-03).

## Próximos pasos

Sprints 1 y 2 cerrados. Añadido este ciclo: pantalla de ingresos (BI admin), historial+deshacer
de stock, métricas y modal del mesero, guarda de auto-borrado en usuarios, y el arreglo del nombre
en el sidebar. **Requiere el backend al día** (con `db/001` y `db/002` aplicados en Supabase).
Deuda viva: escapar `innerHTML` en las tablas antiguas (B-F-11), manejo de 403 (B-F-13) y
responsive/media queries (no hay ninguna hoy).
