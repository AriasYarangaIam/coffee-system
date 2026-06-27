package com.coffee.backend.repository;

import com.coffee.backend.entity.Pedidos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedidos,Long> {
    // Pedidos del mesero logueado (se filtra por el correo del JWT).
    List<Pedidos> findByUsuario_CorreoUsuario(String correoUsuario);
}
