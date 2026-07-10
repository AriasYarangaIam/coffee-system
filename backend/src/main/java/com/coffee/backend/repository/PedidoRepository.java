package com.coffee.backend.repository;

import com.coffee.backend.entity.Pedidos;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedidos,Long> {
    // Pedidos del mesero logueado (se filtra por el correo del JWT).
    List<Pedidos> findByUsuario_CorreoUsuario(String correoUsuario);

    // Rehidratación de la cola de despacho (RF-DS-04): pedidos en orden FIFO de llegada.
    List<Pedidos> findAllByOrderByFechaPedidoAsc();

    // --- Reporte mensual / Matriz producto × día (B-22, RF-DS-02) ---

    // Proyección de una celda (producto, día del mes, ventas) del rango [inicio, fin).
    interface VentaProductoDia {
        String getProducto();
        int getDia();
        double getTotal();
    }

    // Ventas (cantidad * precio) agrupadas por producto y día del mes. EXTRACT(DAY ...)
    // es portable a Postgres (Supabase). El servicio arma la matriz 2D con estas filas.
    @Query(value = """
            SELECT pr.nombre_producto AS producto,
                   EXTRACT(DAY FROM p.fecha_pedido) AS dia,
                   SUM(d.cantidad_pedida * d.precio_unitario) AS total
            FROM detalle_pedido d
            JOIN pedidos p ON d.pedido_id = p.pedido_id
            JOIN productos pr ON d.producto_id = pr.producto_id
            WHERE p.fecha_pedido >= :inicio AND p.fecha_pedido < :fin
            GROUP BY pr.nombre_producto, EXTRACT(DAY FROM p.fecha_pedido)
            ORDER BY pr.nombre_producto
            """, nativeQuery = true)
    List<VentaProductoDia> ventasPorProductoYDia(@Param("inicio") LocalDateTime inicio,
                                                 @Param("fin") LocalDateTime fin);

    // --- Dashboard admin (B-05) ---

    // Suma de ventas (cantidad * precio) de los pedidos del rango [inicio, fin).
    @Query("""
            SELECT COALESCE(SUM(d.cantidadPedida * d.precioUnitario), 0)
            FROM DetallePedido d
            WHERE d.pedidos.fechaPedido >= :inicio AND d.pedidos.fechaPedido < :fin
            """)
    double sumarVentasEntre(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Cantidad de pedidos del rango [inicio, fin).
    @Query("""
            SELECT COUNT(p) FROM Pedidos p
            WHERE p.fechaPedido >= :inicio AND p.fechaPedido < :fin
            """)
    long contarPedidosEntre(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Nombres de producto ordenados por cantidad vendida (desc) en el rango [inicio, fin).
    // El servicio toma el primero (Pageable top 1) como "producto estrella".
    @Query("""
            SELECT d.productos.nombreProducto
            FROM DetallePedido d
            WHERE d.pedidos.fechaPedido >= :inicio AND d.pedidos.fechaPedido < :fin
            GROUP BY d.productos.nombreProducto
            ORDER BY SUM(d.cantidadPedida) DESC
            """)
    List<String> productosMasVendidos(@Param("inicio") LocalDateTime inicio,
                                       @Param("fin") LocalDateTime fin,
                                       Pageable pageable);

    // --- Ingresos / BI del admin (módulo de ingresos) ---

    // Proyección de una semana (fecha de inicio de semana, ventas del periodo).
    interface IngresoSemana {
        java.time.LocalDate getSemana();
        double getTotal();
    }

    // Ventas agrupadas por semana ISO (lunes). Alias en minúscula: Postgres pliega los
    // identificadores sin comillas, como en ventasPorProductoYDia.
    @Query(value = """
            SELECT date_trunc('week', p.fecha_pedido)::date AS semana,
                   COALESCE(SUM(d.cantidad_pedida * d.precio_unitario), 0) AS total
            FROM detalle_pedido d
            JOIN pedidos p ON d.pedido_id = p.pedido_id
            WHERE p.fecha_pedido >= :inicio AND p.fecha_pedido < :fin
            GROUP BY 1
            ORDER BY 1
            """, nativeQuery = true)
    List<IngresoSemana> ingresosPorSemana(@Param("inicio") LocalDateTime inicio,
                                          @Param("fin") LocalDateTime fin);

    // Proyección de una boleta emitida (resumen para el detalle transaccional).
    interface BoletaResumen {
        Long getPedido();
        String getAlias();
        LocalDateTime getFecha();
        String getMesero();
        double getTotal();
    }

    // Todas las boletas del rango con su mesero y total. LEFT JOIN a detalle para no
    // perder pedidos sin líneas (total 0).
    @Query(value = """
            SELECT p.pedido_id AS pedido,
                   p.alias_ticket AS alias,
                   p.fecha_pedido AS fecha,
                   (u.nombre_usuario || ' ' || u.apellido_usuario) AS mesero,
                   COALESCE(SUM(d.cantidad_pedida * d.precio_unitario), 0) AS total
            FROM pedidos p
            JOIN usuarios u ON p.usuario_id = u.usuario_id
            LEFT JOIN detalle_pedido d ON d.pedido_id = p.pedido_id
            WHERE p.fecha_pedido >= :inicio AND p.fecha_pedido < :fin
            GROUP BY p.pedido_id, p.alias_ticket, p.fecha_pedido, u.nombre_usuario, u.apellido_usuario
            ORDER BY p.fecha_pedido DESC
            """, nativeQuery = true)
    List<BoletaResumen> boletasEntre(@Param("inicio") LocalDateTime inicio,
                                     @Param("fin") LocalDateTime fin);

    // --- Métricas del turno del mesero (filtradas por su correo y rango del día) ---

    @Query("""
            SELECT COUNT(p) FROM Pedidos p
            WHERE p.usuario.correoUsuario = :correo
              AND p.fechaPedido >= :inicio AND p.fechaPedido < :fin
            """)
    long contarPedidosDeMeseroEntre(@Param("correo") String correo,
                                    @Param("inicio") LocalDateTime inicio,
                                    @Param("fin") LocalDateTime fin);

    @Query("""
            SELECT COALESCE(SUM(d.cantidadPedida * d.precioUnitario), 0)
            FROM DetallePedido d
            WHERE d.pedidos.usuario.correoUsuario = :correo
              AND d.pedidos.fechaPedido >= :inicio AND d.pedidos.fechaPedido < :fin
            """)
    double sumarVentasDeMeseroEntre(@Param("correo") String correo,
                                    @Param("inicio") LocalDateTime inicio,
                                    @Param("fin") LocalDateTime fin);

    @Query("""
            SELECT d.productos.nombreProducto
            FROM DetallePedido d
            WHERE d.pedidos.usuario.correoUsuario = :correo
              AND d.pedidos.fechaPedido >= :inicio AND d.pedidos.fechaPedido < :fin
            GROUP BY d.productos.nombreProducto
            ORDER BY SUM(d.cantidadPedida) DESC
            """)
    List<String> productosMasVendidosDeMesero(@Param("correo") String correo,
                                              @Param("inicio") LocalDateTime inicio,
                                              @Param("fin") LocalDateTime fin,
                                              Pageable pageable);
}
