> SDD coffee-system · Documento 3 de 8 · Versión 0.1 · Responsable: Equipo coffee-system · Fecha: 2026-06-26

# 03 · Arquitectura — Frontend

## Estilo

**Multi-página estática** (un `.html` por pantalla) con **Vanilla JavaScript ES6** en
módulos `import/export`. Sin framework ni bundler: cada HTML carga su controlador con
`<script type="module">`. Patrón por pantalla: *controlador de página* que (1) protege
la ruta con `requireRole`, (2) llama al back vía `apiFetch`, (3) renderiza DOM y
(4) reacciona a eventos.

```
HTML (pantalla)
   │ <script type="module">
   ▼
js/pages/<pantalla>.js   ── requireRole() ──► js/core/auth.js
   │
   ├─ apiFetch() ─────────► js/core/api.js ──► fetch ──► http://localhost:8080/api
   ├─ mostrarToast/Spinner ► js/utils/dom.js
   └─ formatearMoneda/Fecha ► js/utils/format.js
```

## Estructura real de carpetas

```
frontend/
├── index.html                 # Login (pestañas Personal/Administrador)
├── pages/
│   ├── mesero/
│   │   ├── pedidos.html        # POS: grilla + carrito
│   │   ├── mis-pedidos.html    # tabla de pedidos del turno
│   │   └── boleta.html         # comprobante
│   └── admin/
│       ├── dashboard.html      # KPIs + gráfico
│       ├── productos.html      # CRUD productos
│       ├── insumos.html        # CRUD insumos
│       ├── stock.html          # ingreso + tabla de stock
│       └── usuarios.html       # CRUD usuarios
├── js/
│   ├── core/
│   │   ├── api.js              # apiFetch (fetch + JWT + 401/204 + JSON)
│   │   ├── auth.js             # sesión localStorage, logout, requireRole
│   │   └── router.js           # redirigirPorRol (post-login)
│   ├── pages/                  # 1 controlador por pantalla (9 archivos)
│   └── utils/
│       ├── dom.js              # toast, spinner, estado vacío, crearModal
│       └── format.js           # moneda, fecha, badge de estado
└── css/
    ├── main.css                # variables, reset
    ├── components.css          # botones, tarjetas, modales, badges
    ├── layout.css              # layout general
    └── pages/                  # 1 CSS por pantalla
```

## Capa de acceso a datos — `api.js`

```js
const BASE_URL = 'http://localhost:8080/api';
// apiFetch(endpoint, options):
//   - inyecta Authorization: Bearer <jwt_token> si existe
//   - 401 → logout() y corta
//   - 204 → null
//   - !ok → throw (json del error o { message })
//   - ok  → response.json()
```

> No hay capa de "servicios por dominio" ni tipos: cada página llama `apiFetch` con la
> ruta a mano. Es simple y suficiente para el tamaño actual. (ponytail: añadir una capa
> `services/` solo si la duplicación de rutas empieza a doler.)

## Flujo Front↔Back

- Todas las llamadas pasan por `apiFetch` → `http://localhost:8080/api`.
- El front debe servirse en `:5500` (Live Server) por el CORS del back.
- Contrato detallado y divergencias en [05_api_design.md](05_api_design.md).

## Despliegue

Archivos estáticos servidos por Live Server (VS Code) en `http://localhost:5500`.
No hay build. Dependencias externas por CDN: Chart.js, Google Fonts (Material Symbols).

---
Anterior: [« 02 · Requisitos](02_requirements.md) · Siguiente: [05 · API que consume »](05_api_design.md)
