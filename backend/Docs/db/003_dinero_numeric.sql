-- 003 · Precisión monetaria (B-18)
-- Ejecutar en Supabase ANTES de arrancar el backend tras migrar el dinero a BigDecimal
-- (ddl-auto=validate: si la entidad usa BigDecimal y la columna sigue en double precision,
-- la validación falla el arranque).
--
-- Pasa las columnas de dinero de double precision a numeric(10,2): sin arrastre de coma
-- flotante en precios ni en las sumas de ventas.

ALTER TABLE productos      ALTER COLUMN precio_actual   TYPE numeric(10,2);
ALTER TABLE detalle_pedido ALTER COLUMN precio_unitario TYPE numeric(10,2);
