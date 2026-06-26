> SDD coffee-system · Documento 0 de 8 · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 00 · Índice maestro — Frontend

Documentación SDD (Spec-Driven Development, modo *brownfield*) del **frontend** de
**coffee-system**. Documenta la **realidad del código** (as-built) y **el contrato tal
como el front lo consume**. Las divergencias Front↔Back se marcan ⚠️ y viven en
[`09_backlog_brechas.md`](09_backlog_brechas.md); **no se corrigen en esta fase**.

## El sistema en una frase

POS web para una cafetería MYPE: el mesero registra pedidos y emite boleta; el
administrador gestiona catálogo, inventario, usuarios y métricas.

## Índice de documentos (frontend)

| # | Documento | Contenido |
|---|---|---|
| 00 | [Índice](00_index.md) | Este documento: mapa, stack front, convenciones, glosario |
| 01 | [Overview](01_overview.md) | Propósito, actores, alcance SÍ/NO (lente front) |
| 02 | [Requisitos](02_requirements.md) | RF/RNF del front + estructura de datos del sílabo (Pila) |
| 03 | [Arquitectura](03_architecture.md) | Vanilla JS ES6, estructura real de carpetas, flujo |
| 05 | [API que consume](05_api_design.md) | Contrato visto desde el front + ⚠️ lo que el back no expone |
| 06 | [Diseño frontend](06_frontend_design.md) | Mapa de pantallas, capa core/utils, tabla pantalla→endpoints |
| 07 | [Seguridad](07_security.md) | Sesión localStorage, `requireRole`, logout ante 401 |
| 08 | [Sprints (track front)](08_sprints.md) | Plan de 2 semanas del front (Joaquin, Yefrie) |
| 09 | [Backlog de brechas](09_backlog_brechas.md) | Divergencias ⚠️, bugs, deuda técnica |
| — | [MEMORY](MEMORY.md) | Memoria viva del frontend |
| — | [design](design.md) | Design system (paleta, tipografía, componentes) |

> El **08** aquí es el *track del front*; la metodología, flujo Git, DoD y el track del
> back están en el doc maestro [backend/Docs/08_sprints.md](../../backend/Docs/08_sprints.md).
> Los docs de **base de datos (04)** y **scripts SQL (10)** son transversales y viven en
> [`../../backend/Docs/`](../../backend/Docs/00_index.md) para no duplicar. El **gemelo
> backend** de esta documentación está ahí.

## Stack (frontend)

| Elemento | Tecnología | Evidencia |
|---|---|---|
| Estructura | HTML5 | `frontend/*.html`, `pages/**` |
| Estilos | CSS3 (variables, flex, grid) | `css/main.css`, `css/components.css`, `css/pages/*` |
| Lógica | **Vanilla JavaScript ES6** (sin framework, sin bundler) | `js/**` con `import/export` |
| Módulos | ES6 `import`/`export` | `js/pages/*.js` |
| HTTP | `fetch` nativo, envuelto en `apiFetch` | `js/core/api.js` |
| Sesión | `localStorage` (`jwt_token`, `usuario`) | `js/core/auth.js` |
| Gráficos | Chart.js (CDN, solo dashboard) | `js/pages/dashboard.js` |
| Iconos/tipografía | Material Symbols + Google Fonts (CDN) | HTML `<head>` |
| ❌ Prohibido | React/Angular/Vue/jQuery | spec |

## Convenciones reales

- **Base URL:** `http://localhost:8080/api` (constante `BASE_URL` en `api.js`).
- **Servido en:** `http://localhost:5500` (Live Server) — lo que el CORS del back permite.
- **Naming JSON:** camelCase (espeja los DTOs del back).
- **Sin envelope:** el front lee el DTO directo (`data.token`, `p.nombreProducto`…).
- **Auth:** header `Authorization: Bearer <jwt>` inyectado por `apiFetch`.
- **Errores:** ante 401 → `logout()`; otros → `throw` y `mostrarToast`.

## Glosario

Igual que el backend (ver [backend/Docs/00_index.md](../../backend/Docs/00_index.md)):
Insumo, Receta, Stock, Pedido, Boleta, alias_ticket, MESERO/ADMIN. Términos del front:

| Término | Significado |
|---|---|
| **carrito** | Estado en memoria de la pantalla de pedidos (líneas + cantidades) |
| **apiFetch** | Wrapper de `fetch` con JWT, manejo de 401/204 y parseo JSON |
| **requireRole** | Guard que protege cada página según el rol guardado |
| **toast** | Notificación efímera (3 s) |

---
Siguiente: [01 · Overview »](01_overview.md)
