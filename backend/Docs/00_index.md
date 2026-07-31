> SDD coffee-system · Documento 0 de 11 · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 00 · Índice maestro — Backend

Documentación SDD (Spec-Driven Development, holamodo *brownfield*) del **backend** del
sistema **coffee-system**. Documenta la **realidad del código** (as-built), no un
diseño ideal. Las divergencias Front↔Back se marcan ⚠️ y viven en
[`09_backlog_brechas.md`](09_backlog_brechas.md); **no se corrigen en esta fase**.

## El sistema en una frase

POS web para una cafetería MYPE: el mesero registra pedidos que descuentan insumos
del stock por receta y emiten una boleta; el administrador gestiona catálogo,
inventario, usuarios y métricas.

## Índice de documentos (backend)

| # | Documento | Contenido |
|---|---|---|
| 00 | [Índice](00_index.md) | Este documento: mapa, equipo, stack, convenciones, glosario |
| 01 | [Overview](01_overview.md) | Propósito, actores, alcance SÍ/NO, reglas de negocio, lógica de venta |
| 02 | [Requisitos](02_requirements.md) | RF/RNF numerados + estructuras de datos del sílabo |
| 03 | [Arquitectura](03_architecture.md) | Capas, estructura real de carpetas, despliegue, config |
| 04 | [Base de datos](04_database.md) | Modelo real (DDL Supabase), tablas, relaciones, ER |
| 05 | [Diseño de API](05_api_design.md) | Contrato REST as-built (el contrato que el front consume) |
| 07 | [Seguridad](07_security.md) | JWT, BCrypt, roles, `@PreAuthorize`, CORS |
| 08 | [Sprints](08_sprints.md) | Metodología, flujo git, DoD, hitos del sílabo |
| 09 | [Backlog de brechas](09_backlog_brechas.md) | Divergencias ⚠️, deuda técnica, riesgos |
| 10 | [Scripts de BD](10_db_scripts.md) | DDL idempotente + seed, orden de ejecución |
| — | [MEMORY](MEMORY.md) | Memoria viva del backend |
| — | [design](design.md) | Decisiones de diseño y patrones |

> El gemelo de esta documentación vive en [`../../frontend/Docs/`](../../frontend/Docs/00_index.md).

## Equipo y roles

| Persona | GitHub | Rol |
|---|---|---|
| Equipo coffee-system | `AriasYarangaIam` (org del repo) | Full-stack (back + front) |
| — | `JoaquinStudent` | Contribuidor |

> SUPUESTO: el reparto fino de responsabilidades no se deduce del código. Confirmar.

## Stack (backend)

| Elemento | Tecnología | Evidencia |
|---|---|---|
| Lenguaje | Java 21 | `pom.xml` (`<java.version>21`) |
| Framework | Spring Boot 4.0.5 | `pom.xml` parent |
| Web | Spring MVC (`spring-boot-starter-webmvc`) | `pom.xml` |
| Build | Maven (wrapper `mvnw`) | `backend/mvnw` |
| ORM | Spring Data JPA + Hibernate | `pom.xml` |
| BD | PostgreSQL (Supabase) | `pom.xml` driver + DDL real |
| Seguridad | Spring Security + JWT (jjwt 0.12.6) + BCrypt | `security/*` |
| Validación | Bean Validation (jakarta.validation) | `pom.xml` |
| Utilidades | Lombok, devtools | `pom.xml` |

## Convenciones reales (detectadas del código)

- **Envelope de respuesta:** ⚠️ **no hay wrapper**. Las respuestas son el DTO directo
  (`ResponseEntity.ok(dto)`). El error solo está estandarizado para una excepción
  (ver 05/07). *No se usa* el `{success,data,message}` de la plantilla canónica SDD.
- **Naming JSON:** **camelCase** (defaults de Jackson sobre campos Java). Ej.
  `nombreProducto`, `aliasTicket`, `cantidadPedida`.
- **Naming SQL:** **snake_case** (mapeo `@Column(name="...")`). Ej. `alias_ticket`.
- **Naming código:** Entidades `PascalCase` en plural (`Productos`, `Pedidos`);
  DTOs `PascalCase` + sufijo (`PedidoRequestDTO`); métodos `camelCase` en español.
- **Fechas:** `LocalDateTime` serializado `dd/MM/yyyy HH:mm:ss` (`@JsonFormat` en DTOs).
- **Base URL:** `http://localhost:8080/api`.

## Glosario

| Término | Significado |
|---|---|
| **Insumo** | Materia prima (café molido, leche, vaso). Se consume por receta |
| **Receta** | Relación Producto↔Insumo con `cantidad_usada` por unidad de producto |
| **Stock** | Cantidad de un insumo en un almacén |
| **Pedido** | Venta registrada por un mesero; agrupa líneas de `DetallePedido` |
| **Boleta** | Comprobante generado al final del pedido (`alias_ticket` + total) |
| **alias_ticket** | Código único de boleta |
| **MESERO / ADMIN** | Los dos únicos roles del sistema |

---
Siguiente: [01 · Overview »](01_overview.md)
