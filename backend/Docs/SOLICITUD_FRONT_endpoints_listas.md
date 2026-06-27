# Solicitud del Frontend — Endpoints de listas (categorías y almacenes)

> Solicitud del equipo **frontend** al **backend** · Fecha: 2026-06-26 · Bloquea: cierre
> del Sprint 1 front (tareas F5 y F6, ver [frontend/Docs/08_sprints.md](../../frontend/Docs/08_sprints.md)).

## Contexto

El back ya expone el CRUD admin con el contrato nuevo (`/api/admin/productos` con
`categoriaId`/`precioActual`/`nombreCategoria`, `/api/admin/stocks` con `almacenId`). Para
cerrar F5 y F6 el front necesita **poblar dos `<select>`**:

- **F5** — formulario de alta/edición de producto: elegir la **categoría** (`categoriaId`).
- **F6** — formulario de ingreso de stock: elegir el **almacén** (`almacenId`).

Hoy **no existe** un endpoint para listar categorías ni almacenes (no hay
`CategoriaController` ni `AlmacenController`), por lo que el front no tiene de dónde sacar
las opciones. El front ya quedó **cableado a este contrato**; funcionará en cuanto el back
publique los endpoints de abajo.

## Endpoints solicitados

Mismas convenciones que el resto: base `http://localhost:8080/api`, DTO directo (sin
envelope), camelCase, `Authorization: Bearer <jwt>`.

### 1. Listar categorías

| Método | Ruta | Auth | Response 2xx |
|---|---|---|---|
| GET | `/api/admin/categorias` | `ADMIN` | `200` `[{ categoriaId, nombreCategoria }]` |

Los nombres de campo deben coincidir con los que ya devuelve `ProductoAdminResponseDTO`
(`categoriaId`, `nombreCategoria`).

### 2. Listar almacenes

| Método | Ruta | Auth | Response 2xx |
|---|---|---|---|
| GET | `/api/admin/almacenes` | `ADMIN` | `200` `[{ almacenId, nombreAlmacen }]` |

Los nombres de campo deben coincidir con los que ya devuelve `StockResponseDTO`
(`almacenId`, `nombreAlmacen`) — nótese que la PK de la entidad es `codigo_almacen`, pero
el contrato JSON expuesto es `almacenId`.

## Recomendado (para poder probar end-to-end)

Agregar un **seed mínimo** de categorías y almacenes (hoy [10_db_scripts.md](10_db_scripts.md)
solo siembra roles). Sin filas en esas tablas, los `<select>` saldrán vacíos aunque los
endpoints existan. Ejemplo:

```sql
INSERT INTO categorias (nombre_categoria) VALUES ('Bebidas'), ('Comidas'), ('Postres'), ('Otros');
INSERT INTO almacenes (nombre_almacen) VALUES ('Almacén Principal');
```

## Fuera de esta solicitud

- CRUD de categorías/almacenes (crear/editar/eliminar): no se necesita para Sprint 1, solo
  el `GET` de listado.
- El cableado completo de las pantallas admin (dashboard, usuarios, insumos) es Sprint 2.
