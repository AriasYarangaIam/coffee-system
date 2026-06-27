# design — Design system del frontend

Sistema de diseño as-built, extraído de `css/main.css` (`:root`) y los CSS por
pantalla. Tema **cafetería**: paleta de cafés y cremas.

## Paleta (variables CSS)

| Token | Valor | Uso |
|---|---|---|
| `--color-primary` | `#5C3317` | café tostado (acentos, botones, gráfico) |
| `--color-primary-dark` | `#3A1F0A` | espresso oscuro (hover) |
| `--color-secondary` | `#8B6347` | latte |
| `--color-secondary-light` | `#EDD9C0` | crema suave |
| `--color-success` | `#4A7A1E` | éxito |
| `--color-danger` | `#BA1A1A` | error/eliminar |
| `--color-warning` | `#C07830` | caramelo (alertas) |
| `--color-bg` | `#F7EDE0` | fondo crema |
| `--color-surface` | `#FDF6EE` | tarjetas (leche espumada) |
| `--color-surface-high` | `#F0DFC8` | superficies elevadas |
| `--color-text` | `#1E0F06` | texto principal |
| `--color-text-muted` | `#6B4A2E` | texto secundario |
| `--color-border` | `#D9C4AA` | bordes |

## Tipografía

- Cuerpo: `--font-family: 'DM Sans', sans-serif`.
- Títulos: `--font-heading: 'DM Serif Display', serif`.
- Escala: `--font-size-sm .875rem` · `base 1rem` · `lg 1.125rem` · `xl 1.5rem` · `2xl 2rem`.
- Iconos: Material Symbols Outlined (Google Fonts CDN).

## Espaciado, radios y sombras

- Espaciado: `xs 4px` · `sm 8px` · `md 16px` · `lg 24px` · `xl 40px`.
- Radios: `sm 4px` · `md 8px` · `lg 16px` · `xl 24px` · `full 9999px`.
- Sombras: `--shadow-sm/md/lg` (tinte café `rgba(58,31,10,…)`).

## Componentes (en `css/components.css`)

- **Botones**: `.btn` + variantes `btn-primary`, `btn-secondary`, `btn-outline`,
  `btn-danger`, `btn-sm`.
- **Tarjetas**: `.product-card` (grilla del POS), con estado `.out-of-stock`.
- **Modales**: `.modal-overlay`/`.modal` (factory `crearModal`) y modales declarativos
  con clase `.hidden` en las páginas admin. ⚠️ dos patrones (ver B-F-10).
- **Badges de estado**: `.badge` + `badge-pendiente`/`badge-pagado`/`badge-cancelado`
  (`format.js renderBadgeEstado`). ⚠️ atados a la feature de estados, fuera de alcance.
- **Feedback**: `.toast` (3 s), `.spinner`, `.empty-state` (`dom.js`).
- **Tablas**: `.table` para listados admin y boleta.

## Gráficos

- Chart.js (CDN), solo en `dashboard.html`: barras de ventas mensuales, color de barra
  `#5C3317` (`--color-primary`), `borderRadius: 6`, sin leyenda, eje Y con prefijo `S/`.

## Formato (es-PE)

- Moneda: `Intl.NumberFormat('es-PE', { currency: 'PEN' })` → "S/ 12.50".
- Fechas: `toLocaleDateString('es-PE', …)` → "15/07/2025, 10:30".

## Notas de diseño

- Layout responsive con CSS grid/flex (`layout.css`); revisar breakpoints móviles (RNF-05).
- Un CSS por pantalla en `css/pages/` para estilos específicos; lo común en
  `main.css`/`components.css`.
