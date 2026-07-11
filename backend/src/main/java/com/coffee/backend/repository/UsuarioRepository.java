package com.coffee.backend.repository;

import com.coffee.backend.entity.Usuarios;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Repositorio JPA de Usuarios: busca por correo y por rol.
@Repository
public interface UsuarioRepository extends JpaRepository<Usuarios, Long> {
    Optional<Usuarios> findByCorreoUsuario(String correoUsuario);
    List<Usuarios> findByRoles_NombreRol(String nombreRol);
}