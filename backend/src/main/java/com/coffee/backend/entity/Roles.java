package com.coffee.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "roles")
public class Roles {
    @Id
    @SequenceGenerator(name = "secuencia_rol",sequenceName ="secuencia_rol",allocationSize = 1)
    @GeneratedValue(generator = "secuencia_rol",strategy = GenerationType.SEQUENCE)
    @Column(name = "rol_id")
    private Long rolID;
    @Column(name = "nombre_rol",nullable = false)
    private String nombreRol;
    @OneToMany(mappedBy = "roles",cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE})
    private List <Usuarios> usuarios;
}
