> SDD coffee-system · Documento 5 de 11 · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 05 · Diseño de API — Backend (contrato REST as-built)

**Este es el contrato crítico que el frontend consume.** Documenta lo que el back
expone HOY. Lo que el front consume pero el back no expone aún está al final (⏳) y en
[09](09_backlog_brechas.md).

## Convenciones reales

- **Base URL:** `http://localhost:8080/api`
- **Envelope:** ⚠️ **ninguno**. Éxito = DTO directo. No hay `{success,data,message}`.
- **Naming JSON:** camelCase.
- **Auth:** header `Authorization: Bearer <jwt>`. Público solo `/api/auth/**`.
- **Errores:** solo `StockInsuficienteException` está estandarizado:
  `409` + `{ "error": "<mensaje>" }` (`GlobalExceptionHandler`). El resto de
  `RuntimeException` (usuario/producto/pedido no encontrado) cae al **500** por defecto
  de Spring. `ProductoNoEncontradoException` **no** tiene handler. Ver 09.

## Endpoints implementados

### Auth — `AuthController` (`/api/auth`)

| Método | Ruta | Auth | Request | Response 2xx |
|---|---|---|---|---|
| POST | `/api/auth/login` | público | `{ correo, contraseña }` (`LoginRequestDTO`) | `200` `{ token, correo, rol, nombreCompleto }` (`LoginResponseDTO`) |
| GET | `/api/auth/get` | público | — | `200` `"Test aprobado con éxito"` ⚠️ endpoint de debug (imprime un hash en consola) — quitar (09) |

> El campo de contraseña se llama literalmente `contraseña` (con ñ) en request — el
> front lo envía igual. ✅ coinciden.

### Pedidos — `PedidoController` (`/api/pedidos`)

| Método | Ruta | Auth | Request | Response 2xx | Errores |
|---|---|---|---|---|---|
| POST | `/api/pedidos` | `ROLE_MESERO` | `{ detalles:[{ productoId, cantidadPedida }] }` | `201` `{ pedidoId, aliasTicket, fechaPedido }` (`PedidoResponseDTO`) | `409 {error}` stock insuficiente |
| GET | `/api/pedidos/{id}/boleta` | `ROLE_MESERO` | — | `200` `{ pedidoId, aliasTicket, fechaPedido, detalle:[{ nombreProducto, cantidadPedida, precioUnitario }], total }` (`BoletaResponseDTO`) | `500` si no existe |

> ✅ **CONTRATO FIJADO (decisión de equipo, 2026-06-26):** en `POST /api/pedidos` el
> request es **solo** `{ detalles:[...] }`. El back **deriva el usuario del JWT**
> (`userDetails.getUsername()`) y **genera el `aliasTicket`** (UUID corto/secuencial, per
> spec §lógica de venta 3d). → `PedidoRequestDTO` debe **quitar `usuarioId` y
> `aliasTicket`** (hoy los pide `@NotNull`; ver B-14 en 09).
> ⚠️ `PedidoResponseDTO` **no** incluye `total` ni `estado` (el front asume `total` en la
> lista de pedidos — divergencia 09).

### Productos — `ProductoController` (`/api/productos`)

| Método | Ruta | Auth | Request | Response 2xx |
|---|---|---|---|---|
| GET | `/api/productos` | `ROLE_MESERO` | — | `200` `[{ productoId, nombreProducto, categoria, precio, disponible }]` (`ProductoListadoResponseDTO`) |
| GET | `/api/productos/{id}/receta` | `ROLE_ADMINISTRADOR` | — | `200` `{ productoId, nombreProducto, insumos:[{ idInsumo, nombreInsumo, cantidadUsada }] }` (`ProductoRecetaResponseDTO`) |

> ⚠️ Listar productos exige `MESERO` (`hasAnyRole('MESERO')`) → un ADMIN **no** puede
> usar este endpoint. Revisar (09).

### Usuarios — `UsuarioController` (base `/api/usuario/`)

| Método | Ruta | Auth | Request | Response 2xx |
|---|---|---|---|---|
| POST | `/api/usuario/registrar` | `ADMINISTRADOR` o `MESERO` | `{ nombreUsuario, apellidoUsuario, correoUsuario, telefonoUsuario, claveCifrada }` | `200` (cuerpo vacío) |
| GET | `/api/usuario/obtenerMeseros` | `ADMINISTRADOR` | — | `200` `[{ nombreUsuario, apellidoUsuario, correoUsuario, telefonoUsuario }]` |
| DELETE | `/api/usuario/delete` | `ADMINISTRADOR` | `{ correo }` (`DeleteUserDTO` ⚠️ falta el archivo) | `200` (vacío) |
| PATCH | `/api/usuario/actualizar` | `ADMINISTRADOR` o `MESERO` | `{ nombreUsuario?, apellidoUsuario?, correoUsuario?, telefonoUsuario? }` | `200` (vacío) |

> ⚠️ Base con barra final `/api/usuario/` + sub-ruta `/registrar` → ruta efectiva con
> doble barra. ⚠️ `registrar` fija el rol a `id=2` (no se puede crear ADMIN). ⚠️
> `actualizar` actúa sobre el usuario del JWT, no sobre un id arbitrario. Ver 09.

## Endpoints que el FRONT consume pero el BACK no expone (⏳ / ⚠️)

| Ruta que llama el front | Método | RF | Estado back |
|---|---|---|---|
| `/admin/dashboard` | GET | RF-11 | ⏳ ausente |
| `/admin/reportes/mensual` | GET | RF-12 | ⏳ ausente |
| `/admin/productos` | GET/POST/PUT/DELETE | RF-13 | ⏳ solo existe `GET /api/productos` |
| `/admin/insumos` | GET/POST/PUT/DELETE | RF-14 | ⏳ ausente |
| `/admin/stocks` | GET/POST | RF-15 | ⏳ ausente |
| `/admin/usuarios` | GET/POST/PUT/DELETE | RF-07..10 | ⚠️ existe pero en `/api/usuario/*` (rutas y verbos distintos) |
| `/pedidos` (listar) | GET | — | ⚠️ no existe (back solo POST y boleta) |
| `/pedidos/{id}/estado` | PATCH | — | ⚠️ fuera de alcance (sin `estado`) |

> Detalle y propuestas de resolución en [09_backlog_brechas.md](09_backlog_brechas.md).

## Addendum 2026-07-10 — Ingresos, stock con traza, métricas y guardas de usuarios

Nuevos endpoints (contrato as-built). Todos bajo `Authorization: Bearer`.

### Ingresos / BI — `ReporteController` (`/api/admin/reportes`, `ROLE_ADMIN`)

| Método | Ruta | Request | Response 2xx |
|---|---|---|---|
| GET | `/api/admin/reportes/ingresos/comparativa?anio&mes` | query opcional (sin params ⇒ mes actual) | `200` `{ anio, mes, totalMesActual, totalMesAnterior, variacionPorcentual }` (`BigDecimal`; `variacionPorcentual` `null` si el mes previo fue 0) |
| GET | `/api/admin/reportes/ingresos/semanal?desde&hasta` | fechas ISO `yyyy-MM-dd` | `200` `{ semanas:[{ inicioSemana, total, variacionPct }] }` |
| GET | `/api/admin/reportes/boletas?desde&hasta` | fechas ISO `yyyy-MM-dd` | `200` `[{ pedidoId, aliasTicket, fecha, mesero, total }]` |

### Stock: histórico y deshacer — `StockController` (`/api/admin/stocks`, `ROLE_ADMIN`)

| Método | Ruta | Request | Response 2xx | Errores |
|---|---|---|---|---|
| POST | `/api/admin/stocks` | `{ insumoId, almacenId, cantidad }` (sin cambios; ahora deriva el autor del JWT y registra la traza) | `201` `StockResponseDTO` | — |
| GET | `/api/admin/stocks/movimientos` | — | `200` `[{ id, insumoId, nombreInsumo, unidad, almacenId, nombreAlmacen, cantidad, fecha, registradoPor, estado }]` | — |
| POST | `/api/admin/stocks/deshacer` | — | `200` `StockResponseDTO` (saldo tras revertir) | `409 {error}` sin ingresos que deshacer o saldo ya consumido |

### Métricas del mesero — `PedidoController` (`/api/pedidos`)

| Método | Ruta | Auth | Response 2xx |
|---|---|---|---|
| GET | `/api/pedidos/mis-metricas` | `ROLE_MESERO` | `200` `{ pedidosAtendidos, totalVendido, ticketPromedio, productoEstrella }` (del día) |

### Usuarios (contrato corregido) — `UsuarioController` (`/api/admin/usuarios`, `ROLE_ADMIN`)

| Método | Ruta | Request | Response 2xx | Errores |
|---|---|---|---|---|
| PUT | `/api/admin/usuarios/{id}` | `{ nombreUsuario?, apellidoUsuario?, correoUsuario?, telefonoUsuario?, rol?, claveUsuario? }` (`ActualizarUsuarioRequestDTO`; `claveUsuario` vacío ⇒ no cambia la clave) | `200` (vacío) | `404` id inexistente |
| DELETE | `/api/admin/usuarios/{id}` | — (autor del JWT) | `200` (vacío) — **borrado lógico** (`activo=false`) | `409 {error}` auto-borrado o último ADMIN |

> El `DELETE /api/admin/usuarios` con `{ correo }` en el body y su `DeleteUserDTO`
> **fueron eliminados** (reemplazados por `DELETE /{id}`). Los errores 409 los emite
> `ReglaNegocioException` vía `GlobalExceptionHandler`. **Nota a backend (Jose/Iam/Jonathan):**
> este cambio quita el endpoint legacy de borrado.

## Addendum 2026-07-10 (2) — Pila del carrito + dinero BigDecimal

### Pila de deshacer del carrito — `PedidoController` (`/api/pedidos`, `ROLE_MESERO`)

RF-DS-03 en Java: la Pila del "Deshacer" del carrito se movió del front al back
(`CarritoUndoService`, una `PilaEnlazada` por mesero en memoria).

| Método | Ruta | Request | Response 2xx |
|---|---|---|---|
| POST | `/api/pedidos/carrito/push` | `{ items:[{ productoId, nombre, precio, cantidad }] }` (`CarritoSnapshotDTO`) | `200` `{ items, profundidad }` |
| POST | `/api/pedidos/carrito/undo` | — | `200` `{ items, profundidad }` (snapshot anterior; `items:[]` si no hay nada que deshacer) |

Al confirmar un pedido (`POST /api/pedidos`) la pila del mesero se vacía.

### Dinero como `BigDecimal` (B-18)

`productos.precio_actual` y `detalle_pedido.precio_unitario` pasaron a `numeric(10,2)`
(DDL `db/003`) y las entidades/DTOs de dinero a `BigDecimal`: precios, totales de boleta
y de listado, `totalVentasDia` del dashboard, y las sumas de ventas. El JSON sigue siendo
numérico (`12.30`), sin cambios para el front. La matriz del reporte mensual se mantiene en
`double` (display derivado).

---
Anterior: [« 04 · Base de datos](04_database.md) · Siguiente: [07 · Seguridad »](07_security.md)
