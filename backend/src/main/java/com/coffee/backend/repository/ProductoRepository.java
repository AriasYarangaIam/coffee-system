package com.coffee.backend.repository;

import com.coffee.backend.entity.Productos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Productos,Long> {
}
