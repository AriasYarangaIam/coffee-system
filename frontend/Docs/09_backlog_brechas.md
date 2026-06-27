> SDD coffee-system · Documento 8 de 8 (doc 09 del esquema SDD) · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 09 · Backlog de brechas — Frontend ⚠️

Brechas del frontend y divergencias con el back. **Nada se aplicó en código** (fase
solo documentación). El backlog del back (B-xx) está en
[backend/Docs/09_backlog_brechas.md](../../backend/Docs/09_backlog_brechas.md).

## A. Bugs que rompen pantallas (prioridad ALTA)

| ID | Brecha | Evidencia | Propuesta |
|---|---|---|---|
| B-F-01 | **Crear pedido falla**: body no coincide con `PedidoRequestDTO` | `pedidos.js:100-103` envía `{ detalle:[...] }` | ✅ **DECIDIDO**: contrato = `{ detalles:[...] }`. El back toma `usuarioId` del JWT y genera `aliasTicket`. Front: solo renombrar `detalle` → `detalles` |
| B-F-02 | **Boleta no renderiza**: ID de DOM inexistente | `boleta.js:9` usa `#boleta-contenido`; `boleta.html` tiene `#boleta-detalle`/`#boleta-lineas`/`#boleta-total` | Alinear `boleta.js` a los IDs reales del HTML (o viceversa) |
| B-F-04 | **Rol admin inconsistente** | `router.js:12` `ADMINISTRADOR` vs `auth.js:30` `ADMIN` | Unificar a `ADMIN` (coordinar con back B-03) |

## B. Divergencias Front↔Back (dependen del back)

| ID | Brecha | Detalle | Propuesta |
|---|---|---|---|
| B-F-03 | Listar pedidos | `mis-pedidos.js` usa `GET /pedidos` con `total`/`estado` que el back no ofrece | Back añade `GET /api/pedidos` (B-07); definir si incluye `total` |
| B-F-05 | Estados de pedido | UI Cobrado/Cancelar + `PATCH /pedidos/{id}/estado` + `renderBadgeEstado` | **Fuera de alcance** (Regla #6): quitar acciones, columna estado y `PATCH`; simplificar `mis-pedidos` a histórico de boletas |
| B-F-06 | Categoría como nombre | `productos.js` envía/lee `categoria` string (hardcode Bebidas/Comidas/Postres/Otros) | Usar `categoriaId`; poblar el `<select>` desde un endpoint de categorías |
| B-F-07 | Usuarios sin `rol`/`usuarioId` | `usuarios.js` lee `u.usuarioId`, `u.rol`; `UsuarioResponseDTO` no los trae | Back amplía el DTO; front deja de partir el nombre por espacios |
| B-F-08 | Endpoints admin ausentes | dashboard/reportes/productos/insumos/stocks/usuarios `/admin/*` | Implementar en back (B-04, B-05, B-06) |

## C. Deuda técnica / UX (prioridad MEDIA/BAJA)

| ID | Brecha | Evidencia | Propuesta |
|---|---|---|---|
| B-F-09 | `select-almacen` nunca se puebla | `stock.js` solo llena `select-insumo`; `stock.html:66` `<select id="select-almacen">` vacío | Poblar almacenes desde un endpoint o lista |
| B-F-10 | Dos estilos de modal conviven | `crearModal` (factory) sin uso vs modales `hidden` en HTML | Unificar a un solo patrón |
| B-F-11 | Render con `innerHTML` sin escape | varias páginas interpolan datos del back | Escapar/sanitizar si se abre a más usuarios (XSS) |
| B-F-12 | Sin estado de carga global ni dedupe | `apiFetch` | Aceptable hoy; revisar si crece |
| B-F-13 | Manejo de 403 inexistente | `api.js` solo trata 401 | Mensaje claro de "sin permiso" |
| B-F-14 | Búsqueda/filtros declarados pero no implementados | inputs de búsqueda en HTML sin lógica | Implementar o quitar de la UI |

## D. Académico — estructura pendiente (sílabo)

| ID | Estructura | Estado | Nota |
|---|---|---|---|
| B-F-15 | RF-DS-03 Pila para deshacer en el carrito (U3) | ⏳ | TAD propio en JS; push al modificar el carrito, pop para deshacer |

---
Anterior: [« 07 · Seguridad](07_security.md) · Fin del set numerado. Ver [MEMORY](MEMORY.md) y [design](design.md).
