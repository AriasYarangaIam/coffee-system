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

