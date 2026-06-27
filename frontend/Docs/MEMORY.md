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

## Gotchas (no tropezar de nuevo)

- **Rol admin**: `router.js` usa `ADMINISTRADOR`, el resto `ADMIN`. Unificar a `ADMIN` (B-F-04).
- **Crear pedido roto**: body `{detalle:[...]}` no coincide con el back (`detalles`+`usuarioId`+`aliasTicket`) — B-F-01.
- **Boleta rota**: `boleta.js` apunta a `#boleta-contenido`, el HTML tiene `#boleta-detalle` — B-F-02.
- **Estados de pedido** = fuera de alcance, a quitar (B-F-05).
- Casi toda la sección **admin depende de endpoints `/admin/*` que el back no expone aún**.
- `usuarios.js` parte el nombre por espacios (corrompe compuestos) y lee `rol`/`usuarioId` que el DTO no trae.

## Decisiones

- El front se construyó fiel a la spec (`PROMPT_MAESTRO_CAFETERIA_MYPE.md`),
  adelantándose al back. Las divergencias se resuelven con el back, no rompiendo el front.
- Estructura del sílabo en el front: **Pila** para deshacer en el carrito (RF-DS-03).

## Próximos pasos

Arreglar bugs ALTA (B-F-01, B-F-02, B-F-04) y coordinar con el back los endpoints `/admin/*`.
