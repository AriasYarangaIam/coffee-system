# Coffee System — Cafetería MYPE

Sistema de gestión para una cafetería MYPE: backend Spring Boot + frontend en JavaScript
vanilla (módulos ES). El backend expone una API REST bajo `/api` y el frontend la consume.

- **Backend:** `backend/` (Spring Boot, PostgreSQL, JWT). Puerto `8080`.
- **Frontend:** `frontend/` (HTML/CSS/JS vanilla). Debe servirse en el puerto **5500**
  (CORS del backend solo permite `http://localhost:5500` y `http://127.0.0.1:5500`).
- **Documentación de contexto:** ver [`docs/`](docs/) — estado de la integración FE↔BE,
  contrato de endpoints y registro de cambios.

## Cómo levantar el proyecto

1. PostgreSQL en marcha con una BD `test` (ver `backend/src/main/resources/application.yaml`).
2. Backend: `cd backend && ./mvnw spring-boot:run`
3. Frontend: servir `frontend/` en el puerto **5500** (Live Server de VS Code o IntelliJ)
   y abrir `http://localhost:5500/index.html`.
4. Datos semilla: ver [`docs/INTEGRACION-FRONTEND-BACKEND.md`](docs/INTEGRACION-FRONTEND-BACKEND.md).

---

## ✅ Sprint 1 Backend — completado

Cambios de contrato que el **frontend debe re-alinear** en su integración:

| Tema | Antes | Ahora |
|---|---|---|
| Rol admin canónico | `ADMINISTRADOR` | **`ADMIN`** (alinear `auth.js`/guards al valor que devuelve el login) |
| Crear pedido | `{usuarioId, aliasTicket, detalles}` | `{ detalles:[{productoId, cantidadPedida}] }` (usuario del JWT, `aliasTicket` lo genera el back) |
| Listar pedidos del mesero | ❌ no existía | `GET /api/pedidos` → `[{pedidoId, aliasTicket, fechaPedido, total}]` |
| Gestión de usuarios | `/api/usuario/*` (doble slash, solo MESERO) | `GET/POST/DELETE /api/admin/usuarios` + `PATCH /api/admin/usuarios/actualizar`; respuesta con `usuarioId` y `rol`; `POST` acepta `rol` (crea ADMIN) |
| CRUD admin | ❌ no existían | `GET/POST/PUT/DELETE /api/admin/productos` e `/api/admin/insumos`; `GET/POST /api/admin/stocks` |
| Config | credenciales hardcodeadas | variables de entorno (`backend/.env`, plantilla `backend/.env.example`) → **Supabase** |
| Endpoint debug `/api/auth/get` | existía | eliminado |

### Configuración (Supabase)

`application.yaml` lee variables de entorno. Copiar `backend/.env.example` → `backend/.env`
(gitignored) y rellenar `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (Connection Pooler de Supabase,
`?sslmode=require`) y `JWT_SECRET`/`JWT_EXPIRATION`.

### Pendiente (Sprint 2)

`GET /api/admin/dashboard`, `GET /api/admin/reportes/mensual` (matriz), cola FIFO de despacho,
ampliar `GET /api/productos` a ADMIN, `PATCH /api/pedidos/{id}/estado` queda **fuera de alcance**
(sin estados de cocina). `mesero/mis-pedidos` ya puede consumir `GET /api/pedidos`.

> Para el detalle completo del contrato y el registro de cambios del frontend, ver la carpeta
> [`docs/`](docs/).
