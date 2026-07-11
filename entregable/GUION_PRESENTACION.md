# GUION DE PRESENTACIÓN — coffee-system

> Guion para armar la presentación en **Canva** y exponer. **Duración total: 18 minutos · 5 integrantes.** Estructura: **Introducción → Desarrollo → Conclusión**. El bloque fuerte es "**Temas del curso**" y el **demo en vivo**: ahí el docente evalúa que entendemos las estructuras de datos.

---

## Reparto por integrante (18 min)

| Integrante | Rol en la expo | Slides | Tiempo |
|---|---|---|---|
| **[INTEGRANTE 1]** | Introducción: problema, objetivos, equipo | 1–3 | ~3 min |
| **[INTEGRANTE 2]** | Arquitectura y módulos del sistema | 4–6 | ~3 min |
| **[INTEGRANTE 3]** | **Temas del curso**: Pila y Cola (con el código) | 7–9 | ~4 min |
| **[INTEGRANTE 4]** | **Temas del curso**: Matriz, Arreglos, TAD, POO | 10–12 | ~3.5 min |
| **[INTEGRANTE 5]** | **Demo en vivo** + conclusiones y cierre | 13–17 | ~4.5 min |

> Consejo: cada quien practica SOLO sus slides y su minutaje. Dejen 30 s de colchón para preguntas rápidas entre bloques.

---

## INTRODUCCIÓN — [INTEGRANTE 1] (~3 min)

**Slide 1 — Portada**
- Muestra: título "coffee-system", curso, nombres del equipo, docente.
- Qué decir: "Buenas, somos el grupo [NN]. Presentamos coffee-system, un sistema de gestión para una cafetería, hecho en Java aplicando estructuras de datos."

**Slide 2 — El problema**
- Muestra: 3-4 viñetas del problema (ventas sin control, inventario a ciegas, sin visión del negocio, errores al tomar pedidos).
- Qué decir: explica en una frase cada dolor de una cafetería que trabaja a mano.

**Slide 3 — Objetivos del sistema**
- Muestra: objetivo general + 3 específicos clave (registrar ventas, controlar stock con "deshacer", analizar ingresos).
- Qué decir: "Nuestro objetivo fue resolver esto con software, y de paso aplicar los temas del curso con un uso real."

---

## DESARROLLO — Arquitectura — [INTEGRANTE 2] (~3 min)

**Slide 4 — Visión general / Arquitectura**
- Muestra: diagrama simple → Frontend (navegador) ↔ Backend (Java/Spring Boot) ↔ Base de datos. Dos roles: Administrador y Mesero.
- Qué decir: "Es una app web. El backend está en Java, organizado en capas: controlador, servicio, repositorio y entidades."

**Slide 5 — Módulos**
- Muestra: la tabla de módulos (Autenticación, POS/Pedidos, Boletas, Productos, Insumos, Stock, Usuarios, Dashboard, Ingresos, Despacho, TAD).
- Qué decir: recorre los módulos en 20 segundos; destaca que cada uno es un paquete de clases.

**Slide 6 — Organización del proyecto (POO)**
- Muestra: el árbol de paquetes (`controller`, `service`, `repository`, `entity`, `tad`, `security`, `exception`).
- Qué decir: "Aplicamos POO: encapsulamiento en las entidades, herencia en las excepciones, interfaces para los servicios y para nuestras estructuras de datos."

---

## DESARROLLO — Temas del curso (el corazón) — [INTEGRANTE 3] (~4 min)

**Slide 7 — Pila (Stack) — qué es y dónde la usamos**
- Muestra: definición LIFO + los dos usos: **deshacer del carrito** (mesero) y **deshacer del ingreso de stock** (admin).
- Qué decir: "Implementamos nuestra propia Pila con lista enlazada, sin usar `java.util.Stack`, como pide el curso. La usamos para deshacer."
- Apóyate en el archivo: `backend/.../tad/PilaEnlazada.java`.

**Slide 8 — Pila: el código**
- Muestra: fragmento de `push` y `pop` (nodo enlazado, O(1)).
- Qué decir: explica que `push` enlaza un nodo nuevo al tope y `pop` lo quita — LIFO. Muestra que el `size` se mantiene en un contador.

**Slide 9 — Cola con prioridad (Queue)**
- Muestra: definición FIFO + prioridad; uso en el **despacho** de pedidos.
- Qué decir: "La Cola ordena la atención por orden de llegada. También es TAD propio (`ColaPrioridad`), con `enqueue`/`dequeue`."
- Archivo: `backend/.../tad/ColaPrioridad.java`.

---

## DESARROLLO — Matriz, Arreglos, TAD — [INTEGRANTE 4] (~3.5 min)

**Slide 10 — Matriz (arreglo 2D)**
- Muestra: la idea del reporte mensual como tabla producto (filas) × día (columnas).
- Qué decir: "El reporte mensual es una **matriz** `double[][]`: cada celda `[i][j]` son las ventas del producto i en el día j. Se recorre con doble índice."
- Archivo: `backend/.../service/implement/ReporteServiceImpl.java` (método `reporteMensual`).

**Slide 11 — Arreglos 1D y TAD**
- Muestra: catálogo/listas de productos y líneas de pedido; el concepto de TAD (contrato + implementación).
- Qué decir: "Los arreglos/listas están en todo el sistema: catálogo, detalle de pedidos. Y nuestras estructuras son TAD: una interfaz define el contrato y una clase lo implementa."

**Slide 12 — Tabla: tema del curso → dónde en el código**
- Muestra: la tabla del capítulo 3 del informe (tema | archivo | cómo se aplica).
- Qué decir: "Para que quede claro, esta tabla mapea cada tema del curso al lugar exacto del código." (Aquí el docente suele preguntar — tenla dominada.)
- **Honestidad:** si preguntan por árboles/AVL, responder: "Quedaron fuera de alcance; priorizamos estructuras con uso real. Lo dejamos como mejora futura." (Está en Recomendaciones.)

---

## DEMO EN VIVO + CONCLUSIÓN — [INTEGRANTE 5] (~4.5 min)

**Antes de exponer (checklist técnico):**
- Backend corriendo en `:8080`; scripts `backend/Docs/db/001`, `002`, `003` ejecutados en la base; carta y usuarios sembrados.
- Frontend servido en `:5500`; navegador abierto en el login; sesión cerrada.
- Ten a mano un usuario ADMIN y uno MESERO.

**Slide 13 — Demo: login y POS (mesero)**
- Acción: inicia sesión como **mesero** → agrega productos al carrito → pulsa **"Deshacer"** un par de veces.
- Qué decir mientras lo haces: "Cada acción del carrito se apila; 'Deshacer' hace `pop` de la **Pila** en el backend."

**Slide 14 — Demo: confirmar pedido y boleta**
- Acción: confirma el pedido → se muestra la **boleta** con detalle y total.
- Qué decir: "Al confirmar, se descuenta el stock de los insumos según la receta."

**Slide 15 — Demo: panel admin (matriz e ingresos)**
- Acción: cierra sesión, entra como **admin** → **Dashboard** (KPIs y gráfico del mes) → **Ingresos** (comparativa mensual + gráfico por semana).
- Qué decir: "El gráfico del mes se arma con la **matriz** producto × día que vimos."

**Slide 16 — Demo: stock con Pila**
- Acción: **Control de Stock** → registra un ingreso → aparece en "Últimos Ingresos" → pulsa **"Deshacer"** → el saldo baja.
- Qué decir: "Mismo TAD Pila, otro uso: deshacer el último ingreso de inventario."

**Slide 17 — Conclusiones y cierre**
- Muestra: 3-4 conclusiones (sistema funcional, estructuras con propósito real, código modular y comentado, seguridad por roles).
- Qué decir: cierra con el aprendizaje del equipo y agradece. Abre a preguntas.

---

## Consejos de tiempo (para no pasar de 18 min)

- **No** leas código línea por línea: muestra el fragmento y explica la idea.
- El demo es lo que más se alarga: ensáyalo con cronómetro; si algo falla, ten **capturas de respaldo** en las slides.
- Si el docente interrumpe con preguntas, réstalo del colchón, no del siguiente expositor.
- Frase de rescate si falla el demo: "Como respaldo, aquí están las capturas del flujo."

## Qué NO mostrar (para ahorrar tiempo)

- Configuración de Maven / dependencias.
- El detalle de cada DTO o cada endpoint.
- Código de seguridad JWT en profundidad (basta mencionarlo).

---

> Reemplaza los `[INTEGRANTE N]` y `[NN]`. Ajusta el reparto si alguien domina más un tema. El orden Introducción → Desarrollo → Conclusión es el que pide la consigna.
