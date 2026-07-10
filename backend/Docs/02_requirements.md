> SDD coffee-system · Documento 2 de 11 · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 02 · Requisitos — Backend

Estado: ✅ implementado · 🟡 parcial · ⏳ pendiente · ⚠️ divergencia Front↔Back.
"Lado" = dónde vive (B=back, F=front, A=ambos).

## Requisitos funcionales (RF)

| ID | Requisito | Lado | Estado | Evidencia |
|---|---|---|---|---|
| RF-01 | Login con correo+contraseña que devuelve JWT | A | ✅ | `AuthController.iniciarSesion`, `AuthServiceImpl` |
| RF-02 | Listar productos con disponibilidad por stock | A | ✅ | `ProductoController` GET `/`, `ProductoServiceImpl.listarProductos` |
| RF-03 | Registrar pedido transaccional con validación+descuento de stock | A | ✅ | `PedidoServiceImpl.registrarPedido` |
| RF-04 | Rechazar venta si el stock de insumos es insuficiente | B | ✅ | `StockInsuficienteException` (HTTP 409) |
| RF-05 | Generar boleta con detalle y total | A | ✅ | `PedidoServiceImpl.obtenerBoleta`, `BoletaResponseDTO` |
| RF-06 | Consultar receta (insumos) de un producto | B | ✅ | `ProductoController` GET `/{id}/receta` |
| RF-07 | Registrar usuario | A | 🟡 | `UsuarioServiceImpl.registrarUsuario` — rol fijo a id=2 (09) |
| RF-08 | Listar meseros | A | ✅ | `UsuarioController` GET `/obtenerMeseros` |
| RF-09 | Eliminar usuario por correo | A | 🟡 | `deleteMesero` — ⚠️ falta `DeleteUserDTO` (no compila, 09) |
| RF-10 | Actualizar parcialmente al usuario logueado | A | ✅ | `UsuarioServiceImpl.actualizarParcial` (PATCH) |
| RF-11 | Dashboard de métricas (ventas día, top producto, stock bajo, pedidos día) | F | ⏳ | spec §Dashboard; front `dashboard.js`; back ausente |
| RF-12 | Reporte mensual de ventas | F | ⏳ | front `dashboard.js`; back ausente |
| RF-13 | CRUD de productos (admin) | F | ⏳ | front `productos.js`; back solo GET listar |
| RF-14 | CRUD de insumos (admin) | F | ⏳ | front `insumos.js`; back ausente |
| RF-15 | CRUD de stock (admin) | F | ⏳ | front `stock.js`; back ausente |

## Requisitos no funcionales (RNF)

| ID | Requisito | Estado | Evidencia |
|---|---|---|---|
| RNF-01 | Autenticación stateless por JWT (Bearer) | ✅ | `SecurityConfig` STATELESS, `JwtFilter` |
| RNF-02 | Contraseñas con hash BCrypt | ✅ | `BCryptPasswordEncoder` |
| RNF-03 | Autorización por rol a nivel de método | ✅ | `@EnableMethodSecurity`, `@PreAuthorize` |
| RNF-04 | Atomicidad en el registro de venta | ✅ | `@Transactional` en `registrarPedido` |
| RNF-05 | Bloqueo ante concurrencia en descuento de stock | 🟡 | `@Lock(PESSIMISTIC_WRITE)` en `StockRepository` (revisar, 09) |
| RNF-06 | CORS restringido al front (Live Server :5500) | ✅ | `SecurityConfig.corsConfigurationSource` |
| RNF-07 | DTOs en el borde (no exponer entidades) | ✅ | `dto/request`, `dto/response` |
| RNF-08 | Precisión monetaria | ⚠️ | `Double`/`double precision` en vez de `NUMERIC` (09) |
| RNF-09 | Esquema de BD versionado/migraciones | ⏳ | hoy `ddl-auto` Hibernate, sin migraciones (09) |

## Estructuras de datos del sílabo (académico)

> Curso *Algoritmos y Estructuras de Datos* (`silabo_algoritmos_estructuras_datos.md`).
> El proyecto debe **aplicar** estructuras de datos. Estas son las **adoptadas**; su
> implementación es de **fase posterior** (hoy ⏳), respetando el stack y la arquitectura.

| ID | Estructura | Unidad sílabo | Lado | Uso en el proyecto | Estado |
|---|---|---|---|---|---|
| RF-DS-01 | Arreglos 1D | U1 | A | catálogo, líneas de pedido (ya en uso) | ✅ |
| RF-DS-02 | **Matriz (arreglo 2D)** | U1 | B (Java) | reporte mensual producto × día → `GET /api/admin/reportes/mensual` | ✅ |
| RF-DS-03 | **Pila (Stack)** | U3 | F (JS) + B (Java) | deshacer última acción del carrito (`frontend/js/utils/pila.js`) **y** deshacer último ingreso de stock (`tad/Pila`+`PilaEnlazada` → `POST /api/admin/stocks/deshacer`) | ✅ |
| RF-DS-04 | **Cola FIFO + prioridad** | U3 | B (Java) | cola lógica de **despacho** → `tad/ColaPrioridad` + `GET /api/admin/despacho` | ✅ |

### Mapeo Sílabo ↔ Estructuras

| Unidad | Tema del sílabo | Cubierto por |
|---|---|---|
| U1 — Lineales estáticas | Arreglos 1D y 2D (matrices) | RF-DS-01, **RF-DS-02** |
| U2 — Dinámicas (listas) | Listas enlazadas, TAD | *(opcional)* lista enlazada en detalle de pedido — ver 09 |
| U3 — Pilas y Colas | push/pop, enqueue/dequeue, prioridad | **RF-DS-03, RF-DS-04** |
| U4 — No lineales (árboles) | Árbol, ABB, AVL | *(opcional)* árbol Categoría→Producto / ABB de búsqueda — ver 09 |

> Nota de alcance (RF-DS-04): se documenta como **cola de despacho lógica** (orden de
> atención), **no** como gestión de cocina/estados, que está fuera de alcance.
> Requisito académico: implementar el TAD propio (no usar `java.util` directamente).

---
Anterior: [« 01 · Overview](01_overview.md) · Siguiente: [03 · Arquitectura »](03_architecture.md)
