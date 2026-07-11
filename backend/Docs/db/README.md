# Scripts de esquema (`db/`)

El proyecto usa `spring.jpa.hibernate.ddl-auto: validate`: Hibernate **no crea ni altera** tablas, solo valida que el esquema de Supabase case con las entidades. Por eso cada cambio de esquema vive aquí como un `.sql` numerado y **se ejecuta a mano en Supabase antes de arrancar el backend**. Si una entidad nueva no tiene su tabla, el arranque falla.

No hay Flyway todavía (deuda B-21); el prefijo numérico emula el orden de versión.

## Orden

| Script | Qué hace | Cuándo |
|--------|----------|--------|
| `001_movimientos_stock.sql` | Crea `movimientos_stock` (traza de ingresos de stock, RF-DS-03) | Antes de la entidad `MovimientoStock` |
| `002_usuarios_activo.sql` | Añade `usuarios.activo` (borrado lógico) | Antes de tocar la entidad `Usuarios` |
| `003_dinero_numeric.sql` | Pasa `productos.precio_actual` y `detalle_pedido.precio_unitario` a `numeric(10,2)` (B-18) | Antes de migrar el dinero a `BigDecimal` |

Ejecutar en orden ascendente. Son idempotentes solo si la tabla/columna aún no existe (usa `IF NOT EXISTS` a mano si re-corres).
