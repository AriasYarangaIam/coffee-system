package com.coffee.backend.repository;

import com.coffee.backend.entity.Categorias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Repositorio JPA de Categorias (acceso a la tabla categorias).
@Repository
public interface CategoriaRepository extends JpaRepository<Categorias,Long> {
}
