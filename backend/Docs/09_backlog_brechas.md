> SDD coffee-system · Documento 9 de 11 · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 09 · Backlog de brechas — Backend ⚠️

Brechas detectadas en Fase 0/1. **Nada de esto se aplicó en código** (fase solo de
documentación). Cada ítem: descripción, evidencia, propuesta, prioridad.
El gemelo del front está en [`../../frontend/Docs/09_backlog_brechas.md`](../../frontend/Docs/09_backlog_brechas.md).

## A. Bloqueantes (prioridad ALTA)

| ID | Brecha | Evidencia | Propuesta |
|---|---|---|---|
| B-01 | **No compila**: `DeleteUserDTO` importado pero el archivo no existe | `UsuarioController` línea 3, `dto/request/` | Crear `public record DeleteUserDTO(String correo) {}` |
| B-02 | **Config faltante**: `application.yaml` solo tiene `application.name`; faltan `datasource`, `jpa`, `jwt.secret`, `jwt.expiration` | `resources/application.yaml`, `JwtUtil` | Añadir `application-local.yaml`/env no versionado + documentar setup; commitear plantilla `application-example.yaml` |
| B-03 | **Nombre de rol ADMIN inconsistente** (`ADMINISTRADOR` vs `ADMIN`) → 403 en endpoints admin si BD guarda `ADMIN` | `@PreAuthorize`, `UserDetailsServiceImpl:22`, front `auth.js`/`router.js` | Canónico **`ADMIN`** (spec Regla #1): alinear `@PreAuthorize`, `router.js` y el seed de `roles` |

## B. Divergencias Front↔Back (prioridad ALTA/MEDIA)

| ID | Brecha | Front espera | Back expone | Propuesta |
|---|---|---|---|---|
| B-04 | Endpoints admin ausentes | `/admin/productos|insumos|stocks` CRUD | solo `GET /api/productos` | Implementar controllers admin bajo `/api/admin/*` (spec §endpoints) |
| B-05 | Dashboard/reportes ausentes | `/admin/dashboard`, `/admin/reportes/mensual` | nada | Implementar con `@Query` JPQL/nativas; el mensual usa **matriz** (RF-DS-02) |
| B-06 | Ruta de usuarios distinta | `/admin/usuarios` (GET/POST/PUT/DELETE) | `/api/usuario/*` (verbos/rutas distintos) | Unificar en `/api/admin/usuarios`; alinear verbos |
| B-07 | Listar pedidos | `GET /pedidos` | no existe | Añadir `GET /api/pedidos` (del mesero logueado) |
| B-08 | `total` en respuesta de pedido | usa `data.total` y `detalle[]` | `PedidoResponseDTO` sin `total`/detalle | Incluir `total` en el listado `GET /api/pedidos`; al crear, el front redirige a la boleta (que sí trae `total`) |
| B-09 | `estado` de pedido | UI PENDIENTE/PAGADO/CANCELADO + `PATCH /pedidos/:id/estado` | sin `estado` (correcto) | **Fuera de alcance** (Regla #6): el **front** quita esa UI/llamada |
| B-10 | `GET /api/productos` solo MESERO | el admin necesita listar productos | `hasAnyRole('MESERO')` | Ampliar a `hasAnyRole('MESERO','ADMIN')` |

## C. Deuda técnica / calidad (prioridad MEDIA/BAJA)

| ID | Brecha | Evidencia | Propuesta |
|---|---|---|---|
| B-11 | Endpoint debug expone hash en consola | `AuthController.test()` `/api/auth/get` | Eliminar |
| B-12 | Registro fija rol a `id=2` (no crea ADMIN) | `UsuarioServiceImpl.registrarUsuario:28` | Recibir rol en el DTO o endpoint admin dedicado |
| B-13 | Manejo de errores incompleto | `GlobalExceptionHandler` solo cubre `StockInsuficienteException`; `ProductoNoEncontradoException` sin handler → 500 | Añadir handlers (404/400) y un cuerpo de error uniforme |
| B-14 | `usuarioId` del request ignorado | `PedidoController`/`PedidoServiceImpl` usan JWT | ✅ **DECIDIDO**: quitar `usuarioId` **y** `aliasTicket` de `PedidoRequestDTO`; el back toma el usuario del JWT y genera el `aliasTicket` (UUID corto/secuencial, spec 3d). Contrato fijado en [05](05_api_design.md) |
| B-15 | Base de usuarios con barra final | `@RequestMapping("/api/usuario/")` | Quitar la barra final |
| B-16 | `actualizarParcial` no valida unicidad de correo | `UsuarioServiceImpl` | Validar `correoUsuario` único antes de `save` |
| B-17 | `import java.sql.Date` sin uso en `Pedidos` | `entity/Pedidos.java` | Limpiar import |

## D. Base de datos (prioridad MEDIA)

| ID | Brecha | Propuesta |
|---|---|---|
| B-18 | Dinero como `double precision` (D1) | Migrar a `NUMERIC(10,2)` / `BigDecimal` |
| B-19 | `cantidad_usada`/`cantidad` `bigint` (D2) | `NUMERIC(10,3)` si se requieren insumos fraccionarios |
| B-20 | `fecha_pedido` sin zona (D3) | `timestamptz` (UTC) |
| B-21 | Sin migraciones versionadas (D4) | Adoptar Flyway/Liquibase; fijar `ddl-auto=validate` en prod |

## E. Académico — estructuras pendientes (sílabo)

| ID | Estructura | Estado | Nota |
|---|---|---|---|
| B-22 | RF-DS-02 Matriz reporte mensual (U1) | ⏳ | implementar junto con B-05 |
| B-23 | RF-DS-04 Cola de despacho FIFO+prioridad (U3) | ⏳ | TAD propio en backend; cola lógica, sin estados de cocina |
| B-24 | *(opcional)* Lista enlazada en detalle de pedido (U2) | 💡 | cubriría U2 del sílabo |
| B-25 | *(opcional)* Árbol Categoría→Producto / ABB de búsqueda (U4) | 💡 | cubriría U4 del sílabo |

---
Anterior: [« 08 · Sprints](08_sprints.md) · Siguiente: [10 · Scripts de BD »](10_db_scripts.md)
