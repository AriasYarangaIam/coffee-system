> SDD coffee-system · Documento 1 de 11 · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 01 · Overview — Backend

## Propósito

Digitalizar la operación de una **cafetería MYPE** que hoy trabaja de forma manual
(papel y memoria del dueño). El sistema resuelve:

- Errores frecuentes en pedidos.
- Pérdida de ventas por falta de stock.
- Nulo control de inventario.
- Ausencia de métricas para decidir.
- Cuellos de botella en horas pico.

> Fuente de negocio: `PROMPT_MAESTRO_CAFETERIA_MYPE.md` (intención original).

## Actores

| Actor | Puede |
|---|---|
| **MESERO** | Ver productos disponibles, registrar pedidos, generar boleta |
| **ADMIN** | Dashboard, reportes, inventario, productos, insumos, stock, usuarios |

Solo existen **2 roles**. No se gestionan clientes (flujo POS rápido).

## Alcance

### ✅ Lo que el sistema SÍ hace (back as-built)

- Autenticación por correo + contraseña con emisión de **JWT** (`/api/auth/login`).
- Listado de productos con **disponibilidad calculada** según stock de insumos.
- Registro de pedido **transaccional** con validación y descuento de stock.
- Generación de **boleta** con total calculado.
- Registro / listado (meseros) / borrado / actualización parcial de usuarios.
- Consulta de la **receta** de un producto.

### ⏳ Definido por la spec pero AÚN NO en el backend

- `GET /api/admin/dashboard` (métricas) y `GET /api/admin/reportes/mensual`.
- CRUD de `/api/admin/productos`, `/api/admin/insumos`, `/api/admin/stocks`.
- Gestión de usuarios bajo la ruta `/api/admin/usuarios` (hoy está en `/api/usuario`).
- Registro de usuarios con rol configurable (hoy el rol está fijo, ver 09).

> El **frontend ya consume** estos endpoints faltantes → ver divergencias en
> [`09_backlog_brechas.md`](09_backlog_brechas.md).

### ❌ Fuera de alcance (no implementar)

Clientes/perfiles · **estados de preparación de pedido (cocina)** · pagos
integrados · múltiples sucursales · app móvil · panel de cocina · reservas ·
fidelización · notificaciones en tiempo real.

> ⚠️ El frontend agregó estados de pedido (PENDIENTE/PAGADO/CANCELADO), que están
> **fuera de alcance** según la Regla #6. Decisión del equipo: **corregir en el front**
> (ver 09). El backend correctamente NO tiene `estado`.

## Reglas de negocio críticas

| # | Regla | Estado |
|---|---|---|
| 1 | Solo 2 roles: `ADMIN` y `MESERO` | ⚠️ nombre del rol admin inconsistente (09) |
| 2 | No se gestionan clientes (POS rápido) | ✅ |
| 3 | Los productos se componen de insumos (Recetas) | ✅ |
| 4 | Al registrar venta → los insumos se descuentan del stock | ✅ `PedidoServiceImpl` |
| 5 | Si los insumos son insuficientes → el producto NO se vende | ✅ `StockInsuficienteException` |
| 6 | El sistema termina en la boleta; sin gestión de cocina ni estados | ✅ back / ⚠️ front |
| 7 | El ADMIN accede a dashboard, reportes, inventario, productos, usuarios | ⏳ parcial en back |
| 8 | El MESERO solo ve productos, registra pedidos y genera boleta | ✅ |

## Lógica de negocio clave — Registro de venta (`@Transactional`)

Operación atómica más importante. Implementada en
`backend/src/main/java/com/coffee/backend/service/implement/PedidoServiceImpl.java`:

1. **Validar stock (pasada 1):** por cada línea del pedido, obtiene las recetas del
   producto; para cada insumo calcula `necesario = cantidad_usada × cantidad_pedida`
   y compara contra `stocks.cantidad`. Si falta → `StockInsuficienteException`
   (rollback automático, HTTP 409).
2. **Resolver usuario:** se busca por el **correo del JWT** (`userDetails.getUsername()`).
   ⚠️ El `usuarioId` del request se ignora (ver 09).
3. **Armar pedido:** `aliasTicket` + `fechaPedido = now()` + usuario.
4. **Crear detalles y descontar stock (pasada 2):** por cada línea crea `DetallePedido`
   con `precioUnitario = producto.precioActual`, y resta el stock (`save`).
5. **Persistir:** un solo `save(pedido)` (los detalles se guardan por *cascade*).

### Ejemplo numérico

Producto **Café Americano** con receta: 15 g café molido, 1 vaso.
Stock: café = 100 g, vaso = 3 u.

- Pedido: 3 Cafés → necesario café = 15×3 = 45 g (≤100 ✅), vasos = 1×3 = 3 u (≤3 ✅).
- Resultado: pedido OK. Stock final: café = 55 g, vaso = 0 u.
- Si se pidieran 4 Cafés → vasos necesarios = 4 > 3 → **Stock insuficiente para: vaso**
  (HTTP 409, nada se persiste).

> ⚠️ `cantidad_usada` y `stocks.cantidad` son **enteros** (`bigint`) en la BD, así que
> no se pueden modelar insumos fraccionarios (ej. 0,2 L de leche). Ver 04 y 09.

## Supuestos y preguntas abiertas

- ¿El `nombre_rol` del admin en BD es `ADMIN` o `ADMINISTRADOR`? (decide si autorizan
  los endpoints admin — ver 07/09).
- ¿Dónde viven `jwt.secret`, `jwt.expiration` y el `datasource`? No están en
  `application.yaml` (ver 03). SUPUESTO: en config local/entorno no versionada.

---
Anterior: [« 00 · Índice](00_index.md) · Siguiente: [02 · Requisitos »](02_requirements.md)
