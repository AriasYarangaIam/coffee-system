# **Items Pendientes de Backend Puro \- Sprint 1**

Este documento contiene el listado filtrado de los requerimientos técnicos y funcionales correspondientes de forma exclusiva a la capa de **Spring Boot (Java)** que quedaron pendientes o incompletos en el Sprint 1\. Se han excluido todas las tareas que interactúan de forma directa con el almacenamiento, transacciones o persistencia en PostgreSQL, dejando únicamente aquellas cuyo comportamiento e implementación pueden ser testeados de manera aislada en la lógica de negocio y configuraciones en memoria.

## **1\. Restricción de Accesos Fina por Roles en Catálogos**

* **Estado:** ✅ DONE (B-10, Sprint 2 · rama `feat/back/dashboard-y-roles-sprint-2`).
* **Resolución:** `GET /api/productos` ahora se autoriza con `@PreAuthorize("hasAnyRole('MESERO','ADMIN')")` (`ProductoController.java:28`); antes solo `MESERO`, por lo que un ADMIN recibía 403. El `@PreAuthorize` se ejerce de verdad (method security ya estaba activo en `SecurityConfig` con `@EnableMethodSecurity`); no hubo defecto de deserialización del rol. Cercado con slice hermético `web/ProductoControllerWebMvcTest`: ADMIN → 200, MESERO → 200, rol no autorizado → 403, sin BD.

## **2\. Estructura FIFO (Cola) para el Despacho de Pedidos**

* **Estado:** Faltante.  
* \*\*Bloqueo:\*\* Indefinición en el protocolo de comunicación de la API cliente-servidor.  
* **Descripción de la razón:** Requiere el diseño e implementación de una estructura de datos de tipo cola (como \`Queue\` o \`ConcurrentLinkedQueue\` en Java) administrada en el Service de pedidos para gestionar el flujo FIFO de despacho. Se encuentra detenido debido a que no se ha cerrado la definición de red sobre si el cliente consumirá este recurso mediante peticiones síncronas repetitivas (*polling*) o a través de una conexión dúplex bidireccional continua (*WebSockets*). Su desarrollo es puramente de software en memoria.

### **3\. Consistencia en el Payload de DTOs (Mapeo de Usuarios)**

* **Estado:** ✅ DONE (rama `feat/back/usuario-mapper-refactor`, cambio SDD `backend-puro-faltantes` cap. `usuario-mapper-refactor`).
* **Resolución:** Se extrajo `mapper/UsuarioMapper` (`@Component`, mira `mapper/ProductoMapper`) y `UsuarioServiceImpl.obtenerUsuarios` delegates la conversión `Usuarios → UsuarioResponseDTO` sin lambda inline. El contrato de wire se cercó con `@WebMvcTest` hermético (`UsuarioControllerWebMvcTest`):
  - `UsuarioResponseDTO.java:4-11` — record con los 6 campos separados (`usuarioId`, `nombreUsuario`, `apellidoUsuario`, `correoUsuario`, `telefonoUsuario`, `rol`); NO concatena nombre+apellido.
  - `UsuarioServiceImpl.java:45-49` — delega a `usuarioMapper::toResponseDTO` (sin `new UsuarioResponseDTO(...)` inline).
  - `mapper/UsuarioMapper.java:13-22` — `toResponseDTO(Usuarios)` produce los 6 campos.
  - `web/UsuarioControllerWebMvcTest` — locks la wire shape (`usuarioId` Long + `rol` String + 4 campos) y el 403 para no-ADMIN (`REQ-UMR-02`, `REQ-UMR-03`); `UsuarioMapperTest` cubre el round-trip (`REQ-UMR-01`) incluyendo nombre compuesto "Maria José".
* **NOTA — el defecto de "separación rígida por espacios" vive en el FRONT, no en el backend:** el backend expone `nombreUsuario` y `apellidoUsuario` por separado y correctamente. La concatenación manual defectuosa que rompe nombres compuestos (p. ej. "Maria José") está en `frontend/js/pages/usuarios.js:73,95-96`, donde se hace `inputNombre.value = `${nombre} ${apellido}`` (L73) y luego `inputNombre.value.trim().split(' ')` con `partes[0]` y `partes.slice(1).join(' ')` (L93-96). Corregir el front está fuera de scope de esta sección (es backend-puro); queda pendiente para un cambio frontend dedicado.