> SDD coffee-system · Documento 10 de 11 · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 10 · Scripts de BD

DDL **reconstruido del esquema real de Supabase** (no de la spec), normalizado e
idempotente. Orden de ejecución respetando dependencias de FK. Refleja el estado
**as-built**; las mejoras (tipos monetarios, etc.) están en [09](09_backlog_brechas.md)
y **no** se aplican aquí.

> Hoy el esquema lo genera Hibernate `ddl-auto`. Este script sirve para reproducir la
> BD en un entorno limpio o como base de futuras migraciones versionadas.

## Orden de ejecución

`roles` → `usuarios` → `categorias` → `productos` → `insumos` → `recetas` →
`almacenes` → `stocks` → `pedidos` → `detalle_pedido`.

## Script

```sql
-- 1. ROLES
CREATE TABLE IF NOT EXISTS roles (
  rol_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre_rol  VARCHAR(20) NOT NULL
);

-- 2. USUARIOS
CREATE TABLE IF NOT EXISTS usuarios (
  usuario_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre_usuario    VARCHAR(100) NOT NULL,
  apellido_usuario  VARCHAR(100) NOT NULL,
  correo_usuario    VARCHAR(150) NOT NULL UNIQUE,
  clave_cifrada     VARCHAR(255) NOT NULL,          -- BCrypt
  telefono_usuario  VARCHAR(20),
  rol_id            BIGINT NOT NULL REFERENCES roles(rol_id)
);

-- 3. CATEGORIAS
CREATE TABLE IF NOT EXISTS categorias (
  categoria_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre_categoria  VARCHAR(100) NOT NULL
);

-- 4. PRODUCTOS
CREATE TABLE IF NOT EXISTS productos (
  producto_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre_producto VARCHAR(150) NOT NULL,
  precio_actual   DOUBLE PRECISION,                 -- ⚠️ ver B-18 (debería ser NUMERIC)
  categoria_id    BIGINT NOT NULL REFERENCES categorias(categoria_id)
);

-- 5. INSUMOS
CREATE TABLE IF NOT EXISTS insumos (
  id_insumo     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre_insumo VARCHAR(150) NOT NULL
);

-- 6. RECETAS (producto N↔M insumo)
CREATE TABLE IF NOT EXISTS recetas (
  receta_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre_receta  VARCHAR(150) NOT NULL,
  cantidad_usada BIGINT NOT NULL,                   -- ⚠️ ver B-19 (sin decimales)
  producto_id    BIGINT NOT NULL REFERENCES productos(producto_id),
  insumo_id      BIGINT NOT NULL REFERENCES insumos(id_insumo)
);

-- 7. ALMACENES
CREATE TABLE IF NOT EXISTS almacenes (
  codigo_almacen    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre_almacen    VARCHAR(100) NOT NULL,
  direccion_almacen VARCHAR(200)
);

-- 8. STOCKS (insumo por almacén)
CREATE TABLE IF NOT EXISTS stocks (
  codigo_stock BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  cantidad     BIGINT NOT NULL,                     -- ⚠️ ver B-19
  almacen_id   BIGINT NOT NULL REFERENCES almacenes(codigo_almacen),
  insumo_id    BIGINT NOT NULL REFERENCES insumos(id_insumo)
);

-- 9. PEDIDOS
CREATE TABLE IF NOT EXISTS pedidos (
  pedido_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  alias_ticket VARCHAR(50) NOT NULL UNIQUE,
  fecha_pedido TIMESTAMP,                            -- ⚠️ ver B-20 (sin zona)
  usuario_id   BIGINT NOT NULL REFERENCES usuarios(usuario_id)
);

-- 10. DETALLE_PEDIDO
CREATE TABLE IF NOT EXISTS detalle_pedido (
  detalle_pedido_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  cantidad_pedida   BIGINT NOT NULL CHECK (cantidad_pedida >= 1),
  precio_unitario   DOUBLE PRECISION,               -- ⚠️ ver B-18
  pedido_id         BIGINT NOT NULL REFERENCES pedidos(pedido_id),
  producto_id       BIGINT NOT NULL REFERENCES productos(producto_id)
);
```

## Seed mínimo (roles)

```sql
-- Canónico: ADMIN / MESERO (ver B-03). Ajustar según resolución del nombre de rol.
INSERT INTO roles (nombre_rol)
SELECT 'ADMIN'  WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nombre_rol = 'ADMIN');
INSERT INTO roles (nombre_rol)
SELECT 'MESERO' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nombre_rol = 'MESERO');
```

> ⚠️ `UsuarioServiceImpl.registrarUsuario` asume `rol_id = 2` = MESERO. El orden de
> inserción de arriba lo respeta solo si `ADMIN` queda con id 1 y `MESERO` con id 2.

## Seed mínimo (categorías y almacenes)

Necesario para poblar los `<select>` del front (tareas F5/F6). Idempotente.

```sql
INSERT INTO categorias (nombre_categoria)
SELECT v FROM (VALUES ('Bebidas'),('Comidas'),('Postres'),('Otros')) AS s(v)
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nombre_categoria = s.v);

INSERT INTO almacenes (nombre_almacen)
SELECT 'Almacén Principal'
WHERE NOT EXISTS (SELECT 1 FROM almacenes WHERE nombre_almacen = 'Almacén Principal');
```

---
Anterior: [« 09 · Backlog de brechas](09_backlog_brechas.md) · Fin del set numerado. Ver [MEMORY](MEMORY.md) y [design](design.md).
