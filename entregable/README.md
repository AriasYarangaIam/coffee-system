# Entregables para el profesor

Esta carpeta reúne lo que pide la consigna del curso (`frontend/indicaciones_del_profesor.md`).

| Archivo | Qué es | Qué hacer con él |
|---|---|---|
| `INFORME_TECNICO.md` | El informe técnico completo, con la estructura del Anexo 1 (carátula, índice, aspectos generales, diseño de la aplicación, temas del curso, conclusiones, recomendaciones, bibliografía). | Volcarlo a **`.docx`**, poner el logo UTP y reemplazar los placeholders `[ ]`. |
| `GUION_PRESENTACION.md` | Guion cronometrado de la exposición (18 min, 5 integrantes) para armar la presentación en **Canva**, con el demo paso a paso. | Basar las diapositivas en él; ensayar con cronómetro. |

## Antes de entregar / exponer

1. **Reemplazar todos los placeholders** `[INTEGRANTE 1..5]`, `[DOCENTE]`, `[GRUPO NN]`, `[CICLO]`, `[SEDE]`, `[AÑO]`, `[FACULTAD]`, `[CARRERA]`.
2. **El código ya está comentado** (qué hace y cómo funciona) en todo el backend Java y el frontend JS.
3. Para el **demo**, tener el backend corriendo con los scripts `backend/Docs/db/001`, `002` y `003` ejecutados en la base, y la carta/usuarios sembrados.
4. **Honestidad de alcance:** el informe declara solo los temas del curso realmente implementados (arreglos, matriz, TAD, lista enlazada, Pila, Cola con prioridad). Árboles/AVL/listas dobles quedan como "fuera de alcance / mejora futura".

## Dónde está cada cosa en el repo

- **Estructuras de datos (TAD):** `backend/src/main/java/com/coffee/backend/tad/` (Pila, PilaEnlazada, Cola, ColaPrioridad).
- **Matriz del reporte mensual:** `backend/.../service/implement/ReporteServiceImpl.java`.
- **Documentación técnica de apoyo:** `backend/Docs/` y `frontend/Docs/`.
