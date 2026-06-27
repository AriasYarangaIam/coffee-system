package com.coffee.backend.repository;

import com.coffee.backend.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Roles,Long> {
    Optional<Roles> findByNombreRol(String nombreRol);
}
