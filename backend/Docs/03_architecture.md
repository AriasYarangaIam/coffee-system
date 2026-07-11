> SDD coffee-system · Documento 3 de 11 · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 03 · Arquitectura — Backend

## Estilo arquitectónico

**Cliente–Servidor REST**, backend en **N-capas** clásico de Spring Boot. Elegido
por ser el patrón estándar del stack (Spring), separar responsabilidades y facilitar
pruebas. Monorepo lógico (`backend/` + `frontend/` en un mismo repo).

```
Request HTTP (JSON)
      │
      ▼
[Controller]  @RestController — recibe DTO, valida (@Valid), aplica @PreAuthorize
      │
      ▼
[Service]     interfaz + Impl — lógica de negocio, @Transactional en ventas
      │
      ▼
[Repository]  Spring Data JPA — acceso a datos, queries derivadas, @Lock
      │
      ▼
[PostgreSQL en Supabase]
```

Capa transversal: `security/` (filtro JWT + autorización), `exception/` (manejo de
errores), `mapper/` (Entity↔DTO), `dto/` (contrato de entrada/salida).

## Flujo Front↔Back

- El front (Vanilla JS) llama `http://localhost:8080/api/...` vía `fetch` con header
  `Authorization: Bearer <jwt>`.
- CORS permite **solo** `http://localhost:5500` y `http://127.0.0.1:5500` (Live Server).
- Respuestas: DTO directo en JSON (sin envelope). Error de stock: `{ "error": "..." }`
  con HTTP 409. Ver [05](05_api_design.md) y [07](07_security.md).

## Estructura real de carpetas

```
backend/
├── pom.xml                      # Spring Boot 4.0.5, Java 21
├── mvnw / mvnw.cmd / .mvn/      # Maven wrapper
└── src/main/
    ├── resources/
    │   └── application.yaml      # ⚠️ solo spring.application.name (ver Config)
    └── java/com/coffee/backend/
        ├── BackendApplication.java
        ├── controller/          # AuthController, PedidoController, ProductoController, UsuarioController
        ├── service/             # interfaces: Auth/Pedido/Producto/UsuarioService
        │   └── implement/       # *ServiceImpl
        ├── repository/          # 9 repos Spring Data JPA
        ├── entity/              # 10 entidades JPA (@Entity)
        ├── dto/
        │   ├── request/         # *RequestDTO (records) — ⚠️ falta DeleteUserDTO
        │   └── response/        # *ResponseDTO (records)
        ├── mapper/              # ProductoMapper
        ├── security/            # SecurityConfig, JwtUtil, JwtFilter, UserDetailsServiceImpl
        └── exception/           # GlobalExceptionHandler + 2 excepciones de negocio
```

> ⚠️ `UsuarioController` importa `com.coffee.backend.dto.request.DeleteUserDTO`, que
> **no existe** como archivo → el proyecto **no compila** tal cual. Ver 09.

## Componentes

```
┌─────────────┐   HTTPS/JSON    ┌──────────────────────────────┐   JDBC    ┌────────────┐
│  Frontend   │ ──────────────► │  Backend Spring Boot (8080)   │ ───────►  │ PostgreSQL │
│ (Vanilla JS)│  Bearer JWT     │  Controller→Service→Repository │           │ (Supabase) │
└─────────────┘ ◄────────────── └──────────────────────────────┘ ◄───────  └────────────┘
```

## Despliegue y configuración

| Aspecto | Realidad | Nota |
|---|---|---|
| Puerto API | 8080 (default Spring) | front apunta a `localhost:8080/api` |
| BD | PostgreSQL en Supabase | DDL real en [04](04_database.md) |
| Esquema | Generado por Hibernate `ddl-auto` | FKs autogeneradas `fkqf5elo…` → ⚠️ sin migraciones (09) |
| Front | Servido por Live Server en :5500 | CORS lo refleja |

### ⚠️ Configuración faltante en el repo

`src/main/resources/application.yaml` contiene **solo**:

```yaml
spring:
  application:
    name: backend
```

No hay `spring.datasource.*`, `spring.jpa.*`, ni `jwt.secret` / `jwt.expiration`
(requeridos por `JwtUtil` vía `@Value`). **El backend no arranca sin esa config.**

> SUPUESTO: existe un `application-local.yaml`/variables de entorno **no versionadas**
> con datasource y secretos JWT. Documentar el procedimiento de setup. Ver 09.

### Variables/propiedades esperadas (a definir en config local)

| Propiedad | Uso | Evidencia |
|---|---|---|
| `spring.datasource.url/username/password` | conexión Supabase | driver postgres en `pom.xml` |
| `spring.jpa.hibernate.ddl-auto` | estrategia de esquema | inferido de FKs autogeneradas |
| `jwt.secret` | firma HMAC del token | `JwtUtil` `@Value("${jwt.secret}")` |
| `jwt.expiration` | vigencia del token (ms) | `JwtUtil` `@Value("${jwt.expiration}")` |

---
Anterior: [« 02 · Requisitos](02_requirements.md) · Siguiente: [04 · Base de datos »](04_database.md)
