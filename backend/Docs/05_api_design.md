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

---
Anterior: [« 04 · Base de datos](04_database.md) · Siguiente: [07 · Seguridad »](07_security.md)
