package com.coffee.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name ="usuarios")
@Data
public class Usuarios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long usuarioId;
    @Column(name = "nombre_usuario",nullable = false)
    private String nombreUsuario;
    @Column(name = "apellido_usuario",nullable = false)
    private String apellidoUsuario;
    @Column(name = "correo_usuario",nullable = false,unique = true)
    private String correoUsuario;
    @Column(name = "telefono_usuario")
    private String telefonoUsuario;
    @Column(name = "clave_cifrada",nullable = false)
    private String claveCifrada;
    @ManyToOne
    @JoinColumn(name = "rol_id",nullable = false)
    private Roles roles;
    @OneToMany(mappedBy = "usuario",
            cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE}
          )
    private List<Pedidos>pedidos;
}
