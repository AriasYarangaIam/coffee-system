package com.coffee.backend.repository;

import com.coffee.backend.entity.Almacenes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlmacenRepository extends JpaRepository<Almacenes, Long> {
}
