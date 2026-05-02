package com.coffee.backend.repository;

import com.coffee.backend.entity.Recetas;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecetaRepository extends JpaRepository<Recetas, Long> {
    List<Recetas> findByProductos_ProductoId(Long productoId);
}