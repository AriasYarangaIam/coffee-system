package com.coffee.backend.repository;

import com.coffee.backend.entity.Pedidos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedidos,Long> {
}
