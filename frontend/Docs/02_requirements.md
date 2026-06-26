> SDD coffee-system · Documento 2 de 8 · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 02 · Requisitos — Frontend

Estado: ✅ implementado · 🟡 parcial · ⏳ pendiente (depende del back) · ⚠️ divergencia.

## Requisitos funcionales (lente front)

| ID | Requisito | Estado | Evidencia | Endpoint |
|---|---|---|---|---|
| RF-01 | Login con pestaña de rol y guardado de sesión | ✅ | `login.js`, `auth.js` | `POST /auth/login` |
| RF-02 | Redirigir por rol tras login | ⚠️ | `router.js` (usa `ADMINISTRADOR`) | — |
| RF-03 | Guard de página por rol | ✅ | `auth.js` `requireRole` | — |
| RF-04 | POS: listar productos disponibles y armar carrito | ✅ | `pedidos.js` | `GET /productos` |
| RF-05 | Confirmar pedido | 🟡 | `pedidos.js` | `POST /pedidos` ⚠️ body no coincide (09) |
| RF-06 | Ver boleta del último pedido | ⚠️ | `boleta.js` | `GET /pedidos/{id}/boleta` (bug DOM, 09) |
| RF-07 | Listar "mis pedidos" | ⏳/⚠️ | `mis-pedidos.js` | `GET /pedidos` (ausente en back) |
| RF-08 | Cambiar estado de pedido | ⚠️ | `mis-pedidos.js` | `PATCH /pedidos/{id}/estado` (**fuera de alcance**) |
| RF-09 | Dashboard KPIs + gráfico mensual | ⏳ | `dashboard.js` | `GET /admin/dashboard`, `/admin/reportes/mensual` |
| RF-10 | CRUD productos | ⏳ | `productos.js` | `/admin/productos` |
| RF-11 | CRUD insumos | ⏳ | `insumos.js` | `/admin/insumos` |
| RF-12 | Registrar/ver stock | ⏳ | `stock.js` | `/admin/stocks` |
| RF-13 | CRUD usuarios | ⏳ | `usuarios.js` | `/admin/usuarios` |

## Requisitos no funcionales (front)

| ID | Requisito | Estado | Evidencia |
|---|---|---|---|
| RNF-01 | Sin framework JS (Vanilla ES6) | ✅ | `js/**` |
| RNF-02 | Capa HTTP central con JWT y manejo de 401 | ✅ | `api.js` |
| RNF-03 | Formato local es-PE (moneda S/, fechas) | ✅ | `format.js` |
| RNF-04 | Feedback de UI (toast, spinner, estado vacío) | ✅ | `dom.js` |
| RNF-05 | Diseño responsive | 🟡 | CSS grid/flex; revisar móviles |
| RNF-06 | Accesibilidad básica | 🟡 | labels presentes; faltan algunos `aria-*` |

## Estructura de datos del sílabo en el front

> Curso *Algoritmos y Estructuras de Datos*. El front aporta una estructura del sílabo.
> Implementación de **fase posterior** (⏳), respetando Vanilla JS.

| ID | Estructura | Unidad | Uso | Estado |
|---|---|---|---|---|
| RF-DS-01 | Arreglos 1D | U1 | `carrito` (array de líneas) — ya en uso | ✅ |
| RF-DS-03 | **Pila (Stack)** | U3 | **deshacer** la última acción del carrito (push al agregar/+/−, pop al deshacer) | ⏳ |

### Mapeo Sílabo ↔ Estructuras (visión global)

| Unidad | Tema | Lado | Cubierto por |
|---|---|---|---|
| U1 | Arreglos 1D / Matriz 2D | front / back | RF-DS-01 (carrito) · RF-DS-02 matriz (back) |
| U2 | Listas enlazadas, TAD | back | *(opcional)* — ver backend 09 |
| U3 | Pilas y Colas | front / back | **RF-DS-03 Pila (front)** · RF-DS-04 Cola (back) |
| U4 | Árboles, ABB, AVL | back | *(opcional)* — ver backend 09 |

> El catálogo completo (incl. matriz y cola del backend) está en
> [backend/Docs/02_requirements.md](../../backend/Docs/02_requirements.md).

---
Anterior: [« 01 · Overview](01_overview.md) · Siguiente: [03 · Arquitectura »](03_architecture.md)
