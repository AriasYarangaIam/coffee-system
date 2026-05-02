package com.coffee.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

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
