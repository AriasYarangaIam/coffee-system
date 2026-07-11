package com.coffee.backend.repository;

import com.coffee.backend.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

// Repositorio JPA de DetallePedido (lineas de pedido).
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

}