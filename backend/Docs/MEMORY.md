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
  (umbral constante, `Insumos` no modela `unidad` → llega `null`). El front lo consume en
  `dashboard.js` junto a `/api/admin/reportes/mensual` (matriz RF-DS-02, tarea de Jonathan,
  aún pendiente: si falta, el `Promise.all` del front no pinta el panel).
- Dinero como `Double`/`double precision` (B-18); insumos enteros (B-19).
- Esquema por `ddl-auto`, sin migraciones (B-21).

## Decisiones

- SDD brownfield: el código es la verdad as-built; la spec
  (`PROMPT_MAESTRO_CAFETERIA_MYPE.md`) arbitra alcance y contrato. DDL real de Supabase
  = verdad del modelo de datos.
- Estructuras del sílabo a implementar en backend: **matriz** (reporte mensual, RF-DS-02)
  y **cola FIFO+prioridad** de despacho (RF-DS-04). Ver [02](02_requirements.md).

## Próximos pasos

Sprint S1 ✅ completado al 100% (B-01..B-08, B-11, B-12, B-14) + solicitud del front de
listas atendida (`GET /api/admin/categorias` y `/api/admin/almacenes`). **Sprint S2** ([08](08_sprints.md)):
Jose ✅ `GET /api/admin/dashboard` (B-05) y ampliar `GET /api/productos` a ADMIN (B-10).
Pendiente S2: reporte mensual matriz (RF-DS-02) y cola FIFO+prioridad (RF-DS-04, Jonathan;
TAD `tad/Cola`+`tad/ColaPrioridad` aún sin mergear a develop → integración de despacho de
Iam bloqueada), precisión monetaria `BigDecimal` (B-18, Iam, si da tiempo).
