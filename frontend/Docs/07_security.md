> SDD coffee-system · Documento 7 de 8 · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 07 · Seguridad — Frontend

El contrato de seguridad del servidor (JWT, BCrypt, roles, CORS) está en
[backend/Docs/07_security.md](../../backend/Docs/07_security.md). Aquí: cómo el front
maneja sesión, token y autorización.

## Sesión y token

- Al hacer login (`login.js`), `guardarSesion(token, usuario)` (`auth.js`) escribe en
  `localStorage`:
  - `jwt_token` → el JWT.
  - `usuario` → `{ correo, rol, nombreCompleto }` (JSON).
- En cada request, `apiFetch` (`api.js`) añade
  `Authorization: Bearer <jwt_token>` si el token existe.
- `logout()` borra ambas claves y redirige a `index.html`.

## Guard de rutas

- Cada controlador de página llama `requireRole('MESERO' | 'ADMIN')` al inicio
  (`auth.js`):
  - sin token o sin `usuario.rol` → redirige a `index.html`;
  - rol distinto al requerido → redirige al home del rol real (admin→dashboard,
    otro→pedidos).
- Tras login, `redirigirPorRol()` (`router.js`) decide el destino.

## ⚠️ Inconsistencia del rol admin (crítica)

| Lugar | Valor que usa |
|---|---|
| `auth.js` `requireRole` / `usuarios.js` badge | `ADMIN` |
| `router.js` `redirigirPorRol` | **`ADMINISTRADOR`** |
| Backend `@PreAuthorize` | **`ADMINISTRADOR`** |

Si el back devuelve `rol = "ADMIN"`, `router.js` no entra en la rama admin y manda al
usuario de vuelta al login (bucle); si devuelve `"ADMINISTRADOR"`, fallan `requireRole`
y el badge. **Hay que unificar a un único valor canónico** (propuesta: `ADMIN`, según
spec) en front y back. Ver [09_backlog_brechas.md](09_backlog_brechas.md) (B-F-04) y el
backlog del back (B-03).

## Manejo de errores de auth

- **401** (token ausente/expirado): `apiFetch` ejecuta `logout()` automáticamente.
- **403** (rol insuficiente): ⚠️ **no hay manejo específico**; cae al `throw` genérico y
  se muestra un toast. Considerar un mensaje claro de "sin permiso".

## Riesgos (→ 09)

- Token JWT en `localStorage`: accesible a JS → expuesto a XSS. Aceptable para el
  alcance académico; mantener el front libre de inyección de HTML no confiable.
- Render con `innerHTML` + interpolación de datos del back (nombres de producto/usuario)
  sin escape → riesgo XSS si esos datos fueran maliciosos. Sanitizar si se abre a más
  usuarios.

---
Anterior: [« 06 · Diseño frontend](06_frontend_design.md) · Siguiente: [09 · Backlog de brechas »](09_backlog_brechas.md)
