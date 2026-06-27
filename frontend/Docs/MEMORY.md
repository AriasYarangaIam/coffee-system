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

## Estado Sprint 1

- ✅ **F1–F4 hechos** (commit `494b839`, rama `docs/metodologia-sdd`):
  - F1 crear pedido envía `{detalles:[...]}` (B-F-01).
  - F2 boleta renderiza en los IDs reales del HTML (B-F-02).
  - F3 rol canónico `ADMIN` en `router.js` (B-F-04).
  - F4 estados de pedido eliminados; `mis-pedidos` es histórico (B-F-05).
- ⏳ **F5–F6 pospuestos**: dependen de endpoints que el back aún no expone (categorías
  con `categoriaId`, almacenes). Esperando el Sprint 1 del backend.

## Gotchas (no tropezar de nuevo)

- Casi toda la sección **admin depende de endpoints `/admin/*` que el back no expone aún**.
- `usuarios.js` parte el nombre por espacios (corrompe compuestos) y lee `rol`/`usuarioId` que el DTO no trae.

## Decisiones

- El front se construyó fiel a la spec (`PROMPT_MAESTRO_CAFETERIA_MYPE.md`),
  adelantándose al back. Las divergencias se resuelven con el back, no rompiendo el front.
- Estructura del sílabo en el front: **Pila** para deshacer en el carrito (RF-DS-03).

## Próximos pasos

Esperando el Sprint 1 del back para retomar F5/F6 + cableado admin. F7 (Pila para
deshacer en el carrito, RF-DS-03) es 100% front y se puede adelantar sin el back.
