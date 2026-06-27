# **Items Pendientes de Backend Puro \- Sprint 1**

Este documento contiene el listado filtrado de los requerimientos técnicos y funcionales correspondientes de forma exclusiva a la capa de **Spring Boot (Java)** que quedaron pendientes o incompletos en el Sprint 1\. Se han excluido todas las tareas que interactúan de forma directa con el almacenamiento, transacciones o persistencia en PostgreSQL, dejando únicamente aquellas cuyo comportamiento e implementación pueden ser testeados de manera aislada en la lógica de negocio y configuraciones en memoria.

## **1\. Restricción de Accesos Fina por Roles en Catálogos**

* **Estado:** Incompleto (Movido al Sprint 2).  
* **Bloqueo:** Fallas en la configuración del contexto de Spring Security y la cadena de autoridades (GrantedAuthorities).  
* **Descripción de la razón:** El filtro JwtFilter valida correctamente la autenticidad del token, pero la lógica de extracción del rol dentro del SecurityContextHolder no se está mapeando de forma dinámica hacia las anotaciones @PreAuthorize en los controladores (como el endpoint GET /api/productos). El problema es puramente de deserialización en memoria RAM y configuración de seguridad, por lo que puede probarse inyectando cabeceras simuladas sin requerir consultas de base de datos.

## **2\. Estructura FIFO (Cola) para el Despacho de Pedidos**

* **Estado:** Faltante.  
* \*\*Bloqueo:\*\* Indefinición en el protocolo de comunicación de la API cliente-servidor.  
* **Descripción de la razón:** Requiere el diseño e implementación de una estructura de datos de tipo cola (como \`Queue\` o \`ConcurrentLinkedQueue\` en Java) administrada en el Service de pedidos para gestionar el flujo FIFO de despacho. Se encuentra detenido debido a que no se ha cerrado la definición de red sobre si el cliente consumirá este recurso mediante peticiones síncronas repetitivas (*polling*) o a través de una conexión dúplex bidireccional continua (*WebSockets*). Su desarrollo es puramente de software en memoria.

### **3\. Consistencia en el Payload de DTOs (Mapeo de Usuarios)**

* \*\*Estado:\*\* Con discrepancias estructurales (Se utiliza un parche temporal).  
* \*\*Bloqueo:\*\* Lógica rígida de transformación de datos en la capa de Mappers.  
* **Descripción de la razón:** La serialización hacia JSON de la clase \`UsuarioResponseDTO\` implementa una concatenación manual defectuosa (separación rígida por espacios para nombres y apellidos) e introduce campos erróneos en las propiedades de \`usuarioId\` y \`rol\`. El bloqueo impide la correcta lectura en los clientes JS y su solución es netamente de refactorización de código Java y Mappers de datos, siendo completamente independiente de la persistencia física en PostgreSQL.