package com.coffee.backend.repository;

import com.coffee.backend.entity.Recetas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecetaRepository extends JpaRepository<Recetas,Long>{
}
