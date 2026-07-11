-- 001 · Traza de ingresos de stock (RF-DS-03 / "Últimos Ingresos" del panel de stock)
-- Ejecutar en Supabase ANTES de levantar el backend: ddl-auto=validate falla el
-- arranque si la entidad MovimientoStock existe y esta tabla no.
--
-- Cada fila es un ingreso de stock. Deshacer no borra: marca estado='REVERSADO'
-- (auditoría preservada). codigo_stock apunta a la fila de stocks afectada para
-- poder revertir el saldo directo.

CREATE TABLE movimientos_stock (
    id           BIGSERIAL PRIMARY KEY,
    insumo_id    BIGINT      NOT NULL REFERENCES insumos(id_insumo),
    almacen_id   BIGINT      NOT NULL REFERENCES almacenes(codigo_almacen),
    cantidad     BIGINT      NOT NULL CHECK (cantidad > 0),
    fecha        TIMESTAMP   NOT NULL DEFAULT now(),
    usuario_id   BIGINT      NOT NULL REFERENCES usuarios(usuario_id),
    tipo         VARCHAR(20) NOT NULL DEFAULT 'INGRESO',
    estado       VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    codigo_stock BIGINT      REFERENCES stocks(codigo_stock)
);

-- Sirve tanto a "últimos ingresos" (estado, fecha DESC) como a la rehidratación
-- de la Pila al arrancar (estado, fecha ASC).
CREATE INDEX idx_movstock_estado_fecha ON movimientos_stock(estado, fecha DESC);
