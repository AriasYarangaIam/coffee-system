# MEMORY — Backend (memoria viva)

Contexto durable del backend para retomar trabajo rápido. Mantener al día.

## Qué es

API REST Spring Boot del POS de cafetería. Capas Controller→Service→Repository→Entity.
PostgreSQL en Supabase. Auth JWT stateless. Detalle en [00_index.md](00_index.md).

## Cómo correrlo

- Java 21, Maven wrapper: `./mvnw spring-boot:run` desde `backend/`.
- **Requiere config local no versionada** (ver [03](03_architecture.md) y B-02):
  `spring.datasource.*`, `spring.jpa.*`, `jwt.secret`, `jwt.expiration`.
- **No compila tal cual**: falta `DeleteUserDTO` (B-01).
- CORS: el front debe servirse en `http://localhost:5500` (Live Server).

## Convenciones

- JSON camelCase · SQL snake_case · sin envelope de respuesta · DTOs (records) en el borde.
- Entidades en plural PascalCase (`Productos`, `Pedidos`). Métodos en español.
- Excepciones de negocio en `exception/`; solo `StockInsuficienteException` tiene handler.

## Gotchas (no tropezar de nuevo)

- **Rol ADMIN ambiguo**: `@PreAuthorize('ADMINISTRADOR')` vs BD/`ADMIN`. Canónico = `ADMIN` (B-03).
- `POST /api/pedidos` ignora `usuarioId`: usa el correo del JWT.
- `registrarUsuario` fija `rol_id=2` (MESERO); no crea ADMIN (B-12).
- `GET /api/productos` solo MESERO (B-10).
- Dinero como `Double`/`double precision` (B-18); insumos enteros (B-19).
- Esquema por `ddl-auto`, sin migraciones (B-21).

## Decisiones

- SDD brownfield: el código es la verdad as-built; la spec
  (`PROMPT_MAESTRO_CAFETERIA_MYPE.md`) arbitra alcance y contrato. DDL real de Supabase
  = verdad del modelo de datos.
- Estructuras del sílabo a implementar en backend: **matriz** (reporte mensual, RF-DS-02)
  y **cola FIFO+prioridad** de despacho (RF-DS-04). Ver [02](02_requirements.md).

## Próximos pasos

Sprint S1 del [08](08_sprints.md): B-01 (compila), B-02 (config), B-03 (rol).
