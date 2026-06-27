> SDD coffee-system · Documento 1 de 8 · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 01 · Overview — Frontend

## Propósito

Interfaz web del POS de la cafetería MYPE. Dos experiencias según rol: **mesero**
(registrar pedidos, ver boleta) y **administrador** (gestión y métricas). Contexto de
negocio completo en [backend/Docs/01_overview.md](../../backend/Docs/01_overview.md).

## Actores y sus pantallas

| Actor | Pantallas |
|---|---|
| **MESERO** | `pedidos.html` (POS), `mis-pedidos.html`, `boleta.html` |
| **ADMIN** | `dashboard.html`, `productos.html`, `insumos.html`, `stock.html`, `usuarios.html` |
| (público) | `index.html` (login con pestañas Personal/Administrador) |

## Alcance

### ✅ Lo que el front SÍ tiene (as-built)

- Login con pestañas de rol y guard de sesión por página.
- POS de mesero: grilla de productos + carrito + confirmar pedido.
- Boleta tras confirmar.
- Listado de "mis pedidos".
- Admin: dashboard con KPIs + gráfico (Chart.js), CRUD de productos, insumos, usuarios,
  y registro/listado de stock.

### ⚠️ Construido sobre endpoints que el back AÚN no expone

Casi toda la sección admin y el listado de pedidos consumen rutas `/admin/*` y
`GET /pedidos` que el backend no implementa todavía. El front fue desarrollado **fiel a
la spec** (`PROMPT_MAESTRO_CAFETERIA_MYPE.md`), adelantándose al back. Ver
[09_backlog_brechas.md](09_backlog_brechas.md) y [05_api_design.md](05_api_design.md).

### ❌ Fuera de alcance

Clientes, pagos, cocina, reservas, etc. (igual que el backend).

> ⚠️ **Estados de pedido** (PENDIENTE/PAGADO/CANCELADO) en `mis-pedidos.js` y
> `format.js` están **fuera de alcance** (spec Regla #6: el sistema termina en la
> boleta). Decisión del equipo: **quitarlos del front**. Ver B-F-05 en 09.

## Estado de las pantallas

| Pantalla | Estado | Nota |
|---|---|---|
| `index.html` (login) | ✅ funcional | ⚠️ `router.js` usa rol `ADMINISTRADOR` (ver 07/09) |
| `pedidos.html` | 🟡 | ⚠️ el body del POST no coincide con el back (B-F-01) |
| `boleta.html` | ⚠️ rota | `boleta.js` apunta a `#boleta-contenido` inexistente (B-F-02) |
| `mis-pedidos.html` | ⚠️ | usa `GET /pedidos` y `estado` que el back no tiene (B-F-03/05) |
| `dashboard.html` | ⏳ | depende de `/admin/dashboard` y `/admin/reportes/mensual` (ausentes) |
| `productos.html` | ⏳ | CRUD `/admin/productos` (ausente); categorías hardcodeadas |
| `insumos.html` | ⏳ | CRUD `/admin/insumos` (ausente) |
| `stock.html` | ⏳ | `/admin/stocks` (ausente); `select-almacen` sin poblar |
| `usuarios.html` | ⏳ | CRUD `/admin/usuarios` (ausente); back devuelve DTO sin `rol`/`usuarioId` |

## Supuestos y preguntas abiertas

- ¿Valor canónico del rol admin? El front es internamente inconsistente
  (`auth.js` → `ADMIN`, `router.js` → `ADMINISTRADOR`). Propuesta: `ADMIN`.
- ¿Se confirma quitar la feature de estados de pedido? (decisión actual: sí).

---
Anterior: [« 00 · Índice](00_index.md) · Siguiente: [02 · Requisitos »](02_requirements.md)
