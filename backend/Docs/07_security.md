> SDD coffee-system · Documento 7 de 11 · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 07 · Seguridad — Backend

## Mecanismo

**JWT stateless** + Spring Security. Evidencia: `security/SecurityConfig.java`,
`JwtUtil.java`, `JwtFilter.java`, `UserDetailsServiceImpl.java`.

- Sesiones: `SessionCreationPolicy.STATELESS`. CSRF deshabilitado.
- Público: `/api/auth/**`. Todo lo demás: `authenticated()`.
- Filtro `JwtFilter` antes de `UsernamePasswordAuthenticationFilter`.
- `@EnableMethodSecurity` → autorización fina con `@PreAuthorize` en controllers.

## Flujo de autenticación

```
POST /api/auth/login { correo, contraseña }
   │
   ▼ AuthServiceImpl
AuthenticationManager.authenticate(correo, contraseña)
   │  └─ DaoAuthenticationProvider + UserDetailsServiceImpl + BCrypt
   ▼
Carga Usuarios → rol = usuario.getRoles().getNombreRol()
   ▼
JwtUtil.generarToken(correo, rol)  → subject=correo, claim "rol", exp=jwt.expiration
   ▼
200 { token, correo, rol, nombreCompleto }
```

En cada request protegido: `JwtFilter` lee `Authorization: Bearer <token>`, valida
firma/expiración (`JwtUtil.esTokenValido`), carga `UserDetails` y coloca la
autenticación en el `SecurityContext`. Authorities = `ROLE_<nombreRol>`
(`UserDetailsServiceImpl`).

## Hashing

- `BCryptPasswordEncoder` (`SecurityConfig.codificadorPassword`).
- Al registrar usuario: `passwordEncoder.encode(claveCifrada)` antes de persistir.

## Roles y matriz de permisos

| Endpoint | MESERO | ADMIN |
|---|---|---|
| `POST /api/auth/login` | público | público |
| `GET /api/productos` | ✅ | ❌ ⚠️ (solo MESERO) |
| `GET /api/productos/{id}/receta` | ❌ | ✅ (`ADMINISTRADOR`) |
| `POST /api/pedidos` | ✅ | ❌ |
| `GET /api/pedidos/{id}/boleta` | ✅ | ❌ |
| `POST /api/usuario/registrar` | ✅ | ✅ |
| `GET /api/usuario/obtenerMeseros` | ❌ | ✅ |
| `DELETE /api/usuario/delete` | ❌ | ✅ |
| `PATCH /api/usuario/actualizar` | ✅ | ✅ |

## ⚠️ Inconsistencia del nombre del rol ADMIN (crítica)

El identificador del rol administrador aparece de **dos formas** en el código:

| Lugar | Valor | Evidencia |
|---|---|---|
| `@PreAuthorize` (Producto/Usuario controllers) | `ADMINISTRADOR` | requiere authority `ROLE_ADMINISTRADOR` |
| Comentario en `UserDetailsServiceImpl` | `"ADMIN" o "MESERO"` | línea 22 |
| Front `auth.js` (`requireRole`) | `ADMIN` | línea 30 |
| Front `router.js` (`redirigirPorRol`) | `ADMINISTRADOR` | línea 12 |

Como las authorities se construyen desde `nombre_rol` de la BD, **si la fila admin
guarda `ADMIN`**, los endpoints con `@PreAuthorize('ADMINISTRADOR')` devolverán **403**
siempre. `MESERO` es consistente en todos lados. → Decidir un valor canónico (la spec
dice `ADMIN`) y alinear back + front + seed de BD. Ver [09](09_backlog_brechas.md).

## Contrato de errores de auth

- Sin token / token inválido o expirado en endpoint protegido → **401** (Spring).
- Rol insuficiente → **403** (Spring, vía `@PreAuthorize`).
- El front: ante **401**, `api.js` ejecuta `logout()` y redirige a `index.html`. No
  hay manejo específico de 403 (ver `frontend/Docs/07_security.md`).

## CORS

`SecurityConfig.corsConfigurationSource`: orígenes `http://localhost:5500` y
`http://127.0.0.1:5500`; métodos `GET,POST,PUT,DELETE,OPTIONS`; headers `*`;
`allowCredentials=true`; aplicado a `/api/**`.

## Riesgos de seguridad (→ 09)

- `GET /api/auth/get` imprime `passwordEncoder.encode("123")` en consola — quitar.
- `jwt.secret` no versionado (correcto que no esté en repo) pero **tampoco** hay
  plantilla/doc de cómo proveerlo → setup frágil.

---
Anterior: [« 05 · Diseño de API](05_api_design.md) · Siguiente: [08 · Sprints »](08_sprints.md)
