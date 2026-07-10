-- 002 · Borrado lógico de usuarios
-- Ejecutar en Supabase ANTES de levantar el backend (ddl-auto=validate).
--
-- Eliminar un usuario ya no borra su fila (ni su histórico de ventas): pasa a
-- activo=false. Un usuario inactivo no puede iniciar sesión y no aparece en la
-- lista de administración, pero sus pedidos siguen contando para los reportes.

ALTER TABLE usuarios ADD COLUMN activo BOOLEAN NOT NULL DEFAULT true;
