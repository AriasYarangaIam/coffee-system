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
}
