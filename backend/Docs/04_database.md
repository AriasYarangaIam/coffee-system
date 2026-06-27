> SDD coffee-system · Documento 4 de 11 · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 04 · Base de datos — Backend

Fuente de verdad del modelo: **DDL real de Supabase (PostgreSQL)** aportado por el
equipo, contrastado con las entidades JPA en `backend/.../entity/*.java`. El esquema
fue creado por Hibernate `ddl-auto` (nombres de FK autogenerados, p. ej. `fkqf5elo…`).

## Tablas (10)

| Tabla | PK | Columnas | Constraints |
|---|---|---|---|
| `roles` | `rol_id` bigint IDENTITY | `nombre_rol` varchar NOT NULL | — |
| `usuarios` | `usuario_id` bigint IDENTITY | `nombre_usuario`, `apellido_usuario` NOT NULL; `correo_usuario` NOT NULL **UNIQUE**; `clave_cifrada` NOT NULL; `telefono_usuario` (null); `rol_id` NOT NULL | FK `rol_id`→`roles` |
| `categorias` | `categoria_id` bigint IDENTITY | `nombre_categoria` NOT NULL | — |
| `productos` | `producto_id` bigint IDENTITY | `nombre_producto` NOT NULL; `precio_actual` **double precision** (null); `categoria_id` NOT NULL | FK `categoria_id`→`categorias` |
| `insumos` | `id_insumo` bigint IDENTITY | `nombre_insumo` NOT NULL | — |
| `recetas` | `receta_id` bigint IDENTITY | `nombre_receta` NOT NULL; `cantidad_usada` **bigint** NOT NULL; `producto_id`, `insumo_id` NOT NULL | FK →`productos`, →`insumos` |
| `almacenes` | `codigo_almacen` bigint IDENTITY | `nombre_almacen` NOT NULL; `direccion_almacen` (null) | — |
| `stocks` | `codigo_stock` bigint IDENTITY | `cantidad` **bigint** NOT NULL; `almacen_id`, `insumo_id` NOT NULL | FK →`almacenes`, →`insumos` |
| `pedidos` | `pedido_id` bigint IDENTITY | `alias_ticket` NOT NULL **UNIQUE**; `fecha_pedido` **timestamp without time zone** (null); `usuario_id` NOT NULL | FK `usuario_id`→`usuarios` |
| `detalle_pedido` | `detalle_pedido_id` bigint IDENTITY | `cantidad_pedida` bigint NOT NULL **CHECK (≥1)**; `precio_unitario` **double precision** (null); `pedido_id`, `producto_id` NOT NULL | FK →`pedidos`, →`productos` |

## Relaciones

- `usuarios` N→1 `roles`
- `usuarios` 1→N `pedidos`
- `pedidos` 1→N `detalle_pedido` (cascade ALL + orphanRemoval en JPA)
- `detalle_pedido` N→1 `productos`
- `productos` N→1 `categorias`
- `productos` N↔M `insumos` vía `recetas` (`cantidad_usada`)
- `insumos` N↔M `almacenes` vía `stocks` (`cantidad`)

## Diagrama ER (textual)

```
roles ──1:N── usuarios ──1:N── pedidos ──1:N── detalle_pedido ──N:1── productos ──N:1── categorias
                                                                          │
                                                          recetas ──N:1───┘  (N:1── insumos)
                                                                                        │
almacenes ──1:N── stocks ──N:1── insumos ───────────────────────────────────────────────┘
```

## Divergencias entidad JPA ↔ BD y de diseño (→ [09](09_backlog_brechas.md))

| # | Hallazgo | Detalle |
|---|---|---|
| ⚠️ D1 | **Dinero como float** | `productos.precio_actual` y `detalle_pedido.precio_unitario` son `double precision` (entidad `Double`). Debería ser `NUMERIC`/`BigDecimal` (precisión monetaria). |
| ⚠️ D2 | **Insumos enteros** | `recetas.cantidad_usada` y `stocks.cantidad` son `bigint` (entidad `Long`). No modela fraccionarios (0,2 L). La spec pedía `NUMERIC(10,3)`. |
| ⚠️ D3 | **Timestamp sin zona** | `pedidos.fecha_pedido` es `timestamp without time zone`. Sin TZ → ambigüedad horaria. |
| ⚠️ D4 | **Sin migraciones** | Esquema por `ddl-auto`; sin Flyway/Liquibase ni control de versiones del esquema. |
| ⚠️ D5 | **Nombres de PK/FK heterogéneos** | PKs mezclan estilos (`id_insumo`, `codigo_almacen`, `codigo_stock`, `producto_id`). FKs autogeneradas por Hibernate. |
| ℹ️ D6 | **Sin columna `estado` en `pedidos`** | Correcto según alcance (el front la asume de más, ver 09). |

> El DDL idempotente reconstruido vive en [10_db_scripts.md](10_db_scripts.md).

---
Anterior: [« 03 · Arquitectura](03_architecture.md) · Siguiente: [05 · Diseño de API »](05_api_design.md)
