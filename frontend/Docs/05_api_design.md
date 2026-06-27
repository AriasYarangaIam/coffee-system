> SDD coffee-system · Documento 5 de 8 · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 05 · API que consume el frontend

Contrato **visto desde el front** (qué llama, qué espera). El contrato as-built del
servidor está en [backend/Docs/05_api_design.md](../../backend/Docs/05_api_design.md).
✅ = el back lo expone igual · ⚠️ = divergencia · ⏳ = el back aún no lo expone.

Todas las rutas son relativas a `BASE_URL = http://localhost:8080/api`.

## Llamadas reales (por archivo)

| Archivo | Método | Endpoint | Body que envía | Lee de la respuesta | Estado |
|---|---|---|---|---|---|
| `login.js` | POST | `/auth/login` | `{ correo, contraseña }` | `token, correo, rol, nombreCompleto` | ✅ |
| `pedidos.js` | GET | `/productos` | — | `productoId, nombreProducto, categoria, precio, disponible` | ✅ |
| `pedidos.js` | POST | `/pedidos` | **objetivo:** `{ detalles:[{ productoId, cantidadPedida }] }` (hoy envía `detalle`) | `pedidoId` | ⚠️ B-F-01 |
| `boleta.js` | GET | `/pedidos/{id}/boleta` | — | `aliasTicket, fechaPedido, detalle[], total` | ✅ (contrato) / ⚠️ bug DOM B-F-02 |
| `mis-pedidos.js` | GET | `/pedidos` | — | `aliasTicket, detalle[], total, estado, pedidoId` | ⏳/⚠️ B-F-03 |
| `mis-pedidos.js` | PATCH | `/pedidos/{id}/estado` | `{ estado }` | — | ⚠️ fuera de alcance B-F-05 |
| `dashboard.js` | GET | `/admin/dashboard` | — | `totalVentasDia, totalPedidosDia, productoEstrella, stockBajo[]` | ⏳ |
| `dashboard.js` | GET | `/admin/reportes/mensual` | — | `[{ dia, total }]` | ⏳ |
| `productos.js` | GET/POST/PUT/DELETE | `/admin/productos[/{id}]` | `{ nombreProducto, categoria, precio }` | `productoId, nombreProducto, categoria, precio` | ⏳/⚠️ B-F-06 |
| `insumos.js` | GET/POST/PUT/DELETE | `/admin/insumos[/{id}]` | `{ nombreInsumo }` | `idInsumo, nombreInsumo` | ⏳ |
| `stock.js` | GET | `/admin/stocks` | — | `nombreInsumo, cantidad, nombreAlmacen, insumoId` | ⏳ |
| `stock.js` | POST | `/admin/stocks` | `{ insumoId, cantidad, codigoAlmacen }` | — | ⏳ |
| `usuarios.js` | GET/POST/PUT/DELETE | `/admin/usuarios[/{id}]` | `{ nombreUsuario, apellidoUsuario, correoUsuario, rol, claveUsuario? }` | `usuarioId, nombreUsuario, apellidoUsuario, correoUsuario, rol` | ⏳/⚠️ B-F-07 |

## Divergencias críticas con el contrato del back (→ [09](09_backlog_brechas.md))

- **B-F-01 — Crear pedido no coincide.** ✅ **Contrato fijado (2026-06-26):** el request
  es **solo** `{ detalles:[{ productoId, cantidadPedida }] }`. El back toma el usuario del
  JWT y **genera el `aliasTicket`**; `PedidoRequestDTO` quita `usuarioId` y `aliasTicket`.
  → El front solo debe **renombrar `detalle` → `detalles`** (ya no manda usuarioId ni
  aliasTicket). Ver [backend/Docs/05](../../backend/Docs/05_api_design.md).
- **B-F-02 — Boleta no renderiza.** `boleta.js` escribe en `#boleta-contenido`, pero
  `boleta.html` define `#boleta-detalle`, `#boleta-lineas`, `#boleta-total`,
  `#boleta-ticket`, `#boleta-fecha`. El contrato JSON sí coincide (`detalle`, `total`).
- **B-F-03 — Listar pedidos.** El front usa `GET /pedidos` con `estado`/`total`, pero el
  back no expone ese endpoint y `PedidoResponseDTO` no trae `total` ni `estado`.
- **B-F-05 — Estados de pedido.** `PATCH /pedidos/{id}/estado` está **fuera de alcance**.
- **B-F-06 — Productos: categoría.** El front maneja `categoria` como **nombre** (string
  hardcodeado: Bebidas/Comidas/Postres/Otros) pero el modelo del back usa `categoria_id`.
- **B-F-07 — Usuarios: campos faltantes.** El front lee `usuarioId` y `rol`, pero
  `UsuarioResponseDTO` solo trae `nombreUsuario, apellidoUsuario, correoUsuario,
  telefonoUsuario`. Además el front parte el nombre por espacios (corrompe nombres
  compuestos).

## Rutas que el back hoy expone y el front NO usa (o usa distinto)

- `GET /api/productos/{id}/receta` (admin) — sin pantalla que lo consuma.
- `/api/usuario/registrar|obtenerMeseros|delete|actualizar` — el front llama
  `/admin/usuarios` en su lugar (rutas y verbos distintos).

---
Anterior: [« 03 · Arquitectura](03_architecture.md) · Siguiente: [06 · Diseño frontend »](06_frontend_design.md)
