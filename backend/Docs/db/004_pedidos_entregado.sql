-- 004 · Estado de entrega de pedidos (Cola de despacho — RF-DS-04)
-- Ejecutar en Supabase ANTES de arrancar el backend tras añadir el campo `entregado`
-- a la entidad Pedidos (ddl-auto=validate: si la entidad tiene el campo y la columna
-- no existe, la validación falla el arranque).
--
-- Da a cada pedido un sello de "ya atendido/entregado". Sin esta columna, la cola de
-- despacho no podía avanzar (no había dónde marcar el dequeue). Con ella:
--   - el bootstrap rehidrata la cola SOLO con pedidos pendientes (entregado = false);
--   - entregar la cabeza de la cola hace dequeue() y marca entregado = true (persistente).

ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS entregado boolean NOT NULL DEFAULT false;
