# MEMORY — Backend (memoria viva)

Contexto durable del backend para retomar trabajo rápido. Mantener al día.

## Qué es

API REST Spring Boot del POS de cafetería. Capas Controller→Service→Repository→Entity.
PostgreSQL en Supabase. Auth JWT stateless. Detalle en [00_index.md](00_index.md).

## Cómo correrlo

- Java 21, Maven wrapper: `./mvnw spring-boot:run` desde `backend/`.
- **Config por variables de entorno** (B-02 ✅): `application.yaml` lee `${DB_URL}`,
  `${DB_USERNAME}`, `${DB_PASSWORD}`, `${JWT_SECRET}`, `${JWT_EXPIRATION}` desde un archivo
  `backend/.env` (gitignored). Copiar `backend/.env.example` → `backend/.env` y rellenar con
  los valores reales de **Supabase** (cadena del Connection Pooler, `?sslmode=require`).
- Compila (`DeleteUserDTO` ya existe, B-01 ✅).
- CORS: el front debe servirse en `http://localhost:5500` (Live Server).
- **Seed mínimo de BD** (ddl-auto crea tablas, no datos): filas en `roles` (`ADMIN`, `MESERO`),
  un usuario ADMIN y uno MESERO (BCrypt), al menos 1 categoría y 1 almacén. `10_db_scripts.md`
  ya trae seed idempotente de categorías/almacenes (antes solo sembraba roles).

## Convenciones

- JSON camelCase · SQL snake_case · sin envelope de respuesta · DTOs (records) en el borde.
- Entidades en plural PascalCase (`Productos`, `Pedidos`). Métodos en español.
- Excepciones de negocio en `exception/`; solo `StockInsuficienteException` tiene handler.

## Gotchas (no tropezar de nuevo)

- **Rol admin canónico = `ADMIN`** (B-03 ✅): `@PreAuthorize` usa `ADMIN`; la fila en `roles`
  debe llamarse `ADMIN`. `UserDetailsServiceImpl` carga authorities desde BD.
- `POST /api/pedidos` request = **solo** `{ detalles:[...] }`: el usuario sale del JWT y el
  `aliasTicket` lo genera el back (B-14 ✅).
- `GET /api/pedidos` lista los pedidos del mesero logueado con `total` (B-07/B-08 ✅).
- Usuarios viven en `/api/admin/usuarios` (B-06 ✅); `registrar` acepta `rol` y puede crear ADMIN
  (B-12 ✅); `UsuarioResponseDTO` trae `usuarioId` y `rol`.
- CRUD admin disponible: `/api/admin/{productos,insumos,stocks}` (B-04 ✅).
- Listados para poblar selects del front: `GET /api/admin/categorias`
  (`[{categoriaId, nombreCategoria}]`) y `GET /api/admin/almacenes`
  (`[{almacenId, nombreAlmacen}]`, mapea `codigoAlmacen → almacenId`), ambos `ADMIN`.
  Cierran F5/F6 del front (ver `SOLICITUD_FRONT_endpoints_listas.md`).
- Endpoint debug `/api/auth/get` eliminado (B-11 ✅).
- `GET /api/productos` lo leen MESERO **y** ADMIN (B-10 ✅, Sprint 2).
- `GET /api/admin/dashboard` (B-05 ✅, Sprint 2): KPIs admin →
  `{ totalVentasDia, totalPedidosDia, productoEstrella, stockBajo:[{nombreInsumo, cantidad, unidad}] }`.
  Ventas/pedidos = del día; producto estrella = más vendido del mes; stock bajo = `cantidad < 10`
  (umbral constante, `Insumos` no modela `unidad` → llega `null`).
- **`GET /api/admin/reportes/mensual` SÍ existe** (`ReporteController` + `ReporteServiceImpl.java:50`,
  matriz `double[][]`, RF-DS-02). La nota antigua de "pendiente / rompe el `Promise.all`" **ya no aplica**.
- **DDL primero (crítico)**: `ddl-auto: validate` no crea tablas. Antes de arrancar tras estos
  cambios hay que ejecutar en Supabase `backend/db/001_movimientos_stock.sql` y
  `002_usuarios_activo.sql` (ver `backend/db/README.md`), o el arranque falla la validación.
- **Módulo de ingresos** (extiende `ReporteController`, ADMIN): `GET /reportes/ingresos/comparativa?anio&mes`
  (mes vs anterior, `variacionPorcentual` null si el previo fue 0), `GET /reportes/ingresos/semanal?desde&hasta`,
  `GET /reportes/boletas?desde&hasta`. DTOs exponen dinero como `BigDecimal` (solo presentación; la
  acumulación sigue en `double`).
- **Stock con traza + deshacer** (RF-DS-03): `POST /admin/stocks` ahora registra un `MovimientoStock`
  y lo apila; `GET /admin/stocks/movimientos` ("Últimos Ingresos") y `POST /admin/stocks/deshacer`
  (LIFO: revierte saldo y marca `REVERSADO`; 409 si el saldo ya se consumió o la pila está vacía).
  La **Pila propia** (`tad/Pila`+`PilaEnlazada`) reside en `StockUndoServiceImpl` (singleton, estado
  en memoria de UNA instancia) y se rehidrata al arrancar con `StockUndoBootstrapRunner`.
- **Métricas del mesero**: `GET /api/pedidos/mis-metricas` (MESERO, del día) →
  `{ pedidosAtendidos, totalVendido, ticketPromedio, productoEstrella }`.
- **Usuarios**: contrato alineado con el front → `PUT /api/admin/usuarios/{id}` y
  `DELETE /api/admin/usuarios/{id}` (el viejo `DELETE` con correo en el body y `DeleteUserDTO`
  **fueron eliminados**). Borrado = **lógico** (`usuarios.activo`, un inactivo no loguea ni se lista;
  quitado `CascadeType.REMOVE` sobre `pedidos` para no perder el histórico). Guardas 409
  (`ReglaNegocioException`): no auto-eliminarse, no eliminar al último ADMIN.
- **Pila del carrito (RF-DS-03) en Java** (2026-07-10): `CarritoUndoService` (una `PilaEnlazada`
  por mesero, en memoria) + `POST /api/pedidos/carrito/{push,undo}`; se vacía al confirmar el pedido.
  La Pila de JS (`frontend/js/utils/pila.js`) **se eliminó**.
- **Dinero en `BigDecimal`** (B-18 ✅, 2026-07-10): `productos.precio_actual` y
  `detalle_pedido.precio_unitario` son `numeric(10,2)` (correr `db/003`); entidades, DTOs y sumas de
  ventas usan `BigDecimal`. La matriz del reporte mensual se queda en `double` (display derivado).
  Insumos aún enteros (B-19).
- Esquema por `ddl-auto: validate`; migraciones a mano en `backend/db/*.sql` (B-21, sin Flyway).
  **Correr `001`, `002` y `003` en Supabase antes de arrancar.**

## Decisiones

- SDD brownfield: el código es la verdad as-built; la spec
  (`PROMPT_MAESTRO_CAFETERIA_MYPE.md`) arbitra alcance y contrato. DDL real de Supabase
  = verdad del modelo de datos.
- Estructuras del sílabo en backend: **matriz** (reporte mensual, RF-DS-02), **cola FIFO+prioridad**
  de despacho (RF-DS-04) y ahora **Pila** (deshacer ingreso de stock, RF-DS-03 aplicada también en
  Java, con consumidor real). Ver [02](02_requirements.md).
- Deshacer marca `estado=REVERSADO`, no borra: "Últimos Ingresos" es un filtro trivial y se
  conserva la auditoría. La Pila es la estructura residente; la tabla es la verdad durable.

## Próximos pasos

Sprints S1/S2 cerrados. Añadido este ciclo: traza+deshacer de stock (Pila RF-DS-03), módulo de
ingresos (comparativa/semanal/boletas), métricas del mesero, y guardas + borrado lógico de
usuarios. **Antes de arrancar: correr `backend/db/001` y `002` en Supabase.** 55 tests en verde
(`./mvnw test`). Deuda viva: dinero a `BigDecimal`/`numeric` en el esquema (B-18) y migraciones
versionadas con Flyway (B-21).
