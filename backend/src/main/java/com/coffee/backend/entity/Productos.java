package com.coffee.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "productos")
@Data
public class Productos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "producto_id")
    private Long productoId;
    @Column(name = "nombre_producto",nullable = false)
    private String nombreProducto;
    @Column(name = "precio_actual")
    private Double precioActual;
    @ManyToOne
    @JoinColumn(name = "categoria_id",nullable = false)
    private Categorias categorias;
    @OneToMany(mappedBy = "productos")
    private List<DetallePedido> detallePedidos;
    @OneToMany(mappedBy = "productos",cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE},orphanRemoval = true)
    private List<Recetas> recetas;
}
