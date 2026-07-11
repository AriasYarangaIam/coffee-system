package com.coffee.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

/**
 * Rol de usuario (ADMIN o MESERO). Mapea la tabla {@code roles}. Define los permisos:
 * las rutas {@code /api/admin/**} exigen ADMIN. Un rol lo tienen muchos {@link Usuarios}.
 */
@Entity
@Data
@Table(name = "roles")
public class Roles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id")
    private Long rolID;
    @Column(name = "nombre_rol",nullable = false)
    private String nombreRol;
    @OneToMany(mappedBy = "roles",cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE})
    private List <Usuarios> usuarios;
}
