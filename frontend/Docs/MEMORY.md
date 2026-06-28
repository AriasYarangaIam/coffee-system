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

- El back ya expone `/admin/{productos,insumos,stocks,usuarios,categorias,almacenes}` y
  ahora **`/admin/dashboard`** (Sprint 2 ✅: `{ totalVentasDia, totalPedidosDia,
  productoEstrella, stockBajo:[{nombreInsumo, cantidad, unidad}] }`; `unidad` puede venir
  `null`). `GET /api/productos` ya lo lee ADMIN además de MESERO. **Aún falta**
  `/admin/reportes/mensual` (matriz, tarea de Jonathan): `dashboard.js` lo pide en el mismo
  `Promise.all` que `/admin/dashboard`, así que **mientras ese GET no exista el panel no
  pinta** aunque el dashboard ya responda.
- `usuarios.js` parte el nombre por espacios (corrompe compuestos) y lee `rol`/`usuarioId` que el DTO no trae.

## Decisiones

- El front se construyó fiel a la spec (`PROMPT_MAESTRO_CAFETERIA_MYPE.md`),
  adelantándose al back. Las divergencias se resuelven con el back, no rompiendo el front.
- Estructura del sílabo en el front: **Pila** para deshacer en el carrito (RF-DS-03).

## Próximos pasos

Sprint 1 cerrado. Pasar al **Sprint 2 (F7–F11)**, empezando por **F7** (Pila para deshacer
en el carrito, RF-DS-03) que es 100% front y no depende del back.
