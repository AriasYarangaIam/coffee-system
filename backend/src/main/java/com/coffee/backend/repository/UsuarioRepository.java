package com.coffee.backend.repository;

import com.coffee.backend.entity.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuarios,Long> {
}
