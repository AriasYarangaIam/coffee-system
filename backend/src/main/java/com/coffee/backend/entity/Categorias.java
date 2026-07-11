package com.coffee.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

/**
 * Categoría de la carta (ej.: Bebidas, Postres) para agrupar productos. Mapea la tabla
 * {@code categorias}. Una categoría agrupa muchos {@link Productos}.
 */
@Entity
@Table(name = "categorias")
@Data
public class  Categorias {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "categoria_id")
    private Long categoriaId;
    @Column(name = "nombre_categoria",nullable = false)
    private String nombreCategoria;
    @OneToMany(mappedBy = "categorias",cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REMOVE})
    private List<Productos>productos;
}
