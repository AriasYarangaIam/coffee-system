> SDD coffee-system · Documento 6 de 8 · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 06 · Diseño Frontend

## Mapa de pantallas

### Mesero
- **pedidos.html** (`pedidos.js`): grilla de productos (deshabilita los `disponible=false`)
  + carrito en memoria (`carrito`) con +/− por línea y total; "Confirmar" hace
  `POST /pedidos`, guarda `ultimo_pedido_id` en localStorage y va a la boleta.
- **mis-pedidos.html** (`mis-pedidos.js`): tabla de pedidos del turno con badge de
  estado y acciones Cobrado/Cancelar. ⚠️ depende de endpoints/campos ausentes.
- **boleta.html** (`boleta.js`): comprobante del `ultimo_pedido_id`. ⚠️ bug de IDs.

### Admin
- **dashboard.html** (`dashboard.js`): KPIs (ventas día, pedidos día, producto estrella,
  alertas de stock) + gráfico de barras mensual con Chart.js.
- **productos.html / insumos.html / usuarios.html**: tabla + modal crear/editar + modal
  eliminar (patrón repetido, modales con clase `hidden`).
- **stock.html** (`stock.js`): formulario de ingreso (insumo, cantidad, almacén) + tabla
  de stock actual.

## Capa core / utils

| Módulo | Exporta | Responsabilidad |
|---|---|---|
| `core/api.js` | `apiFetch` | fetch + JWT + 401→logout + 204→null + parseo JSON |
| `core/auth.js` | `guardarSesion`, `obtenerUsuario`, `obtenerToken`, `logout`, `requireRole` | sesión en localStorage y guard por rol |
| `core/router.js` | `redirigirPorRol` | redirección post-login según `usuario.rol` |
| `utils/dom.js` | `mostrarToast`, `mostrarSpinner`, `mostrarVacio`, `crearModal` | feedback UI y modal factory |
| `utils/format.js` | `formatearMoneda`, `formatearFecha`, `formatearFechaSolo`, `renderBadgeEstado` | formato es-PE y badges |

> `crearModal` (factory) existe pero las páginas admin usan modales declarados en el
> HTML con `classList.toggle('hidden')`. Hay dos estilos de modal conviviendo.

## Tabla pantalla → endpoints

| Pantalla | Rol | Endpoints que consume |
|---|---|---|
| `index.html` | público | `POST /auth/login` |
| `pedidos.html` | MESERO | `GET /productos`, `POST /pedidos` |
| `mis-pedidos.html` | MESERO | `GET /pedidos`, `PATCH /pedidos/{id}/estado` |
| `boleta.html` | MESERO | `GET /pedidos/{id}/boleta` |
| `dashboard.html` | ADMIN | `GET /admin/dashboard`, `GET /admin/reportes/mensual` |
| `productos.html` | ADMIN | `GET/POST/PUT/DELETE /admin/productos` |
| `insumos.html` | ADMIN | `GET/POST/PUT/DELETE /admin/insumos` |
| `stock.html` | ADMIN | `GET/POST /admin/stocks` |
| `usuarios.html` | ADMIN | `GET/POST/PUT/DELETE /admin/usuarios` |

## Manejo de sesión y estado

- Sesión en `localStorage`: `jwt_token` y `usuario` (`{ correo, rol, nombreCompleto }`).
- Estado de UI: en memoria por página (`carrito`, `productosCache`, `usuariosCache`…);
  se pierde al recargar. El `ultimo_pedido_id` se pasa entre pedidos→boleta vía
  localStorage.
- No hay cache de datos ni dedupe de requests (suficiente para el tamaño actual).

## Validación de formularios

- Validación nativa del navegador (`required`, `type`) en los `<form>`.
- Lógica puntual en JS (p. ej. clave requerida al crear usuario, opcional al editar).
- ⚠️ El split de nombre por espacios en `usuarios.js` corrompe nombres compuestos
  (ver B-F-07).

## Estrategia de mocks

No hay mocks (no MSW). El front llama directo al back; las pantallas admin quedan en
error/vacío hasta que el back exponga sus endpoints.

---
Anterior: [« 05 · API que consume](05_api_design.md) · Siguiente: [07 · Seguridad »](07_security.md)
