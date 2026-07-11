package com.coffee.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Usuarios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long usuarioId;
    @Column(name = "nombre_usuario", nullable = false)
    private String nombreUsuario;
    @Column(name = "apellido_usuario", nullable = false)
    private String apellidoUsuario;
    @Column(name = "correo_usuario", nullable = false, unique = true)
    private String correoUsuario;
    @Column(name = "telefono_usuario")
    private String telefonoUsuario;
    @Column(name = "clave_cifrada", nullable = false)
    private String claveCifrada;
    // Borrado lógico: un usuario inactivo no puede iniciar sesión ni aparece en la lista
    // de administración, pero conserva su fila y su histórico de ventas.
    @Builder.Default
    @Column(name = "activo", nullable = false)
    private boolean activo = true;
    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private Roles roles;
    // Sin CascadeType.REMOVE a propósito: borrar un usuario NO debe arrastrar sus pedidos
    // (son el histórico de ventas que leen los reportes). El borrado es lógico (activo=false).
    @OneToMany(mappedBy = "usuario",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private List<Pedidos> pedidos;
}
