# design — Decisiones de diseño del backend

Decisiones y patrones del backend, con su porqué. Complementa [03_architecture.md](03_architecture.md).

## Patrones aplicados

| Patrón | Dónde | Por qué |
|---|---|---|
| N-capas (Controller→Service→Repository) | todo el módulo | estándar Spring, separa responsabilidades |
| Interfaz + Impl en servicios | `service/` + `service/implement/` | desacopla contrato de implementación, facilita test |
| DTO en el borde (records) | `dto/request`, `dto/response` | no exponer entidades JPA; contrato estable |
| Mapper Entity↔DTO | `mapper/ProductoMapper` | aislar la conversión (hoy solo productos) |
| `@RestControllerAdvice` | `exception/GlobalExceptionHandler` | manejo centralizado de errores (hoy parcial) |
| Builder (Lombok) | `Usuarios.builder()...` | construcción legible de entidades |
| Queries derivadas | `repository/*` (`findByCorreoUsuario`, `findByInsumos_IdInsumo`) | sin SQL manual para casos simples |

## Decisiones clave

### Venta transaccional con doble pasada
`registrarPedido` valida **todo** el stock antes de descontar nada (pasada 1), y
recién entonces persiste y descuenta (pasada 2), dentro de `@Transactional`. Así, si
falta un insumo, el `StockInsuficienteException` hace rollback y no deja media venta.

### Bloqueo pesimista en stock
`StockRepository.findByInsumos_IdInsumo` usa `@Lock(PESSIMISTIC_WRITE)` para evitar
que dos ventas concurrentes descuenten el mismo insumo por debajo de cero.
> ponytail: bloqueo a nivel de fila de stock; suficiente para 1 mesero. Si crecen los
> meseros concurrentes, revisar contención (B / RNF-05).

### Disponibilidad calculada, no almacenada
`ProductoServiceImpl.calcularDisponibilidad` deriva `disponible` en tiempo de lectura
comparando recetas vs stock. No se persiste un flag (evita desincronización).

### JWT stateless
Sin estado de sesión en servidor: el token porta `correo` (subject) y `rol` (claim).
Las authorities se cargan desde BD en cada request (`UserDetailsServiceImpl`), no del
token → la BD es la verdad de autorización.

## Deudas de diseño (ver [09](09_backlog_brechas.md))

- Dinero en `Double` (debería `BigDecimal`/`NUMERIC`) — riesgo de redondeo.
- Manejo de errores incompleto (solo stock).
- Sin capa de mapper para el resto de dominios (conversión inline en services).
- Esquema sin migraciones versionadas.

## Estructuras de datos del sílabo en el backend

- **Matriz (RF-DS-02):** el reporte mensual se modelará como arreglo 2D `producto × día`
  agregando ventas; encaja con `GET /api/admin/reportes/mensual`.
- **Cola FIFO + prioridad (RF-DS-04):** TAD propio para ordenar el **despacho** de
  pedidos (no estados de cocina). Implementación en fase posterior, respetando capas.
