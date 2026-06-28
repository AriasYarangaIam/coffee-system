# 11 · Seed de la carta (demo)

Carta de ejemplo para el demo: insumos, almacén + stock, productos y sus recetas. Todo
**idempotente** (`NOT EXISTS`) e **independiente de IDs** (vincula por nombre con subconsultas),
así que puedes correrlo varias veces sin duplicar.

> **Prerrequisito:** corre antes el seed de categorías de [10_db_scripts.md](10_db_scripts.md)
> (necesita existir la categoría `Bebidas`). El admin puede crear/editar el resto desde la UI
> (Productos → la receta se edita en el mismo modal).

## Orden de ejecución

`insumos → almacén → stocks → productos → recetas`.

```sql
-- 0. (una sola vez) columna de unidad de medida en insumos.
ALTER TABLE insumos ADD COLUMN IF NOT EXISTS unidad VARCHAR(10);

-- 1. INSUMOS (materia prima) con su unidad de medida
INSERT INTO insumos (nombre_insumo, unidad)
SELECT v.nombre, v.unidad FROM (VALUES
  ('Café','g'), ('Leche','ml'), ('Azúcar','g'), ('Agua','ml'),
  ('Chocolate','g'), ('Crema','ml'), ('Té','g'), ('Hielo','g')
) AS v(nombre, unidad)
WHERE NOT EXISTS (SELECT 1 FROM insumos WHERE nombre_insumo = v.nombre);

-- Backfill: si ya tenías estos insumos sin unidad (seed viejo), se la asignamos.
UPDATE insumos SET unidad = 'g'
  WHERE unidad IS NULL AND nombre_insumo IN ('Café','Azúcar','Chocolate','Té','Hielo');
UPDATE insumos SET unidad = 'ml'
  WHERE unidad IS NULL AND nombre_insumo IN ('Leche','Agua','Crema');

-- 2. ALMACÉN
INSERT INTO almacenes (nombre_almacen)
SELECT 'Almacén Principal'
WHERE NOT EXISTS (SELECT 1 FROM almacenes WHERE nombre_almacen = 'Almacén Principal');

-- 3. STOCKS: una existencia por insumo en el Almacén Principal (cantidad alta para el demo)
INSERT INTO stocks (cantidad, almacen_id, insumo_id)
SELECT 100000,
       (SELECT codigo_almacen FROM almacenes WHERE nombre_almacen = 'Almacén Principal'),
       i.id_insumo
FROM insumos i
WHERE NOT EXISTS (SELECT 1 FROM stocks s WHERE s.insumo_id = i.id_insumo);

-- 4. PRODUCTOS (categoría Bebidas)
INSERT INTO productos (nombre_producto, precio_actual, categoria_id)
SELECT p.nombre, p.precio,
       (SELECT categoria_id FROM categorias WHERE nombre_categoria = 'Bebidas')
FROM (VALUES
  ('Espresso', 6.0),
  ('Americano', 7.0),
  ('Capuchino', 9.0),
  ('Latte', 10.0),
  ('Mocha', 11.0),
  ('Té', 5.0)
) AS p(nombre, precio)
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre_producto = p.nombre);

-- 5. RECETAS (la carta: qué insumos y cuánto usa cada producto)
INSERT INTO recetas (nombre_receta, cantidad_usada, producto_id, insumo_id)
SELECT r.prod || ' - ' || r.ins, r.cant,
       (SELECT producto_id FROM productos WHERE nombre_producto = r.prod),
       (SELECT id_insumo   FROM insumos   WHERE nombre_insumo   = r.ins)
FROM (VALUES
  ('Espresso',  'Café',       18),
  ('Americano', 'Café',       18),
  ('Americano', 'Agua',      200),
  ('Capuchino', 'Café',       18),
  ('Capuchino', 'Leche',     150),
  ('Latte',     'Café',       18),
  ('Latte',     'Leche',     200),
  ('Mocha',     'Café',       18),
  ('Mocha',     'Leche',     150),
  ('Mocha',     'Chocolate',  30),
  ('Té',        'Té',          5),
  ('Té',        'Agua',      200)
) AS r(prod, ins, cant)
WHERE NOT EXISTS (SELECT 1 FROM recetas WHERE nombre_receta = r.prod || ' - ' || r.ins);
```

## Verificación rápida

```sql
SELECT p.nombre_producto, i.nombre_insumo, r.cantidad_usada
FROM recetas r
JOIN productos p ON p.producto_id = r.producto_id
JOIN insumos   i ON i.id_insumo   = r.insumo_id
ORDER BY p.nombre_producto, i.nombre_insumo;
```

Al vender un producto, `PedidoServiceImpl.registrarPedido` descuenta del stock cada insumo de su
receta (`cantidad_usada × cantidad_pedida`). Si no alcanza, devuelve `409` (StockInsuficiente).

---
Anterior: [« 10 · Scripts de BD](10_db_scripts.md)
