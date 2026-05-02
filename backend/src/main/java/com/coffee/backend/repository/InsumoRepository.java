package com.coffee.backend.repository;

import com.coffee.backend.entity.Insumos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsumoRepository extends JpaRepository<Insumos,Long> {
}
