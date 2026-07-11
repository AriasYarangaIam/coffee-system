# **Items Pendientes de Backend Puro \- Sprint 1**

Este documento contiene el listado filtrado de los requerimientos técnicos y funcionales correspondientes de forma exclusiva a la capa de **Spring Boot (Java)** que quedaron pendientes o incompletos en el Sprint 1\. Se han excluido todas las tareas que interactúan de forma directa con el almacenamiento, transacciones o persistencia en PostgreSQL, dejando únicamente aquellas cuyo comportamiento e implementación pueden ser testeados de manera aislada en la lógica de negocio y configuraciones en memoria.

## **1\. Restricción de Accesos Fina por Roles en Catálogos**

* **Estado:** ✅ DONE (B-10, Sprint 2 · rama `feat/back/sprint-1-contrato-y-endpoints-admin`).
* **Resolución:** `GET /api/productos` ahora se autoriza con `@PreAuthorize("hasAnyRole('MESERO','ADMIN')")` (`ProductoController.java:27`); antes solo `MESERO`, por lo que un ADMIN recibía 403. El `@PreAuthorize` se ejerce de verdad (method security ya estaba activo en `SecurityConfig` con `@EnableMethodSecurity`); no hubo defecto de deserialización del rol. Cercado con slice hermético `web/ProductoControllerWebMvcTest`: ADMIN → 200, MESERO → 200, rol no autorizado → 403, sin BD.

## **2\. Estructura FIFO (Cola) para el Despacho de Pedidos**

* **Estado:** ✅ DONE (RF-DS-04, Sprint 2 Task 11 · rama `feat/back/cola-despacho-fifo`).
* **Resolución:** Se construyó el **TAD propio en el repo** (`tad/Cola<T>` + `tad/ColaPrioridad<T>` con nodos enlazados propios — NO `java.util.Queue`, como exige el sílabo). La premisa del diseño SDD "BLOCKED until `iam-sprint-2-bundle`" **queda anulada**: ese bundle nunca existió, así que el TAD vive aquí. Cola residente en `PedidoDespachoServiceImpl` (singleton ⇒ instancia única); `registrarPedido` encola el token tras el `save`; `DespachoBootstrapRunner` (ApplicationRunner) rehidrata la cola desde la BD al arrancar en orden FIFO (`findAllByOrderByFechaPedidoAsc`).
* **Protocolo resuelto = polling.** El bloqueo polling-vs-WebSocket se cerró a favor de **polling**: se expone `GET /api/admin/despacho` (ADMIN) que devuelve la cola FIFO actual. WebSockets queda fuera de alcance (innecesario para el demo). `siguienteDespacho` es peek (sin dequeue); la máquina de estados PENDIENTE/DESPACHADO se difiere (necesita DDL `estado`).
* **Tests (JUnit puro, sin Spring/BD):** `tad/ColaPrioridadTest` (FIFO, bandas de prioridad, peek/dequeue, vacía) + `service/PedidoDespachoServiceImplTest` (enqueue→listar FIFO, peek, rehidratación del runner con repo mockeado).

### **3\. Consistencia en el Payload de DTOs (Mapeo de Usuarios)**

* **Estado:** ✅ DONE (rama `feat/back/usuario-mapper-refactor`, cambio SDD `backend-puro-faltantes` cap. `usuario-mapper-refactor`).
* **Resolución:** Se extrajo `mapper/UsuarioMapper` (`@Component`, mira `mapper/ProductoMapper`) y `UsuarioServiceImpl.obtenerUsuarios` delegates la conversión `Usuarios → UsuarioResponseDTO` sin lambda inline. El contrato de wire se cercó con `@WebMvcTest` hermético (`UsuarioControllerWebMvcTest`):
  - `UsuarioResponseDTO.java:4-11` — record con los 6 campos separados (`usuarioId`, `nombreUsuario`, `apellidoUsuario`, `correoUsuario`, `telefonoUsuario`, `rol`); NO concatena nombre+apellido.
  - `UsuarioServiceImpl.java:45-49` — delega a `usuarioMapper::toResponseDTO` (sin `new UsuarioResponseDTO(...)` inline).
  - `mapper/UsuarioMapper.java:13-22` — `toResponseDTO(Usuarios)` produce los 6 campos.
  - `web/UsuarioControllerWebMvcTest` — locks la wire shape (`usuarioId` Long + `rol` String + 4 campos) y el 403 para no-ADMIN (`REQ-UMR-02`, `REQ-UMR-03`); `UsuarioMapperTest` cubre el round-trip (`REQ-UMR-01`) incluyendo nombre compuesto "Maria José".
* **NOTA — el defecto de "separación rígida por espacios" vive en el FRONT, no en el backend:** el backend expone `nombreUsuario` y `apellidoUsuario` por separado y correctamente. La concatenación manual defectuosa que rompe nombres compuestos (p. ej. "Maria José") está en `frontend/js/pages/usuarios.js:73,95-96`, donde se hace `inputNombre.value = `${nombre} ${apellido}`` (L73) y luego `inputNombre.value.trim().split(' ')` con `partes[0]` y `partes.slice(1).join(' ')` (L93-96). Corregir el front está fuera de scope de esta sección (es backend-puro); queda pendiente para un cambio frontend dedicado.